package echo.actor.directory.orm

import echo.core.model.domain.Podcast

/**
  * @author Maximilian Irro
  */
trait PodcastDao {

    def save(podcast: Podcast): Podcast

    def find(id: Long): Option[Podcast]

    def findByEchoId(echoId: String): Option[Podcast]

    def getAll: List[Podcast]

    def getByLanguage(language : String): List[Podcast]

}
