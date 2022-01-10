package us.warframestat.moddetection.api.detection;

import net.sourceforge.tess4j.TesseractException;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.warframestat.moddetection.api.utils.WarframeMarketAPI;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imdecode;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;

public class DetectMods {
  private static final Logger logger = LoggerFactory.getLogger(DetectMods.class);

  private DetectMods() { }

  // Lets do some recognising!
  public static Map.Entry<BufferedImage, Integer> run(BufferedImage modImage, String stageFile, double scale, int neighbours, String platform) throws IOException {
    // weird bugs are weird -getClassLoader.getPath() prefixes the path with a / - this is obviously
    // a bug with Windows/Java, but this work around works
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    String cascadeFilePath =
        new File(
                Objects.requireNonNull(
                        DetectMods.class
                            .getClassLoader()
                            .getResource("cascade/" + stageFile + ".xml"))
                    .getFile())
            .getAbsolutePath();

    // Create a new CascadeClassifier based of the cascades created - Which took over 35 computing
    // days to complete....
    opencv_objdetect.CascadeClassifier modDetector =
        new opencv_objdetect.CascadeClassifier(cascadeFilePath);

    // We need to create a Mat based on the image as, hopefully, we'll be drawing on it real soon
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ImageIO.write(modImage, "jpg", byteArrayOutputStream);
    byteArrayOutputStream.flush();
    opencv_core.Mat image =
        opencv_imgcodecs.imdecode(
            new opencv_core.Mat(byteArrayOutputStream.toByteArray()),
            opencv_imgcodecs.IMREAD_UNCHANGED);

    // A list of rectangles we will draw if the detection is successful
    opencv_core.RectVector modDetections = new opencv_core.RectVector();

    // debugging time takes for performance
    long l = System.currentTimeMillis();
    // MAGIC!
    // todo: detect aspect ratio and change min/max size
    modDetector.detectMultiScale(
        image,
        modDetections,
        scale,
        neighbours,
        0,
        new opencv_core.Size(230, 100),
        new opencv_core.Size(270, 140));
    long l1 = System.currentTimeMillis();

    logger.debug("Detected {} mods in {} ms", modDetections.size(), l1 - l);
    DetectModInfo.setup();

    opencv_core.Mat rectangleMat = image.clone();
    int totalPrice = 0;
    // Draw a bounding box around each mod. And cross your fingers. And toes. And your pet's toes.
    // If they have toes...
    for (int i = 0; i < modDetections.size(); i++) {
      opencv_core.Rect rect = modDetections.get(i);
      opencv_core.Mat subMatrix = image.apply(rect);
      try {
        Map.Entry<String, String> mod = DetectModInfo.detectModName(subMatrix);
        totalPrice += WarframeMarketAPI.getPrice(mod.getValue(), platform);
      } catch (TesseractException e) {
        e.printStackTrace();
      }// Rectangle drawing!

      opencv_imgproc.rectangle(
          rectangleMat,
          new opencv_core.Point(rect.x(), rect.y()),
          new opencv_core.Point(rect.x() + rect.width(), rect.y() + rect.height()),
          new opencv_core.Scalar(0, 255, 0, 0));
    }
    OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
    Java2DFrameConverter converterToImage = new Java2DFrameConverter();

    BufferedImage finalImage = converterToImage.convert(converterToMat.convert(rectangleMat));

    modDetector.close();
    return new AbstractMap.SimpleEntry<>(finalImage, totalPrice);
  }
}
