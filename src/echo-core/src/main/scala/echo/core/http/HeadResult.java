package echo.core.http;

import java.util.Optional;

/**
 * @author Maximilian Irro
 */
public class HeadResult {

    private Boolean saveToDownload;
    private Integer statusCode;
    private Optional<String> location;
    private Optional<String> mimeType;
    private Optional<String> eTag;
    private Optional<String> lastModified;

    public Boolean getSaveToDownload() {
        return saveToDownload;
    }

    public void setSaveToDownload(Boolean saveToDownload) {
        this.saveToDownload = saveToDownload;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Optional<String> getLocation() {
        return location;
    }

    public void setLocation(Optional<String> location) {
        this.location = location;
    }

    public Optional<String> getMimeType() {
        return mimeType;
    }

    public void setMimeType(Optional<String> mimeType) {
        this.mimeType = mimeType;
    }

    public Optional<String> geteTag() {
        return eTag;
    }

    public void seteTag(Optional<String> eTag) {
        this.eTag = eTag;
    }

    public Optional<String> getLastModified() {
        return lastModified;
    }

    public void setLastModified(Optional<String> lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "HeadResult{" +
            "saveToDownload=" + saveToDownload +
            ", statusCode=" + statusCode +
            ", location=" + location +
            ", mimeType=" + mimeType +
            ", eTag=" + eTag +
            ", lastModified=" + lastModified +
            '}';
    }
}
