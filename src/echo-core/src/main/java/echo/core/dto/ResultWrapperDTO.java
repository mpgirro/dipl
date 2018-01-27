package echo.core.dto;

/**
 * @author Maximilian Irro
 */
public class ResultWrapperDTO {

    private int currPage;
    private int maxPage;
    private int totalHits;
    private IndexResult[] results;

    public ResultWrapperDTO(){

    }

    public int getCurrPage() {
        return currPage;
    }

    public void setCurrPage(int currPage) {
        this.currPage = currPage;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }

    public int getTotalHits() {
        return totalHits;
    }

    public void setTotalHits(int totalHits) {
        this.totalHits = totalHits;
    }

    public IndexResult[] getResults() {
        return results;
    }

    public void setResults(IndexResult[] results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "ResultWrapperDTO{" +
            "currPage=" + currPage +
            ", maxPage=" + maxPage +
            ", totalHits=" + totalHits +
            ", results=" + results +
            '}';
    }
}