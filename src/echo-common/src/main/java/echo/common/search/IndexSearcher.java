package echo.common.search;

import echo.common.dto.document.Document;

/**
 * @author Maximilian Irro
 */
public interface IndexSearcher {

    Document[] search(String query);

    void close();

}
