package us.warframestat.moddetection.api.models;

import org.bytedeco.opencv.opencv_core.Mat;
import org.json.JSONPropertyName;
import us.warframestat.moddetection.api.detection.DetectModInfo;
import us.warframestat.moddetection.api.detection.ImageManipulator;

/** Model for a Mod, a detectable structure from {@link DetectModInfo} */
public class Mod {
  public final String name;
  public final int price;
  public final String image;

  @JSONPropertyName("name")
  public String getName() {
    return name;
  }

  @JSONPropertyName("price")
  public int getPrice() {
    return price;
  }

  @JSONPropertyName("image")
  public String getImage() {
    return image;
  }

  private Mod(Builder builder) {
    this.name = builder.name;
    this.price = builder.price;
    this.image = builder.image;
  }

  /** Build a Mod object from components */
  public static final class Builder {
    private String name;
    private int price;
    private String image;

    public Builder() {}

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder price(int price) {
      this.price = price;
      return this;
    }

    public Builder image(Mat image) {
      this.image = ImageManipulator.fromMat2Jpeg(image);
      return this;
    }

    public Mod build() {
      return new Mod(this);
    }
  }
}
