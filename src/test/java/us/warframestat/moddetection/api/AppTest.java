package us.warframestat.moddetection.api;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import us.warframestat.moddetection.api.detection.DetectMods;
import us.warframestat.moddetection.api.models.ModResults;

/** Unit test for mod-detection-api. */
class AppTest {
  @Test
  void testModDetection() throws IOException {
    API.setupData();
    BufferedImage image =
        ImageIO.read(Objects.requireNonNull(this.getClass().getResourceAsStream("/images/1.jpg")));
    Map.Entry<BufferedImage, ModResults> output =
        DetectMods.run(image, "cascade19", 1.150, 4, "pc");
    assertNotNull(output.getValue());
    assertNotNull(output.getValue().mods.get(0).name);
    API.cleanupData();
    assertNotNull(output);
  }

  @Test
  void testApi() {
    ModResults modResults = API.main(new String[] {"./temp", "/images/1.jpg"});
    assertNotNull(modResults);
    assertNotNull(modResults.getImage());
    assertTrue(modResults.totalPrice > 0);
    assertTrue(modResults.mods.size() > 0);
  }
}
