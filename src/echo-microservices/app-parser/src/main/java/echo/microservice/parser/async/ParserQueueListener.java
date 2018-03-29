package echo.microservice.parser.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maximilian Irro
 */
public class ParserQueueListener {

    private final Logger log = LoggerFactory.getLogger(ParserQueueListener.class);

    public void receiveMessage(String message) {
        log.info("Recieved Message: " + message);
    }
}
