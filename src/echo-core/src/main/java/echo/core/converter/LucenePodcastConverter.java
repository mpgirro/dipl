package echo.core.converter;

import echo.core.dto.document.PodcastDocument;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.time.LocalDateTime;

/**
 * @author Maximilian Irro
 */
public class LucenePodcastConverter extends DocumentConverter<PodcastDocument,org.apache.lucene.document.Document> {

    @Override
    public PodcastDocument toEchoDocument(Document lDoc) {

        final PodcastDocument pDoc = new PodcastDocument();

        pDoc.setDocId(lDoc.get("doc_id"));
        pDoc.setTitle(lDoc.get("title"));
        pDoc.setLink(lDoc.get("link"));
        pDoc.setDescription(lDoc.get("description"));
        if(lDoc.get("last_build_date") != null){
            pDoc.setLastBuildDate(LocalDateTime.parse(lDoc.get("last_build_date")));
        }
        pDoc.setLanguage(lDoc.get("language"));
        pDoc.setGenerator(lDoc.get("generator"));

        return pDoc;
    }

    @Override
    public Document toEntityDocument(PodcastDocument podcast) {

        final Document doc = new Document();

        doc.add(new StringField("doc_id", podcast.getDocId(), Field.Store.YES));
        doc.add(new StringField("doc_type", "podcast", Field.Store.YES));
        doc.add(new TextField("title", podcast.getTitle(), Field.Store.YES));
        doc.add(new TextField("link", podcast.getLink(), Field.Store.YES));
        doc.add(new TextField("description", podcast.getDescription(), Field.Store.YES));
        if(podcast.getLastBuildDate() != null){
            doc.add(new StringField("last_build_date", podcast.getLastBuildDate().toString(), Field.Store.YES));
        }
        doc.add(new StringField("language", podcast.getLanguage(), Field.Store.YES));
        if(podcast.getGenerator() != null){
            // TODO know to be found as null in:
            // - http://www.fanboys.fm/episodes.mp3.rss
            doc.add(new TextField("generator", podcast.getGenerator(), Field.Store.YES));
        }

        return doc;
    }
}
