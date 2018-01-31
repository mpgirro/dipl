package echo.actor.directory.repository

import java.util.Properties
import javax.persistence.{EntityManager, EntityManagerFactory}
import javax.sql.DataSource

import org.springframework.aop.framework.ProxyFactory
import org.springframework.data.repository.core.RepositoryInformation
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.vendor.HibernateJpaDialect
import org.springframework.transaction.interceptor.{MatchAlwaysTransactionAttributeSource, TransactionInterceptor}
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.orm.jpa.{JpaTransactionManager, LocalContainerEntityManagerFactoryBean}

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
class RepositoryFactoryBuilder {

    private val dataSource = h2DataSource // TODO make this changeable

    private val entityManagerFactory = createEntityManagerFactory(dataSource)
    private val entityManager = entityManagerFactory.createEntityManager

    private def h2DataSource: DataSource = {
        val dataSource: DriverManagerDataSource = new DriverManagerDataSource
        dataSource.setDriverClassName("org.h2.Driver")
        //dataSource.setUrl("jdbc:h2:mem:echo;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;INIT=CREATE SCHEMA IF NOT EXISTS echo")
        dataSource.setUrl("jdbc:h2:mem:echo;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false")
        //dataSource.setSchema("echo")
        dataSource.setUsername("sa")
        dataSource.setPassword("")
        dataSource
    }

    private def postgres2DataSource: DataSource = {
        // TODO
        throw new NotImplementedError("RepositoryFactoryBuilder.postgres2DataSource() not yet implemented")
    }

    private def jpaVendorAdapter = new HibernateJpaVendorAdapter

    private def jpaPropertiesMap: java.util.Map[String, _] = {
        val properties = new Properties
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
        properties.setProperty("hibernate.connection.charSet", "UTF-8")
        properties.setProperty("hibernate.show.sql", "true")
        //properties.setProperty("hibernate.hbm2ddl.auto", "create")
        //properties.setProperty("hibernate.default_schema", "echo_schema")
        properties.asScala.toMap.asJava // this seems silly but the types match up in the end...
    }

    private def createEntityManagerFactory(dataSource: DataSource): EntityManagerFactory = {
        val entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean
        entityManagerFactoryBean.setPersistenceProviderClass(classOf[org.hibernate.jpa.HibernatePersistenceProvider])
        entityManagerFactoryBean.setJpaDialect(new HibernateJpaDialect)
        entityManagerFactoryBean.setPersistenceUnitName("EchoActorEngine_ManuallyCreatedPersistenceUnit")
        entityManagerFactoryBean.setDataSource(dataSource)
        entityManagerFactoryBean.setPackagesToScan("echo.core.model.domain")
        entityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter)
        entityManagerFactoryBean.setJpaPropertyMap(jpaPropertiesMap)

        // these two need to be called, otherwise the factory object will be null
        entityManagerFactoryBean.afterPropertiesSet
        return entityManagerFactoryBean.getObject
    }

    def createFactory: JpaRepositoryFactory = {
        // Create the transaction manager and RespositoryFactory
        val txManager = new JpaTransactionManager()
        txManager.setEntityManagerFactory(entityManagerFactory)
        txManager.setDataSource(dataSource)
        txManager.setJpaPropertyMap(jpaPropertiesMap)
        val repositoryFactory = new JpaRepositoryFactory(entityManager)

        // Make sure calls to the repository instance are intercepted for annotated transactions
        repositoryFactory.addRepositoryProxyPostProcessor(new RepositoryProxyPostProcessor(){
            override def postProcess(factory: ProxyFactory, repositoryInformation: RepositoryInformation) = {
                factory.addAdvice(new TransactionInterceptor(txManager, new MatchAlwaysTransactionAttributeSource))
            }
        })

        // return val
        repositoryFactory
    }

    def getDataSource = this.dataSource

    def getEntityManagerFactory = this.entityManagerFactory

    def getEntityManager = this.entityManager

}
