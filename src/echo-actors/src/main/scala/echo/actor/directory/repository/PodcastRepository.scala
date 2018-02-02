package echo.actor.directory.repository

import echo.core.model.domain.Podcast
import echo.core.model.feed.FeedStatus
import org.springframework.data.jpa.repository.{JpaRepository, Query}
import org.springframework.data.repository.query.Param

/**
  * @author Maximilian Irro
  */
trait PodcastRepository extends JpaRepository[Podcast, java.lang.Long] {

    def findOneByEchoId(echoId: String): Podcast

    // TODO this returns 0 results. Why?
    @Query("SELECT DISTINCT podcast FROM Podcast podcast LEFT JOIN FETCH podcast.feeds feed WHERE feed.lastStatus <> :status")
    def findAllWhereFeedStatusIsNot(@Param("status") status: FeedStatus): java.util.List[Podcast]


}
