package echo.actor.gateway.service

import javax.ws.rs.Path

import akka.actor.{ActorContext, ActorRef}
import akka.dispatch.MessageDispatcher
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import akka.util.Timeout
import echo.actor.gateway.json.JsonSupport
import echo.actor.ActorProtocol.{EpisodeResult, GetEpisode, NoDocumentFound}
import echo.core.domain.dto.EpisodeDTO
import io.swagger.annotations._

/**
  * @author Maximilian Irro
  */

@Path("/api/episode")  // @Path annotation required for Swagger
@Api(value = "/api/episode",
     produces = "application/json")
class EpisodeGatewayService (private val log: LoggingAdapter)
                            (private implicit val context: ActorContext, private implicit val timeout: Timeout) extends GatewayService with Directives with JsonSupport {

    // will be set after construction of the service via the setter method,
    // once the message with the reference arrived
    private var directoryStore: ActorRef = _

    override val blockingDispatcher: MessageDispatcher = context.system.dispatchers.lookup(DISPATCHER_ID)

    override val route: Route = pathPrefix("episode") { pathEndOrSingleSlash { getAllEpisodes ~ postEpisode } } ~
                    pathPrefix("episode" / Segment) { id => getEpisode(id) ~ putEpisode(id) ~ deleteEpisode(id) }


    def setDirectoryStoreActorRef(directoryStore: ActorRef): Unit = this.directoryStore = directoryStore


    @ApiOperation(value = "Get list of all Episodes",
                  nickname = "getAllEpisodes",
                  httpMethod = "GET",
                  response = classOf[Set[EpisodeDTO]],
                  responseContainer = "Set")
    def getAllEpisodes: Route = get {
        /*
        complete {
            //(userRepository ? UserRepository.GetUsers).mapTo[Set[UserRepository.User]]
        }
        */
        complete(StatusCodes.NotImplemented)
    }

    @ApiOperation(value = "Get episode",
                  nickname = "getEpisode",
                  httpMethod = "GET",
                  response = classOf[EpisodeDTO])
    def getEpisode(id: String): Route = get {

        log.info("GET /api/episode/{}", id)

        onSuccess(directoryStore ? GetEpisode(id)) {
            case EpisodeResult(episode)     => complete(StatusCodes.OK, episode)
            case NoDocumentFound(unknownId) => {
                log.error("DirectoryStore responded that there is no Episode with echoId={}", unknownId)
                complete(StatusCodes.NotFound)
            }
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

            /*
            onSuccess(userRepository ? UserRepository.AddUser(user.name)) {
                case UserRepository.UserAdded(_)  => complete(StatusCodes.Created)
                case UserRepository.UserExists(_) => complete(StatusCodes.Conflict)
            }
            */

          complete(StatusCodes.NotImplemented)
        }
    }

    def putEpisode(id: String): Route = put {
        entity(as[EpisodeDTO]) { episode =>

            // TODO update podcast with echoId

            complete(StatusCodes.NotImplemented)
        }
    }

    def deleteEpisode(id: String): Route = delete {

        // TODO delete podcast -  I guess this should not be supported?

        complete(StatusCodes.NotImplemented)
    }


}
