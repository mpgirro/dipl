package echo.core.search;

import echo.core.dto.document.Document;

/**
 * @author Maximilian Irro
 */
public interface IndexSearcher {

    Document[] search(String query);

    Document findByEchoId(String id);

    void refresh();

    void destroy();

}
