package echo.core.mapper;

import echo.core.domain.dto.immutable.ImmutableTestEpisode;
import echo.core.domain.dto.immutable.ImmutableTestPodcast;
import echo.core.domain.dto.immutable.TestEpisode;
import echo.core.domain.dto.immutable.TestPodcast;
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

    default TestPodcast asTeaser(TestPodcast dto) {

        if(dto == null) return null;

        final ImmutableTestPodcast.Builder teaser = ImmutableTestPodcast.builder();

        teaser.setEchoId(dto.getEchoId());
        teaser.setTitle(dto.getTitle());
        teaser.setImage(dto.getImage());
        teaser.setLanguage(dto.getLanguage());
        teaser.setGenerator(dto.getGenerator());
        teaser.setCopyright(dto.getCopyright());
        teaser.setEpisodeCount(dto.getEpisodeCount());
        teaser.setItunesAuthor(dto.getItunesAuthor());
        teaser.setItunesExplicit(dto.getItunesExplicit());
        teaser.setItunesBlock(dto.getItunesBlock());
        teaser.setRegistrationComplete(dto.getRegistrationComplete());
        teaser.setRegistrationTimestamp(dto.getRegistrationTimestamp());

        return teaser.create();
    }

    default TestPodcast asTeaser(Podcast podcast) {

        if(podcast == null) return null;

        final ImmutableTestPodcast.Builder teaser = ImmutableTestPodcast.builder();

        teaser.setEchoId(podcast.getEchoId());
        teaser.setTitle(podcast.getTitle());
        teaser.setImage(podcast.getImage());
        teaser.setLanguage(podcast.getLanguage());

        return teaser.create();
    }

    default TestEpisode asTeaser(TestEpisode dto) {

        if(dto == null) return null;

        final ImmutableTestEpisode.Builder teaser = ImmutableTestEpisode.builder();

        teaser.setEchoId(dto.getEchoId());
        teaser.setTitle(dto.getTitle());
        teaser.setPubDate(dto.getPubDate());
        teaser.setDescription(dto.getDescription());
        teaser.setImage(dto.getImage());
        teaser.setItunesDuration(dto.getItunesDuration());

        return teaser.create();
    }

    default TestEpisode asTeaser(Episode episode) {

        if(episode == null) return null;

        final ImmutableTestEpisode.Builder teaser = ImmutableTestEpisode.builder();

        teaser.setEchoId(episode.getEchoId());
        teaser.setTitle(episode.getTitle());
        teaser.setPubDate(DateMapper.INSTANCE.asLocalDateTime(episode.getPubDate()));
        teaser.setDescription(episode.getDescription());
        teaser.setImage(episode.getImage());
        teaser.setItunesDuration(episode.getItunesDuration());

        return teaser.create();
    }

}
