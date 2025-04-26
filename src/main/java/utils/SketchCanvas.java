package utils;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Map;

public class SketchCanvas extends Canvas {

    private List<Map<String, Object>> shapeData;
    private List<String> colors;

    public SketchCanvas(double width, double height) {
        super(width, height);
    }

    /**
     * Constructor for displaying an existing sketch
     * @param width The width of the canvas
     * @param height The height of the canvas
     * @param shapeData The shape data to display
     * @param colors The colors to use
     */
    public SketchCanvas(double width, double height, List<Map<String, Object>> shapeData, List<String> colors) {
        super(width, height);
        this.shapeData = shapeData;
        this.colors = colors;
        renderSketch();
    }

    /**
     * Updates the sketch data and redraws the canvas
     * @param shapeData The new shape data
     * @param colors The new colors
     */
    public void updateSketch(List<Map<String, Object>> shapeData, List<String> colors) {
        this.shapeData = shapeData;
        this.colors = colors;
        renderSketch();
    }

    /**
     * Renders the sketch on the canvas
     */
    private void renderSketch() {
        if (shapeData == null || colors == null || colors.isEmpty()) {
            return;
        }

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        // Use the first color as the default stroke color
        gc.setStroke(Color.web(colors.get(0)));

        for (Map<String, Object> shape : shapeData) {
            String type = (String) shape.get("type");

            // If shape has a specific color, use it
            if (shape.containsKey("colorIndex")) {
                int colorIndex = ((Number) shape.get("colorIndex")).intValue();
                if (colorIndex >= 0 && colorIndex < colors.size()) {
                    gc.setStroke(Color.web(colors.get(colorIndex)));
                }
            }

            // Set line width if specified
            if (shape.containsKey("lineWidth")) {
                gc.setLineWidth(((Number) shape.get("lineWidth")).doubleValue());
            } else {
                gc.setLineWidth(2.0); // Default line width
            }

            // Draw the shape based on its type
            switch (type) {
                case "line":
                    drawLine(gc, shape);
                    break;
                case "circle":
                    drawCircle(gc, shape);
                    break;
                case "rectangle":
                    drawRectangle(gc, shape);
                    break;
                case "freehand":
                    drawFreehand(gc, shape);
                    break;
                default:
                    System.out.println("Unknown shape type: " + type);
            }
        }
    }

    private void drawLine(GraphicsContext gc, Map<String, Object> shape) {
        double x1 = ((Number) shape.get("x1")).doubleValue();
        double y1 = ((Number) shape.get("y1")).doubleValue();
        double x2 = ((Number) shape.get("x2")).doubleValue();
        double y2 = ((Number) shape.get("y2")).doubleValue();

        gc.strokeLine(x1, y1, x2, y2);
    }

    private void drawCircle(GraphicsContext gc, Map<String, Object> shape) {
        double centerX = ((Number) shape.get("centerX")).doubleValue();
        double centerY = ((Number) shape.get("centerY")).doubleValue();
        double radius = ((Number) shape.get("radius")).doubleValue();

        boolean fill = shape.containsKey("fill") && (boolean) shape.get("fill");

        if (fill) {
            if (shape.containsKey("fillColorIndex")) {
                int colorIndex = ((Number) shape.get("fillColorIndex")).intValue();
                if (colorIndex >= 0 && colorIndex < colors.size()) {
                    gc.setFill(Color.web(colors.get(colorIndex)));
                } else {
                    gc.setFill(Color.web(colors.get(0))); // Default to first color
                }
            }
            gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        } else {
            gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        }
    }

    private void drawRectangle(GraphicsContext gc, Map<String, Object> shape) {
        double x = ((Number) shape.get("x")).doubleValue();
        double y = ((Number) shape.get("y")).doubleValue();
        double width = ((Number) shape.get("width")).doubleValue();
        double height = ((Number) shape.get("height")).doubleValue();

        boolean fill = shape.containsKey("fill") && (boolean) shape.get("fill");

        if (fill) {
            if (shape.containsKey("fillColorIndex")) {
                int colorIndex = ((Number) shape.get("fillColorIndex")).intValue();
                if (colorIndex >= 0 && colorIndex < colors.size()) {
                    gc.setFill(Color.web(colors.get(colorIndex)));
                } else {
                    gc.setFill(Color.web(colors.get(0))); // Default to first color
                }
            }
            gc.fillRect(x, y, width, height);
        } else {
            gc.strokeRect(x, y, width, height);
        }
    }

    private void drawFreehand(GraphicsContext gc, Map<String, Object> shape) {
        @SuppressWarnings("unchecked")
        List<Number> points = (List<Number>) shape.get("points");

        if (points != null && points.size() >= 4) {
            for (int i = 0; i < points.size() - 3; i += 2) {
                double x1 = points.get(i).doubleValue();
                double y1 = points.get(i + 1).doubleValue();
                double x2 = points.get(i + 2).doubleValue();
                double y2 = points.get(i + 3).doubleValue();

                gc.strokeLine(x1, y1, x2, y2);
            }
        }
    }

    /**
     * Clears the canvas
     */
    public void clear() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
    }
}