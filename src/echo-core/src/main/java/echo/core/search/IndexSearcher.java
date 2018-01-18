package echo.core.search;

import echo.core.dto.DTO;
import echo.core.dto.ResultWrapperDTO;
import echo.core.exception.SearchException;

/**
 * @author Maximilian Irro
 */
public interface IndexSearcher {

    ResultWrapperDTO search(String query, int page, int size) throws SearchException;

    DTO findByEchoId(String id);

    void refresh();

    void destroy();

}
