package echo.actor.directory.orm

import echo.core.model.domain.Episode

/**
  * @author Maximilian Irro
  */
trait EpisodeDao {

    def save(episode: Episode): Episode

    def find(id: Long): Option[Episode]

    def findByEchoId(echoId: String): Option[Episode]

    def getAll: List[Episode]

}
