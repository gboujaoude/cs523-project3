package application.immune_model.Cells;

import engine.Circle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class BystanderCell extends Circle2D {
    public BystanderCell(double x, double y, double radiusX, double radiusY, double depth) {
        super(x, y, radiusX, radiusY, depth);
        this.setColor(Color.BEIGE);
    }

    @Override
    public void setWidthHeight(double radiusX, double radiusY) {
        super.setWidthHeight(radiusX, radiusY);
    }

    @Override
    public void render(GraphicsContext gc, double x, double y) {
        super.render(gc, x, y);
    }
}
