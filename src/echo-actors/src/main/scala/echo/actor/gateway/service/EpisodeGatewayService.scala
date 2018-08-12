package echo.actor.gateway.service

import javax.ws.rs.Path

import akka.actor.{ActorContext, ActorRef}
import akka.dispatch.MessageDispatcher
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException, ask}
import akka.util.Timeout
import echo.actor.catalog.CatalogProtocol._
import echo.actor.gateway.json.{ArrayWrapper, JsonSupport}
import echo.core.benchmark.mps.MessagesPerSecondMeter
import echo.core.domain.dto.EpisodeDTO
import io.swagger.annotations._

import scala.util.{Failure, Success}

/**
  * @author Maximilian Irro
  */

/*
@Path("/api/episode")  // @Path annotation required for Swagger
@Api(value = "/api/episode",
     produces = "application/json")
*/
class EpisodeGatewayService (private val log: LoggingAdapter, private val breaker: CircuitBreaker, private val mpsMeter: MessagesPerSecondMeter)
                            (private implicit val context: ActorContext, private implicit val timeout: Timeout) extends GatewayService with Directives with JsonSupport {

    // will be set after construction of the service via the setter method,
    // once the message with the reference arrived
    private var catalogStore: ActorRef = _

    override val blockingDispatcher: MessageDispatcher = context.system.dispatchers.lookup(DISPATCHER_ID)

    override val route: Route = pathPrefix("episode") { pathEndOrSingleSlash { getAllEpisodes ~ postEpisode } } ~
                    pathPrefix("episode" / Segment) { id =>
                        pathEndOrSingleSlash{ getEpisode(id) ~ putEpisode(id) ~ deleteEpisode(id)  } ~
                            getChaptersByEpisode(id)
                    }


    def setCatalogStoreActorRef(catalogStore: ActorRef): Unit = this.catalogStore = catalogStore


    @ApiOperation(value = "Get list of all Episodes",
                  nickname = "getAllEpisodes",
                  httpMethod = "GET",
                  response = classOf[Set[EpisodeDTO]],
                  responseContainer = "Set")
    def getAllEpisodes: Route = get {

        mpsMeter.incrementCounter()

        complete(NotImplemented)
    }

    /*
    @Path("/episode/{exo}")
    @ApiOperation(
        value = "Return the episode corresponding to the EXO.",
        nickname = "getEpisode",
        httpMethod = "GET")
    @ApiImplicitParams(Array(
        new ApiImplicitParam(
            name = "exo",
            value = "The EXO (= external ID) for an episode, which you find in the url https://exo.fm/e/___EXO___",
            example = "bhAShaNAni",
            required = true,
            dataType = "string",
            paramType = "path")))
    @ApiResponses(Array(
        new ApiResponse(code = 200, message = "Return episode", response = classOf[EpisodeDTO]),
        new ApiResponse(code = 500, message = "Internal server error")))
    */
    def getEpisode(exo: String): Route = get {
        log.info("GET /api/episode/{}", exo)
        mpsMeter.incrementCounter()
        onCompleteWithBreaker(breaker)(catalogStore ? GetEpisode(exo)) {
            case Success(res) =>
                res match {
                    case EpisodeResult(episode) => complete(OK, episode)
                    case NothingFound(unknown)  =>
                        log.warning("CatalogStore responded that there is no Episode with EXO : {}", unknown)
                        complete(NotFound)
                }

            //Circuit breaker opened handling
            case Failure(ex: CircuitBreakerOpenException) =>
                log.error("CircuitBreakerOpenException calling CatalogStore -- returning {}: {}", TooManyRequests.intValue, TooManyRequests.defaultMessage)
                complete(HttpResponse(TooManyRequests).withEntity("Server Busy"))

            //General exception handling
            case Failure(ex) =>
                log.error("Exception while getting episode from catalog : {}", exo)
                ex.printStackTrace()
                complete(InternalServerError)
        }
    }

    def getChaptersByEpisode(exo: String): Route = get {
        log.info("GET /api/episode/{}/chapters", exo)
        mpsMeter.incrementCounter()
        onCompleteWithBreaker(breaker)(catalogStore ? GetChaptersByEpisode(exo)) {
            case Success(res) =>
                res match {
                    case ChaptersByEpisodeResult(chapters) => complete(OK, ArrayWrapper(chapters))
                }

            //Circuit breaker opened handling
            case Failure(ex: CircuitBreakerOpenException) =>
                log.error("CircuitBreakerOpenException calling CatalogStore -- returning {}: {}", TooManyRequests.intValue, TooManyRequests.defaultMessage)
                complete(HttpResponse(TooManyRequests).withEntity("Server Busy"))

            //General exception handling
            case Failure(ex) =>
                log.error("Exception while getting chapters by episode from catalog : {}", exo)
                ex.printStackTrace()
                complete(InternalServerError)
        }
    }

    @ApiOperation(value = "Create new user", nickname = "userPost", httpMethod = "POST", produces = "text/plain")
    @ApiImplicitParams(Array(
        new ApiImplicitParam(name = "user", dataType = "nl.codecentric.UserRepository$User", paramType = "body", required = true)
    ))
    @ApiResponses(Array(
        new ApiResponse(code = 201, message = "User created"),
        new ApiResponse(code = 409, message = "User already exists")
    ))
    def postEpisode: Route = post {
        entity(as[EpisodeDTO]) { episode =>

            mpsMeter.incrementCounter()

            /*
            onSuccess(userRepository ? UserRepository.AddUser(user.name)) {
                case UserRepository.UserAdded(_)  => complete(StatusCodes.Created)
                case UserRepository.UserExists(_) => complete(StatusCodes.Conflict)
            }
            */

          complete(NotImplemented)
        }
    }

    def putEpisode(id: String): Route = put {
        entity(as[EpisodeDTO]) { episode =>

            mpsMeter.incrementCounter()

            // TODO update podcast with exo

            complete(NotImplemented)
        }
    }

    def deleteEpisode(id: String): Route = delete {

        mpsMeter.incrementCounter()

        // TODO delete podcast -  I guess this should not be supported?

        complete(NotImplemented)
    }


}
