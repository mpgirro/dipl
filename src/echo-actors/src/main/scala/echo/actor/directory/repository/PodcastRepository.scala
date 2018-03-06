package echo.actor.directory.repository

import echo.core.domain.entity.Podcast
import echo.core.domain.feed.FeedStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.{JpaRepository, Query}
import org.springframework.data.repository.query.Param

/**
  * @author Maximilian Irro
  */
trait PodcastRepository extends JpaRepository[Podcast, java.lang.Long] {

    def findOneByEchoId(echoId: String): Podcast

    @Query("SELECT DISTINCT podcast FROM Podcast podcast " +
           "LEFT JOIN podcast.feeds feed " +
           "WHERE feed.echoId = :feedId")
    def findOneByFeed(@Param("feedId") feedId: String): Podcast

    // TODO this returns 0 results. Why?
    @Query("SELECT DISTINCT podcast FROM Podcast podcast LEFT JOIN FETCH podcast.feeds feed WHERE feed.lastStatus <> :status")
    def findAllWhereFeedStatusIsNot(@Param("status") status: FeedStatus): java.util.List[Podcast]

    def findByRegistrationCompleteTrue(pageable: Pageable): java.util.List[Podcast]

    @Query("SELECT count(podcast) FROM Podcast podcast")
    def countAll(): Long

    @Query("SELECT count(podcast) FROM Podcast podcast WHERE podcast.registrationComplete = true")
    def countAllRegistrationCompleteTrue(): Long
}
