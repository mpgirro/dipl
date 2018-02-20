package echo.core.index;

import echo.core.mapper.IndexDocMapper;
import echo.core.domain.dto.DTO;
import echo.core.domain.dto.IndexDocDTO;

/**
 * This interface is used to standardize writing to search indizes.
 * Currently only Apache Lucene is supported, but this could be
 * extended to support Apache Solr or ElasticSearch as well.
 *
 * @author Maximilian Irro
 */
public interface IndexCommitter {

    void add(IndexDocDTO doc);

    default void add(DTO dto) {
        final IndexDocDTO doc = IndexDocMapper.INSTANCE.dtoToIndexDoc(dto);
        add(doc);
    }

    void update(IndexDocDTO doc);

    default void update(DTO dto) {
        final IndexDocDTO doc = IndexDocMapper.INSTANCE.dtoToIndexDoc(dto);
        update(doc);
    }

    void commit();

    void destroy();

}
