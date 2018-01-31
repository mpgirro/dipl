package echo.actor.directory.repository

import echo.core.model.domain.{Episode, Podcast}
import org.springframework.data.jpa.repository.JpaRepository

/**
  * @author Maximilian Irro
  */
trait EpisodeRepository extends JpaRepository[Episode, java.lang.Long] {

    def findOneByEchoId(echoId: String): Episode

    def findAllByPodcast(podcast: Podcast): java.util.List[Episode]

}
