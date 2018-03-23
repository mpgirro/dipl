package echo.core.index;

import echo.core.domain.dto.immutable.TestEpisode;
import echo.core.domain.dto.immutable.TestIndexDoc;
import echo.core.domain.dto.immutable.TestPodcast;
import echo.core.mapper.TestIndexMapper;

/**
 * This interface is used to standardize writing to search indizes.
 * Currently only Apache Lucene is supported, but this could be
 * extended to support Apache Solr or ElasticSearch as well.
 *
 * @author Maximilian Irro
 */
public interface IndexCommitter {

    void add(TestIndexDoc doc);

    default void add(TestPodcast podcast) {
        add(TestIndexMapper.INSTANCE.map(podcast));
    }

    default void add(TestEpisode episode) {
        add(TestIndexMapper.INSTANCE.map(episode));
    }

    void update(TestIndexDoc doc);

    default void update(TestPodcast podcast) {
        update(TestIndexMapper.INSTANCE.map(podcast));
    }

    default void update(TestEpisode episode) {
        update(TestIndexMapper.INSTANCE.map(episode));
    }

    void commit();

    void destroy();

}
