package echo.core.mapper;

import echo.core.domain.dto.EntityDTO;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.IndexDocDTO;
import echo.core.domain.dto.PodcastDTO;
import echo.core.domain.feed.ChapterDTO;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Maximilian Irro
 */
@Mapper(uses={PodcastMapper.class, EpisodeMapper.class})
public interface IndexMapper {

    IndexMapper INSTANCE = Mappers.getMapper( IndexMapper.class );

    @Mapping(target = "docType", constant = "podcast")
    @Mapping(target = "contentEncoded", ignore = true)
    @Mapping(target = "chapterMarks", ignore = true)
    IndexDocDTO map(PodcastDTO podcast);

    @Mapping(target = "docType", constant = "episode")
    @Mapping(source = "chapters", target = "chapterMarks")
    @Mapping(target = "itunesSummary", ignore = true)
    IndexDocDTO map(EpisodeDTO episodeDTO);

    default String map(List<ChapterDTO> chapters){

        if(chapters == null) return null;

        return String.join("\n", chapters.stream()
            .map(ChapterDTO::getTitle)
            .collect(Collectors.toList()));
    }

    default IndexDocDTO map(EntityDTO dto) {

        if (dto == null) return null;

        if (dto instanceof PodcastDTO) {
            return map((PodcastDTO) dto);
        } else if (dto instanceof EpisodeDTO) {
            return map((EpisodeDTO) dto);
        } else {
            throw new RuntimeException("Unsupported echo EntityDTO type : " + dto.getClass());
        }
    }


    default IndexDocDTO map(Document doc) {

        if (doc == null) return null;

        if (doc.get("doc_type").equals("podcast")) {
            return map(PodcastMapper.INSTANCE.map(doc));
        } else if (doc.get("doc_type").equals("episode")) {
            return map(EpisodeMapper.INSTANCE.map(doc));
        } else {
            throw new RuntimeException("Unsupported lucene document type : " + doc.get("doc_type"));
        }
    }

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
        if (doc.getItunesAuthor()   != null) { lucene.add(new TextField("itunes_author", doc.getItunesAuthor(), Field.Store.NO)); }
        if (doc.getItunesSummary()  != null) { lucene.add(new TextField("itunes_summary", doc.getItunesSummary(), Field.Store.NO)); }
        if (doc.getChapterMarks()   != null) { lucene.add(new TextField("chapter_marks", doc.getChapterMarks(), Field.Store.NO)); }
        if (doc.getContentEncoded() != null) { lucene.add(new TextField("content_encoded", doc.getContentEncoded(), Field.Store.NO)); }
        if (doc.getWebsiteData()    != null) { lucene.add(new TextField("website_data", doc.getWebsiteData(), Field.Store.NO)); }

        return lucene;
    }

}
