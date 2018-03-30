package echo.core.async.job;

import com.google.common.base.MoreObjects;
import echo.core.domain.dto.IndexDocDTO;

/**
 * @author Maximilian Irro
 */
public class AddOrUpdateDocIndexJob implements IndexJob {

    private IndexDocDTO indexDoc;

    public AddOrUpdateDocIndexJob() {

    }

    public AddOrUpdateDocIndexJob(IndexDocDTO indexDoc) {
        this.indexDoc = indexDoc;
    }

    public IndexDocDTO getIndexDoc() {
        return indexDoc;
    }

    public void setIndexDoc(IndexDocDTO indexDoc) {
        this.indexDoc = indexDoc;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("AddOrUpdateDocIndexJob")
            .omitNullValues()
            .add("indexDoc", indexDoc)
            .toString();
    }

}
