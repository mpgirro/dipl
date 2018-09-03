package echo.actor

import akka.actor.ActorRef
import com.google.common.collect.ImmutableList
import echo.core.benchmark.FeedProperty
import echo.core.benchmark.mps.MessagesPerSecondResult
import echo.core.benchmark.rtt.RoundTripTime
import echo.core.domain.dto._


/**
  * @author Maximilian Irro
  */
object ActorProtocol {

    // Job variants for Crawler
    trait FetchJob
    case class NewPodcastFetchJob() extends FetchJob
    case class UpdateEpisodesFetchJob(etag: String, lastMod: String) extends FetchJob
    case class WebsiteFetchJob() extends FetchJob

    // Msg: Catalog -> Updater
    case class ProcessFeed(exo: String, url: String, job: FetchJob, rtt: RoundTripTime)

    // Msg: Updater -> Crawler
    case class DownloadWithHeadCheck(exo: String, url: String, job: FetchJob, rtt: RoundTripTime)
    case class DownloadContent(exo: String, url: String, job: FetchJob, encoding: Option[String], rtt: RoundTripTime)

    // Crawler -> Parser
    case class ParseNewPodcastData(feedUrl: String, podcastExo: String, feedData: String, rtt: RoundTripTime)
    case class ParseUpdateEpisodeData(feedUrl: String, podcastExo: String, episodeFeedData: String, rtt: RoundTripTime)
    case class ParseWebsiteData(exo: String, html: String)
    case class ParseFyydEpisodes(podcastExo: String, episodesData: String)

    // Gateway(= Web) -> Searcher; CLI -> Gateway (Benchmark)
    case class SearchRequest(query: String, page: Option[Int], size: Option[Int], rtt: RoundTripTime)
    case class BenchmarkSearchRequest(query: String, page: Option[Int], size: Option[Int], rtt: RoundTripTime)

    // Searcher -> User
    case class SearchResults(results: ResultWrapperDTO, rtt: RoundTripTime)

    // These messages are sent to propagate actorRefs to other actors, to overcome circular dependencies
    trait ActorRefInfo

    case class ActorRefCatalogStoreActor(ref: ActorRef) extends ActorRefInfo
    case class ActorRefCrawlerActor(ref: ActorRef) extends ActorRefInfo
    case class ActorRefParserActor(ref: ActorRef) extends ActorRefInfo
    case class ActorRefFeedStoreActor(ref: ActorRef) extends ActorRefInfo
    case class ActorRefIndexStoreActor(ref: ActorRef) extends ActorRefInfo
    case class ActorRefSearcherActor(ref: ActorRef) extends ActorRefInfo
    case class ActorRefGatewayActor(ref: ActorRef) extends ActorRefInfo
    case class ActorRefUpdaterActor(ref: ActorRef) extends ActorRefInfo
    case class ActorRefBenchmarkMonitor(ref: ActorRef) extends ActorRefInfo
    case class ActorRefSupervisor(ref: ActorRef) extends ActorRefInfo

    // Benchmark
    case class IndexSubSystemRoundTripTimeReport(rtt: RoundTripTime)
    case class RetrievalSubSystemRoundTripTimeReport(rtt: RoundTripTime)
    case class MonitorFeedProgress(feedProperties: ImmutableList[FeedProperty])
    case class MonitorQueryProgress(queries: ImmutableList[String])
    case class StartMessagePerSecondMonitoring()
    case class StopMessagePerSecondMonitoring()
    case class MessagePerSecondReport(report: MessagesPerSecondResult)
    case class ChildMpsReport(report: MessagesPerSecondResult)

    // These are maintenance methods, I use during development
    case class DebugPrintAllPodcasts()    // User/CLI -> CatalogStore
    case class DebugPrintAllEpisodes()    // User/CLI -> CatalogStore
    case class DebugPrintAllFeeds()
    case class DebugPrintCountAllPodcasts()
    case class DebugPrintCountAllEpisodes()
    case class DebugPrintCountAllFeeds()
    case class LoadTestFeeds()            // CLI -> CatalogStore
    case class LoadMassiveFeeds()         // CLI -> CatalogStore

    // User -> Crawler
    // TODO: automatic: Crawler -> Crawler on a regular basis
    trait CrawlExternalDirectory
    case class CrawlFyyd(count: Int) extends CrawlExternalDirectory
    case class LoadFyydEpisodes(podcastId: String, fyydId: Long) extends CrawlExternalDirectory

    // CLI -> Master
    case class ShutdownSystem()

}
