package echo.actor.directory.repository

import echo.core.domain.entity.{PodcastEntity}
import echo.core.domain.feed.FeedStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.{JpaRepository, Query}
import org.springframework.data.repository.query.Param

/**
  * @author Maximilian Irro
  */
trait PodcastRepository extends JpaRepository[PodcastEntity, java.lang.Long] {

    def findOneByEchoId(echoId: String): PodcastEntity

    @Query("SELECT DISTINCT podcast FROM PodcastEntity podcast " +
           "LEFT JOIN podcast.feeds feed " +
           "WHERE feed.echoId = :feedExo")
    def findOneByFeed(@Param("feedExo") feedExo: String): PodcastEntity

    def findByRegistrationCompleteTrue(pageable: Pageable): java.util.List[PodcastEntity]

    @Query("SELECT count(podcast) FROM PodcastEntity podcast")
    def countAll(): Long

    @Query("SELECT count(podcast) FROM PodcastEntity podcast WHERE podcast.registrationComplete = true")
    def countAllRegistrationCompleteTrue(): Long
}
