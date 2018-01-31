package echo.actor.dispatch

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import echo.actor.protocol.ActorMessages._

/**
  * @author Maximilian Irro
  */

class ParserPriorityActorMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
    // Create a new PriorityGenerator, lower prio means more important
    PriorityGenerator {
        case ActorRefIndexStoreActor(_)     => 0
        case ActorRefDirectoryStoreActor(_) => 0
        case ActorRefCrawlerActor(_)        => 0
        case IndexPodcastData(_,_)          => 1 // ensure the podcast gets parsed before the episode do (TODO do I still rely on this?)
        case IndexEpisodeData(_,_)          => 2
        case IndexWebsiteData(_,_)          => 3
        case IndexFeedData(_,_,_,_)         => 4 // this produces work, so it should be done with lower work than the processing of podcast/episode data
        case _ => 5
    })
