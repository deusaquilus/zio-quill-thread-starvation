import io.getquill._
import io.getquill.context.ZioJdbc
import io.getquill.context.ZioJdbc._
import io.getquill.context.qzio._
import zio._
import zio.clock._
import zio.console._

import java.io.Closeable
import java.sql.{Connection, SQLException}
import javax.sql.DataSource

case class Person(name: String, age: Int)

case class Counters(key: Int, value: Int)

object Rand {
  val rand = new scala.util.Random()
  def nextInt() = rand.nextInt(100) + 1
}

object Main extends App {
  import Rand._

//  implicit class QuillZioExtThrottled[T](qzio: ZIO[Has[Connection], Throwable, T]) {
//    import io.getquill.context.qzio.ImplicitSyntax._
//
//    def onDataSourceThrottled: ZIO[Has[DataSource with Closeable], SQLException, T] =
//      qzio.provideLayer(ThrottledDataSourceLayer.live).refineToOrDie[SQLException]
//
//    object ThrottledDataSourceLayer {
//      def live: ZLayer[Has[DataSource with Closeable], SQLException, Has[Connection]] = layer
//
//      val layer = {
//        val managed =
//          for {
//            sem <- Semaphore.make(1).toManaged_
//            from <- ZManaged.environment[Has[DataSource with Closeable]]
//            r <- ZioJdbc.managedBestEffort(sem.withPermit(ZIO.effect(from.get.getConnection)).refineToOrDie[SQLException]: ZIO[Any, SQLException, Connection])
//          } yield Has(r)
//        ZLayer.fromManagedMany(managed)
//      }
//    }
//  }

  val ctx = new H2ZioJdbcContext(SnakeCase)
  import ctx._

  val dsLayer = DataSourceLayer.fromPrefix("testPostgresDB")

  override def run(args: List[String]): zio.URIO[zio.ZEnv,zio.ExitCode] = {
    dsLayer.build.use { ds =>
//      val conn = ds.get.getConnection
//      val stmt = conn.createStatement
//      stmt.executeUpdate(
//        """
//          |CREATE TABLE customers(
//          |   id INT GENERATED ALWAYS AS IDENTITY,
//          |   count INT NOT NULL DEFAULT 0
//          |);
//          |""".stripMargin)

      // make it dynamic to force the random gen to rerun on each instance
      def q: Quoted[Insert[Counters]] =
        //quote { infix"UPDATE Counters SET value = 1 WHERE key = 1".as[Insert[(Int, Int)]] }
        quote { infix"UPDATE Counters SET value = value + 1 WHERE key = #${(nextInt())}".as[Insert[Counters]] }

      def runQuery =
        ctx
          .run(q)
          .onDataSource
          .provide(ds)

      // 28 seems to starve the pool and make everything hang
      val queries = List.fill(28)(runQuery.fork)

      ZIO.foreachPar(queries) { fiberEffect =>
        for {
          fiber <- fiberEffect
          before <- instant
          _ <- putStrLn(s"Time before join: $before")
          result <- fiber.join
          after <- instant
          _ <- putStrLn(s"Time after join: $after")
        } yield result
      }

//      Semaphore.make(1).flatMap { sem =>
//        ZIO.foreachPar(queries) { fiberEffect =>
//          for {
//            fiber <- sem.withPermit(fiberEffect)
//            before <- instant
//            _ <- putStrLn(s"Time before join: $before")
//            result <- fiber.join
//            after <- instant
//            _ <- putStrLn(s"Time after join: $after")
//          } yield result
//        }
//      }
    }.exitCode
  }
}
