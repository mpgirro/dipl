package echo.actor.directory.repository

import echo.core.domain.entity.{Chapter, Episode}
import org.springframework.data.jpa.repository.{JpaRepository, Query}
import org.springframework.data.repository.query.Param

/**
  * @author Maximilian Irro
  */
trait ChapterRepository extends JpaRepository[Chapter, java.lang.Long] {

    def findAllByEpisode(episode: Episode): java.util.List[Chapter]

    @Query("SELECT DISTINCT chapter FROM Chapter chapter " +
           "LEFT JOIN chapter.episode episode " +
           "WHERE episode.echoId = :echoId")
    def findAllByEpisodeEchoId(@Param("echoId") echoId: String): java.util.List[Chapter]

}
