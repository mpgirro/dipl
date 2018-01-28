package echo.actor.directory.orm.impl

import org.springframework.beans.factory.annotation._
import org.springframework.stereotype._
import org.springframework.transaction.annotation.{Propagation, Transactional}
import javax.persistence._

import echo.actor.directory.orm.PodcastDao
import echo.core.model.persistence.Podcast

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */

@Repository("podcastDao")
@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
class PodcastDaoImpl extends PodcastDao {

    @Autowired
    var entityManager: EntityManager = _

    def save(podcast: Podcast): Unit = {
        if(entityManager == null){
            println("the @Autowired entityManager is NULL!")
        }
        Option(podcast.getId)
            .map(id => entityManager.merge(podcast))
            .getOrElse(entityManager.persist(podcast))
    }

    def find(id: Long): Option[Podcast] = {
        Option(entityManager.find(classOf[Podcast], id))
    }

    def getAll: List[Podcast] = {
        entityManager.createQuery("FROM podcast", classOf[Podcast]).getResultList.asScala.toList
    }

    def getByLanguage(language : String): List[Podcast] = {
        entityManager.createQuery("FROM podcast WHERE language = :language", classOf[Podcast]).setParameter("language", language).getResultList.asScala.toList
    }
}
