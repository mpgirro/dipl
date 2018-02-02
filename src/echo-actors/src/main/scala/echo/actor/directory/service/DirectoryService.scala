package echo.actor.directory.service

import javax.persistence.{EntityManager, EntityTransaction}

import akka.event.LoggingAdapter

/**
  * @author Maximilian Irro
  */
trait DirectoryService[T] {

    protected def log: LoggingAdapter
    protected def em: EntityManager

    def save(dto: T): Option[T] = {
        val tx = em.getTransaction
        tx.begin()
        try {
            val result = save(dto, tx)
            tx.commit()
            return result
        } catch {
            case e: Exception => {
                log.error("Error trying to save : {}", dto)
                tx.rollback()
            }
        }
        None
    }

    def save(dto: T, tx: EntityTransaction): Option[T]

    def findOne(id: Long): Option[T] = {
        val tx = em.getTransaction
        tx.begin()
        try {
            val result = findOne(id, tx)
            tx.commit()
            return result
        } catch {
            case e: Exception => {
                log.error("Error trying to find one by id={}", id)
                tx.rollback()
            }
        }
        None
    }

    def findOne(id: Long, tx: EntityTransaction): Option[T]

    def findOneByEchoId(echoId: String): Option[T] = {
        val tx = em.getTransaction
        tx.begin()
        try {
            val result = findOneByEchoId(echoId, tx)
            tx.commit()
            return result
        } catch {
            case e: Exception => {
                log.error("Error trying to find one by echoId={}", echoId)
                tx.rollback()
            }
        }
        None
    }

    def findOneByEchoId(echoId: String, tx: EntityTransaction): Option[T]

    def findAll(): List[T] = {
        val tx = em.getTransaction
        tx.begin()
        try {
            val results = findAll(tx)
            tx.commit()
            return results
        } catch {
            case e: Exception => {
                log.error("Error trying to find all") // typeOf[T].typeSymbol.name.toString
                tx.rollback()
            }
        }
        List.empty
    }

    def findAll(tx: EntityTransaction): List[T]

}
