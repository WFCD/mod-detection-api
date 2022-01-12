package us.warframestat.moddetection.api;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.warframestat.moddetection.api.detection.DetectMods;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

public class App {
    public static Logger logger = LoggerFactory.getLogger(App.class);

    public static File data = new File(System.getProperty("java.io.tmpdir"), "ModDetectionOpenCV");

    public static void main( String[] args ) {
        try {
            App.setupData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info(Arrays.toString(args));
    }

    /**
     * Setup the temp data directory.
     * @throws IOException  if the directory cannot be created.
     */
    public static void setupData() throws IOException {
        data.mkdirs();
        FileUtils.cleanDirectory(data);
        File cascade = data.toPath().resolve("cascade").toFile();
        File tesseract = data.toPath().resolve("tesseract").toFile();
        cascade.mkdirs();
        tesseract.mkdirs();
        Files.copy(Objects.requireNonNull(App.class.getResourceAsStream("/cascade/params.xml")), cascade.toPath().resolve("params.xml"));
        Files.copy(Objects.requireNonNull(App.class.getResourceAsStream("/tesseract/eng.traineddata")), tesseract.toPath().resolve("eng.traineddata"));
        Files.copy(Objects.requireNonNull(App.class.getResourceAsStream("/tesseract/Mods.json")), tesseract.toPath().resolve("Mods.json"));
        for (int i = 0; i <= 19; i++) {
            Files.copy(Objects.requireNonNull(App.class.getResourceAsStream("/cascade/cascade" + i + ".xml")), cascade.toPath().resolve("cascade" + i + ".xml"));
            Files.copy(Objects.requireNonNull(App.class.getResourceAsStream("/cascade/stage" + i + ".xml")), cascade.toPath().resolve("stage" + i + ".xml"));
        }
    }

}
