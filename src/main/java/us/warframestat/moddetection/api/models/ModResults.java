package us.warframestat.moddetection.api.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bytedeco.opencv.opencv_core.Mat;
import org.json.JSONPropertyName;
import us.warframestat.moddetection.api.detection.ImageManipulator;

public class ModResults {
  public final List<Mod> mods;
  public final String image;
  public final int totalPrice;

  @JSONPropertyName("mods")
  public List<Mod> getMods() {
    return mods;
  }

  @JSONPropertyName("image")
  public String getImage() {
    return image;
  }

  @JSONPropertyName("totalPrice")
  public int getTotalPrice() {
    return totalPrice;
  }

  private ModResults(Builder builder) {
    this.mods = Collections.unmodifiableList(builder.mods);
    this.image = builder.image;
    this.totalPrice = builder.totalPrice;
  }

  public static final class Builder {
    private List<Mod> mods = new ArrayList<>();
    private String image;
    private int totalPrice;

    /**
     * Appends mod to current list of mods
     *
     * @param mod mod to ad
     * @return Builder
     */
    public Builder mod(Mod mod) {
      mods.add(mod);
      return this;
    }

    /**
     * Replaces the current list of mods with the provided list of mods
     *
     * @param mods list of mods to replace current with
     * @return Builder
     */
    public Builder mods(List<Mod> mods) {
      this.mods = mods;
      return this;
    }

    public Builder image(Mat mat) {
      this.image = ImageManipulator.fromMat2Jpeg(mat);
      return this;
    }

    public Builder totalPrice(int totalPrice) {
      this.totalPrice = totalPrice;
      return this;
    }

    public ModResults build() {
      return new ModResults(this);
    }
  }
}
