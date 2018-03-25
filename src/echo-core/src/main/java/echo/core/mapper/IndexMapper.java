package echo.core.mapper;

import echo.core.domain.dto.*;
import echo.core.index.IndexField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Maximilian Irro
 */
@Mapper(uses={PodcastMapper.class, EpisodeMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface IndexMapper {

    IndexMapper INSTANCE = Mappers.getMapper( IndexMapper.class );

    @Mapping(target = "docType", constant = "podcast")
    @Mapping(target = "podcastTitle", ignore = true)
    @Mapping(target = "contentEncoded", ignore = true)
    @Mapping(target = "chapterMarks", ignore = true)
    @Mapping(target = "websiteData", ignore = true)
    ModifiableIndexDocDTO toModifiable(PodcastDTO podcast);

    default ImmutableIndexDocDTO toImmutable(PodcastDTO podcast) {
        return Optional.ofNullable(podcast)
            .map(p -> toModifiable(p).toImmutable())
            .orElse(null);
    }

    @Mapping(target = "docType", constant = "episode")
    @Mapping(source = "chapters", target = "chapterMarks")
    @Mapping(target = "itunesSummary", ignore = true)
    @Mapping(target = "websiteData", ignore = true)
    ModifiableIndexDocDTO toModifiable(EpisodeDTO episodeDTO);

    default ImmutableIndexDocDTO toImmutable(EpisodeDTO episode) {
        return Optional.ofNullable(episode)
            .map(e -> toModifiable(e).toImmutable())
            .orElse(null);
    }

    default String map(List<ChapterDTO> chapters){

        if (chapters == null) return null;

        return String.join("\n", chapters.stream()
            .map(ChapterDTO::getTitle)
            .collect(Collectors.toList()));
    }

    default ImmutableIndexDocDTO toImmutable(IndexDocDTO doc) {

        if (doc == null) return null;

        if (doc instanceof  ImmutableIndexDocDTO) {
            return (ImmutableIndexDocDTO) doc;
        }
        return ((ModifiableIndexDocDTO) doc).toImmutable();
    }

    default ImmutableIndexDocDTO toIndexDoc(Document doc) {

        if (doc == null) return null;

        switch (doc.get(IndexField.DOC_TYPE)) {
            case "podcast": return toImmutable(PodcastMapper.INSTANCE.toImmutable(doc));
            case "episode": return toImmutable(EpisodeMapper.INSTANCE.toImmutable(doc));
            default: throw new RuntimeException("Unsupported lucene document type : " + doc.get(IndexField.DOC_TYPE));
        }
    }

    default Document toLucene(IndexDocDTO doc) {

        if (doc == null) return null;

        final Document lucene = new Document();

        Optional.ofNullable(doc.getDocType())
            .ifPresent(value -> lucene.add(new StringField(IndexField.DOC_TYPE, value, Field.Store.YES)));
        Optional.ofNullable(doc.getEchoId())
            .ifPresent(value -> lucene.add(new StringField(IndexField.ECHO_ID, value, Field.Store.YES)));
        Optional.ofNullable(doc.getTitle())
            .ifPresent(value -> lucene.add(new TextField(IndexField.TITLE, value, Field.Store.YES)));
        Optional.ofNullable(doc.getLink())
            .ifPresent(value -> lucene.add(new TextField(IndexField.LINK, value, Field.Store.YES)));
        Optional.ofNullable(doc.getDescription())
            .ifPresent(value -> lucene.add(new TextField(IndexField.DESCRIPTION, value, Field.Store.YES)));
        Optional.ofNullable(doc.getPodcastTitle())
            .ifPresent(value -> lucene.add(new TextField(IndexField.PODCAST_TITLE, value, Field.Store.YES)));
        Optional.ofNullable(doc.getPubDate())
            .map(DateMapper.INSTANCE::asString)
            .ifPresent(value -> lucene.add(new StringField(IndexField.PUB_DATE, value, Field.Store.YES)));
        Optional.ofNullable(doc.getImage())
            .ifPresent(value -> lucene.add(new TextField(IndexField.ITUNES_IMAGE, value, Field.Store.YES)));
        Optional.ofNullable(doc.getItunesAuthor())
            .ifPresent(value -> lucene.add(new TextField(IndexField.ITUNES_AUTHOR, value, Field.Store.NO)));
        Optional.ofNullable(doc.getItunesSummary())
            .ifPresent(value -> lucene.add(new TextField(IndexField.ITUNES_SUMMARY, value, Field.Store.YES)));
        Optional.ofNullable(doc.getChapterMarks())
            .ifPresent(value -> lucene.add(new TextField(IndexField.CHAPTER_MARKS, value, Field.Store.NO)));
        Optional.ofNullable(doc.getContentEncoded())
            .ifPresent(value -> lucene.add(new TextField(IndexField.CONTENT_ENCODED, value, Field.Store.NO)));
        Optional.ofNullable(doc.getWebsiteData())
            .ifPresent(value -> lucene.add(new TextField(IndexField.WEBSITE_DATA, value, Field.Store.NO)));

        /*
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
        */

        return lucene;
    }
}
