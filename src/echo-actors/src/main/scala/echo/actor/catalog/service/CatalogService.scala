package echo.actor.catalog.service

import javax.persistence.EntityManager

/**
  * @author Maximilian Irro
  */
trait CatalogService {

    /*
    protected val rfb: RepositoryFactoryBuilder
    protected var repositoryFactory: JpaRepositoryFactory = _
    protected var repository: T = _
    */

    def refresh(em: EntityManager): Unit

}
