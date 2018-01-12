package echo.core.converter;

import echo.core.dto.document.EpisodeDocument;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.time.LocalDateTime;

/**
 * @author Maximilian Irro
 */
public class LuceneEpisodeConverter extends DocumentConverter<EpisodeDocument,org.apache.lucene.document.Document> {

    @Override
    public EpisodeDocument toEchoDocument(Document lDoc) {

        final EpisodeDocument eDoc = new EpisodeDocument();

        if(lDoc.get("echo_id")         != null){ eDoc.setDocId(lDoc.get("echo_id")); }
        if(lDoc.get("doc_id")          != null){ eDoc.setDocId(lDoc.get("doc_id")); }
        if(lDoc.get("title")           != null){ eDoc.setTitle(lDoc.get("title")); }
        if(lDoc.get("link")            != null){ eDoc.setLink(lDoc.get("link")); }
        if(lDoc.get("pub_date")        != null){ eDoc.setPubDate(LocalDateTime.parse(lDoc.get("pub_date"))); }
        if(lDoc.get("guid")            != null){ eDoc.setGuid(lDoc.get("guid")); }
        if(lDoc.get("description")     != null){ eDoc.setDescription(lDoc.get("description")); }
        if(lDoc.get("itunes_image")    != null){ eDoc.setItunesImage(lDoc.get("itunes_image")); }
        if(lDoc.get("itunes_duration") != null){ eDoc.setItunesDuration(lDoc.get("itunes_duration")); }

        // note: we do not retrieve websiteData

        return eDoc;
    }

    @Override
    public Document toEntityDocument(EpisodeDocument episode) {

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
