package echo.microservice.crawler.async;

/**
 * @author Maximilian Irro
 */
public class ParserQueueListener {
    public void receiveMessage(String message) {
        System.out.println("ParserQueueListener: Message Received <" + message + ">");
    }
}
