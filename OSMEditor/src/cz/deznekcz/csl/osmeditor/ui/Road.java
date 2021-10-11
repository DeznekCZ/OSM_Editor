package cz.deznekcz.csl.osmeditor.ui;

import cz.deznekcz.csl.osmeditor.data.OSM;
import cz.deznekcz.csl.osmeditor.data.OSMWay;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public class Road implements Painter {

	private int size;
	private int outerSize;
	private Color fillColor;
	private Color borderColor;
	private OSMWay way;

	public Road(OSMWay way, int size, Color fillColor, Color borderColor) {
		this.size = size;
		this.outerSize = size + 2;
		this.fillColor = fillColor;
		this.borderColor = borderColor;
		
		this.way = way;
	}

	@Override
	public void consume(GraphicsContext gc, OSM map, Bounds bd) {
		var count = way.getNodes().size();
		var x = new double[count];
		var y = new double[count];
		
		var i = 0;
		for (var nodeIndex : way.getNodes()) {
			var innerNode = map.getNodes().get(nodeIndex);
			var point = Painter.GetPoint(innerNode, map, bd);

			x[i] = point.getX();
			y[i] = point.getY();
					
			i++;
		}

		gc.setStroke(borderColor);
		gc.setLineWidth(outerSize);
		gc.setLineCap(StrokeLineCap.BUTT);
		gc.strokePolyline(x, y, x.length);

		gc.setStroke(fillColor);
		gc.setLineWidth(size);
		gc.setLineCap(StrokeLineCap.ROUND);
		gc.strokePolyline(x, y, x.length);
	}

}
