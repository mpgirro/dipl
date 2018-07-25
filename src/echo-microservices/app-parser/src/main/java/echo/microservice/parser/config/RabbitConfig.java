package echo.microservice.parser.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

/**
 * @author Maximilian Irro
 */
@Configuration
@EnableRabbit
@Import(JacksonConfig.class)
public class RabbitConfig {

    @Autowired
    private Environment env;

    @Autowired
    private JacksonConfig jacksonConfig;

    @Bean
    public ConnectionFactory connectionFactory() {
        final String rabbitHost = env.getProperty("spring.rabbitmq.host", "localhost");
        final Integer rabbitPort = env.getProperty("spring.rabbitmq.port", Integer.class, 5672);

        final CachingConnectionFactory connectionFactory = new CachingConnectionFactory(rabbitHost, rabbitPort);
        connectionFactory.setUsername(env.getProperty("spring.rabbitmq.username"));
        connectionFactory.setPassword(env.getProperty("spring.rabbitmq.password"));
        return connectionFactory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setConcurrentConsumers(5);
        factory.setMaxConcurrentConsumers(10);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter(jacksonConfig.objectMapper());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

}
