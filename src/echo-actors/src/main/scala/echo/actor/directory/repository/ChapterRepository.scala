package echo.actor.directory.repository

import echo.core.domain.entity.{ChapterEntity, EpisodeEntity}
import org.springframework.data.jpa.repository.{JpaRepository, Query}
import org.springframework.data.repository.query.Param

/**
  * @author Maximilian Irro
  */
trait ChapterRepository extends JpaRepository[ChapterEntity, java.lang.Long] {

    def findAllByEpisode(episode: EpisodeEntity): java.util.List[ChapterEntity]

    @Query("SELECT DISTINCT chapter FROM ChapterEntity chapter WHERE chapter.episode.echoId = :episodeExo")
    def findAllByEpisodeEchoId(@Param("episodeExo") episodeExo: String): java.util.List[ChapterEntity]

}
