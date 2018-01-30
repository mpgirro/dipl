package echo.actor.directory.orm.impl

import javax.persistence.{EntityManager, NoResultException}

import echo.actor.directory.orm.EpisodeDao
import echo.core.model.domain.Episode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.{Propagation, Transactional}

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */

@Repository("episodeDao")
@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
class EpisodeDaoImpl extends EpisodeDao {

    @Autowired
    var entityManager: EntityManager = _

    def save(episode: Episode): Episode = {
        Option(episode.getId)
            .map(id => entityManager.merge(episode))
            .getOrElse(entityManager.persist(episode))
        entityManager.flush()
        return episode
    }

    def find(id: Long): Option[Episode] = {
        Option(entityManager.find(classOf[Episode], id))
    }

    def findByEchoId(echoId: String): Option[Episode] = {
        try {
            Some(entityManager.createQuery("FROM Episode WHERE echoId = :id", classOf[Episode])
                .setParameter("id", echoId)
                .getSingleResult)
        } catch {
            case e: NoResultException => None
        }
    }

    def getAll: List[Episode] = {
        entityManager.createQuery("FROM Episode", classOf[Episode]).getResultList.asScala.toList
    }

}
