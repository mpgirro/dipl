package echo.actor.dispatch

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import echo.actor.protocol.ActorMessages._

/**
  * @author Maximilian Irro
  */
class DirectoryPriorityActorMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
    // Create a new PriorityGenerator, lower prio means more important
    PriorityGenerator {
        case ActorRefCrawlerActor(_)    => 0
        case ActorRefIndexStoreActor(_) => 0
        case GetPodcast(_)              => 1
        case GetAllPodcasts()           => 1
        case GetEpisode(_)              => 1
        case GetEpisodesByPodcast(_)    => 1
        case DebugPrintAllPodcasts()    => 1
        case DebugPrintAllEpisodes()    => 1
        case UpdatePodcastMetadata(_,_) => 2
        case UpdateEpisodeMetadata(_,_) => 2
        case _                          => 3
    })
