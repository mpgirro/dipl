package echo.core.mapper;

import echo.core.model.dto.IndexDocDTO;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Maximilian Irro
 */
@Mapper
public interface LuceneMapper {

    LuceneMapper INSTANCE = Mappers.getMapper( LuceneMapper.class );

    default Document map(IndexDocDTO doc) {

        if (doc == null) return null;

        final Document lucene = new Document();

        if (doc.getDocType()        != null) { lucene.add(new StringField("doc_type", doc.getDocType(), Field.Store.YES)); }
        if (doc.getEchoId()         != null) { lucene.add(new StringField("echo_id", doc.getEchoId(), Field.Store.YES)); }
        if (doc.getTitle()          != null) { lucene.add(new TextField("title", doc.getTitle(), Field.Store.YES)); }
        if (doc.getLink()           != null) { lucene.add(new TextField("link", doc.getLink(), Field.Store.YES)); }
        if (doc.getDescription()    != null) { lucene.add(new TextField("description", doc.getDescription(), Field.Store.YES)); }
        if (doc.getPubDate()        != null) { lucene.add(new StringField("pub_date", DateMapper.INSTANCE.asString(doc.getPubDate()), Field.Store.YES)); }
        if (doc.getItunesImage()    != null) { lucene.add(new TextField("itunes_image", doc.getItunesImage(), Field.Store.YES)); }
        if (doc.getContentEncoded() != null) { lucene.add(new TextField("content_encoded", doc.getContentEncoded(), Field.Store.NO)); }
        if (doc.getWebsiteData()    != null) { lucene.add(new TextField("website_data", doc.getWebsiteData(), Field.Store.NO)); }

        return lucene;
    }

}
