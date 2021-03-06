package echo.actor.gateway.service

import javax.ws.rs.Path

import akka.actor.{ActorContext, ActorRef}
import akka.dispatch.MessageDispatcher
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException, ask}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import echo.actor.catalog.CatalogProtocol._
import echo.actor.gateway.json.{ArrayWrapper, JsonSupport}
import echo.core.benchmark.mps.MessagesPerSecondMeter
import echo.core.domain.dto.PodcastDTO
import io.swagger.annotations._

import scala.util.{Failure, Success}

/**
  * @author Maximilian Irro
  */

@Path("/api/podcast")  // @Path annotation required for Swagger
@Api(value = "/api/podcast",
     produces = "application/json")
class PodcastGatewayService (private val log: LoggingAdapter, private val breaker: CircuitBreaker, private val mpsMeter: MessagesPerSecondMeter)
                            (private implicit val context: ActorContext, private implicit val timeout: Timeout) extends GatewayService with Directives with JsonSupport {

    private val CONFIG = ConfigFactory.load()
    // TODO these values are used by searcher and gateway, so save them somewhere more common for both
    private val DEFAULT_PAGE: Int = CONFIG.getInt("echo.catalog.default-page")
    private val DEFAULT_SIZE: Int = CONFIG.getInt("echo.catalog.default-size")

    // will be set after construction of the service via the setter method,
    // once the message with the reference arrived
    private var catalogStore: ActorRef = _

    override val blockingDispatcher: MessageDispatcher = context.system.dispatchers.lookup(DISPATCHER_ID)

    override val route: Route = pathPrefix("podcast") { pathEndOrSingleSlash { getAllPodcasts ~ postPodcast } } ~
                    pathPrefix("podcast" / Segment) { id =>
                        pathEndOrSingleSlash{ getPodcast(id) ~ putPodcast(id) ~ deletePodcast(id) } ~
                            pathPrefix("episodes") {
                                pathEndOrSingleSlash { getEpisodesByPodcast(id) } } ~
                            pathPrefix("feeds") {
                                pathEndOrSingleSlash { getFeedsByPodcast(id) } }
                    }


    def setCatalogStoreActorRef(catalogStore: ActorRef): Unit = this.catalogStore = catalogStore


    @ApiOperation(value = "Get list of all Podcasts",
                  nickname = "getAllPodcasts",
                  httpMethod = "GET",
                  response = classOf[ArrayWrapper[Set[PodcastDTO]]],
                  responseContainer = "Set")
    def getAllPodcasts: Route = get {
        parameters('p.as[Int].?, 's.as[Int].?) { (page, size) =>
            log.info("GET /api/podcast?p={}&s={}", page.getOrElse(DEFAULT_PAGE), size.getOrElse(DEFAULT_SIZE))
            mpsMeter.tick()

            val p: Int = page.map(p => p-1).getOrElse(DEFAULT_PAGE)
            val s: Int = size.getOrElse(DEFAULT_SIZE)

            onCompleteWithBreaker(breaker)(catalogStore ? GetAllPodcastsRegistrationComplete(p,s)) {
                case Success(res) =>
                    res match {
                        case AllPodcastsResult(results) =>
                            log.info("PodcastGatewayService returns {} podcast entries on REST interface", results.size)
                            complete(StatusCodes.OK, ArrayWrapper(results))
                    }

                //Circuit breaker opened handling
                case Failure(ex: CircuitBreakerOpenException) =>
                    log.error("CircuitBreakerOpenException calling CatalogStore -- returning {}: {}", TooManyRequests.intValue, TooManyRequests.defaultMessage)
                    complete(HttpResponse(TooManyRequests).withEntity("Server Busy"))

                //General exception handling
                case Failure(ex) =>
                    log.error("Exception while getting all podcasts from catalog")
                    ex.printStackTrace()
                    complete(InternalServerError)
            }
        }
    }

    @ApiOperation(value = "Get podcast",
                  nickname = "getPodcast",
                  httpMethod = "GET",
                  response = classOf[PodcastDTO])
    def getPodcast(exo: String): Route = get {
        log.info("GET /api/podcast/{}", exo)
        mpsMeter.tick()
        onCompleteWithBreaker(breaker)(catalogStore ? GetPodcast(exo)) {
            case Success(res) =>
                res match {
                    case PodcastResult(podcast) => complete(StatusCodes.OK, podcast)
                    case NothingFound(unknown)  =>
                        log.error("CatalogStore responded that there is no Podcast : {}", unknown)
                        complete(StatusCodes.NotFound)
                }

            //Circuit breaker opened handling
            case Failure(ex: CircuitBreakerOpenException) =>
                log.error("CircuitBreakerOpenException calling CatalogStore -- returning {}: {}", TooManyRequests.intValue, TooManyRequests.defaultMessage)
                complete(HttpResponse(TooManyRequests).withEntity("Server Busy"))

            //General exception handling
            case Failure(ex) =>
                log.error("Exception while getting podcasts from catalog : {}", exo)
                ex.printStackTrace()
                complete(InternalServerError)
        }
    }

    def getEpisodesByPodcast(exo: String): Route = get {
        log.info("GET /api/podcast/{}/episodes", exo)
        mpsMeter.tick()
        onCompleteWithBreaker(breaker)(catalogStore ? GetEpisodesByPodcast(exo)) {
            case Success(res) =>
                res match {
                    case EpisodesByPodcastResult(episodes) => complete(StatusCodes.OK, ArrayWrapper(episodes))
                }

            //Circuit breaker opened handling
            case Failure(ex: CircuitBreakerOpenException) =>
                log.error("CircuitBreakerOpenException calling CatalogStore -- returning {}: {}", TooManyRequests.intValue, TooManyRequests.defaultMessage)
                complete(HttpResponse(TooManyRequests).withEntity("Server Busy"))

            //General exception handling
            case Failure(ex) =>
                log.error("Exception while getting episodes by podcast from catalog : {}", exo)
                ex.printStackTrace()
                complete(InternalServerError)
        }
    }

    def getFeedsByPodcast(exo: String): Route = get {
        log.info("GET /api/podcast/{}/feeds", exo)
        mpsMeter.tick()
        onCompleteWithBreaker(breaker)(catalogStore ? GetFeedsByPodcast(exo)) {
            case Success(res) =>
                res match {
                    case FeedsByPodcastResult(feeds) => complete(StatusCodes.OK, ArrayWrapper(feeds))
                }

            //Circuit breaker opened handling
            case Failure(ex: CircuitBreakerOpenException) =>
                log.error("CircuitBreakerOpenException calling CatalogStore -- returning {}: {}", TooManyRequests.intValue, TooManyRequests.defaultMessage)
                complete(HttpResponse(TooManyRequests).withEntity("Server Busy"))

            //General exception handling
            case Failure(ex) =>
                log.error("Exception while getting feeds by podcast from catalog : {}", exo)
                ex.printStackTrace()
                complete(InternalServerError)
        }
    }

    def postPodcast: Route = post {
        entity(as[PodcastDTO]) { podcast =>

            mpsMeter.tick()

            complete(StatusCodes.NotImplemented)
        }
    }

    def putPodcast(id: String): Route = put {
        entity(as[PodcastDTO]) { podcast =>

            mpsMeter.tick()

            // TODO update podcast with exo

            complete(StatusCodes.NotImplemented)
        }
    }

    def deletePodcast(id: String): Route = delete {

        mpsMeter.tick()

        // TODO delete podcast -  I guess this should not be supported?

        complete(StatusCodes.NotImplemented)
    }


}
