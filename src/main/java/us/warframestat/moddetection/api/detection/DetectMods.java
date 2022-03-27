package us.warframestat.moddetection.api.detection;

import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_UNCHANGED;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imdecode;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.TesseractException;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.warframestat.moddetection.api.API;
import us.warframestat.moddetection.api.models.Mod;
import us.warframestat.moddetection.api.models.ModResults;
import us.warframestat.moddetection.api.utils.WarframeMarketAPI;

public class DetectMods {
  private static final Logger LOGGER = LoggerFactory.getLogger(DetectMods.class);

  private DetectMods() {}

  /**
   * Runs opencv and tesseract Keys & Data: - mods: {@link List <Mod>} - image: {@link String} -
   * totalPrimce: {@link int}
   *
   * @param modImage original image
   * @param stageFile cascade file
   * @param scale scale factor
   * @param neighbours number of neighbours
   * @param platform platform (ps4, xbox, pc, switch)
   * @return map of complete image and detected mods
   * @throws IOException on error
   */
  public static Map.Entry<BufferedImage, ModResults> run(
      BufferedImage modImage, String stageFile, double scale, int neighbours, String platform)
      throws IOException {
    API.setupData();
    CascadeClassifier modDetector =
        new CascadeClassifier(API.resolve("cascade").resolve(stageFile + ".xml").toString());

    // We need to create a Mat based on the image as, hopefully, we'll be drawing on it real soon
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ImageIO.write(modImage, "jpg", byteArrayOutputStream);
    byteArrayOutputStream.flush();
    Mat image = imdecode(new Mat(byteArrayOutputStream.toByteArray()), IMREAD_UNCHANGED);

    // A list of rectangles we will draw if the detection is successful
    RectVector modDetections = new RectVector();

    // debugging time takes for performance
    long l = System.currentTimeMillis();
    // MAGIC!
    // TODO: detect aspect ratio and change min/max size
    modDetector.detectMultiScale(
        image, modDetections, scale, neighbours, 0, new Size(230, 100), new Size(270, 140));
    long l1 = System.currentTimeMillis();

    LOGGER.debug("Detected {} mods in {} ms", modDetections.size(), l1 - l);
    DetectModInfo.setup();

    Mat rectangleMat = image.clone();
    List<Mod> modArray = new ArrayList<>();
    int totalPrice = 0;
    // Draw a bounding box around each mod. And cross your fingers. And toes. And your pet's toes.
    // If they have toes...
    for (int i = 0; i < modDetections.size(); i++) {
      Rect rect = modDetections.get(i);
      Mat subMatrix = image.apply(rect);
      try {
        Map.Entry<String, String> modName = DetectModInfo.detectModName(subMatrix);
        int price = WarframeMarketAPI.getPrice(modName.getValue(), platform);
        totalPrice += price;
        Mod mod = new Mod.Builder().name(modName.getKey()).price(price).image(subMatrix).build();
        modArray.add(mod);
      } catch (TesseractException e) {
        e.printStackTrace();
      } // Rectangle drawing!

      rectangle(
          rectangleMat,
          new Point(rect.x(), rect.y()),
          new Point(rect.x() + rect.width(), rect.y() + rect.height()),
          new Scalar(0, 255, 0, 0));
    }

    modDetector.close();

    ModResults modInfo =
        new ModResults.Builder().mods(modArray).image(rectangleMat).totalPrice(totalPrice).build();

    return new AbstractMap.SimpleEntry<>(ImageManipulator.fromMat2Buffer(rectangleMat), modInfo);
  }

  /**
   * convert base64 image string back to bufferedimage
   *
   * @param base64String base64 image string
   * @return bufferedimage from string
   * @deprecated ?
   */
  public static BufferedImage base64StringToImg(final String base64String) {
    try {
      return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64String)));
    } catch (final IOException ioe) {
      throw new UncheckedIOException(ioe);
    }
  }
}
