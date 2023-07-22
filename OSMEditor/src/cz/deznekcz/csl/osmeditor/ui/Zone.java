package cz.deznekcz.csl.osmeditor.ui;

import java.util.ArrayList;
import java.util.List;

import cz.deznekcz.csl.osmeditor.data.config.Drawer;
import cz.deznekcz.csl.osmeditor.data.config.Generator;
import cz.deznekcz.csl.osmeditor.data.OSM;
import cz.deznekcz.csl.osmeditor.data.OSMRelation;
import cz.deznekcz.csl.osmeditor.data.config.Painter;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class Zone implements Painter {

	private OSMRelation relation;
	private Color borderColor;
	private Paint filament;

	public Zone(OSMRelation relation, Drawer fillColor) {
		this(relation, fillColor.getForeground(), fillColor.getBackground());
	}

	public Zone(OSMRelation relation, Color fillColor, Color borderColor) {
		this(
			relation,
			(Paint) fillColor.interpolate(
				Color.hsb(
						fillColor.getHue(),
						fillColor.getSaturation(),
						fillColor.getOpacity(), 0)
				, 0.5),
			borderColor
		);
	}
	
	public Zone(OSMRelation relation, Paint filament, Color borderColor) {
		this.relation = relation;
		this.borderColor = borderColor;
		this.filament = filament;
	}

	public static Generator<OSMRelation> of(Drawer drawer) {
		return (relation) -> new Zone(relation, drawer);
	}

	@Override
	public void consume(GraphicsContext gc, OSM map, Bounds bd, boolean background) {

		if (background) {
			List<Double> x = new ArrayList<>();
			List<Double> y = new ArrayList<>();

			for (var member : relation.getMembers()) {
				switch (member.getType()) {
					case "way":
						switch (member.getRole()) {
							case "outer":
								var way = map.getWays().get(member.getRef());
								if (way != null) {
									var line = new Line(way, 1, Drawer.of(this.borderColor));
									line.calculatePoints(map, bd);
									for (var i : line.x) x.add(i);
									for (var i : line.y) y.add(i);
								}
								break;
							default:
								break;
						}
						break;

					default:
						break;
				}

			}

			double[] xa = new double[x.size()];
			double[] ya = new double[y.size()];

			int n;
			n = 0; for (var i : x) xa[n++] = i;
			n = 0; for (var i : y) ya[n++] = i;

			gc.setFill(filament);
			gc.fillPolygon(xa, ya, y.size());
		} else {
			for (var member : relation.getMembers()) {
				switch (member.getType()) {
					case "way":
						switch (member.getRole()) {
							case "outer":
								var way = map.getWays().get(member.getRef());
								if (way != null) {
									var line = new Line(way, 1, Drawer.of(this.borderColor));
									line.consume(gc, map, bd, false);
								}
								break;
							default:
								break;
						}
						break;

					default:
						break;
				}
			}
		}
	}

}
