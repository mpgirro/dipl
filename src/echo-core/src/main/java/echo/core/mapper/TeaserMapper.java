package echo.core.mapper;

import echo.core.domain.dto.ImmutableEpisodeDTO;
import echo.core.domain.dto.ImmutablePodcastDTO;
import echo.core.domain.dto.PodcastDTO;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.entity.Episode;
import echo.core.domain.entity.Podcast;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * @author Maximilian Irro
 */
@Mapper(uses={UrlMapper.class, DateMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface TeaserMapper {

    TeaserMapper INSTANCE = Mappers.getMapper( TeaserMapper.class );

    default PodcastDTO asTeaser(PodcastDTO dto) {

        if(dto == null) return null;

        return ImmutablePodcastDTO.builder()
            .setEchoId(dto.getEchoId())
            .setTitle(dto.getTitle())
            .setImage(dto.getImage())
            .setLanguage(dto.getLanguage())
            .setGenerator(dto.getGenerator())
            .setCopyright(dto.getCopyright())
            .setEpisodeCount(dto.getEpisodeCount())
            .setItunesAuthor(dto.getItunesAuthor())
            .setItunesExplicit(dto.getItunesExplicit())
            .setItunesBlock(dto.getItunesBlock())
            .setRegistrationComplete(dto.getRegistrationComplete())
            .setRegistrationTimestamp(dto.getRegistrationTimestamp())
            .create();
    }

    default PodcastDTO asTeaser(Podcast podcast) {

        if(podcast == null) return null;

        return ImmutablePodcastDTO.builder()
            .setEchoId(podcast.getEchoId())
            .setTitle(podcast.getTitle())
            .setImage(podcast.getImage())
            .setLanguage(podcast.getLanguage())
            .create();
    }

    default EpisodeDTO asTeaser(EpisodeDTO dto) {

        if(dto == null) return null;

        return ImmutableEpisodeDTO.builder()
            .setEchoId(dto.getEchoId())
            .setTitle(dto.getTitle())
            .setPubDate(dto.getPubDate())
            .setDescription(dto.getDescription())
            .setImage(dto.getImage())
            .setItunesDuration(dto.getItunesDuration())
            .create();
    }

    default EpisodeDTO asTeaser(Episode episode) {

        if(episode == null) return null;

        return ImmutableEpisodeDTO.builder()
            .setEchoId(episode.getEchoId())
            .setTitle(episode.getTitle())
            .setPubDate(DateMapper.INSTANCE.asLocalDateTime(episode.getPubDate()))
            .setDescription(episode.getDescription())
            .setImage(episode.getImage())
            .setItunesDuration(episode.getItunesDuration())
            .create();
    }

}
