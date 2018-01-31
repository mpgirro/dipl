package echo.actor.directory.orm.impl

import javax.persistence.{EntityManager, EntityManagerFactory, NoResultException}

import echo.actor.directory.orm.EpisodeDao
import echo.core.model.domain.{Episode, Podcast}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.{Propagation, Transactional}

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
class EpisodeDaoImpl(private val emf: EntityManagerFactory) extends EpisodeDao {

    def save(episode: Episode): Episode = {
        val em = emf.createEntityManager
        val tx = em.getTransaction
        tx.begin()

        Option(episode.getId)
            .map(id => em.merge(episode))
            .getOrElse(em.persist(episode))
        em.flush()

        tx.commit
        em.close()

        episode
    }

    def find(id: Long): Option[Episode] = {
        val em = emf.createEntityManager
        val tx = em.getTransaction
        tx.begin

        val result = Option(em.find(classOf[Episode], id))

        tx.commit
        em.close

        result
    }

    def findByEchoId(echoId: String): Option[Episode] = {
        val em = emf.createEntityManager
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
            em.close
        }
    }

    def getAll: List[Episode] = {
        val em = emf.createEntityManager
        val tx = em.getTransaction
        tx.begin

        val result = em.createQuery("FROM Episode", classOf[Episode]).getResultList.asScala.toList

        tx.commit
        em.close

        result
    }

    def getAllByPodcast(podcast: Podcast): List[Episode] = {
        val em = emf.createEntityManager
        val tx = em.getTransaction
        tx.begin

        val result = em.createQuery("FROM Episode WHERE podcast=:podcast", classOf[Episode])
            .setParameter("podcast", podcast)
            .getResultList.asScala.toList

        tx.commit
        em.close

        result
    }

}
