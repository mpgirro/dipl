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
class PodcastDaoImpl(private val em: EntityManager) extends PodcastDao {

    def save(podcast: Podcast): Podcast = {
        val tx = em.getTransaction
        tx.begin
        try {
            Option(podcast.getId)
                .map(id => em.merge(podcast))
                .getOrElse(em.persist(podcast))
            em.flush()
            podcast
        } finally {
            tx.commit()
        }
    }

    def find(id: Long): Option[Podcast] = {
        val tx = em.getTransaction
        tx.begin
        try {
            Option(em.find(classOf[Podcast], id))
        } finally {
            tx.commit()
        }
    }

    def findByEchoId(echoId: String): Option[Podcast] = {
        val tx = em.getTransaction
        tx.begin
        try {
            Some(em.createQuery("FROM Podcast WHERE echoId = :id", classOf[Podcast])
                .setParameter("id", echoId)
                .getSingleResult)
        } catch {
            case e: NoResultException => None
        } finally {
            tx.commit()
        }
    }

    def getAll: List[Podcast] = {
        val tx = em.getTransaction
        tx.begin
        try {
            em.createQuery("FROM Podcast", classOf[Podcast]).getResultList.asScala.toList
        } finally {
            tx.commit()
        }
    }

    def getByLanguage(language : String): List[Podcast] = {
        val tx = em.getTransaction
        tx.begin
        try {
            em.createQuery("FROM Podcast WHERE language = :language", classOf[Podcast])
                .setParameter("language", language)
                .getResultList.asScala.toList
        } finally {
            tx.commit()
        }
    }
}
