package echo.core.mapper;

import com.icosillion.podengine.exceptions.DateFormatException;
import com.icosillion.podengine.exceptions.MalformedFeedException;
import echo.core.exception.ConversionException;
import echo.core.model.dto.PodcastDTO;
import echo.core.model.domain.Podcast;
import org.apache.lucene.document.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Maximilian Irro
 */
@Mapper(uses={UrlMapper.class, DateMapper.class})
public interface PodcastMapper {

    PodcastMapper INSTANCE = Mappers.getMapper( PodcastMapper.class );

    @Mappings( {
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "echoId", target = "echoId"),
        @Mapping(source = "title", target = "title"),
        @Mapping(source = "link", target = "link"),
        @Mapping(source = "description", target = "description"),
        @Mapping(source = "pubDate", target = "pubDate"),
        @Mapping(source = "lastBuildDate", target = "lastBuildDate"),
        @Mapping(source = "language", target = "language"),
        @Mapping(source = "generator", target = "generator"),
        @Mapping(source = "copyright", target = "copyright"),
        @Mapping(source = "docs", target = "docs"),
        @Mapping(source = "managingEditor", target = "managingEditor"),
        @Mapping(source = "itunesImage", target = "itunesImage"),
        @Mapping(source = "itunesCategories", target = "itunesCategories"),
        @Mapping(source = "itunesSummary", target = "itunesSummary"),
        @Mapping(source = "itunesAuthor", target = "itunesAuthor"),
        @Mapping(source = "itunesKeywords", target = "itunesKeywords"),
        @Mapping(source = "itunesExplicit", target = "itunesExplicit"),
        @Mapping(source = "itunesBlock", target = "itunesBlock"),
        @Mapping(source = "itunesType", target = "itunesType"),
        @Mapping(source = "itunesOwnerName", target = "itunesOwnerName"),
        @Mapping(source = "itunesOwnerEmail", target = "itunesOwnerEmail"),
        @Mapping(source = "feedpressLocale", target = "feedpressLocale"),
        @Mapping(source = "fyydVerify", target = "fyydVerify"),
        @Mapping(source = "episodeCount", target = "episodeCount"),
        @Mapping(target = "websiteData", ignore = true)
    } )
    PodcastDTO podcastToPodcastDto(Podcast podcast);

    List<PodcastDTO> podcastsToPodcastDtos(List<Podcast> podcasts);

    @Mappings( {
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "echoId", target = "echoId"),
        @Mapping(source = "title", target = "title"),
        @Mapping(source = "link", target = "link"),
        @Mapping(source = "description", target = "description"),
        @Mapping(source = "pubDate", target = "pubDate"),
        @Mapping(source = "lastBuildDate", target = "lastBuildDate"),
        @Mapping(source = "language", target = "language"),
        @Mapping(source = "generator", target = "generator"),
        @Mapping(source = "copyright", target = "copyright"),
        @Mapping(source = "docs", target = "docs"),
        @Mapping(source = "managingEditor", target = "managingEditor"),
        @Mapping(source = "itunesImage", target = "itunesImage"),
        @Mapping(source = "itunesCategories", target = "itunesCategories"),
        @Mapping(source = "itunesSummary", target = "itunesSummary"),
        @Mapping(source = "itunesAuthor", target = "itunesAuthor"),
        @Mapping(source = "itunesKeywords", target = "itunesKeywords"),
        @Mapping(source = "itunesExplicit", target = "itunesExplicit"),
        @Mapping(source = "itunesBlock", target = "itunesBlock"),
        @Mapping(source = "itunesType", target = "itunesType"),
        @Mapping(source = "itunesOwnerName", target = "itunesOwnerName"),
        @Mapping(source = "itunesOwnerEmail", target = "itunesOwnerEmail"),
        @Mapping(source = "feedpressLocale", target = "feedpressLocale"),
        @Mapping(source = "fyydVerify", target = "fyydVerify"),
        @Mapping(source = "episodeCount", target = "episodeCount"),
        @Mapping(target = "episodes", ignore = true),
        @Mapping(target = "feeds", ignore = true)
    } )
    Podcast podcastDtoToPodcast(PodcastDTO podcastDto);

    List<Podcast> podcastDtosToPodcasts(List<PodcastDTO> podcastDtos);

    default PodcastDTO luceneDocumentToPodcastDto(Document doc){

        if(doc == null){
            return null;
        }

        final PodcastDTO dto = new PodcastDTO();

        if(doc.get("echo_id")      != null){ dto.setEchoId(doc.get("echo_id")); }
        if(doc.get("title")        != null){ dto.setTitle(doc.get("title")); }
        if(doc.get("link")         != null){ dto.setLink(doc.get("link")); }
        if(doc.get("description")  != null){ dto.setDescription(doc.get("description")); }
        if(doc.get("pub_date")     != null){ dto.setLastBuildDate(DateMapper.INSTANCE.asLocalDateTime(doc.get("pub_date"))); }
        if(doc.get("itunes_image") != null){ dto.setItunesImage(doc.get("itunes_image")); }

        return dto;
    }

    List<PodcastDTO> luceneDocumentsToPodcastDtos(List<Document> docs);

    default PodcastDTO podenginePodcastToPodcastDto(com.icosillion.podengine.models.Podcast podcast) throws ConversionException {

        if (podcast == null) {
            return null;
        }

        final PodcastDTO dto = new PodcastDTO();
        try {
            if(podcast.getTitle()         != null){ dto.setTitle(podcast.getTitle()); }
            if(podcast.getLink()          != null){ dto.setLink(podcast.getLink().toExternalForm()); }
            if(podcast.getDescription()   != null){ dto.setDescription(podcast.getDescription()); }
            if(podcast.getPubDate()       != null){ dto.setPubDate(LocalDateTime.ofInstant(podcast.getPubDate().toInstant(), ZoneId.systemDefault())); }
            if(podcast.getLastBuildDate() != null){ dto.setLastBuildDate(LocalDateTime.ofInstant(podcast.getLastBuildDate().toInstant(), ZoneId.systemDefault())); }
            if(podcast.getLanguage()      != null){ dto.setLanguage(podcast.getLanguage()); }
            if(podcast.getGenerator()     != null){ dto.setGenerator(podcast.getGenerator()); }
            if(podcast.getCategories()    != null){ dto.setItunesCategories(new HashSet<>(Arrays.asList(podcast.getCategories()))); }
            if(podcast.getITunesInfo()    != null){ dto.setItunesImage(podcast.getITunesInfo().getImageString()); }
        } catch (MalformedFeedException | MalformedURLException | DateFormatException e) {
            throw new ConversionException("Exception during converting podengine.Podcast to PodcastDTO [reason: {}]", e);
        }

        return dto;
    }

    List<PodcastDTO> podenginePodcastsToPodcastDtos(List<com.icosillion.podengine.models.Podcast> podcasts) throws ConversionException;

}
