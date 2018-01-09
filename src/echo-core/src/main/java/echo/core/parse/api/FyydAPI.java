package echo.core.parse.api;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Minimalistic(!) wrapper around the fyyd.de API
 *
 *
 * For API documentation, see https://github.com/eazyliving/fyyd-api
 *
 * @author Maximilian Irro
 */
public class FyydAPI extends API {

    private static String API_URL = "https://api.fyyd.de/0.2";

    @Override
    public String getURL() {
        return API_URL;
    }

    @Override
    public List<String> getFeedUrls(int count) throws IOException {
        final Map<String,Object> apiData = jsonToMap(getPodcasts(count));
        if(apiData.containsKey("data")){
            //final List<Map<String,Object>> data = jsonToListMap((String) apiData.get("data"));
            final List<Map<String,Object>> data = (List<Map<String,Object>>) apiData.get("data");
            if(data.size() > 0){
                return data.stream()
                    .filter(d -> d.containsKey("xmlURL"))   // sanity check if field is present
                    .map(d -> d.get("xmlURL"))              // extract only feed url
                    .map(d -> (String) d)                   // by type it is still an object, so make it a string
                    .collect(Collectors.toList());
            }
        }

        return new LinkedList<>();
    }

    public String getPodcasts(int count) throws IOException {
        return get(API_URL+"/podcasts?count="+count);
    }

    public String getPodcast(String id) throws IOException {
        return get(API_URL+"/podcast/?podcast_id="+id); // TODO schaut die API wirklich so aus?
    }




}
