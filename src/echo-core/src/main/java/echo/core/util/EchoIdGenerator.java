package echo.core.util;

import org.hashids.Hashids;

/**
 * @author Maximilian Irro
 */
public class EchoIdGenerator {

    private static final Hashids hashids = new Hashids("Bit useless for my purpose, but why not");

    public static String getNewId(){
        return hashids.encode(randomWithRange(1, 10000) + Math.round(System.currentTimeMillis()*Math.random()));
    }

    private static int randomWithRange(int min, int max) {
        final int range = (max - min) + 1;
        return (int)(Math.random() * range) + min;
    }

}
