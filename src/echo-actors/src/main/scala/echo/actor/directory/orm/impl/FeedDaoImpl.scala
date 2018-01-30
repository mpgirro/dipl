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
@Repository("feedDao")
@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
class FeedDaoImpl extends FeedDao {

    @Autowired
    var entityManager: EntityManager = _

    def save(feed: Feed): Feed = {
        Option(feed.getId)
            .map(id => entityManager.merge(feed))
            .getOrElse(entityManager.persist(feed))
        entityManager.flush()
        return feed
    }

    def find(id: Long): Option[Feed] = {
        Option(entityManager.find(classOf[Feed], id))
    }

    def findByEchoId(echoId: String): Option[Feed] = {
        try {
            Some(entityManager.createQuery("FROM Feed WHERE echoId = :id", classOf[Feed])
                .setParameter("id", echoId)
                .getSingleResult)
        } catch {
            case e: NoResultException => None
        }
    }

    def getAll: List[Feed] = {
        entityManager.createQuery("FROM Feed", classOf[Feed]).getResultList.asScala.toList
    }

}
