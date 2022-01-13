package us.warframestat.moddetection.api;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import us.warframestat.moddetection.api.detection.DetectMods;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static us.warframestat.moddetection.api.API.LOGGER;

/**
 * Unit test for simple App.
 */
class AppTest
{
    /**
     * Rigorous Test :-)
     */
    @Test
    void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    void testModDetection() throws IOException {
        API.setupData();
        BufferedImage image = ImageIO.read(Objects.requireNonNull(this.getClass().getResourceAsStream("/images/1.jpg")));
        Map.Entry<BufferedImage, JSONObject> output = DetectMods.run(image, "cascade19", 1.150, 4, "pc");
        LOGGER.info(output.getValue().toString());
        LOGGER.info(String.valueOf(output.getValue().toString().length()));
        API.cleanupData();
        assertNotEquals(null, output);
    }
}
