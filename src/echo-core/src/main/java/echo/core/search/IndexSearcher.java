package echo.core.search;

import echo.core.dto.document.Document;

/**
 * @author Maximilian Irro
 */
public interface IndexSearcher {

    Document[] search(String query);

    void refresh();

    void destroy();

}
