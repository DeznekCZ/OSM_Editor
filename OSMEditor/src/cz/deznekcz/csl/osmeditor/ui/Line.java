package cz.deznekcz.csl.osmeditor.ui;

import cz.deznekcz.csl.osmeditor.data.*;
import cz.deznekcz.csl.osmeditor.data.config.Drawer;
import cz.deznekcz.csl.osmeditor.data.config.Generator;
import cz.deznekcz.csl.osmeditor.data.config.Painter;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public class Line implements Painter {

	protected OSMWay way;
	protected Drawer fillColor;
	protected float size;
	protected double[] x;
	protected double[] y;
	protected boolean tunelable;
	private final boolean tunnel;

	public Line(OSMWay way, float size, Drawer fillColor) {
		this.way = way;
		this.size = size;
		this.fillColor = fillColor;
		this.tunelable = false;

		tunnel = OSMConfig.is(way.getTags(), "tunel", "yes");
	}

	@Override
	public void consume(GraphicsContext gc, OSM map, Bounds bd, boolean background) {
		if (background) return;

		var count = way.getNodes().size();
		x = new double[count];
		y = new double[count];

		var i = 0;
		for (var nodeIndex : way.getNodes()) {
			var innerNode = map.getNodes().get(nodeIndex);
			var point = Painter.GetPoint(innerNode, map, bd);

			x[i] = point.getX();
			y[i] = point.getY();

			i++;
		}

		if (tunelable && tunnel)
			gc.setStroke(fillColor.getForeground().interpolate(Color.TRANSPARENT, 0.5));
		else
			gc.setStroke(fillColor.getForeground());
		gc.setLineWidth(size);
		
		if (fillColor.isDashed()) {
			// has dash background
			if (fillColor.hasDashedBackground()) {
				var defaultStroke = gc.getStroke();
				var defaultCap = gc.getLineCap();
				gc.setStroke(fillColor.getDashBackground());
				gc.setLineCap(StrokeLineCap.BUTT);
				gc.strokePolyline(x, y, x.length);
				gc.setLineCap(defaultCap);
				gc.setStroke(defaultStroke);
			}

			var defaultDashes = gc.getLineDashes();
			var defaultCap = gc.getLineCap();
			gc.setLineCap(StrokeLineCap.BUTT);
			gc.setLineDashes(fillColor.getDashes());
			gc.strokePolyline(x, y, x.length);
			gc.setLineDashes(defaultDashes);
			gc.setLineCap(defaultCap);
		}
		else {
			gc.strokePolyline(x, y, x.length);
		}
	}

	public Line tunelable() {
		this.tunelable = true;
		return this;
	}

	public static LineGenerator of(float size, Drawer fillColor) {
		return (way) -> new Line(way, size, fillColor);
	}

	public interface LineGenerator extends Generator<OSMWay> {
		default LineGenerator tunelable() {
			return (way) -> ((Line)this.apply(way)).tunelable();
		}
	}
}
