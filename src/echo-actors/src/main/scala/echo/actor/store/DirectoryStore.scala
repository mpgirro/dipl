package echo.actor.store

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging}
import echo.actor.protocol.Protocol._
import echo.core.feed.FeedStatus

/**
  * @author Maximilian Irro
  */
class DirectoryStore extends Actor with ActorLogging {

    override def receive: Receive = {

        case ProposeNewFeed(feedUrl: String) => {
            // TODO
        }

        case FeedStatusUpdate(feedUrl: String, timestamp: LocalDateTime, feedStatus: FeedStatus) => {
            // TODO
        }



    }
}
