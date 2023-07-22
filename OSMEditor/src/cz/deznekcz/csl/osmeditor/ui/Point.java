package cz.deznekcz.csl.osmeditor.ui;

import cz.deznekcz.csl.osmeditor.data.config.Drawer;
import cz.deznekcz.csl.osmeditor.data.config.Generator;
import cz.deznekcz.csl.osmeditor.data.OSM;
import cz.deznekcz.csl.osmeditor.data.OSMNode;
import cz.deznekcz.csl.osmeditor.data.config.Painter;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;

public class Point implements Painter {

    private final OSMNode node;
    private final Drawer paint;

    public Point(OSMNode node, Drawer paint) {
        this.node = node;
        this.paint = paint;
    }

    @Override
    public void consume(GraphicsContext gc, OSM map, Bounds bd, boolean background) {
        if (background) return;
        var point = Painter.GetPoint(node, map, bd);
        gc.setStroke(paint.getForeground());
        gc.setLineWidth(1);
        gc.strokeOval(point.getX() - 1, point.getY() - 1, 2, 2);
    }

    public static Generator<OSMNode> of(Drawer drawer) {
        return (node) -> new Point(node, drawer);
    }
}
