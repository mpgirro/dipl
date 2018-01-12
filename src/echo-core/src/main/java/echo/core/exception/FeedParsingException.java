package echo.core.exception;

import com.icosillion.podengine.exceptions.MalformedFeedException;

import java.io.IOException;

/**
 * @author Maximilian Irro
 */
public class FeedParsingException extends EchoException {

    public FeedParsingException(String s, Exception e) {
        super(s,e);
    }
}
