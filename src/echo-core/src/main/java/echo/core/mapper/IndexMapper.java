package echo.core.mapper;

import echo.core.domain.dto.EntityDTO;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.IndexDocDTO;
import echo.core.domain.dto.PodcastDTO;
import echo.core.domain.dto.ChapterDTO;
import echo.core.index.IndexField;
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
    @Mapping(target = "podcastTitle", ignore = true)
    @Mapping(target = "contentEncoded", ignore = true)
    @Mapping(target = "chapterMarks", ignore = true)
    @Mapping(target = "websiteData", ignore = true)
    IndexDocDTO map(PodcastDTO podcast);

    @Mapping(target = "docType", constant = "episode")
    @Mapping(source = "chapters", target = "chapterMarks")
    @Mapping(target = "itunesSummary", ignore = true)
    @Mapping(target = "websiteData", ignore = true)
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

        switch (doc.get(IndexField.DOC_TYPE)) {
            case "podcast": return map(PodcastMapper.INSTANCE.map(doc));
            case "episode": return map(EpisodeMapper.INSTANCE.map(doc));
            default: throw new RuntimeException("Unsupported lucene document type : " + doc.get(IndexField.DOC_TYPE));
        }
    }

    default Document map(IndexDocDTO doc) {

        if (doc == null) return null;

        final Document lucene = new Document();

        if (doc.getDocType()        != null) { lucene.add(new StringField(IndexField.DOC_TYPE, doc.getDocType(), Field.Store.YES)); }
        if (doc.getEchoId()         != null) { lucene.add(new StringField(IndexField.ECHO_ID, doc.getEchoId(), Field.Store.YES)); }
        if (doc.getTitle()          != null) { lucene.add(new TextField(IndexField.TITLE, doc.getTitle(), Field.Store.YES)); }
        if (doc.getLink()           != null) { lucene.add(new TextField(IndexField.LINK, doc.getLink(), Field.Store.YES)); }
        if (doc.getDescription()    != null) { lucene.add(new TextField(IndexField.DESCRIPTION, doc.getDescription(), Field.Store.YES)); }
        if (doc.getPodcastTitle()   != null) { lucene.add(new TextField(IndexField.PODCAST_TITLE, doc.getPodcastTitle(), Field.Store.YES)); }
        if (doc.getPubDate()        != null) { lucene.add(new StringField(IndexField.PUB_DATE, DateMapper.INSTANCE.asString(doc.getPubDate()), Field.Store.YES)); }
        if (doc.getImage()          != null) { lucene.add(new TextField(IndexField.ITUNES_IMAGE, doc.getImage(), Field.Store.YES)); }
        if (doc.getItunesAuthor()   != null) { lucene.add(new TextField(IndexField.ITUNES_AUTHOR, doc.getItunesAuthor(), Field.Store.NO)); }
        if (doc.getItunesSummary()  != null) { lucene.add(new TextField(IndexField.ITUNES_SUMMARY, doc.getItunesSummary(), Field.Store.YES)); }
        if (doc.getChapterMarks()   != null) { lucene.add(new TextField(IndexField.CHAPTER_MARKS, doc.getChapterMarks(), Field.Store.NO)); }
        if (doc.getContentEncoded() != null) { lucene.add(new TextField(IndexField.CONTENT_ENCODED, doc.getContentEncoded(), Field.Store.NO)); }
        if (doc.getWebsiteData()    != null) { lucene.add(new TextField(IndexField.WEBSITE_DATA, doc.getWebsiteData(), Field.Store.NO)); }

        return lucene;
    }

}
