package us.warframestat.moddetection.api.detection;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;

/** Convert/manage/interchage images */
public class ImageManipulator {
  private static final OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
  private static final Java2DFrameConverter converterToImage = new Java2DFrameConverter();

  public static String fromMat2Jpeg(Mat mat) {
    return imgToBase64String(fromMat2Buffer(mat), "jpeg");
  }

  public static BufferedImage fromMat2Buffer(Mat mat) {
    return converterToImage.convert(converterToMat.convert(mat));
  }

  /**
   * convert image to base64 string for use in json
   *
   * @param img bufferedimage to convert
   * @param formatName format of image i.e. jpg, png
   * @return base64 string of image
   */
  private static String imgToBase64String(final BufferedImage img, final String formatName) {
    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    try (final OutputStream b64os = Base64.getEncoder().wrap(os)) {
      ImageIO.write(img, formatName, b64os);
    } catch (final IOException ioe) {
      throw new UncheckedIOException(ioe);
    }
    return os.toString();
  }
}
