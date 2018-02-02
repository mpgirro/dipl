package echo.actor.directory.repository

import echo.core.model.domain.Feed
import org.springframework.data.jpa.repository.JpaRepository

/**
  * @author Maximilian Irro
  */
trait FeedRepository extends JpaRepository[Feed, java.lang.Long] {

    def findOneByUrl(url: String): Feed

    def findOneByEchoId(echoId: String): Feed

}
