package echo.common.converter;

import echo.common.dto.document.EpisodeDocument;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

/**
 * @author Maximilian Irro
 */
public class LuceneEpisodeConverter extends DocumentConverter<EpisodeDocument,org.apache.lucene.document.Document> {

    @Override
    public EpisodeDocument toEchoDocument(Document doc) {
        throw new UnsupportedOperationException("LuceneEpisodeConverter.toEchoDocument() not yet implemented");
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
