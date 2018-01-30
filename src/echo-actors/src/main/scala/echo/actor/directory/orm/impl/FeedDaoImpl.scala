package echo.actor.directory.orm.impl

import javax.persistence.{EntityManager, NoResultException}

import echo.actor.directory.orm.FeedDao
import echo.core.model.domain.Feed
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.{Propagation, Transactional}
import org.springframework.stereotype._

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
class FeedDaoImpl(private val em: EntityManager) extends FeedDao {

    def save(feed: Feed): Feed = {
        val tx = em.getTransaction
        tx.begin
        try {
            Option(feed.getId)
                .map(id => em.merge(feed))
                .getOrElse(em.persist(feed))
            em.flush()
            feed
        } finally {
            tx.commit
        }
    }

    def find(id: Long): Option[Feed] = {
        val tx = em.getTransaction
        tx.begin
        try {
            Option(em.find(classOf[Feed], id))
        } finally {
            tx.commit
        }
    }

    def findByEchoId(echoId: String): Option[Feed] = {
        val tx = em.getTransaction
        tx.begin
        try {
            Some(em.createQuery("FROM Feed WHERE echoId = :id", classOf[Feed])
                .setParameter("id", echoId)
                .getSingleResult)
        } catch {
            case e: NoResultException => None
        } finally {
            tx.commit
        }
    }

    def findByUrl(url: String): Option[Feed] = {
        val tx = em.getTransaction
        tx.begin
        try {
            Some(em.createQuery("FROM Feed WHERE url = :url", classOf[Feed])
                .setParameter("url", url)
                .getSingleResult)
        } catch {
            case e: NoResultException => None
        } finally {
            tx.commit
        }
    }

    def getAll: List[Feed] = {
        val tx = em.getTransaction
        tx.begin
        try {
            em.createQuery("FROM Feed", classOf[Feed]).getResultList.asScala.toList
        } finally {
            tx.commit
        }
    }

}
