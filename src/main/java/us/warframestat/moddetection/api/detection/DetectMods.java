package us.warframestat.moddetection.api.detection;

import net.sourceforge.tess4j.TesseractException;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.warframestat.moddetection.api.App;
import us.warframestat.moddetection.api.utils.WarframeMarketAPI;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_UNCHANGED;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imdecode;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;

public class DetectMods {
  private static final Logger logger = LoggerFactory.getLogger(DetectMods.class);

  private DetectMods() { }

  /**
   * Runs opencv and tesseract
   * @param modImage  original image
   * @param stageFile cascade file
   * @param scale    scale factor
   * @param neighbours  number of neighbours
   * @param platform platform (ps4, xbox, pc, switch)
   * @return map of complete image and detected mods
   * @throws IOException on error
   */
  public static Map.Entry<BufferedImage, JSONObject> run(BufferedImage modImage, String stageFile, double scale, int neighbours, String platform) throws IOException {
    CascadeClassifier modDetector = new CascadeClassifier(App.data.toPath().resolve("cascade").resolve(stageFile + ".xml").toString());

    OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
    Java2DFrameConverter converterToImage = new Java2DFrameConverter();

    // We need to create a Mat based on the image as, hopefully, we'll be drawing on it real soon
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ImageIO.write(modImage, "jpg", byteArrayOutputStream);
    byteArrayOutputStream.flush();
    Mat image =
        imdecode(
            new Mat(byteArrayOutputStream.toByteArray()),
            IMREAD_UNCHANGED);

    // A list of rectangles we will draw if the detection is successful
    RectVector modDetections = new RectVector();

    // debugging time takes for performance
    long l = System.currentTimeMillis();
    // MAGIC!
    // toodo: detect aspect ratio and change min/max size
    modDetector.detectMultiScale(
        image,
        modDetections,
        scale,
        neighbours,
        0,
        new Size(230, 100),
        new Size(270, 140));
    long l1 = System.currentTimeMillis();

    logger.debug("Detected {} mods in {} ms", modDetections.size(), l1 - l);
    DetectModInfo.setup();

    Mat rectangleMat = image.clone();
    JSONArray modArray = new JSONArray();
    int totalPrice = 0;
    // Draw a bounding box around each mod. And cross your fingers. And toes. And your pet's toes.
    // If they have toes...
    for (int i = 0; i < modDetections.size(); i++) {
      Rect rect = modDetections.get(i);
      Mat subMatrix = image.apply(rect);
      try {
        Map.Entry<String, String> mod = DetectModInfo.detectModName(subMatrix);
        int price =  WarframeMarketAPI.getPrice(mod.getValue(), platform);
        totalPrice += price;
        modArray.put(new JSONObject()
                .put("name", mod.getKey())
                .put("price", price)
                .put("image", imgToBase64String(converterToImage.convert(converterToMat.convert(subMatrix)), "jpg")));
      } catch (TesseractException e) {
        e.printStackTrace();
      }// Rectangle drawing!

      rectangle(
          rectangleMat,
          new Point(rect.x(), rect.y()),
          new Point(rect.x() + rect.width(), rect.y() + rect.height()),
          new Scalar(0, 255, 0, 0));
    }

    BufferedImage finalImage = converterToImage.convert(converterToMat.convert(rectangleMat));

    modDetector.close();

    JSONObject modInfo = new JSONObject();
    modInfo.put("mods", modArray);
    modInfo.put("image", imgToBase64String(finalImage, "jpg"));
    modInfo.put("totalPrice", totalPrice);

    return new AbstractMap.SimpleEntry<>(finalImage, modInfo);
  }

  public static String imgToBase64String(final BufferedImage img, final String formatName) {
    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    try (final OutputStream b64os = Base64.getEncoder().wrap(os)) {
      ImageIO.write(img, formatName, b64os);
    } catch (final IOException ioe) {
      throw new UncheckedIOException(ioe);
    }
    return os.toString();
  }

  public static BufferedImage base64StringToImg(final String base64String) {
    try {
      return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64String)));
    } catch (final IOException ioe) {
      throw new UncheckedIOException(ioe);
    }
  }
}
