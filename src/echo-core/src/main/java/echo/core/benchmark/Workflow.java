package echo.core.benchmark;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Maximilian Irro
 */
public enum Workflow {
    PODCAST_INDEX("PODCAST_INDEX"),
    EPISODE_INDEX("EPISODE_INDEX"),
    RESULT_RETRIEVAL("RESULT_RETRIEVAL");

    private String name;

    private static final Map<String,Workflow> MAP;

    static {
        final Map<String,Workflow> map = new ConcurrentHashMap<>();
        for (Workflow instance : Workflow.values()) {
            map.put(instance.getName(),instance);
        }
        MAP = Collections.unmodifiableMap(map);
    }

    private Workflow(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

}
