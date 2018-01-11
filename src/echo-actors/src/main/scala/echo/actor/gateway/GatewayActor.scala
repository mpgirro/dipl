package echo.actor.gateway

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, get, parameter, path, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.{ParserSettings, RoutingSettings}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import echo.actor.protocol.ActorMessages.{SearchRequest, SearchResults}
import echo.core.dto.document.{Document, EpisodeDocument, PodcastDocument}
import spray.json.{DefaultJsonProtocol, JsonFormat}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */

// needed for marshalling
case class ResultDoc(title: String, link: String, description: String, pubDate: String, itunesImage: String)

// Required to protect against JSON Hijacking for Older Browsers: Always return JSON with an Object on the outside
case class ArrayWrapper[T](results: T)

// collect your json format instances into a support trait:
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val resultFormat = jsonFormat5(ResultDoc)
    implicit def arrayWrapper[T: JsonFormat] = jsonFormat1(ArrayWrapper.apply[T])
}

class GatewayActor (val searcher : ActorRef) extends Actor with ActorLogging with JsonSupport{

    val GATEWAY_HOST = ConfigFactory.load().getString("echo.gateway.host")
    val GATEWAY_PORT = ConfigFactory.load().getInt("echo.gateway.port")

    override def preStart = {

        // the following implicit values are somehow required and used by Akka HTTP
        implicit val actorSystem = this.context.system
        implicit val actorMaterializer = ActorMaterializer.create(actorSystem)
        implicit val routingSettings = RoutingSettings(actorSystem)
        implicit val parserSettings = ParserSettings(actorSystem)

        val route: Route =
            path("api") {
                complete(StatusCodes.MethodNotAllowed)
            } ~
            path("api" / "search") {
                get {
                    parameter("query") { (query) =>

                        log.info("Received HTTP request /search?query={}", query)

                        val foundDocs = search(query)
                        val results: Array[ResultDoc] = foundDocs.map(d => {
                            d match {
                                case pDoc: PodcastDocument => {
                                    val pubDate = { if (pDoc.getPubDate != null) pDoc.getPubDate.toString else "" }
                                    val itunesImage = { if (pDoc.getItunesImage != null) pDoc.getItunesImage else "" }
                                    ResultDoc(pDoc.getTitle, pDoc.getLink, pDoc.getDescription, pubDate, itunesImage)
                                }
                                case eDoc: EpisodeDocument => {
                                    val pubDate = { if (eDoc.getPubDate != null) eDoc.getPubDate.toString else "" }
                                    val itunesImage = { if (eDoc.getItunesImage != null) eDoc.getItunesImage else "" }
                                    ResultDoc(eDoc.getTitle, eDoc.getLink, eDoc.getDescription, pubDate, itunesImage)
                                }
                            }
                        })

                        complete(StatusCodes.OK, ArrayWrapper(results))
                    }
                } ~
                    complete(StatusCodes.MethodNotAllowed)
            }

        val routeFlow: Flow[HttpRequest, HttpResponse, NotUsed] = Route.handlerFlow(route)
        val fServerBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routeFlow, GATEWAY_HOST, GATEWAY_PORT)

        log.info("listening to http://{}:{}", GATEWAY_HOST, GATEWAY_PORT)
    }

    override def receive: Receive = {
        case _ => {
            log.warning("GatewayActor does not handle any Actor-messages yet")
        }
    }

    private def search(query: String): Array[Document] = {
        implicit val timeout = Timeout(5 seconds)
        val future = searcher ? SearchRequest(query)
        val response = Await.result(future, timeout.duration).asInstanceOf[SearchResults]
        response match {

            case SearchResults(results) => {
                return results
            }
        }
    }
}
