package echo.actor.gateway

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, get, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.{ParserSettings, RoutingSettings}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import echo.actor.gateway.json.JsonSupport
import echo.actor.gateway.routes.{EpisodeRoutes, PodcastRoutes, SearchRoutes}
import echo.actor.gateway.service.EpisodeService
import echo.actor.protocol.ActorMessages._
import echo.core.dto.{DTO, EpisodeDTO, PodcastDTO, ResultWrapperDTO}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */
class GatewayActor (val searcher : ActorRef) extends Actor with ActorLogging with JsonSupport {

    val GATEWAY_HOST = ConfigFactory.load().getString("echo.gateway.host")
    val GATEWAY_PORT = ConfigFactory.load().getInt("echo.gateway.port")

    // TODO see https://github.com/ArchDev/akka-http-rest/blob/master/src/main/scala/me/archdev/restapi/http/HttpRoute.scala
    val SECRET_KEY = ConfigFactory.load().getString("echo.gateway.secret-key")

    private var directoryStore: ActorRef = _

    implicit val internalTimeout = Timeout(5 seconds)

    val episodeService = new EpisodeService(log, internalTimeout)

    override def preStart = {

        // the following implicit values are somehow required and used by Akka HTTP
        implicit val actorSystem = this.context.system
        implicit val actorMaterializer = ActorMaterializer.create(actorSystem)
        implicit val routingSettings = RoutingSettings(actorSystem)
        implicit val parserSettings = ParserSettings(actorSystem)

        val searchRouter = new SearchRoutes(search)               // TODO replace by a service
        val podcastRouter = new PodcastRoutes(log, getPodcast)    // TODO replace by a service
        // val episodeRouter = new EpisodeRoutes(log, getEpisode) // TODO delete


        /*
        def assets = pathPrefix("swagger") {
            getFromResourceDirectory("swagger") ~ pathSingleSlash(get(redirect("index.html", StatusCodes.PermanentRedirect))) }
        */

        //assets ~ new EpisodeService(directoryStore, internalTimeout).route

        val route: Route = cors() (
            pathPrefix("swagger") {
                getFromResourceDirectory("swagger") ~ pathSingleSlash(get(redirect("index.html", StatusCodes.PermanentRedirect)))
            } ~
            pathPrefix("api") {
                //searchRouter.route ~ podcastRouter.route ~ episodeRouter.route
                searchRouter.route ~ podcastRouter.route ~ episodeService.route
            } ~
            pathPrefix("healthcheck") {
                get {
                    complete("OK")
                }
            } ~
                SwaggerDocService.routes ~
                complete(StatusCodes.MethodNotAllowed)
        )

        val routeFlow: Flow[HttpRequest, HttpResponse, NotUsed] = Route.handlerFlow(route)
        val fServerBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routeFlow, GATEWAY_HOST, GATEWAY_PORT)

        log.info("listening to http://{}:{}", GATEWAY_HOST, GATEWAY_PORT)
    }

    override def receive: Receive = {

        case ActorRefDirectoryStoreActor(ref) => {
            log.debug("Received ActorRefDirectoryStoreActor(_)")
            directoryStore = ref;
            episodeService.setDirectoryStoreActorRef(ref)
        }

        case _ => {
            log.warning("GatewayActor does not handle any Actor-messages yet")
        }
    }

    private def search(query: String, page: Int, size: Int): ResultWrapperDTO = {

        val future = searcher ? SearchRequest(query, page, size)
        val response = Await.result(future, internalTimeout.duration).asInstanceOf[SearchResults]
        response match {

            case SearchResults(results) => {
                return results
            }
        }
    }

    private def getPodcast(echoId: String): PodcastDTO = {

        var result: PodcastDTO = null

        val future = directoryStore ? GetPodcast(echoId)
        val response = Await.result(future, internalTimeout.duration).asInstanceOf[DirectoryResult]
        response match {

            case PodcastResult(podcast) => {
                result = podcast
            }

            case NoDocumentFound(unknownId) => {
                log.error("DirectoryStore responded that there is no Podcast with echoId={}", unknownId)
            }
        }
        return result;
    }

    /*
    private def getEpisode(echoId: String): EpisodeDTO = {

        var result: EpisodeDTO = null

        val future = directoryStore ? GetEpisode(echoId)
        val response = Await.result(future, internalTimeout.duration).asInstanceOf[DirectoryResult]
        response match {

            case EpisodeResult(episode) => {
                result = episode
            }

            case NoDocumentFound(unknownId) => {
                log.error("DirectoryStore responded that there is no Episode with echoId={}", unknownId)
            }
        }
        return result;
    }
    */
}
