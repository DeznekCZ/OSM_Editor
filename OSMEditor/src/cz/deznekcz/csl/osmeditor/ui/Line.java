package cz.deznekcz.csl.osmeditor.ui;

import cz.deznekcz.csl.osmeditor.data.OSM;
import cz.deznekcz.csl.osmeditor.data.OSMWay;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Line implements Painter {

	protected OSMWay way;
	protected Color fillColor;
	protected int size;
	protected double[] x;
	protected double[] y;

	public Line(OSMWay way, int size, Color fillColor) {
		this.way = way;
		this.size = size;
		this.fillColor = fillColor;
	}

	@Override
	public void consume(GraphicsContext gc, OSM map, Bounds bd) {
		calculatePoints(map, bd);

		gc.setStroke(fillColor);
		gc.setLineWidth(size);
		gc.strokePolyline(x, y, x.length);
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

}
