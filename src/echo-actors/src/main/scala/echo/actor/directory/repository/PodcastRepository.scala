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
           "WHERE feed.echoId = :feedExo")
    def findOneByFeed(@Param("feedExo") feedExo: String): Podcast

    def findByRegistrationCompleteTrue(pageable: Pageable): java.util.List[Podcast]

    @Query("SELECT count(podcast) FROM Podcast podcast")
    def countAll(): Long

    @Query("SELECT count(podcast) FROM Podcast podcast WHERE podcast.registrationComplete = true")
    def countAllRegistrationCompleteTrue(): Long
}
