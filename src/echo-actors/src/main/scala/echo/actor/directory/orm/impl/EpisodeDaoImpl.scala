package echo.actor.directory.orm.impl

import javax.persistence.{EntityManager, NoResultException}

import echo.actor.directory.orm.EpisodeDao
import echo.core.model.domain.{Episode, Podcast}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.{Propagation, Transactional}

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
class EpisodeDaoImpl(private val em: EntityManager) extends EpisodeDao {

    def save(episode: Episode): Episode = {
        val tx = em.getTransaction
        tx.begin
        try {
            Option(episode.getId)
                .map(id => em.merge(episode))
                .getOrElse(em.persist(episode))
            em.flush()
            episode
        } finally {
            tx.commit
        }
    }

    def find(id: Long): Option[Episode] = {
        val tx = em.getTransaction
        tx.begin
        try {
            Option(em.find(classOf[Episode], id))
        } finally {
            tx.commit
        }
    }

    def findByEchoId(echoId: String): Option[Episode] = {
        val tx = em.getTransaction
        tx.begin
        try {
            Some(em.createQuery("FROM Episode WHERE echoId = :id", classOf[Episode])
                .setParameter("id", echoId)
                .getSingleResult)
        } catch {
            case e: NoResultException => None
        } finally {
            tx.commit
        }
    }

    def getAll: List[Episode] = {
        val tx = em.getTransaction
        tx.begin
        try {
            em.createQuery("FROM Episode", classOf[Episode]).getResultList.asScala.toList
        } finally {
            tx.commit
        }
    }

    def getAllByPodcast(podcast: Podcast): List[Episode] = {
        val tx = em.getTransaction
        tx.begin
        try {
            em.createQuery("FROM Episode WHERE podcast=:podcast", classOf[Episode])
                .setParameter("podcast", podcast)
                .getResultList.asScala.toList
        } finally {
            tx.commit
        }
    }

}
