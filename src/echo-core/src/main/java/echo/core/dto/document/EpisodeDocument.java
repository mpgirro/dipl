package echo.core.dto.document;

import java.time.LocalDateTime;

/**
 * @author Maximilian Irro
 */
public class EpisodeDocument implements Document {

    private String title;
    private String link;
    private LocalDateTime pubDate;
    private String guid;
    private boolean guidIsPermaLink;
    private String description;

    // TODO atom elements

    // TODO itunes elements

    // TODO content:encoded

    // TODO psc:chapters

    // TODO fyyd

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public LocalDateTime getPubDate() {
        return pubDate;
    }

    public void setPubDate(LocalDateTime pubDate) {
        this.pubDate = pubDate;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public boolean isGuidIsPermaLink() {
        return guidIsPermaLink;
    }

    public void setGuidIsPermaLink(boolean guidIsPermaLink) {
        this.guidIsPermaLink = guidIsPermaLink;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /* TODO do I need these?
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EpisodeDocument d = (EpisodeDocument) o;

        if ( ! EpisodeDocument.equals(id, EpisodeDocument.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    */

    @Override
    public String toString() {
        return "EpisodeDocument{" +
            "title='" + title + '\'' +
            ", link='" + link + '\'' +
            ", pubDate=" + pubDate +
            ", guid='" + guid + '\'' +
            ", guidIsPermaLink=" + guidIsPermaLink +
            ", description='" + description + '\'' +
            '}';
    }
}
