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
class PodcastDaoImpl(private val emf: EntityManagerFactory) extends PodcastDao {

    def save(podcast: Podcast): Podcast = {
        val em = emf.createEntityManager
        val tx = em.getTransaction
        tx.begin

        Option(podcast.getId)
            .map(id => em.merge(podcast))
            .getOrElse(em.persist(podcast))
        em.flush

        tx.commit
        em.close

        podcast
    }

    def find(id: Long): Option[Podcast] = {
        val em = emf.createEntityManager
        val tx = em.getTransaction
        tx.begin

        val result = Option(em.find(classOf[Podcast], id))

        tx.commit
        em.close

        result
    }

    def findByEchoId(echoId: String): Option[Podcast] = {
        val em = emf.createEntityManager
        val tx = em.getTransaction
        tx.begin
        try {
            Some(em.createQuery("FROM Podcast WHERE echoId = :id", classOf[Podcast])
                .setParameter("id", echoId)
                .getSingleResult)
        } catch {
            case e: NoResultException => None
        } finally {
            tx.commit
            em.close
        }
    }

    def getAll: List[Podcast] = {
        val em = emf.createEntityManager
        val tx = em.getTransaction
        tx.begin

        val result = em.createQuery("FROM Podcast", classOf[Podcast]).getResultList.asScala.toList

        tx.commit
        em.close

        result
    }

    def getByLanguage(language : String): List[Podcast] = {
        val em = emf.createEntityManager
        val tx = em.getTransaction
        tx.begin

        val result = em.createQuery("FROM Podcast WHERE language = :language", classOf[Podcast])
            .setParameter("language", language)
            .getResultList.asScala.toList

        tx.commit
        em.close

        result
    }
}
