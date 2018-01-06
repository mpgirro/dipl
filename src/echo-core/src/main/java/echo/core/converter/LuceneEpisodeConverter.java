package echo.core.converter;

import echo.core.dto.document.EpisodeDocument;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

/**
 * @author Maximilian Irro
 */
public class LuceneEpisodeConverter extends DocumentConverter<EpisodeDocument,org.apache.lucene.document.Document> {

    @Override
    public EpisodeDocument toEchoDocument(Document lDoc) {

        final EpisodeDocument eDoc = new EpisodeDocument();

        eDoc.setTitle(lDoc.get("title"));
        eDoc.setLink(lDoc.get("link"));

 //       eDoc.setPubDate(TODO);
        eDoc.setGuid(lDoc.get("guid"));
        eDoc.setDescription(lDoc.get("description"));

        return eDoc;
    }

    @Override
    public Document toIndexDocument(EpisodeDocument episode) {

        final Document doc = new Document();

        doc.add(new StringField("doc_type", "episode", Field.Store.YES));
        doc.add(new TextField("title", episode.getTitle(), Field.Store.YES));
        doc.add(new TextField("link", episode.getLink(), Field.Store.YES));

        // TODO
        //doc.add(new StringField("pubDate", episode.getPubDate().toString(), Field.Store.YES));

        doc.add(new StringField("guid", episode.getGuid(), Field.Store.YES));
        doc.add(new TextField("description", episode.getDescription(), Field.Store.YES));

        return doc;
    }

}
