package echo.actor.gateway.service

import javax.ws.rs.Path

import akka.actor.{ActorContext, ActorLogging, ActorRef}
import akka.dispatch.MessageDispatcher
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import echo.actor.gateway.json.JsonSupport
import com.typesafe.config.ConfigFactory
import echo.actor.gateway.json.{ArrayWrapper, JsonSupport}
import echo.actor.ActorProtocol._
import echo.core.domain.dto.PodcastDTO
import io.swagger.annotations._

/**
  * @author Maximilian Irro
  */

@Path("/api/podcast")  // @Path annotation required for Swagger
@Api(value = "/api/podcast",
     produces = "application/json")
class PodcastGatewayService (private val log: LoggingAdapter)
                            (private implicit val context: ActorContext, private implicit val timeout: Timeout) extends GatewayService with Directives with JsonSupport {

    // TODO these values are used by searcher and gateway, so save them somewhere more common for both
    private val DEFAULT_PAGE: Int = ConfigFactory.load().getInt("echo.directory.default-page")
    private val DEFAULT_SIZE: Int = ConfigFactory.load().getInt("echo.directory.default-size")

    // will be set after construction of the service via the setter method,
    // once the message with the reference arrived
    private var directoryStore: ActorRef = _

    // TODO better use the logger of the actor, for cluster use later, via constructor --> log: LoggingAdapter
    // val log = Logging(context.system, classOf[EpisodeService])

    override val blockingDispatcher: MessageDispatcher = context.system.dispatchers.lookup(DISPATCHER_ID)

    override val route: Route = pathPrefix("podcast") { pathEndOrSingleSlash { getAllPodcasts ~ postPodcast } } ~
                    pathPrefix("podcast" / Segment) { id =>
                        pathEndOrSingleSlash{ getPodcast(id) ~ putPodcast(id) ~ deletePodcast(id) } ~
                            getEpisodesByPodcast(id) ~ getFeedsByPodcast(id)
                    }


    def setDirectoryStoreActorRef(directoryStore: ActorRef): Unit = this.directoryStore = directoryStore


    @ApiOperation(value = "Get list of all Podcasts",
                  nickname = "getAllPodcasts",
                  httpMethod = "GET",
                  response = classOf[ArrayWrapper[Set[PodcastDTO]]],
                  responseContainer = "Set")
    def getAllPodcasts: Route = get {
        /*
        complete {
            //(userRepository ? UserRepository.GetUsers).mapTo[Set[UserRepository.User]]
        }
        */
        parameters('p.as[Int].?, 's.as[Int].?) { (page, size) =>
            log.info("GET /api/podcast?p={}&s={}", page.getOrElse(DEFAULT_PAGE), size.getOrElse(DEFAULT_SIZE))

            val p: Int = page.map(p => p-1).getOrElse(DEFAULT_PAGE)
            val s: Int = size.map(s => s-1).getOrElse(DEFAULT_SIZE)

            onSuccess(directoryStore ? GetAllPodcastsRegistrationComplete(p,s)) { // TODO
                case AllPodcastsResult(results) => {
                    log.info("PodcastGatewayService returns {} podcast entries on REST interface", results.size)
                    complete(StatusCodes.OK, ArrayWrapper(results))
                }
            }
        }
    }

    @ApiOperation(value = "Get podcast",
                  nickname = "getPodcast",
                  httpMethod = "GET",
                  response = classOf[PodcastDTO])
    def getPodcast(id: String): Route = get {

        log.info("GET /api/podcast/{}", id)

        onSuccess(directoryStore ? GetPodcast(id)) {
            case PodcastResult(podcast)     => complete(StatusCodes.OK, podcast)
            case NoDocumentFound(unknownId) => {
                log.error("DirectoryStore responded that there is no Podcast with echoId={}", unknownId)
                complete(StatusCodes.NotFound)
            }
        }
    }

    def getEpisodesByPodcast(id: String): Route = get {
        log.info("GET /api/podcast/{}/episodes", id)

        onSuccess(directoryStore ? GetEpisodesByPodcast(id)) {
            case EpisodesByPodcastResult(episodes)  => complete(StatusCodes.OK, episodes)
            case NoDocumentFound(unknownId)         => {
                log.error("DirectoryStore responded that there are not Episodes for Podcast with echoId={}", unknownId)
                complete(StatusCodes.NotFound)
            }
        }
    }

    def getFeedsByPodcast(id: String): Route = get {
        log.info("GET /api/podcast/{}/feeds", id)

        // TODO

        complete(StatusCodes.NotImplemented)
    }

    @ApiOperation(value = "Create new user", nickname = "userPost", httpMethod = "POST", produces = "text/plain")
    @ApiImplicitParams(Array(
        new ApiImplicitParam(name = "user", dataType = "nl.codecentric.UserRepository$User", paramType = "body", required = true)
    ))
    @ApiResponses(Array(
        new ApiResponse(code = 201, message = "User created"),
        new ApiResponse(code = 409, message = "User already exists")
    ))
    def postPodcast: Route = post {
        entity(as[PodcastDTO]) { podcast =>

            /*
            onSuccess(userRepository ? UserRepository.AddUser(user.name)) {
                case UserRepository.UserAdded(_)  => complete(StatusCodes.Created)
                case UserRepository.UserExists(_) => complete(StatusCodes.Conflict)
            }
            */

          complete(StatusCodes.NotImplemented)
        }
    }

    def putPodcast(id: String): Route = put {
        entity(as[PodcastDTO]) { podcast =>

            // TODO update podcast with echoId

            complete(StatusCodes.NotImplemented)
        }
    }

    def deletePodcast(id: String): Route = delete {

        // TODO delete podcast -  I guess this should not be supported?

        complete(StatusCodes.NotImplemented)
    }


}
