package echo.core.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * @author Maximilian Irro
 */
public class BenchmarkUtil {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkUtil.class);

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

    private String baseDir;

    public BenchmarkUtil(String baseDir) {
        this.baseDir = baseDir;
    }

    public void writeToFile(String filename, String content) {

        final Timestamp now = new Timestamp(System.currentTimeMillis());
        final String timestamp = sdf.format(now);
        final String dest = baseDir + "data/" + filename + "_" + timestamp + ".csv";

        log.info("Writing data to : {}", dest);

        final Path path = Paths.get(dest);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendToFile(String filename, String content) {

        final String dest = baseDir + filename + ".csv";

        log.info("Appending data to : {}", dest);

        try (FileWriter writer = new FileWriter(dest, true)) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
