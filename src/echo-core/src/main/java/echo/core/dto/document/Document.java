package echo.core.dto.document;

import java.io.Serializable;

/**
 * This is the placeholder type for Documents to be stored in a search index by an
 * {@link echo.core.index.IndexCommitter}. The transformation to the index-specific
 * is up to the concrete IndexCommitter implementation.
 *
 * @author Maximilian Irro
 */
public interface Document extends Serializable {

    String getDocId();
    void setDocId(String docId);

}
