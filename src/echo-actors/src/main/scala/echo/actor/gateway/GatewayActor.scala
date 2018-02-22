package echo.actor.gateway

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.dispatch.MessageDispatcher
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, get, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.{ParserSettings, RoutingSettings}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.typesafe.config.ConfigFactory
import echo.actor.gateway.json.JsonSupport
import echo.actor.gateway.service.{EpisodeGatewayService, FeedGatewayService, PodcastGatewayService, SearchGatewayService}
import echo.actor.ActorProtocol._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */
class GatewayActor extends Actor with ActorLogging with JsonSupport {

    log.info("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val GATEWAY_HOST = CONFIG.getString("echo.gateway.host")
    private val GATEWAY_PORT = CONFIG.getInt("echo.gateway.port")

    // TODO see https://github.com/ArchDev/akka-http-rest/blob/master/src/main/scala/me/archdev/restapi/http/HttpRoute.scala
    private val SECRET_KEY = CONFIG.getString("echo.gateway.secret-key")

    private var searcher: ActorRef = _
    private var directoryStore: ActorRef = _

    private implicit val INTERNAL_TIMEOUT: Timeout = Option(CONFIG.getInt("echo.internal-timeout")).getOrElse(5).seconds

    private val searchService = new SearchGatewayService(log)
    private val podcastService = new PodcastGatewayService(log)
    private val episodeService = new EpisodeGatewayService(log)
    private val feedService = new FeedGatewayService(log)

    override def preStart: Unit = {

        // the following implicit values are somehow required and used by Akka HTTP
        implicit val actorSystem = this.context.system
        implicit val actorMaterializer = ActorMaterializer.create(actorSystem)
        implicit val routingSettings = RoutingSettings(actorSystem)
        implicit val parserSettings = ParserSettings(actorSystem)

        /*
        def assets = pathPrefix("swagger") {
            getFromResourceDirectory("swagger") ~ pathSingleSlash(get(redirect("index.html", StatusCodes.PermanentRedirect))) }
        */

        val route: Route = cors() (
            pathPrefix("swagger") {
                getFromResourceDirectory("swagger") ~ pathSingleSlash(get(redirect("index.html", StatusCodes.PermanentRedirect)))
            } ~
            pathPrefix("api") {
                searchService.route ~ podcastService.route ~ episodeService.route ~ feedService.route
            } ~
            pathPrefix("load-test") { // TODO
                get {
                    directoryStore ! LoadTestFeeds()
                    complete(StatusCodes.OK)
                }
            } ~
            pathPrefix("ping") {
                get {
                    complete(StatusCodes.OK, "pong")
                }
            } ~
                SwaggerDocService.routes ~
                complete(StatusCodes.MethodNotAllowed)
        )

        val routeFlow: Flow[HttpRequest, HttpResponse, NotUsed] = Route.handlerFlow(route)
        val fServerBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routeFlow, GATEWAY_HOST, GATEWAY_PORT)

        log.info("listening to http://{}:{}", GATEWAY_HOST, GATEWAY_PORT)
    }

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {
        case ActorRefSearcherActor(ref) =>
            log.debug("Received ActorRefSearcherActor(_)")
            searcher = ref
            searchService.setSearcherActorRef(searcher)
        case ActorRefDirectoryStoreActor(ref) =>
            log.debug("Received ActorRefDirectoryStoreActor(_)")
            directoryStore = ref
            podcastService.setDirectoryStoreActorRef(ref)
            episodeService.setDirectoryStoreActorRef(ref)
            feedService.setDirectoryStoreActorRef(ref)
        case _ =>
            log.warning("GatewayActor does not handle any Actor-messages yet")
    }

}
