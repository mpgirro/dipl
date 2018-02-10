package echo.core.model.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * This is the placeholder type for Documents to be stored in a search index by an
 * {@link echo.core.index.IndexCommitter}. The transformation to the index-specific
 * is up to the concrete IndexCommitter implementation.
 *
 * @author Maximilian Irro
 */
public interface DTO extends Serializable {

    String getEchoId();
    void setEchoId(String echoId);

}
