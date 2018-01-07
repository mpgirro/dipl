package echo.actor.protocol

object CrawlerProtocol {

    trait CrawlerMessage

    case class CrawlFeed(feed : String) extends CrawlerMessage

}
