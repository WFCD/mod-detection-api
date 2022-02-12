package us.warframestat.moddetection.api;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.warframestat.moddetection.api.detection.DetectMods;
import us.warframestat.moddetection.api.models.ModResults;

/** Entrypoint on the API. */
public class API {
  private static final Logger LOGGER = LoggerFactory.getLogger(API.class);
  private static File DATA = new File(System.getProperty("java.io.tmpdir"), "ModDetectionOpenCV");

  /**
   * Run the detection.
   *
   * @param args datafolder
   */
  public static ModResults main(String[] args) {
    if (args.length != 0) {
      try {
        DATA = new File(args[0]);
      } catch (NullPointerException e) {
        DATA = new File(System.getProperty("java.io.tmpdir"), "ModDetectionOpenCV");
      }
    }
    try {
      API.setupData();
    } catch (IOException e) {
      LOGGER.error("Stuff happened", e);
    }

    if (args.length < 2) {
      LOGGER.error("No file specified, no file to find");
      return null;
    }
    BufferedImage inputImage;
    Map.Entry<BufferedImage, ModResults> output;
    try {
      String imageLoc = args[1];
      inputImage = ImageIO.read(Objects.requireNonNull(API.class.getResourceAsStream(imageLoc)));
      String cascadeFile = "cascade19"; // TODO: Read from args[2] or default
      double scale = 1.150; // TODO: Allow reading scale from args[3] or default
      int neighbors = 4; // TODO: Read from args[4] or default
      String platform = "pc"; // TODO: Allow reading platform from args[5] or default
      output = DetectMods.run(inputImage, cascadeFile, scale, neighbors, platform);
      LOGGER.error(new JSONObject(output.getValue()).toString());
      return output.getValue();
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
    } finally {
      API.cleanupData();
    }
    return null;
  }

  /**
   * Set up the temp data directory.
   *
   * @throws IOException if the directory cannot be created.
   */
  public static void setupData() throws IOException {
    DATA.mkdirs();
    FileUtils.cleanDirectory(DATA);
    File cascade = DATA.toPath().resolve("cascade").toFile();
    File tesseract = DATA.toPath().resolve("tesseract").toFile();
    cascade.mkdirs();
    tesseract.mkdirs();
    Files.copy(
        Objects.requireNonNull(API.class.getResourceAsStream("/cascade/params.xml")),
        cascade.toPath().resolve("params.xml"));
    Files.copy(
        Objects.requireNonNull(API.class.getResourceAsStream("/tesseract/eng.traineddata")),
        tesseract.toPath().resolve("eng.traineddata"));
    Files.copy(
        Objects.requireNonNull(API.class.getResourceAsStream("/tesseract/Mods.json")),
        tesseract.toPath().resolve("Mods.json"));
    for (int i = 0; i <= 19; i++) {
      Files.copy(
          Objects.requireNonNull(API.class.getResourceAsStream("/cascade/cascade" + i + ".xml")),
          cascade.toPath().resolve("cascade" + i + ".xml"));
      Files.copy(
          Objects.requireNonNull(API.class.getResourceAsStream("/cascade/stage" + i + ".xml")),
          cascade.toPath().resolve("stage" + i + ".xml"));
    }
  }

  /** Clean up API DATA directory. */
  public static void cleanupData() {
    try {
      FileUtils.deleteDirectory(DATA);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Resolve a file path based on API data location
   *
   * @param path string of path to resolve
   * @return resolved {Path} object
   */
  public static Path resolve(String path) {
    return DATA.toPath().resolve(path);
  }
}
