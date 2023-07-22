package cz.deznekcz.csl.osmeditor.ui;

import java.util.ArrayList;
import java.util.List;

import cz.deznekcz.csl.osmeditor.data.config.Drawer;
import cz.deznekcz.csl.osmeditor.data.config.Generator;
import cz.deznekcz.csl.osmeditor.data.OSM;
import cz.deznekcz.csl.osmeditor.data.OSMRelation;
import cz.deznekcz.csl.osmeditor.data.config.Painter;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
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
			drawBackground(gc, map, bd);
		} else {
			drawForeground(gc, map, bd);
		}
	}

	private void drawBackground(GraphicsContext gc, OSM map, Bounds bd) {
		List<Double> lon = new ArrayList<>();
		List<Double> lat = new ArrayList<>();

		for (var member : relation.getMembers()) {
			switch (member.getType()) {
				case "way":
					switch (member.getRole()) {
						case "outer":
							// collect all points
							map.getWays().get(member.getRef())
								.getNodes()
									.stream()
									.map(map.getNodes()::get)
									.forEach(node -> {
										lon.add(node.getLon());
										lat.add(node.getLat());
									});
							break;
						default:
							break;
					}
					break;

				default:
					break;
			}

		}

		double[] xa = new double[lon.size()];
		double[] ya = new double[lat.size()];

		for (int i = 0; i < lon.size(); i++) {
			var mapWidth = map.getMaxlon() - map.getMinlon();
			var mapHeight = map.getMaxlat() - map.getMinlat();
			var posX = lon.get(i) - map.getMinlon();
			var posY = lat.get(i) - map.getMinlat();
			xa[i] = bd.getWidth() * posX / mapWidth;
			ya[i] = bd.getHeight() - bd.getHeight() * posY / mapHeight;
		}

		gc.setFill(filament);
		gc.fillPolygon(xa, ya, lat.size());
	}

	private void drawForeground(GraphicsContext gc, OSM map, Bounds bd) {
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
