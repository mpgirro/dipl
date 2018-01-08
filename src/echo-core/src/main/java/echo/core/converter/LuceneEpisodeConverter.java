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

        eDoc.setDocId(lDoc.get("doc_id"));
        eDoc.setTitle(lDoc.get("title"));
        eDoc.setLink(lDoc.get("link"));
        if(lDoc.get("pub_date") != null){
            eDoc.setPubDate(LocalDateTime.parse(lDoc.get("pub_date")));
        }
        eDoc.setGuid(lDoc.get("guid"));
        eDoc.setDescription(lDoc.get("description"));

        return eDoc;
    }

    @Override
    public Document toEntityDocument(EpisodeDocument episode) {

        final Document doc = new Document();

        doc.add(new StringField("doc_id", episode.getDocId(), Field.Store.YES));
        doc.add(new StringField("doc_type", "episode", Field.Store.YES));
        doc.add(new TextField("title", episode.getTitle(), Field.Store.YES));
        doc.add(new TextField("link", episode.getLink(), Field.Store.YES));
        if(episode.getPubDate() != null){
            doc.add(new StringField("pub_date", episode.getPubDate().toString(), Field.Store.YES));
        }
        doc.add(new StringField("guid", episode.getGuid(), Field.Store.YES));
        doc.add(new TextField("description", episode.getDescription(), Field.Store.YES));

        return doc;
    }

}
