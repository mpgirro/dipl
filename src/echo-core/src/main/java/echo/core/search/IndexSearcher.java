package echo.core.search;

import echo.core.dto.document.DTO;
import echo.core.dto.document.IndexResult;

/**
 * @author Maximilian Irro
 */
public interface IndexSearcher {

    DTO[] search(String query);

    DTO findByEchoId(String id);

    void refresh();

    void destroy();

}
