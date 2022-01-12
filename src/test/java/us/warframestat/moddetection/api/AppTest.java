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
import static us.warframestat.moddetection.api.App.logger;

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
        App.setupData();
        BufferedImage image = ImageIO.read(Objects.requireNonNull(this.getClass().getResourceAsStream("/images/1.jpg")));
        Map.Entry<BufferedImage, JSONObject> output = DetectMods.run(image, "cascade19", 1.150, 4, "pc");
        logger.info(output.getValue().toString());
        logger.info(String.valueOf(output.getValue().toString().length()));
        assertNotEquals(null, output);
    }
}
