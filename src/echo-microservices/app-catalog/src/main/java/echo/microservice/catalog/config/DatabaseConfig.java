package echo.microservice.catalog.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Maximilian Irro
 */
@Configuration
@EntityScan("echo.core.domain.entity")
//@PropertySource("classpath:application.properties")
@EnableJpaRepositories("echo.microservice.catalog.repository")
//@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware") // TODO guess I'll need this at some point
@EnableTransactionManagement
public class DatabaseConfig {

    private final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Autowired
    private Environment environment;

    /*
    @Bean
    public DataSource dataSource() {
        return h2DataSource();
    }

    private DataSource h2DataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        //dataSource.setUrl("jdbc:h2:mem:echo;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;INIT=CREATE SCHEMA IF NOT EXISTS echo")
        dataSource.setUrl("jdbc:h2:mem:echo;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false");
        //dataSource.setSchema("echo")
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }
    */

    /*
    @Bean
    public SpringLiquibase liquibase(DataSource dataSource,
                                     DataSourceProperties dataSourceProperties,
                                     LiquibaseProperties liquibaseProperties) {

        // Use liquibase.integration.spring.SpringLiquibase if you don't want Liquibase to start asynchronously
        SpringLiquibase liquibase = new AsyncSpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:config/liquibase/master.xml");
        liquibase.setContexts(liquibaseProperties.getContexts());
        liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());
        if (env.acceptsProfiles(Constants.SPRING_PROFILE_NO_LIQUIBASE)) {
            liquibase.setShouldRun(false);
        } else {
            liquibase.setShouldRun(liquibaseProperties.isEnabled());
            log.debug("Configuring Liquibase");
        }

        return liquibase;
    }

    @Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog("classpath:liquibase-changeLog.xml");
        liquibase.setDataSource(dataSource());
        return liquibase;
    }

    /*
    @Bean
    public Hibernate4Module hibernate4Module() {
        return new Hibernate4Module();
    }
    */

}
