package echo.core.util;

import org.hashids.Hashids;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;

/**
 * @author Maximilian Irro
 */
public class EchoIdGenerator {

    private final Hashids hashids = new Hashids("Bit useless for my purpose, but why not");
    private final Long shardId;
    private int seq;

    public EchoIdGenerator(long shardId) {
        this.shardId = shardId;  // should be 13 bit
        this.seq = 0;            // should be 10bit
    }

    public String getNewId(){
        final long now = System.currentTimeMillis();
        seq = (seq+1) % 1024;
        return hashids.encode(now, shardId, seq);
    }

    @Deprecated
    private int randomWithRange(int min, int max) {
        final int range = (max - min) + 1;
        return (int)(Math.random() * range) + min;
    }

    @Deprecated
    private BitSet convert(long value) {
        final BitSet bits = new BitSet();
        int index = 0;
        while (value != 0L) {
            if (value % 2L != 0) {
                bits.set(index);
            }
            ++index;
            value = value >>> 1;
        }
        return bits;
    }

    @Deprecated
    private long convert(BitSet bits) {
        long value = 0L;
        for (int i = 0; i < bits.length(); ++i) {
            value += bits.get(i) ? (1L << i) : 0L;
        }
        return value;
    }

}
