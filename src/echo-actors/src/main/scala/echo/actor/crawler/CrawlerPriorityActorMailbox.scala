package echo.actor.crawler

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import echo.actor.protocol.ActorMessages._

/**
  * @author Maximilian Irro
  */
class CrawlerPriorityActorMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
    // Create a new PriorityGenerator, lower prio means more important
    PriorityGenerator {
        case ActorRefDirectoryStoreActor(_) => 0
        case FetchNewFeed(_,_)    => 1
        case CrawlFyyd(_)         => 1
        case FetchUpdateFeed(_,_) => 2
        case FetchWebsite(_,_)    => 2
        case _ => 3
    })
