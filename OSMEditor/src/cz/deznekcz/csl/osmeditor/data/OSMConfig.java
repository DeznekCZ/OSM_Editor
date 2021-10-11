package cz.deznekcz.csl.osmeditor.data;

import java.util.ArrayList;

import cz.deznekcz.csl.osmeditor.ui.ExtendedLine;
import cz.deznekcz.csl.osmeditor.ui.Line;
import cz.deznekcz.csl.osmeditor.ui.OSMNodeInfo;
import cz.deznekcz.csl.osmeditor.ui.OSMWayInfo;
import cz.deznekcz.csl.osmeditor.ui.Painter;
import cz.deznekcz.csl.osmeditor.ui.Road;
import cz.deznekcz.csl.osmeditor.ui.Zone;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public class OSMConfig {

	public OSMNodeInfo getInfo(OSMNode node) {
		if (node.getTags().isEmpty())
			return null;
		
		var painters = new ArrayList<Painter>();
		
		for (var nodeTag : node.getTags().keySet()) {
			switch (nodeTag) {
			case "power": {
				switch (node.getTags().get("power")) {
				case "pole":
					painters.add((gc,map,bd) -> {
						var point = Painter.GetPoint(node, map, bd);
						gc.setStroke(Color.BLACK);
						gc.setLineWidth(1);
						gc.strokeOval(point.getX() - 1, point.getY() - 1, 2, 2);
					});
					break;

				default:
					break;
				}
				break;
			}
			case "name": {
				painters.add((gc,map,bd) -> {
					var point = Painter.GetPoint(node, map, bd);
					gc.setStroke(Color.BLACK);
					gc.setLineWidth(1);
					gc.strokeText(node.getTags().get("name"), point.getX(), point.getY());
				});
				break;
			}
			default:
				break;
			}
		}
		
		if (painters.isEmpty())
			return null;
		
		return new OSMNodeInfo(painters);
	}

	public OSMWayInfo getInfo(OSMWay way) {
		if (way.getTags().isEmpty())
			return null;
		
		var painters = new ArrayList<Painter>();
		
		for (var nodeTag : way.getTags().keySet()) {
			switch (nodeTag) {
			case "power": {
				painters.add(new Line(way, 1, Color.BLACK));
				break;
			}
			case "railway": {
				painters.add(new ExtendedLine(
					way,
					(x,y,gc) -> {
						gc.setStroke(Color.GRAY);
						gc.setLineWidth(6);
						gc.setLineCap(StrokeLineCap.BUTT);
						gc.strokePolyline(x, y, x.length);

						gc.setStroke(Color.WHITE);
						gc.setLineWidth(4);
						gc.setLineCap(StrokeLineCap.ROUND);
						gc.strokePolyline(x, y, x.length);
						
						gc.setStroke(Color.BLACK);
						gc.setLineCap(StrokeLineCap.BUTT);
						gc.setLineWidth(4);
						var defaultDashes = gc.getLineDashes();
						gc.setLineDashes(new double[] {5,5,5});
						gc.strokePolyline(x, y, x.length);
						
						gc.setLineDashes(defaultDashes);
					}
				));
				break;
			}
			case "highway": {
				switch (way.getTags().get("highway")) {
				case "primary":
				case "primary_link":
					painters.add(new Road(way, 4, Color.ORANGE, Color.DARKORANGE));
					break;

				case "secondary":
				case "secondary_link":
					painters.add(new Road(way, 4, Color.YELLOW, Color.BROWN));
					break;

				case "tertiary":
				case "tertiary_link":
					painters.add(new Road(way, 4, Color.LIGHTYELLOW, Color.BROWN));
					break;
					
				case "unclassified":
				case "residential":
				case "service":
					painters.add(new Road(way, 4, Color.WHITE, Color.GRAY));
					break;
					
				default:
					break;
				}
				break;
			}
			default:
				break;
			}
		}
		
		if (painters.isEmpty())
			return null;
		
		return new OSMWayInfo(way.getLayer(), painters);
	}

	public OSMRelationInfo getInfo(OSMRelation relation) {
		if (relation.getTags().isEmpty())
			return null;
		
		var painters = new ArrayList<Painter>();
		
		for (var nodeTag : relation.getTags().keySet()) {
			switch (nodeTag) {
				case "landuse":
					switch (relation.getTags().get("landuse")) {
					case "forest":
						painters.add(new Zone(relation, Color.DARKGREEN, Color.DARKGREEN.darker()));
						break;

					case "meadow":
						painters.add(new Zone(relation, Color.YELLOWGREEN, Color.YELLOWGREEN.darker()));
						break;
						
					case "farmland":
						painters.add(new Zone(relation, Color.YELLOW, Color.YELLOW.darker()));
						break;

					default:
						break;
					}
					break;
					
				case "natural":
					switch (relation.getTags().get("natural")) {
					case "water":
						painters.add(new Zone(relation, Color.LIGHTSKYBLUE, Color.LIGHTSKYBLUE.darker()));
						break;

					default:
						break;
					}
			
				default:
					break;
			}
		}
		
		if (painters.isEmpty())
			return null;
		
		return new OSMRelationInfo(painters);
	}
}
