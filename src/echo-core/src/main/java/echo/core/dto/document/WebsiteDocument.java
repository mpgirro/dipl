package echo.core.dto.document;

/**
 * @author Maximilian Irro
 */
public class WebsiteDocument implements Document {

    private String docId;

    @Override
    public String getDocId(){
        return this.docId;
    }

    @Override
    public void setDocId(String docId){
        this.docId = docId;
    }

}
