package echo.actor.gateway.routes

import scala.collection.JavaConverters._
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.slf4j.Logger
import echo.core.dto.document.{DTO, EpisodeDTO, IndexResult, PodcastDTO}
import echo.actor.gateway.json.{ArrayWrapper, JsonSupport}
import echo.core.converter.ResultConverter

/**
  * @author Maximilian Irro
  */
class SearchRoutes(log: LoggingAdapter, search: (String,Int,Int) => Array[DTO]) extends JsonSupport {

    val resultConverter = new ResultConverter()

    val route = logRequestResult("SearchRoutes") {
        pathPrefix("search"){
            get {
                parameter("query", "page" ? "1", "size" ? "20") { (query, page, size) =>

                    log.info("Received HTTP request /search?query={}&page={}&size={}", query, page.toInt, size.toInt)

                    val foundDocs = search(query, page.toInt, size.toInt)
                    val results: Array[IndexResult] = asScalaBuffer(resultConverter.toResultList(seqAsJavaList(foundDocs))).toArray

                    /*
                    for(r <- results){
                        println(r)
                    }
                    */

                    complete(ArrayWrapper(results))
                }
            }

        }
    }
}
