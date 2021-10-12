package cz.deznekcz.csl.osmeditor.ui;

import cz.deznekcz.csl.osmeditor.data.OSM;
import cz.deznekcz.csl.osmeditor.data.OSMConfig;
import cz.deznekcz.csl.osmeditor.data.OSMWay;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public class Line implements Painter {

	protected OSMWay way;
	protected Color fillColor;
	protected int size;
	protected double[] x;
	protected double[] y;
	protected double[] dash;
	protected boolean tunelable;

	public Line(OSMWay way, int size, Color fillColor) {
		this.way = way;
		this.size = size;
		this.fillColor = fillColor;
		this.dash = null;
		this.tunelable = false;
	}

	@Override
	public void consume(GraphicsContext gc, OSM map, Bounds bd) {
		calculatePoints(map, bd);

		if (tunelable && OSMConfig.is(way.getTags(), "tunel", "yes"))
			gc.setStroke(fillColor.interpolate(Color.TRANSPARENT, 0.5));
		else
			gc.setStroke(fillColor);
		gc.setLineWidth(size);
		
		if (dash != null) {
			var defaultDashes = gc.getLineDashes();
			var defaultCap = gc.getLineCap();
			gc.setLineCap(StrokeLineCap.BUTT);
			gc.setLineDashes(new double[] {5,5,5});
			gc.strokePolyline(x, y, x.length);
			gc.setLineDashes(defaultDashes);
			gc.setLineCap(defaultCap);
		}
		else {
			gc.strokePolyline(x, y, x.length);
		}
	}

	protected void calculatePoints(OSM map, Bounds bd) {
		
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
	}

	public Line dashed(double...dash) {
		this.dash = dash;
		return this;
	}

	public Line tunelable() {
		this.tunelable = true;
		return this;
	}

}
