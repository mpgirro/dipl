package echo.actor.directory.repository

import echo.core.domain.entity.{FeedEntity}
import org.springframework.data.jpa.repository.{JpaRepository, Query}
import org.springframework.data.repository.query.Param

/**
  * @author Maximilian Irro
  */
trait FeedRepository extends JpaRepository[FeedEntity, java.lang.Long] {

    def findOneByEchoId(echoId: String): FeedEntity

    def findAllByUrl(url: String): java.util.List[FeedEntity]

    @Query("SELECT DISTINCT feed FROM FeedEntity feed WHERE feed.podcast.echoId = :podcastExo")
    def findAllByPodcast(@Param("podcastExo") podcastExo: String): java.util.List[FeedEntity]

    @Query("SELECT DISTINCT feed FROM FeedEntity feed WHERE feed.url = :url AND feed.podcast.echoId = :podcastExo")
    def findOneByUrlAndPodcastEchoId(@Param("url") url: String,
                                     @Param("podcastExo") podcastExo: String): FeedEntity

    @Query("SELECT count(feed) FROM FeedEntity feed")
    def countAll(): Long

    @Query("SELECT count(feed) FROM FeedEntity feed WHERE feed.podcast.echoId = :podcastExo")
    def countByPodcast(@Param("podcastExo") podcastExo: String): Long

}
