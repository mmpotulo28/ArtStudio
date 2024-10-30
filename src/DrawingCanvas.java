
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class DrawingCanvas extends JPanel {

    private static final long serialVersionUID = 1L;

    private Color currentColor = Color.BLACK; // Default color for brush

    /*======================FLAGS===========================*/
    private boolean isEraserActive = false; // Flag for eraser mode
    private boolean isDrawingShape = false; // Flag for shape drawing mode
    private boolean isResizingImage = false; // Flag for resizing the image
    private boolean isDraggingImage = false; // Flag for dragging the image

    /*======================FLAGS===========================*/
    private String currentShape = "Rectangle"; // Current shape type
    private int lastX, lastY; // Last mouse coordinates
    private int brushWidth = 5; // Default brush width
    private Image canvasImage; // Image for the canvas
    private Graphics2D g2d; // Graphics context for drawing

    /*======================Image Hanlding===========================*/
    private BufferedImage loadedImage;
    private int imageX, imageY; // Position of the image
    private int imageWidth, imageHeight; // Size of the image

    public DrawingCanvas() {
        setBackground(Color.WHITE); // Set background color
        setDoubleBuffered(true); // Enable double buffering

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX(); // Store starting coordinates
                lastY = e.getY(); // Store starting coordinates
                if (isEraserActive) {
                    erase(lastX, lastY); // Erase at the starting point
                } // Check if the mouse is over the loaded image
                if (loadedImage != null && e.getX() >= imageX && e.getX() <= (imageX + imageWidth)
                        && e.getY() >= imageY && e.getY() <= (imageY + imageHeight)) {
                    if (isDraggingImage) {
                        isDraggingImage = true; // Start dragging the image
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDrawingShape) {
                    drawShape(e.getX(), e.getY()); // Draw shape at the end point when released
                } else if (!isEraserActive) {
                    draw(e.getX(), e.getY()); // Draw at the end point when released
                }

                isDraggingImage = false; // Stop dragging 
                isResizingImage = false;  // Stop resizing
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isEraserActive) {
                    erase(e.getX(), e.getY()); // Erase while dragging
                } else if (isDrawingShape) {
                    System.out.println("isDrawing Shape - Drag");
//                    drawShape(e.getX(), e.getY()); // Draw shape at the end point when released
                } else if (isDraggingImage) {
                    repositionImage(e.getX(), e.getY()); // Reposition the loaded image while dragging
                } else if (isResizingImage) {
                    resizeImage(e.getX(), e.getY());  // Resize the loaded image while dragging (optional)
                } else {
                    draw(e.getX(), e.getY()); // Draw while dragging
                    lastX = e.getX(); // Update last coordinates
                    lastY = e.getY(); // Update last coordinates
                }
                repaint(); // Repaint to show changes while dragging
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (canvasImage != null) {
            g.drawImage(canvasImage, 0, 0, null); // Draw the canvas image on the panel
        }
        if (loadedImage != null) {
            g.drawImage(loadedImage, imageX, imageY, imageWidth, imageHeight, null);
            // Draw border around the loaded image when resizing or repositioning
            if (isDraggingImage || isResizingImage) {
                g.setColor(Color.RED);
                g.drawRect(imageX, imageY, imageWidth, imageHeight);  // Draw border around the image.
            }
        }
    }

    public void initializeCanvas(int width, int height) {
        canvasImage = createImage(width, height);
        g2d = (Graphics2D) canvasImage.getGraphics();
        clearCanvas(); // Clear canvas initially
    }

    public void clearCanvas() {
        if (g2d != null) {  // Check if g2d is initialized before using it
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            repaint();
        }
    }

    public void setCurrentColor(Color color) {
        this.currentColor = color;
        this.isEraserActive = false;
    }

    public void activateEraser() {
        this.isEraserActive = true;
        this.isDrawingShape = false;
        this.isResizingImage = false;  // Deactivate resizing when eraser is active.
        this.isDraggingImage = false;  // Deactivate dragging when eraser is active.
    }

    public void deactivateEraser() {
        this.isEraserActive = false;
    }

    public void setBrushWidth(int width) {
        this.brushWidth = width;
        if (g2d != null) {  // Update stroke width in g2d if initialized
            g2d.setStroke(new BasicStroke(brushWidth));
        }
    }

    public void setDrawingShape(boolean drawingShape, String shapeType) {
        this.isDrawingShape = drawingShape;
        this.currentShape = shapeType;

        this.isResizingImage = false;  // Deactivate resizing when drawing shapes.
        this.isDraggingImage = false;   // Deactivate dragging when drawing shapes.
    }

    public void setResizingMode(boolean resizingMode) {
        isResizingImage = resizingMode;
        isDrawingShape = false;   // Deactivate shape drawing when resizing.
        isDraggingImage = false;   // Deactivate dragging when resizing.
    }

    public void setDraggingMode(boolean draggingMode) {
        isDraggingImage = draggingMode;
        isDrawingShape = false;   // Deactivate shape drawing when dragging.
        isResizingImage = false;   // Deactivate resizing when dragging.
    }

    private void drawShape(int x, int y) {
        if (g2d != null) {  // Check if g2d is initialized before using it
            g2d.setColor(currentColor);
            int width = Math.abs(x - lastX);
            int height = Math.abs(y - lastY);

            System.out.println("isDrawing Shape - Release" + height + "-" + width);

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
        int[] xPoints = {x1, x2, (x1 + x2) / 2};
        int[] yPoints = {y1, y1, y1 - Math.abs(x2 - x1)};
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
        System.out.println("Drawing" + x + "-" + y);
        if (g2d != null) {  // Check if g2d is initialized before using it
            g2d.setColor(currentColor);
            g2d.drawLine(lastX, lastY, x, y);
            System.out.println("DrawLine" + lastX + "-" + lastY);
            repaint();
        }
    }

    private void erase(int x, int y) {
        if (g2d != null) {  // Check if g2d is initialized before using it
            g2d.setColor(Color.WHITE);
            g2d.fillOval(x - brushWidth / 2, y - brushWidth / 2, brushWidth, brushWidth);
            repaint();
        }
    }

//    image processing    
    public void addImage(String filePath) throws IOException {
        loadedImage = ImageIO.read(new File(filePath));  // Load the image from file path.

        this.imageX = 50;  // Default position on canvas (can be adjusted)
        this.imageY = 50;  // Default position on canvas (can be adjusted)

        if (loadedImage != null) {  // Check if the loaded image is not null.
            this.imageWidth = loadedImage.getWidth();
            this.imageHeight = loadedImage.getHeight();
            repaint();
        } else {
            throw new IOException("Failed to load image.");
        }

        System.out.print(this.imageWidth + " " + this.imageHeight);
        repaint();
    }

    public void resizeImage(int newWidth, int newHeight) {
        if (loadedImage != null) {
            Image tempImg = loadedImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            loadedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

            Graphics g = loadedImage.getGraphics();
            g.drawImage(tempImg, 0, 0, null);
            g.dispose();

            this.imageWidth = newWidth;
            this.imageHeight = newHeight;
            repaint();
        }
    }

    public void repositionImage(int newX, int newY) {
        this.imageX = newX;
        this.imageY = newY;
        repaint();
    }
}
