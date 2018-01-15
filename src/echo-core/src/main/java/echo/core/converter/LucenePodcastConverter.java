package echo.core.converter;

import echo.core.dto.document.PodcastDTO;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.time.LocalDateTime;

/**
 * @author Maximilian Irro
 */
public class LucenePodcastConverter extends DocumentConverter<PodcastDTO,org.apache.lucene.document.Document> {

    @Override
    public PodcastDTO toDTO(Document lDoc) {

        final PodcastDTO dto = new PodcastDTO();

        if(lDoc.get("echo_id")         != null){ dto.setDocId(lDoc.get("echo_id")); }
        if(lDoc.get("doc_id")          != null){ dto.setDocId(lDoc.get("doc_id")); }
        if(lDoc.get("title")           != null){ dto.setTitle(lDoc.get("title")); }
        if(lDoc.get("link")            != null){ dto.setLink(lDoc.get("link")); }
        if(lDoc.get("description")     != null){ dto.setDescription(lDoc.get("description")); }
        if(lDoc.get("pub_date")        != null){ dto.setLastBuildDate(LocalDateTime.parse(lDoc.get("pub_date"))); }
        if(lDoc.get("last_build_date") != null){ dto.setLastBuildDate(LocalDateTime.parse(lDoc.get("last_build_date"))); }
        if(lDoc.get("language")        != null){ dto.setLanguage(lDoc.get("language")); }
        if(lDoc.get("generator")       != null){ dto.setGenerator(lDoc.get("generator")); }
        if(lDoc.get("itunes_image")    != null){ dto.setItunesImage(lDoc.get("itunes_image")); }
        if(lDoc.get("itunes_category") != null){ dto.setItunesCategory(lDoc.get("itunes_category")); }

        // note: we do not retrieve websiteData

        return dto;
    }

    @Override
    public Document toIndex(PodcastDTO podcast) {

        final Document doc = new Document();
        doc.add(new StringField("doc_type", "podcast", Field.Store.YES));

        if(podcast.getEchoId()         != null){ doc.add(new StringField("echo_id", podcast.getEchoId(), Field.Store.YES)); }
        if(podcast.getDocId()          != null){ doc.add(new StringField("doc_id", podcast.getDocId(), Field.Store.YES)); }
        if(podcast.getTitle()          != null){ doc.add(new TextField("title", podcast.getTitle(), Field.Store.YES)); }
        if(podcast.getLink()           != null){ doc.add(new TextField("link", podcast.getLink(), Field.Store.YES)); }
        if(podcast.getDescription()    != null){ doc.add(new TextField("description", podcast.getDescription(), Field.Store.YES)); }
        if(podcast.getPubDate()        != null){ doc.add(new StringField("pub_date", podcast.getPubDate().toString(), Field.Store.YES)); }
        if(podcast.getLastBuildDate()  != null){ doc.add(new StringField("last_build_date", podcast.getLastBuildDate().toString(), Field.Store.YES)); }
        if(podcast.getLanguage()       != null){ doc.add(new StringField("language", podcast.getLanguage(), Field.Store.YES)); }
        if(podcast.getGenerator()      != null){ doc.add(new TextField("generator", podcast.getGenerator(), Field.Store.YES)); }
        if(podcast.getItunesImage()    != null){ doc.add(new TextField("itunes_image", podcast.getItunesImage(), Field.Store.YES)); }
        if(podcast.getItunesCategory() != null){ doc.add(new TextField("itunes_category", podcast.getItunesCategory(), Field.Store.YES)); }

        if(podcast.getWebsiteData()    != null){ doc.add(new TextField("website_data", podcast.getWebsiteData(), Field.Store.NO)); }

        return doc;
    }
}
