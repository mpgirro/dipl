package echo.core.mapper;

import echo.core.domain.entity.Episode;
import echo.core.domain.entity.Podcast;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.PodcastDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * @author Maximilian Irro
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses={UrlMapper.class, DateMapper.class})
public interface TeaserMapper {

    TeaserMapper INSTANCE = Mappers.getMapper( TeaserMapper.class );

    default PodcastDTO asTeaser(PodcastDTO dto) {

        if(dto == null) return null;

        final PodcastDTO teaser = new PodcastDTO();

        teaser.setEchoId(dto.getEchoId());
        teaser.setTitle(dto.getTitle());
        teaser.setItunesImage(dto.getItunesImage());

        return teaser;
    }

    default PodcastDTO asTeaser(Podcast podcast) {

        if(podcast == null) return null;

        final PodcastDTO teaser = new PodcastDTO();

        teaser.setEchoId(podcast.getEchoId());
        teaser.setTitle(podcast.getTitle());
        teaser.setItunesImage(podcast.getItunesImage());
        teaser.setLanguage(podcast.getLanguage());

        return teaser;
    }

    default EpisodeDTO asTeaser(EpisodeDTO dto) {

        if(dto == null) return null;

        final EpisodeDTO teaser = new EpisodeDTO();

        teaser.setEchoId(dto.getEchoId());
        teaser.setTitle(dto.getTitle());
        teaser.setPubDate(dto.getPubDate());
        teaser.setDescription(dto.getDescription());
        teaser.setItunesImage(dto.getItunesImage());
        teaser.setItunesDuration(dto.getItunesDuration());

        return teaser;
    }

    default EpisodeDTO asTeaser(Episode episode) {

        if(episode == null) return null;

        final EpisodeDTO teaser = new EpisodeDTO();

        teaser.setEchoId(episode.getEchoId());
        teaser.setTitle(episode.getTitle());
        teaser.setPubDate(DateMapper.INSTANCE.asLocalDateTime(episode.getPubDate()));
        teaser.setDescription(episode.getDescription());
        teaser.setItunesImage(episode.getItunesImage());
        teaser.setItunesDuration(episode.getItunesDuration());

        return teaser;
    }

}
