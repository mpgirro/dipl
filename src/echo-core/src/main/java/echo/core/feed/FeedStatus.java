package echo.core.feed;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Maximilian Irro
 */
public enum FeedStatus {

    NEVER_CHECKED("never_checked"),
    SUCCESS("success"),
    HTTP_403("http_403"),
    PARSE_ERROR("parse_error");

    private String name;

    private static final Map<String,FeedStatus> STATUS_MAP;

    static {
        final Map<String,FeedStatus> map = new ConcurrentHashMap<>();
        for (FeedStatus instance : FeedStatus.values()) {
            map.put(instance.getName(),instance);
        }
        STATUS_MAP = Collections.unmodifiableMap(map);
    }

    private FeedStatus(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public static FeedStatus getByName (String name) {
        return STATUS_MAP.get(name);
    }

}
