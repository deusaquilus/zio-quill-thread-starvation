import io.getquill._
import io.getquill.context.ZioJdbc._
import io.getquill.context.qzio._
import zio._
import zio.clock._
import zio.console._

case class Person(name: String, age: Int)

object Main extends App {
  val ctx = new H2ZioJdbcContext(SnakeCase)
  import ctx._

  val dsLayer = DataSourceLayer.fromPrefix("ctx")

  override def run(args: List[String]): zio.URIO[zio.ZEnv,zio.ExitCode] = {
    dsLayer.build.use { ds =>
      val conn = ds.get.getConnection
      val stmt = conn.createStatement
      stmt.executeUpdate("CREATE TABLE Person(name varchar, age int)")

      val runQuery =
        ctx
          .run(query[Person].filter(p => p.name == "John"))
          .onDataSource
          .provide(ds)

      val queries = List.fill(50)(runQuery.fork)

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
    }.exitCode
  }
}
