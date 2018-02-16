package echo.actor.directory.repository

import echo.core.model.domain.Feed
import org.springframework.data.jpa.repository.{JpaRepository, Query}
import org.springframework.data.repository.query.Param

/**
  * @author Maximilian Irro
  */
trait FeedRepository extends JpaRepository[Feed, java.lang.Long] {

    def findOneByEchoId(echoId: String): Feed

    def findAllByUrl(url: String): java.util.List[Feed]

    @Query("select distinct feed from Feed feed where feed.url = :url and feed.podcast.echoId = :podcastId")
    def findOneByUrlAndPodcastEchoId(@Param("url") url: String, @Param("podcastId") podcastId: String): Feed

}
