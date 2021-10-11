package cz.deznekcz.csl.osmeditor.ui;

import cz.deznekcz.csl.osmeditor.data.OSM;
import cz.deznekcz.csl.osmeditor.data.OSMWay;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ExtendedLine extends Line {

	@FunctionalInterface
	public static interface LineData {
		public void consume(double[] x, double[] y, GraphicsContext gc);
	}

	private LineData data;

	public ExtendedLine(OSMWay way, LineData data) {
		super(way, 0, Color.BLACK);
		this.data = data;
	}

	@Override
	public void consume(GraphicsContext gc, OSM map, Bounds bd) {
		calculatePoints(map, bd);
		data.consume(x, y, gc);
	}

}
