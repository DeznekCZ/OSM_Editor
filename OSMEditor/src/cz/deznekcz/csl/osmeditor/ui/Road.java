package cz.deznekcz.csl.osmeditor.ui;

import cz.deznekcz.csl.osmeditor.data.*;
import cz.deznekcz.csl.osmeditor.data.config.Drawer;
import cz.deznekcz.csl.osmeditor.data.config.Generator;
import cz.deznekcz.csl.osmeditor.data.config.Painter;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public class Road implements Painter {
	private final boolean bridge;
	private final boolean tunnel;
	private float size;
	private float outerSize;
	private Drawer fillColor;
	private OSMWay way;

	public Road(OSMWay way, float size, boolean applyWidth, Drawer fillColor) {
		this(way, applyWidth ? (way.getIntTag("lanes", 2) * size) : size, fillColor);
	}
	
	public Road(OSMWay way, float size, Drawer fillColor) {
		this.size = size;
		this.outerSize = size + 2;
		this.fillColor = fillColor;
		
		this.way = way;
		this.bridge = OSMConfig.is(way.getTags(), "bridge", "yes");
		this.tunnel = OSMConfig.is(way.getTags(), "tunnel", "yes");
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
			gc.setStroke(fillColor.getBackground());
			gc.setLineWidth(outerSize);
			gc.setLineCap(StrokeLineCap.BUTT);
			gc.strokePolyline(x, y, x.length);
			if (tunnel) {
				gc.setStroke(fillColor.getForeground());
				gc.setLineWidth(size);
				gc.setLineCap(StrokeLineCap.BUTT);
				gc.strokePolyline(x, y, x.length);
			}
		} else {
			if (bridge) {
				gc.setStroke(Color.GRAY);
				gc.setLineWidth(outerSize+2);
				gc.setLineCap(StrokeLineCap.BUTT);
				gc.strokePolyline(x, y, x.length);
				gc.setStroke(fillColor.getBackground());
				gc.setLineWidth(outerSize);
				gc.setLineCap(StrokeLineCap.BUTT);
				gc.strokePolyline(x, y, x.length);
			}
			if (!tunnel) {
				gc.setStroke(fillColor.getForeground());
				gc.setLineWidth(size);
				gc.setLineCap(StrokeLineCap.BUTT);
				gc.strokePolyline(x, y, x.length);
			}
		}
	}

	public static Generator<OSMWay> of(float size, boolean applyWidth, Drawer fillColor) {
		return (way) -> new Road(way, size, applyWidth, fillColor);
	}

	public static Generator<OSMWay> of(float size, Drawer fillColor) {
		return (way) -> new Road(way, size, fillColor);
	}
}
