package echo.actor.gateway.routes

import akka.actor.{ActorContext, ActorSystem}

import scala.collection.JavaConverters._
import scala.language.postfixOps
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.typesafe.config.ConfigFactory
import echo.core.dto.document._
import echo.actor.gateway.json.{ArrayWrapper, JsonSupport}
import echo.core.converter.ResultConverter

/**
  * @author Maximilian Irro
  */
class SearchRoutes(search: (String,Int,Int) => ResultWrapperDTO)(implicit val context: ActorContext) extends JsonSupport {

    val log = Logging(context.system, classOf[SearchRoutes])

    val DEFAULT_PAGE = ConfigFactory.load().getInt("echo.gateway.default-page")
    val DEFAULT_SIZE = ConfigFactory.load().getInt("echo.gateway.default-size")

    val route =
        pathPrefix("search"){
            get {
                parameters('query, 'page.?, 'size.?) { (query, page, size) =>

                    val p: Int = page match {
                        case Some(x) => x.toInt
                        case None    => DEFAULT_PAGE
                    }
                    val s: Int = size match {
                        case Some(x) => x.toInt
                        case None    => DEFAULT_SIZE
                    }

                    log.info("Received HTTP request /search?query={}&page={}&size={}", query, p, s)

                    val results = search(query, p, s)

                    complete(results)

                }
            }

        }

}
