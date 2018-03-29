package echo.microservice.crawler.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maximilian Irro
 */
public class CrawlerQueueListener {

    private final Logger log = LoggerFactory.getLogger(CrawlerQueueListener.class);

    public void receiveMessage(String message) {
        log.info("Recieved Message: " + message);
    }
}
