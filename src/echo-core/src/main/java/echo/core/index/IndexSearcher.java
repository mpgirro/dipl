package echo.core.index;

import echo.core.model.dto.DTO;
import echo.core.model.dto.IndexDocDTO;
import echo.core.model.dto.ResultWrapperDTO;
import echo.core.exception.SearchException;

/**
 * @author Maximilian Irro
 */
public interface IndexSearcher {

    ResultWrapperDTO search(String query, int page, int size) throws SearchException;

    IndexDocDTO findByEchoId(String id);

    void refresh();

    void destroy();

}
