package echo.microservice.catalog;

import echo.core.util.ExoGenerator;

/**
 * @author Maximilian Irro
 */
public class ExoUtil {

    private static final ExoGenerator exoGenerator = new ExoGenerator(1); // TODO set the microservice worker count

    private static ExoUtil instance = null;
    protected ExoUtil() {
        // Exists only to defeat instantiation.
    }
    public static ExoUtil getInstance() {
        if(instance == null) {
            instance = new ExoUtil();
        }
        return instance;
    }

    public ExoGenerator getExoGenerator() {
        return exoGenerator;
    }


}
