package echo.core.search;

import echo.core.dto.document.DTO;
import echo.core.dto.document.IndexResult;
import echo.core.exception.SearchException;

/**
 * @author Maximilian Irro
 */
public interface IndexSearcher {

    DTO[] search(String query, int page, int size) throws SearchException;

    DTO findByEchoId(String id);

    void refresh();

    void destroy();

}
