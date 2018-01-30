package echo.actor.directory.orm

import echo.core.model.domain.Feed

/**
  * @author Maximilian Irro
  */
trait FeedDao {

    def save(feed: Feed): Feed

    def find(id: Long): Option[Feed]

    def findByEchoId(echoId: String): Option[Feed]

    def findByUrl(url: String): Option[Feed]

    def getAll: List[Feed]

}
