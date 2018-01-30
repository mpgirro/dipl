package echo.actor.directory.orm.impl

import org.springframework.beans.factory.annotation._
import org.springframework.stereotype._
import org.springframework.transaction.annotation.{Propagation, Transactional}
import javax.persistence._

import echo.actor.directory.orm.PodcastDao
import echo.core.model.domain.Podcast

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */

@Repository("podcastDao")
@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
class PodcastDaoImpl extends PodcastDao {

    @Autowired
    var entityManager: EntityManager = _

    def save(podcast: Podcast): Podcast = {
        Option(podcast.getId)
            .map(id => entityManager.merge(podcast))
            .getOrElse(entityManager.persist(podcast))
        entityManager.flush()
        return podcast
    }

    def find(id: Long): Option[Podcast] = {
        Option(entityManager.find(classOf[Podcast], id))
    }

    def findByEchoId(echoId: String): Option[Podcast] = {
        try {
            Some(entityManager.createQuery("FROM Podcast WHERE echoId = :id", classOf[Podcast])
                .setParameter("id", echoId)
                .getSingleResult)
        } catch {
            case e: NoResultException => None
        }
    }

    def getAll: List[Podcast] = {
        entityManager.createQuery("FROM Podcast", classOf[Podcast]).getResultList.asScala.toList
    }

    def getByLanguage(language : String): List[Podcast] = {
        entityManager.createQuery("FROM Podcast WHERE language = :language", classOf[Podcast])
            .setParameter("language", language)
            .getResultList.asScala.toList
    }
}
