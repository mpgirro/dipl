package echo.actor.directory.repository

import echo.core.model.domain.{Episode, Podcast}
import org.springframework.data.jpa.repository.{JpaRepository, Query}
import org.springframework.data.repository.query.Param

/**
  * @author Maximilian Irro
  */
trait EpisodeRepository extends JpaRepository[Episode, java.lang.Long] {

    def findOneByEchoId(echoId: String): Episode

    def findAllByPodcast(podcast: Podcast): java.util.List[Episode]

    @Query("SELECT DISTINCT episode from Episode episode " +
           "LEFT JOIN episode.podcast podcast " +
           "WHERE podcast.echoId = :echoId")
    def findAllByPodcastEchoId(@Param("echoId") echoId: String): java.util.List[Episode]

}
