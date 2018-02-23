package echo.actor.directory.repository

import echo.core.domain.entity.Feed
import org.springframework.data.jpa.repository.{JpaRepository, Query}
import org.springframework.data.repository.query.Param

/**
  * @author Maximilian Irro
  */
trait FeedRepository extends JpaRepository[Feed, java.lang.Long] {

    def findOneByEchoId(echoId: String): Feed

    def findAllByUrl(url: String): java.util.List[Feed]

    @Query("SELECT DISTINCT feed FROM Feed feed WHERE feed.podcast.echoId = :podcastId")
    def findAllByPodcast(@Param("podcastId") podcastId: String): java.util.List[Feed]

    @Query("SELECT DISTINCT feed FROM Feed feed WHERE feed.url = :url AND feed.podcast.echoId = :podcastId")
    def findOneByUrlAndPodcastEchoId(@Param("url") url: String,
                                     @Param("podcastId") podcastId: String): Feed

    @Query("SELECT count(feed) FROM Feed feed")
    def countAll(): Long

    @Query("SELECT count(feed) FROM Feed feed WHERE feed.podcast.echoId = :podcastId")
    def countByPodcast(@Param("podcastId") podcastId: String)

}
