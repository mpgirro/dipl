package echo.core.benchmark;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Maximilian Irro
 */
public enum ArchitectureType {
    ECHO_AKKA("ECHO_AKKA"),
    ECHO_MSA("ECHO_MSA");

    private String name;
    private static final Map<String,Workflow> MAP;

    static {
        final Map<String,Workflow> map = new ConcurrentHashMap<>();
        for (Workflow instance : Workflow.values()) {
            map.put(instance.getName(),instance);
        }
        MAP = Collections.unmodifiableMap(map);
    }

    private ArchitectureType(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
