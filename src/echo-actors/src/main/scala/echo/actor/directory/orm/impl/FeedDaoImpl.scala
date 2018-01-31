package echo.actor.directory.orm.impl

import javax.persistence.{EntityManager, EntityManagerFactory, NoResultException}

import echo.actor.directory.orm.FeedDao
import echo.core.model.domain.Feed
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.{Propagation, Transactional}
import org.springframework.stereotype._

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
class FeedDaoImpl(private val emf: EntityManagerFactory) extends FeedDao {

    def save(feed: Feed): Feed = {
        val em = emf.createEntityManager
        val tx = em.getTransaction
        tx.begin()

        Option(feed.getId)
            .map(id => em.merge(feed))
            .getOrElse(em.persist(feed))
        em.flush()

        tx.commit
        em.close()

        feed
    }

    def find(id: Long): Option[Feed] = {
        val em = emf.createEntityManager
        val tx = em.getTransaction
        tx.begin

        val result = Option(em.find(classOf[Feed], id))

        tx.commit
        em.close

        result
    }

    def findByEchoId(echoId: String): Option[Feed] = {
        val em = emf.createEntityManager
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
            em.close
        }
    }

    def findByUrl(url: String): Option[Feed] = {
        val em = emf.createEntityManager
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
            em.close
        }
    }

    def getAll: List[Feed] = {
        val em = emf.createEntityManager
        val tx = em.getTransaction
        tx.begin

        val result = em.createQuery("FROM Feed", classOf[Feed]).getResultList.asScala.toList

        tx.commit
        em.close

        result
    }

}
