package echo.actor.directory.service

import echo.core.model.dto.DTO

/**
  * @author Maximilian Irro
  */
trait DirectoryService[T] {

    def save(dto: T): T

    def findOne(id: Long): Option[T]

    def findOneByEchoId(echoId: String): Option[T]

    def findAll: List[T]

}
