package echo.actor.crawler

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import echo.actor.ActorProtocol._

/**
  * @author Maximilian Irro
  */
class CrawlerPriorityActorMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
    // Create a new PriorityGenerator, lower prio means more important
    PriorityGenerator {
        case ActorRefParserActor(_)          => 0
        case ActorRefDirectoryStoreActor(_)  => 0
        case DownloadWithHeadCheck(_,_,_)    => 1
        case DownloadContent(_,_,_)                 => 1
        //case DownloadAsync(_,_,_)            => 2
        case CrawlFyyd(_)                    => 1
        //case FetchFeedForNewPodcast(_,_)     => 3
        //case FetchFeedForUpdateEpisodes(_,_) => 2
        //case FetchWebsite(_,_)               => 3
        case _                               => 5
    })
