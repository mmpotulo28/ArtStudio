import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageHandler {
 private BufferedImage loadedImage;
 private int imageX, imageY; // Position of the image
 private int imageWidth, imageHeight; // Size of the image
 private int cropStartX, cropStartY; // Starting point of crop rectangle
 private int cropEndX, cropEndY; // Ending point of crop rectangle

 public BufferedImage getLoadedImage() {
  return loadedImage;
 }

 public int getImageX() {
  return imageX;
 }

 public int getImageY() {
  return imageY;
 }

 public int getImageWidth() {
  return imageWidth;
 }

 public int getImageHeight() {
  return imageHeight;
 }

 public void addImage(String filePath) {
  try {
   loadedImage = ImageIO.read(new File(filePath)); // Load the image from file path.

   this.imageX = 50; // Default position on canvas (can be adjusted)
   this.imageY = 50; // Default position on canvas (can be adjusted)

   if (loadedImage != null) { // Check if the loaded image is not null.
    this.imageWidth = loadedImage.getWidth();
    this.imageHeight = loadedImage.getHeight();
   } else {
    throw new IOException("Failed to load image.");
   }
  } catch (IOException e) {
   e.printStackTrace();
  }
 }

 public void performCrop() {
  if (loadedImage != null) {
   try {
    int x1 = Math.min(cropStartX, cropEndX);
    int y1 = Math.min(cropStartY, cropEndY);
    int width = Math.abs(cropEndX - cropStartX);
    int height = Math.abs(cropEndY - cropStartY);

    BufferedImage croppedImg = loadedImage.getSubimage(x1 - imageX, y1 - imageY, width, height);
    loadedImage = croppedImg;
    this.imageWidth = croppedImg.getWidth();
    this.imageHeight = croppedImg.getHeight();

    this.imageX += x1 - Math.min(imageX + width, x1); // Adjust position after cropping.
    this.imageY += y1 - Math.min(imageY + height, y1);
   } catch (RasterFormatException e) {
    e.printStackTrace();
   }
  }
 }

 public void resizeImage(int newWidth, int newHeight) {
  if (loadedImage != null) {
   try {
    // Create a new BufferedImage with the desired dimensions
    BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = resizedImage.createGraphics();

    // Set rendering hints for better quality
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Draw the original image scaled to the new size
    g.drawImage(loadedImage, 0, 0, newWidth, newHeight, null);
    g.dispose();

    // Update loaded image and dimensions
    loadedImage = resizedImage;
    this.imageWidth = newWidth;
    this.imageHeight = newHeight;
   } catch (Exception e) {
    e.printStackTrace();
   }
  }
 }

 public void repositionImage(int newX, int newY) {
  this.imageX = newX;
  this.imageY = newY;
 }

 public void rotateImageClockwise() {
  if (loadedImage != null) {
   try {
    loadedImage = rotateImage(loadedImage, 45);
   } catch (Exception e) {
    e.printStackTrace();
   }
  }
 }

 public void rotateImageAntiClockwise() {
  if (loadedImage != null) {
   try {
    loadedImage = rotateImage(loadedImage, -45);
   } catch (Exception e) {
    e.printStackTrace();
   }
  }
 }

 private BufferedImage rotateImage(BufferedImage image, double angle) {
  int w = image.getWidth();
  int h = image.getHeight();
  BufferedImage rotatedImage = new BufferedImage(w, h, image.getType());
  Graphics2D g2d = rotatedImage.createGraphics();
  g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
  g2d.rotate(Math.toRadians(angle), w / 2, h / 2);
  g2d.drawImage(image, 0, 0, null);
  g2d.dispose();
  return rotatedImage;
 }

 public void setCropStart(int x, int y) {
  cropStartX = x;
  cropStartY = y;
 }

 public void setCropEnd(int x, int y) {
  cropEndX = x;
  cropEndY = y;
 }

 public void saveCanvasAsPNG(Image canvasImage, String filePath) {
  if (canvasImage != null) {
   try {
    BufferedImage bufferedImage = new BufferedImage(canvasImage.getWidth(null), canvasImage.getHeight(null),
      BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = bufferedImage.createGraphics();
    g2d.drawImage(canvasImage, 0, 0, null);
    g2d.drawImage(loadedImage, imageX, imageY, null); // Draw the loaded image onto the canvas
    g2d.dispose();
    ImageIO.write(bufferedImage, "png", new File(filePath));
   } catch (IOException e) {
    e.printStackTrace();
   }
  }
 }
}
