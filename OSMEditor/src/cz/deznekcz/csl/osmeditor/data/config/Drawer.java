package cz.deznekcz.csl.osmeditor.data.config;

import javafx.scene.paint.Color;

public class Drawer {
    private Color background;
    private Color foreground;
    private Color dashBackground;
    private double[] dashes;

    public Drawer() {
    }

    public Color getBackground() {
        return background;
    }

    public Color getForeground() {
        return foreground;
    }

    public boolean isDashed() {
        return dashes != null;
    }

    public double[] getDashes() {
        return dashes;
    }

    public static Drawer of(Color color) {
        Drawer drawer = new Drawer();
        drawer.background = color;
        drawer.foreground = color.darker();
        return drawer;
    }

    public static Drawer of(Color background, Color foreground) {
        Drawer drawer = new Drawer();
        drawer.background = background;
        drawer.foreground = foreground;
        return drawer;
    }

    public Drawer dashed(double... dashes) {
        this.dashes = dashes;
        return this;
    }

    public Drawer dashed(Color dashBackground, double... dashes) {
        this.dashBackground = dashBackground;
        this.dashes = dashes;
        return this;
    }

    public Drawer dashed(Drawer dashBackground, double... dashes) {
        this.dashBackground = dashBackground.getForeground();
        this.dashes = dashes;
        return this;
    }

    public Color getDashBackground() {
        return dashBackground;
    }

    public boolean hasDashedBackground() {
        return dashes != null;
    }
}
