package cz.deznekcz.csl.osmeditor.ui;

import cz.deznekcz.csl.osmeditor.data.OSM;
import cz.deznekcz.csl.osmeditor.data.OSMWay;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class Shape implements Painter {

	private OSMWay way;
	private Color borderColor;
	private Paint filament;

	public Shape(OSMWay way, Color fillColor) {
		this(way, fillColor, fillColor.darker());
	}

	public Shape(OSMWay way, Color fillColor, Color borderColor) {
		this(
			way,
			(Paint) fillColor.interpolate(
				Color.hsb(
						fillColor.getHue(),
						fillColor.getSaturation(),
						fillColor.getOpacity(), 0)
				, 0.5),
			borderColor
		);
	}
	
	public Shape(OSMWay way, Paint filament, Color borderColor) {
		this.way = way;
		this.borderColor = borderColor;
		this.filament = filament;
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
		
		gc.setFill(filament);
		gc.fillPolygon(x, y, y.length);
		
		gc.setStroke(borderColor);
		gc.setLineWidth(1);
		gc.strokePolyline(x, y, y.length);
	}

}
