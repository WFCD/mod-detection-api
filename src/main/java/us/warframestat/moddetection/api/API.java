package us.warframestat.moddetection.api;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

/**
 * Entrypoint on the API.
 */
public class API {
    public static final Logger LOGGER = LoggerFactory.getLogger(API.class);

    public static final File DATA = new File(System.getProperty("java.io.tmpdir"), "ModDetectionOpenCV");

    /**
     * Run the detection.
     * @param args datafolder
     */
    public static void main( String[] args ) {
        try {
            API.setupData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info(Arrays.toString(args));
    }

    /**
     * Setup the temp data directory.
     * @throws IOException  if the directory cannot be created.
     */
    public static void setupData() throws IOException {
        DATA.mkdirs();
        FileUtils.cleanDirectory(DATA);
        File cascade = DATA.toPath().resolve("cascade").toFile();
        File tesseract = DATA.toPath().resolve("tesseract").toFile();
        cascade.mkdirs();
        tesseract.mkdirs();
        Files.copy(Objects.requireNonNull(API.class.getResourceAsStream("/cascade/params.xml")), cascade.toPath().resolve("params.xml"));
        Files.copy(Objects.requireNonNull(API.class.getResourceAsStream("/tesseract/eng.traineddata")), tesseract.toPath().resolve("eng.traineddata"));
        Files.copy(Objects.requireNonNull(API.class.getResourceAsStream("/tesseract/Mods.json")), tesseract.toPath().resolve("Mods.json"));
        for (int i = 0; i <= 19; i++) {
            Files.copy(Objects.requireNonNull(API.class.getResourceAsStream("/cascade/cascade" + i + ".xml")), cascade.toPath().resolve("cascade" + i + ".xml"));
            Files.copy(Objects.requireNonNull(API.class.getResourceAsStream("/cascade/stage" + i + ".xml")), cascade.toPath().resolve("stage" + i + ".xml"));
        }
    }

    public static void cleanupData() {
        try {
            FileUtils.deleteDirectory(DATA);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
