package echo.core.exception;

import com.icosillion.podengine.exceptions.MalformedFeedException;

/**
 * @author Maximilian Irro
 */
public class FeedParsingException extends EchoException {

    public FeedParsingException(String s, MalformedFeedException e) {
        super(s,e);
    }
}
