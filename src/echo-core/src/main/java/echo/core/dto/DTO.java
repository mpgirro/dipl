package echo.core.dto;

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

    String getDocId();
    void setDocId(String docId);

    String getTitle();
    void setTitle(String title);

    String getLink();
    void setLink(String link);

    LocalDateTime getPubDate();
    void setPubDate(LocalDateTime pubDate);

    String getDescription();
    void setDescription(String description);

    String getItunesImage();
    void setItunesImage(String itunesImage);

    String getWebsiteData();
    void setWebsiteData(String websiteData);

}
