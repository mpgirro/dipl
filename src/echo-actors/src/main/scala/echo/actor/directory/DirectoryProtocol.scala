package echo.actor.directory

import java.time.LocalDateTime

import echo.core.domain.dto.{ChapterDTO, EpisodeDTO, FeedDTO, PodcastDTO}
import echo.core.domain.feed.FeedStatus

/**
  * @author Maximilian Irro
  */
object DirectoryProtocol {

    trait DirectoryCommand

    // Crawler -> DirectoryStore
    case class FeedStatusUpdate(podcastId: String, feedUrl: String, timestamp: LocalDateTime, status: FeedStatus) extends DirectoryCommand
    case class UpdateFeedUrl(oldUrl: String, newUrl: String) extends DirectoryCommand
    case class UpdateLinkByEchoId(echoId: String, newUrl: String) extends DirectoryCommand

    case class SaveChapter(chapter: ChapterDTO) extends DirectoryCommand

    // Parser -> DirectoryStore
    case class UpdatePodcast(podcastId: String, feedUrl: String, podcast: PodcastDTO) extends DirectoryCommand
    case class UpdateEpisode(podcastId: String, episode: EpisodeDTO) extends DirectoryCommand
    case class UpdateEpisodeWithChapters(podcastId: String, episode: EpisodeDTO, chapter: List[ChapterDTO]) extends DirectoryCommand
    case class AddOrUpdatePodcastAndFeed(podcast: PodcastDTO, feed: FeedDTO) extends DirectoryCommand


    trait DirectoryQuery

    // Gateway -> DirectoryStore
    case class GetPodcast(podcastId: String) extends DirectoryQuery
    case class GetAllPodcasts(page: Int, size: Int) extends DirectoryQuery
    case class GetAllPodcastsRegistrationComplete(page: Int, size: Int) extends DirectoryQuery
    case class GetAllFeeds(page: Int, size: Int) extends DirectoryQuery
    case class GetEpisode(episodeId: String) extends DirectoryQuery
    case class GetEpisodesByPodcast(podcastId: String) extends DirectoryQuery
    case class GetFeedsByPodcast(podcastId: String) extends DirectoryQuery
    case class GetChaptersByEpisode(episodeId: String) extends DirectoryQuery

    // these are not queries at first glance, but basically they ask if something is known (and maybe lead to a side effect then)
    case class ProposeNewFeed(url: String) extends DirectoryQuery                                  // Web/CLI -> DirectoryStore
    case class RegisterEpisodeIfNew(podcastId: String, episode: EpisodeDTO) extends DirectoryQuery // Questions: Parser -> DirectoryStore

    // Web/CLI -> DirectoryStore
    case class CheckPodcast(echoId: String) extends DirectoryQuery
    case class CheckFeed(echoId: String) extends DirectoryQuery
    case class CheckAllPodcasts() extends DirectoryQuery
    case class CheckAllFeeds() extends DirectoryQuery


    trait DirectoryResult

    // DirectoryStore -> Gateway
    case class PodcastResult(podcast: PodcastDTO) extends DirectoryResult
    case class AllPodcastsResult(results: List[PodcastDTO]) extends DirectoryResult
    case class AllFeedsResult(results: List[FeedDTO]) extends DirectoryResult
    case class EpisodeResult(episode: EpisodeDTO) extends DirectoryResult
    case class EpisodesByPodcastResult(episodes: List[EpisodeDTO]) extends DirectoryResult
    case class FeedsByPodcastResult(feeds: List[FeedDTO]) extends DirectoryResult
    case class ChaptersByEpisodeResult(chapters: List[ChapterDTO]) extends DirectoryResult
    case class NothingFound(echoId: String) extends DirectoryResult

}
