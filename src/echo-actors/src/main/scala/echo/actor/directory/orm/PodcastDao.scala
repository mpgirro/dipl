package echo.actor.directory.orm

import echo.core.model.persistence.Podcast

/**
  * @author Maximilian Irro
  */
trait PodcastDao {

    def save(podcast: Podcast): Unit

    def find(id: Long): Option[Podcast]

    def getAll: List[Podcast]

    def getByLanguage(language : String): List[Podcast]

}
