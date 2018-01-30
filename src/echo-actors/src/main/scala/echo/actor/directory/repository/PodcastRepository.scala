package echo.actor.directory.repository

import echo.core.model.domain.Podcast
import org.springframework.data.jpa.repository.JpaRepository

/**
  * @author Maximilian Irro
  */
trait PodcastRepository extends JpaRepository[Podcast, java.lang.Long] {

}
