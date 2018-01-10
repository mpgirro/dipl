package echo.actor.gateway

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, get, parameter, path}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.typesafe.config.ConfigFactory
import echo.core.dto.document.{Document, EpisodeDocument, PodcastDocument}
import spray.json.{DefaultJsonProtocol, JsonFormat}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.{ParserSettings, RoutingSettings}
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import echo.actor.protocol.Protocol.{SearchRequest, SearchResults}
import echo.core.dto.document.{Document, EpisodeDocument, PodcastDocument}
import spray.json.{DefaultJsonProtocol, JsonFormat}
import spray.json.DefaultJsonProtocol._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.{Await, Future}

/**
  * @author Maximilian Irro
  */

// needed for marshalling
case class ResultDoc(title: String, link: String, description: String)

// Required to protect against JSON Hijacking for Older Browsers: Always return JSON with an Object on the outside
case class ArrayWrapper[T](wrappedArray: T)

// collect your json format instances into a support trait:
trait JsonSupport extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport with DefaultJsonProtocol {
    implicit val resultFormat = jsonFormat3(ResultDoc)
    implicit def arrayWrapper[T: JsonFormat] = jsonFormat1(ArrayWrapper.apply[T])
}

class GatewayActor (val searcher : ActorRef)  extends Actor with ActorLogging with JsonSupport{

    val GATEWAY_HOST = ConfigFactory.load().getString("echo.gateway.host")
    val GATEWAY_PORT = ConfigFactory.load().getInt("echo.gateway.port")

    override def preStart = {

        // the following implicit values are somehow required and used by Akka HTTP
        implicit val actorSystem = this.context.system
        implicit val actorMaterializer = ActorMaterializer.create(actorSystem)
        implicit val routingSettings = RoutingSettings(actorSystem)
        implicit val parserSettings = ParserSettings(actorSystem)

        val route: Route =
            path("search") {
                get {
                    parameter("query") { (query) =>

                        val foundDocs = search(query)
                        val results: Array[ResultDoc] = foundDocs.map(d => {
                            d match {
                                case pDoc: PodcastDocument => ResultDoc(pDoc.getTitle, pDoc.getLink, pDoc.getDescription)
                                case eDoc: EpisodeDocument => ResultDoc(eDoc.getTitle, eDoc.getLink, eDoc.getDescription)
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
            log.warning("GatewayActor does not handle any messages at the moment")
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
