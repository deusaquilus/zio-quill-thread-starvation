import io.getquill._
import io.getquill.context.ZioJdbc._
import io.getquill.context.qzio._
import zio._
import zio.clock._
import zio.console._

object Simple extends App {
  import Rand._

  val ctx = new H2ZioJdbcContext(SnakeCase)
  import ctx._

  val dsLayer = DataSourceLayer.fromPrefix("testPostgresDB")

  def q: Quoted[Insert[Counters]] =
  //quote { infix"UPDATE Counters SET value = 1 WHERE key = 1".as[Insert[(Int, Int)]] }
    quote { infix"UPDATE Counters SET value = value + 1 WHERE key = #${(nextInt())}".as[Insert[Counters]] }

  override def run(args: List[String]): zio.URIO[zio.ZEnv,zio.ExitCode] = {

    val runQuery =
      ctx
        .run(q)
        .onDataSource
        .provideCustomLayer(dsLayer)

    runQuery.orDie.exitCode
  }
}
