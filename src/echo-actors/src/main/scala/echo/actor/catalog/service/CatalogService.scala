package echo.actor.catalog.service

import javax.persistence.EntityManager

/**
  * @author Maximilian Irro
  */
trait CatalogService {

    def refresh(em: EntityManager): Unit

}
