package echo.actor.parser

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}
import akka.event.LoggingReceive
import echo.actor.ActorProtocol._
import echo.actor.parser.DirectoryStoreResponseHandler.DirectoryResponseTimeout
import echo.core.domain.dto.EpisodeDTO
import echo.core.mapper.IndexMapper
import echo.core.util.EchoIdGenerator
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

import scala.concurrent.duration._

/**
  * @author Maximilian Irro
  */

object DirectoryStoreResponseHandler {
    case object DirectoryResponseTimeout

    def props(podcastId: String, episode: EpisodeDTO, directoryStore: ActorRef, indexStore: ActorRef, crawler: ActorRef): Props = {
        Props(new DirectoryStoreResponseHandler(podcastId, episode, directoryStore, indexStore, crawler))
    }
}

class DirectoryStoreResponseHandler(podcastId: String,
                                    episode: EpisodeDTO,
                                    directoryStore: ActorRef,
                                    indexStore: ActorRef,
                                    crawler: ActorRef) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val timeout = 10.seconds

    import context.dispatcher
    val timeoutMessager: Cancellable = context.system.scheduler.
        scheduleOnce(timeout) { // TODO read timeout val from config
            self ! DirectoryResponseTimeout
        }

    override def receive = LoggingReceive {

        case EpisodeRegistered(episodeId) =>
            log.debug("Received EpisodeRegistered({})", episodeId)
            timeoutMessager.cancel

            // TODO maybe we want to update the data in directory/index ?

            context.stop(self)

        case EpisodeNotRegistered =>
            log.debug("Received EpisodeNotRegistered()")
            timeoutMessager.cancel

            registerNewEpisode()

            context.stop(self)

        case DirectoryResponseTimeout =>
            log.info("Received DirectoryResponseTimeout() -- stopping now")
            context.stop(self)

        case unknown =>
            log.debug("Received an unknown message type : {}", unknown.getClass)
            log.debug("Stopping because received an unknown message : {}", self.path.name)
            context.stop(self)
    }

    private def registerNewEpisode(): Unit = {

        // generate a new episode echoId - the generator is (almost) ensuring uniqueness
        episode.setEchoId(EchoIdGenerator.getNewId)

        // cleanup some potentially markuped texts
        Option(episode.getDescription).foreach(d => episode.setDescription(Jsoup.clean(d, Whitelist.basic())))
        Option(episode.getContentEncoded).foreach(c => episode.setContentEncoded(Jsoup.clean(c, Whitelist.basic())))

        // send out the episode data for for registration/indexation
        directoryStore ! UpdateEpisodeMetadata(podcastId, episode)
        indexStore ! IndexStoreAddDoc(IndexMapper.INSTANCE.map(episode))

        // request that the website will get added to the episodes index entry as well
        Option(episode.getLink) match {
            case Some(link) => crawler ! FetchWebsite(episode.getEchoId, link)
            case None => log.debug("No link set for episode {} --> no website data will be added to the index", episode.getEchoId)
        }
    }

}
