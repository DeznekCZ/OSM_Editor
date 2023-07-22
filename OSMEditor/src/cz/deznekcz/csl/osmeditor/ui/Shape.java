package cz.deznekcz.csl.osmeditor.ui;

import cz.deznekcz.csl.osmeditor.data.*;
import cz.deznekcz.csl.osmeditor.data.config.Drawer;
import cz.deznekcz.csl.osmeditor.data.config.Generator;
import cz.deznekcz.csl.osmeditor.data.config.Painter;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class Shape implements Painter {

	private OSMWay way;
	private Color borderColor;
	private Paint filament;

	public Shape(OSMWay way, Drawer fillColor) {
		this(
			way,
			fillColor.getForeground().interpolate(
				Color.hsb(
						fillColor.getForeground().getHue(),
						fillColor.getForeground().getSaturation(),
						fillColor.getForeground().getOpacity(), 0)
				, 0.5),
			fillColor.getBackground()
		);
	}
	
	public Shape(OSMWay way, Paint filament, Color borderColor) {
		this.way = way;
		this.borderColor = borderColor;
		this.filament = filament;
	}

	@Override
	public void consume(GraphicsContext gc, OSM map, Bounds bd, boolean background) {
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

		if (background) {
			gc.setFill(filament);
			gc.fillPolygon(x, y, y.length);
		} else {
			gc.setStroke(borderColor);
			gc.setLineWidth(1);
			gc.strokePolyline(x, y, y.length);
		}
	}

	public static Generator<OSMWay> of(Drawer drawer) {
		return (way) -> new Shape(way, drawer);
	}
}
