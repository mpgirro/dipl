package echo.core.converter;

import echo.core.dto.document.DTO;
import echo.core.dto.document.EpisodeDTO;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.time.LocalDateTime;

/**
 * @author Maximilian Irro
 */
public class LuceneEpisodeConverter extends DocumentConverter<EpisodeDTO,org.apache.lucene.document.Document> {

    @Override
    public EpisodeDTO toDTO(Document lDoc) {

        final EpisodeDTO dto = new EpisodeDTO();

        if(lDoc.get("echo_id")         != null){ dto.setDocId(lDoc.get("echo_id")); }
        if(lDoc.get("doc_id")          != null){ dto.setDocId(lDoc.get("doc_id")); }
        if(lDoc.get("title")           != null){ dto.setTitle(lDoc.get("title")); }
        if(lDoc.get("link")            != null){ dto.setLink(lDoc.get("link")); }
        if(lDoc.get("pub_date")        != null){ dto.setPubDate(LocalDateTime.parse(lDoc.get("pub_date"))); }
        if(lDoc.get("guid")            != null){ dto.setGuid(lDoc.get("guid")); }
        if(lDoc.get("description")     != null){ dto.setDescription(lDoc.get("description")); }
        if(lDoc.get("itunes_image")    != null){ dto.setItunesImage(lDoc.get("itunes_image")); }
        if(lDoc.get("itunes_duration") != null){ dto.setItunesDuration(lDoc.get("itunes_duration")); }

        // note: we do not retrieve websiteData

        return dto;
    }

    @Override
    public Document toIndex(EpisodeDTO episode) {

        final Document doc = new Document();
        doc.add(new StringField("doc_type", "episode", Field.Store.YES));

        if(episode.getEchoId()         != null){ doc.add(new StringField("echo_id", episode.getEchoId(), Field.Store.YES)); }
        if(episode.getDocId()          != null){ doc.add(new StringField("doc_id", episode.getDocId(), Field.Store.YES)); }
        if(episode.getTitle()          != null){ doc.add(new TextField("title", episode.getTitle(), Field.Store.YES)); }
        if(episode.getLink()           != null){ doc.add(new TextField("link", episode.getLink(), Field.Store.YES)); }
        if(episode.getPubDate()        != null){ doc.add(new StringField("pub_date", episode.getPubDate().toString(), Field.Store.YES)); }
        if(episode.getGuid()           != null){ doc.add(new StringField("guid", episode.getGuid(), Field.Store.YES)); }
        if(episode.getDescription()    != null){ doc.add(new TextField("description", episode.getDescription(), Field.Store.YES)); }
        if(episode.getItunesImage()    != null){ doc.add(new TextField("itunes_image", episode.getItunesImage(), Field.Store.YES)); }
        if(episode.getItunesDuration() != null){ doc.add(new TextField("itunes_duration", episode.getItunesDuration(), Field.Store.YES)); }

        if(episode.getWebsiteData()    != null){ doc.add(new TextField("website_data", episode.getWebsiteData(), Field.Store.NO)); }

        return doc;
    }

}
