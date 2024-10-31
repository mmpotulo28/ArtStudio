import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import javax.swing.*;

public class DrawingCanvas extends JPanel {

    private static final long serialVersionUID = 1L;

    private Color currentColor = Color.BLACK; // Default color for brush

    /* ======================FLAGS=========================== */
    private boolean isEraserActive = false; // Flag for eraser mode
    private boolean isDrawingShape = false; // Flag for shape drawing mode
    private boolean isResizingImage = false; // Flag for resizing the image
    private boolean isDraggingImage = false; // Flag for dragging the image
    private boolean isCroppingImage = false; // Flag for cropping the image

    /* ======================FLAGS=========================== */
    private String currentShape = "Rectangle"; // Current shape type
    private int lastX, lastY; // Last mouse coordinates
    private int brushWidth = 5; // Default brush width
    private Image canvasImage; // Image for the canvas
    private Graphics2D g2d; // Graphics context for drawing

    /* ======================Image Handling=========================== */
    private ImageHandler imageHandler = new ImageHandler();

    /* ======================Zoom Handling=========================== */
    private double zoomLevel = 1.0; // Default zoom level

    /* ======================Text Handling=========================== */
    private String currentText = "";
    private boolean isAddingText = false;
    private int textX, textY; // Coordinates for text positioning

    public DrawingCanvas() {
        setBackground(Color.WHITE); // Set background color
        setDoubleBuffered(true); // Enable double buffering

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
                if (isEraserActive) {
                    erase(lastX, lastY);
                }
                if (isAddingText) {
                    textX = e.getX();
                    textY = e.getY();
                    drawText(textX, textY);
                }
                if (imageHandler.getLoadedImage() != null && e.getX() >= imageHandler.getImageX()
                        && e.getX() <= (imageHandler.getImageX() + imageHandler.getImageWidth())
                        && e.getY() >= imageHandler.getImageY()
                        && e.getY() <= (imageHandler.getImageY() + imageHandler.getImageHeight())) {
                    if (isDraggingImage) {
                        isDraggingImage = true;
                    } else if (isCroppingImage) {
                        imageHandler.setCropStart(e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDrawingShape) {
                    drawShape(e.getX(), e.getY()); // Draw shape at the end point when released
                } else if (isCroppingImage) {
                    imageHandler.setCropEnd(e.getX(), e.getY());
                    imageHandler.performCrop(); // Perform cropping when mouse is released.
                } else if (!(isEraserActive || isDrawingShape || isDraggingImage || isResizingImage
                        || isCroppingImage)) {
                    draw(e.getX(), e.getY()); // Draw at the end point when released
                }

                isDraggingImage = false; // Stop dragging
                isResizingImage = false; // Stop resizing
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isEraserActive) {
                    erase(e.getX(), e.getY());
                } else if (isDrawingShape) {
                } else if (isDraggingImage) {
                    imageHandler.repositionImage(e.getX(), e.getY());
                } else if (isResizingImage) {
                    imageHandler.resizeImage(e.getX(), e.getY());
                } else if (isAddingText) {
                    textX = e.getX();
                    textY = e.getY();
                    repaint();
                } else if (!(isEraserActive || isDrawingShape || isDraggingImage || isResizingImage
                        || isCroppingImage)) {
                    draw(e.getX(), e.getY());
                    lastX = e.getX();
                    lastY = e.getY();
                }

                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform originalTransform = g2d.getTransform();

        g2d.scale(zoomLevel, zoomLevel);

        if (canvasImage != null) {
            g2d.drawImage(canvasImage, 0, 0, null);
        }
        if (imageHandler.getLoadedImage() != null) {
            g2d.drawImage(imageHandler.getLoadedImage(), imageHandler.getImageX(), imageHandler.getImageY(),
                    imageHandler.getImageWidth(), imageHandler.getImageHeight(), null);
            if (isDraggingImage || isResizingImage) {
                g2d.setColor(Color.RED);
                g2d.drawRect(imageHandler.getImageX(), imageHandler.getImageY(), imageHandler.getImageWidth(),
                        imageHandler.getImageHeight());
            } else if (isCroppingImage) {
                g2d.setColor(Color.BLUE);
                g2d.drawRect(Math.min(imageHandler.getImageX(), imageHandler.getImageY()),
                        Math.min(imageHandler.getImageX(), imageHandler.getImageY()),
                        Math.abs(imageHandler.getImageX() - imageHandler.getImageY()),
                        Math.abs(imageHandler.getImageX() - imageHandler.getImageY()));
            } else if (isAddingText && !currentText.isEmpty()) {
                g2d.setColor(currentColor);
                g2d.drawString(currentText, textX, textY);
            }
        }

        g2d.setTransform(originalTransform);
    }

    public void initializeCanvas(int width, int height) {
        canvasImage = createImage(width, height);
        g2d = (Graphics2D) canvasImage.getGraphics();
        clearCanvas(); // Clear canvas initially
    }

    public void clearCanvas() {
        if (g2d != null) { // Check if g2d is initialized before using it
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            imageHandler = new ImageHandler(); // Clear loaded image reference.
            repaint();
        }
    }

    public void setCurrentColor(Color color) {
        this.currentColor = color;
        this.isEraserActive = false;
    }

    public void deactivateEraser() {
        this.isEraserActive = false;
    }

    public void setBrushWidth(int width) {
        this.brushWidth = width;
        if (g2d != null) { // Update stroke width in g2d if initialized
            g2d.setStroke(new BasicStroke(brushWidth));
        }
    }

    public void setDrawingShape(boolean drawingShape, String shapeType) {
        this.isDrawingShape = drawingShape;
        this.currentShape = shapeType;

        this.isResizingImage = false; // Deactivate resizing when drawing shapes.
        this.isDraggingImage = false; // Deactivate dragging when drawing shapes.
        isCroppingImage = false;
        this.isAddingText = false;
    }

    public void activateEraser() {
        this.isEraserActive = true;
        this.isDrawingShape = false;
        this.isResizingImage = false; // Deactivate resizing when eraser is active.
        this.isDraggingImage = false; // Deactivate dragging when eraser is active.
        this.isAddingText = false;
    }

    public void setResizingMode(boolean resizingMode) {
        isResizingImage = resizingMode;
        isDrawingShape = false;
        isDraggingImage = false;
        isCroppingImage = false;
        this.isAddingText = false;
    }

    public void setDraggingMode(boolean draggingMode) {
        isDraggingImage = draggingMode;
        isDrawingShape = false; // Deactivate shape drawing when dragging.
        isResizingImage = false; // Deactivate resizing when dragging.
        isCroppingImage = false;
        this.isAddingText = false;
    }

    public void setCroppingMode(boolean croppingMode) {
        isCroppingImage = croppingMode;
        isDraggingImage = false;
        isDrawingShape = false; // Deactivate shape drawing when dragging.
        isResizingImage = false; // Deactivate resizing when dragging.
        this.isAddingText = false;
    }

    public void setAddingTextMode(boolean addingText) {
        this.isAddingText = addingText;
        this.isDrawingShape = false;
        this.isResizingImage = false;
        this.isDraggingImage = false;
        this.isCroppingImage = false;
    }

    public void setCurrentText(String text) {
        this.currentText = text;
    }

    private void drawShape(int x, int y) {
        if (g2d != null) { // Check if g2d is initialized before using it
            g2d.setColor(currentColor);
            int width = Math.abs(x - lastX);
            int height = Math.abs(y - lastY);

            switch (currentShape) {
                case "Rectangle":
                    g2d.drawRect(Math.min(lastX, x), Math.min(lastY, y), width, height); // Draw rectangle
                    break;
                case "Circle":
                    int diameter = Math.max(width, height);
                    g2d.drawOval(Math.min(lastX, x), Math.min(lastY, y), diameter, diameter); // Draw circle
                    break;
                case "Square":
                    int sideLength = Math.min(width, height);
                    g2d.drawRect(Math.min(lastX, x), Math.min(lastY, y), sideLength, sideLength); // Draw square
                    break;
                case "Triangle":
                    drawTriangle(lastX, lastY, x, y); // Draw triangle using custom method
                    break;
                case "Star":
                    drawStar(lastX, lastY, width); // Draw star using custom method
                    break;
                case "Pentagon":
                    drawPentagon(lastX, lastY, Math.max(width, height)); // Draw pentagon using custom method
                    break;
                case "Hexagon":
                    drawHexagon(lastX, lastY, Math.max(width, height)); // Draw hexagon using custom method
                    break;
                default:
                    break;
            }

            repaint();
        }
    }

    private void drawTriangle(int x1, int y1, int x2, int y2) {
        int[] xPoints = { x1, x2, (x1 + x2) / 2 };
        int[] yPoints = { y1, y1, y1 - Math.abs(x2 - x1) };
        g2d.drawPolygon(xPoints, yPoints, 3); // Draw triangle as a polygon
    }

    private void drawStar(int centerX, int centerY, int size) {
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];

        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI / 5;
            double radius = (i % 2 == 0) ? size : size / 2.5;
            xPoints[i] = centerX + (int) (Math.cos(angle) * radius);
            yPoints[i] = centerY - (int) (Math.sin(angle) * radius);
        }

        g2d.drawPolygon(xPoints, yPoints, 10); // Draw star as a polygon
    }

    private void drawPentagon(int centerX, int centerY, int size) {
        int[] xPoints = new int[5];
        int[] yPoints = new int[5];

        for (int i = 0; i < 5; i++) {
            double angle = i * 2 * Math.PI / 5 - Math.PI / 10; // Adjust angle for pentagon shape
            xPoints[i] = centerX + (int) (Math.cos(angle) * size);
            yPoints[i] = centerY + (int) (Math.sin(angle) * size);
        }

        g2d.drawPolygon(xPoints, yPoints, 5); // Draw pentagon as a polygon
    }

    private void drawHexagon(int centerX, int centerY, int size) {
        int[] xPoints = new int[6];
        int[] yPoints = new int[6];

        for (int i = 0; i < 6; i++) {
            double angle = i * 2 * Math.PI / 6 - Math.PI / 12; // Adjust angle for hexagon shape
            xPoints[i] = centerX + (int) (Math.cos(angle) * size);
            yPoints[i] = centerY + (int) (Math.sin(angle) * size);
        }

        g2d.drawPolygon(xPoints, yPoints, 6); // Draw hexagon as a polygon
    }

    private void draw(int x, int y) {
        if (g2d != null) { // Check if g2d is initialized before using it
            g2d.setColor(currentColor);
            g2d.drawLine(lastX, lastY, x, y);
            repaint();
        }
    }

    private void erase(int x, int y) {
        if (g2d != null) { // Check if g2d is initialized before using it
            g2d.setColor(Color.WHITE);
            g2d.fillOval(x - brushWidth / 2, y - brushWidth / 2, brushWidth, brushWidth);
            repaint();
        }
    }

    // image processing
    public void addImage(String filePath) throws IOException {
        imageHandler.addImage(filePath);
        repaint();
    }

    public void performCrop() {
        imageHandler.performCrop();
        repaint();
    }

    public void resizeImage(int newWidth, int newHeight) {
        imageHandler.resizeImage(newWidth, newHeight);
        repaint();
    }

    public void repositionImage(int newX, int newY) {
        imageHandler.repositionImage(newX, newY);
        repaint();
    }

    public void rotateImageClockwise() {
        imageHandler.rotateImageClockwise();
        repaint();
    }

    public void rotateImageAntiClockwise() {
        imageHandler.rotateImageAntiClockwise();
        repaint();
    }

    // use save functionality from IMageHandle
    public void saveCanvasAsPNG(String filePath) throws IOException {
        imageHandler.saveCanvasAsPNG(canvasImage, filePath);
    }

    // Zoom functionality
    public void zoomIn() {
        zoomLevel *= 1.1; // Increase zoom level by 10%
        repaint();
    }

    public void zoomOut() {
        zoomLevel /= 1.1; // Decrease zoom level by 10%
        repaint();
    }

    private void drawText(int x, int y) {
        if (g2d != null && !currentText.isEmpty()) {
            g2d.setColor(currentColor);
            g2d.drawString(currentText, x, y);
            repaint();
        }
    }
}
