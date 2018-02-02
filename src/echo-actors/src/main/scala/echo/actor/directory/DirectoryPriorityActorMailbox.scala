package echo.actor.directory

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import echo.actor.ActorProtocol._

/**
  * @author Maximilian Irro
  */
class DirectoryPriorityActorMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
    // Create a new PriorityGenerator, lower prio means more important
    PriorityGenerator {
        case ActorRefCrawlerActor(_)    => 0
        case ActorRefIndexStoreActor(_) => 0
        case DebugPrintAllPodcasts      => 0
        case DebugPrintAllEpisodes      => 0
        case GetPodcast(_)              => 1
        case GetAllPodcasts             => 1
        case GetEpisode(_)              => 1
        case GetEpisodesByPodcast(_)    => 1
        case FeedStatusUpdate(_,_,_)    => 2
        case UpdatePodcastMetadata(_,_) => 3
        case UpdateEpisodeMetadata(_,_) => 3
        case UsePodcastItunesImage(_)   => 4
        case ProposeNewFeed(_)          => 5
        case _                          => 6
    })
