package echo.microservice.parser.config;

import echo.microservice.parser.async.ParserQueueListener;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Maximilian Irro
 */
@Configuration
@EnableRabbit
public class RabbitConfig {

    //public static final String CRAWLER_QUEUE = "echo.rabbit.crawler-queue";
    public static final String PARSER_QUEUE = "echo.rabbit.parser-queue";
    //public static final String INDEX_QUEUE = "echo.rabbit.index-queue";

    private String rabbitmqHost;

    private Integer rabbitmqPort;

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        return connectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    /*
    @Bean
    public Queue crawlerQueue() {
        return new Queue(CRAWLER_QUEUE);
    }
    */

    @Bean
    public Queue parserQueue() {
        return new Queue(PARSER_QUEUE);
    }

    /*
    @Bean
    public Queue indexQueue() {
        return new Queue(INDEX_QUEUE);
    }
    */

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        return rabbitTemplate;
    }

    /*
    @Bean
    public DirectExchange crawlerExchange() {
        DirectExchange exchange = new DirectExchange("echo.direct");
        return exchange;
    }
    */

    @Bean
    public DirectExchange parserExchange() {
        DirectExchange exchange = new DirectExchange("echo.direct");
        return exchange;
    }

    /*
    @Bean
    public DirectExchange indexExchange() {
        DirectExchange exchange = new DirectExchange("echo.direct");
        return exchange;
    }
    */

    /*
    @Bean
    public Binding crawlerBinding() {
        return BindingBuilder
            .bind(crawlerQueue())
            .to(crawlerExchange())
            .with("echo.crawler.#");
    }
    */

    @Bean
    public Binding parserBinding() {
        return BindingBuilder
            .bind(parserQueue())
            .to(parserExchange())
            .with("echo.parser.#");
    }

    /*
    @Bean
    public Binding indexBinding() {
        return BindingBuilder
            .bind(parserQueue())
            .to(parserExchange())
            .with("echo.index.#");
    }
    */

    @Bean
    SimpleMessageListenerContainer parserListenerContainer(ConnectionFactory connectionFactory, @Qualifier("parserListenerAdapter") MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueues(parserQueue());
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter parserListenerAdapter(ParserQueueListener receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    @Bean
    ParserQueueListener parserListener() {
        return new ParserQueueListener();
    }

}
