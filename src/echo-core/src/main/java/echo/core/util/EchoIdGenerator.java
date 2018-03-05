package echo.core.util;

import org.hashids.Hashids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maximilian Irro
 */
public class EchoIdGenerator {

    private static final Logger log = LoggerFactory.getLogger(EchoIdGenerator.class);

    private final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final Hashids hashids = new Hashids("Bit useless for my purpose, but why not", 0, ALPHABET);
    private final Integer shardId;
    private Integer seq;

    public EchoIdGenerator(int shardId) {
        this.shardId = shardId;
        this.seq = 0;
    }

    public String getNewId() {
        final long now = System.currentTimeMillis();
        final String id = hashids.encode(now, shardId, seq);
        seq = (seq+1) % 1024;
        return id;
    }

}
