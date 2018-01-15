package echo.actor.gateway.routes

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.slf4j.Logger

import echo.core.dto.document.{DTO, EpisodeDTO, PodcastDTO}
import echo.actor.gateway.json.JsonSupport
import echo.actor.gateway.{ArrayWrapper, ResultDoc}

/**
  * @author Maximilian Irro
  */
class SearchRoutes(log: LoggingAdapter, search: String => Array[DTO]) extends JsonSupport {

    val route = logRequestResult("SearchRoutes") {
        pathPrefix("search"){
            get {
                parameter("query") { (query) =>

                    log.info("Received HTTP request /search?query={}", query)

                    val foundDocs = search(query)

                    for(f <- foundDocs){
                        println(f)
                    }

                    val results: Array[ResultDoc] = foundDocs.map(d => {
                        d match {
                            case pDoc: PodcastDTO => {
                                val title       = { if(pDoc.getTitle        != null) pDoc.getTitle            else "<TITLE NOT SET>"}
                                val link        = { if(pDoc.getLink         != null) pDoc.getLink             else "<LINK NOT SET>"}
                                val description = { if(pDoc.getDescription  != null) pDoc.getDescription      else "<DESCRIPTION NOT SET>"}
                                val pubDate     = { if (pDoc.getPubDate     != null) pDoc.getPubDate.toString else "" }
                                val itunesImage = { if (pDoc.getItunesImage != null) pDoc.getItunesImage      else "" }
                                ResultDoc(title, link, description, pubDate, itunesImage)
                            }
                            case eDoc: EpisodeDTO => {
                                val title       = { if(eDoc.getTitle        != null) eDoc.getTitle            else "<TITLE NOT SET>"}
                                val link        = { if(eDoc.getLink         != null) eDoc.getLink             else "<LINK NOT SET>"}
                                val description = { if(eDoc.getDescription  != null) eDoc.getDescription      else "<DESCRIPTION NOT SET>"}
                                val pubDate     = { if (eDoc.getPubDate     != null) eDoc.getPubDate.toString else "" }
                                val itunesImage = { if (eDoc.getItunesImage != null) eDoc.getItunesImage      else "" }
                                ResultDoc(title, link, description, pubDate, itunesImage)
                            }
                        }
                    })


                    complete(ArrayWrapper(results))
                }
            }

        }
    }
}
