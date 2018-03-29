package echo.microservice.crawler.config;

import echo.microservice.crawler.async.CrawlerQueueListener;
import echo.microservice.crawler.async.ParserQueueListener;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Maximilian Irro
 */
@Configuration
@EnableRabbit
public class RabbitConfig {

    public static final String CRAWLER_QUEUE = "echo.rabbit.crawler-queue";
    public static final String PARSER_QUEUE = "echo.rabbit.parser-queue";

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

    @Bean
    public Queue crawlerQueue() {
        return new Queue(CRAWLER_QUEUE);
    }

    @Bean
    public Queue parserQueue() {
        return new Queue(PARSER_QUEUE);
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }


    @Bean
    public DirectExchange crawlerExchange() {
        DirectExchange exchange = new DirectExchange("echo.direct");
        return exchange;
    }

    @Bean
    public DirectExchange parserExchange() {
        DirectExchange exchange = new DirectExchange("echo.direct");
        return exchange;
    }


    @Bean
    public Binding crawlerBinding() {
        return BindingBuilder.bind(crawlerQueue()).to(crawlerExchange()).with("echo.crawler.#");
        //return new Binding(crawlerQueue(), crawlerExchange());
        //return BindingBuilder.bind(crawlerQueue()).to(crawlerExchange());
    }

    /*
    @Bean
    public Binding parserBinding() {
        return BindingBuilder.bind(parserQueue()).to(parserExchange()).with("echo.parser.#");
        //return BindingBuilder.bind(parserQueue()).to(parserExchange());
    }
    */

    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    SimpleMessageListenerContainer crawlerListenerContainer(ConnectionFactory connectionFactory,
                                                            @Qualifier("crawlerListenerAdapter") MessageListenerAdapter listenerAdapter,
                                                            MessageConverter messageConverter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueues(crawlerQueue(), parserQueue());
        container.setMessageListener(listenerAdapter);
        container.setMessageConverter(messageConverter);
        return container;
    }

    @Bean
    MessageListenerAdapter crawlerListenerAdapter(CrawlerQueueListener receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    /*
    @Bean
    SimpleMessageListenerContainer parserContainer(ConnectionFactory connectionFactory, @Qualifier("parserListenerAdapter") MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueues(crawlerQueue(), parserQueue());
        container.setMessageListener(listenerAdapter);
        return container;
    }
    */

    /*
    @Bean
    MessageListenerAdapter parserListenerAdapter(ParserQueueListener webAppListener) {
        return new MessageListenerAdapter(webAppListener, "receiveMessage");
    }
    */

    @Bean
    CrawlerQueueListener crawlerListener() {
        return new CrawlerQueueListener();
    }

    /*
    @Bean
    ParserQueueListener parserListener() {
        return new ParserQueueListener();
    }
    */

}
