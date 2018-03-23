package echo.core.index;

import echo.core.domain.dto.immutable.TestIndexDoc;
import echo.core.domain.dto.immutable.TestResultWrapper;
import echo.core.exception.SearchException;

/**
 * @author Maximilian Irro
 */
public interface IndexSearcher {

    TestResultWrapper search(String query, int page, int size) throws SearchException;

    TestIndexDoc findByEchoId(String id);

    void refresh();

    void destroy();

}
