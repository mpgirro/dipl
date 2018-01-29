package echo.core.search;

import echo.core.model.dto.DTO;
import echo.core.model.dto.ResultWrapperDTO;
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
