package cz.deznekcz.csl.osmeditor.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import cz.deznekcz.csl.osmeditor.ui.ExtendedLine;
import cz.deznekcz.csl.osmeditor.ui.Line;
import cz.deznekcz.csl.osmeditor.ui.OSMNodeInfo;
import cz.deznekcz.csl.osmeditor.ui.OSMWayInfo;
import cz.deznekcz.csl.osmeditor.ui.Painter;
import cz.deznekcz.csl.osmeditor.ui.Road;
import cz.deznekcz.csl.osmeditor.ui.Zone;
import cz.deznekcz.csl.osmeditor.ui.Shape;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Pair;

public class OSMConfig {

	private Map<String, Map<String, Function<OSMRelation, Painter>[]>> relationConfig;
	private Map<String, Map<String, Function<OSMWay, Painter>[]>> wayConfig;
	private Map<String, Map<String, Function<OSMNode, Painter>[]>> nodeConfig;
	
	public OSMConfig() {
		relationConfig = filter(
				group("landuse", 
						entry("forest", (relation) -> new Zone(relation, Color.DARKGREEN)),
						entry("meadow", (relation) -> new Zone(relation, Color.YELLOWGREEN)),
						entry("farmland", (relation) -> new Zone(relation, Color.YELLOW))
				),
				group("natural", 
						entry("water", (relation) -> new Zone(relation, Color.LIGHTSKYBLUE)),
						entrySame("pond")
				)
		);
		
		wayConfig = filter(
				group("highway",
						entry("primary", (way) -> new Road(way, 6, Color.ORANGE)),
						entrySame("primary_link"),
						entry("secondary", (way) -> new Road(way, 5, Color.YELLOW)),
						entrySame("secondary_link"),
						entry("tertiary", (way) -> new Road(way, 4, Color.LIGHTYELLOW)),
						entrySame("tertiary_link"),
						entry("unclassified", (way) -> new Road(way, 4, Color.WHITE)),
						entrySame("residential"),
						entrySame("service"),
						entry("track", (way) -> new Line(way, 1, Color.BROWN).dashed(2,1))
				),
				group("railway",
						entry("rail", (way) -> new ExtendedLine(
								way,
								(x,y,gc) -> {
									if (is(way.getTags(), "bridge", "yes"))
									{
										gc.setStroke(Color.GRAY);
										gc.setLineWidth(4);
										gc.setLineCap(StrokeLineCap.BUTT);
										gc.strokePolyline(x, y, x.length);
									}
									
									gc.setStroke(Color.GRAY);
									gc.setLineWidth(3);
									gc.setLineCap(StrokeLineCap.BUTT);
									gc.strokePolyline(x, y, x.length);

									gc.setStroke(Color.WHITE);
									gc.setLineWidth(2);
									gc.setLineCap(StrokeLineCap.ROUND);
									gc.strokePolyline(x, y, x.length);
									
									gc.setStroke(Color.BLACK);
									gc.setLineCap(StrokeLineCap.BUTT);
									gc.setLineWidth(2);
									var defaultDashes = gc.getLineDashes();
									gc.setLineDashes(new double[] {5,5,5});
									gc.strokePolyline(x, y, x.length);
									
									gc.setLineDashes(defaultDashes);
								}
							)),
						entry("platform", (way) -> new Line(way, 1, Color.GRAY)),
						entry("disused", (way) -> new ExtendedLine(
								way,
								(x,y,gc) -> {
									gc.setStroke(Color.GRAY);
									gc.setLineWidth(3);
									gc.setLineCap(StrokeLineCap.BUTT);
									gc.strokePolyline(x, y, x.length);

									gc.setStroke(Color.WHITE);
									gc.setLineWidth(2);
									gc.setLineCap(StrokeLineCap.ROUND);
									gc.strokePolyline(x, y, x.length);
									
									gc.setStroke(Color.GRAY.interpolate(Color.GREEN, 0.5));
									gc.setLineCap(StrokeLineCap.BUTT);
									gc.setLineWidth(2);
									var defaultDashes = gc.getLineDashes();
									gc.setLineDashes(new double[] {5,5,5});
									gc.strokePolyline(x, y, x.length);
									
									gc.setLineDashes(defaultDashes);
								}
							)),
						entrySame("dismantled")
				),
				group("power",
						entry("line", (way) -> new Line(way, 2, Color.BLACK.interpolate(Color.TRANSPARENT, 0.5))),
						entry("minor_line", (way) -> new Line(way, 1, Color.BLACK.interpolate(Color.TRANSPARENT, 0.5)))
				),
				groupAny("building",
						(way) -> new Shape(way, Color.RED.brighter())
				),
				group("waterway",
						entry("river",	(way) -> new Line(way, 3, Color.LIGHTSKYBLUE.darker()).tunelable()),
						entry("stream",	(way) -> new Line(way, 2, Color.LIGHTSKYBLUE.darker()).tunelable()),
						entrySame("canal"),
						entry("ditch", (way) -> new Line(way, 1, Color.LIGHTSKYBLUE.darker()).tunelable())
				),
				group("natural", 
						entry("water", (way) -> new Shape(way, Color.LIGHTSKYBLUE))
				),
				group("landuse", 
						entry("forest", (way) -> new Shape(way, Color.DARKGREEN)),
						entry("meadow", (way) -> new Shape(way, Color.YELLOWGREEN)),
						entry("farmland", (way) -> new Shape(way, Color.YELLOW))
				)
		);
		
		nodeConfig = filter(
				group("place", 
						entry("village", (node) -> (gc,map,bd) -> {
							var point = Painter.GetPoint(node, map, bd);
							var name = node.getTags().getOrDefault("name", "<UNSET>");
							gc.setStroke(Color.BLACK);
							gc.setLineWidth(1);
							gc.strokeText(name, point.getX(), point.getY());
						})
				),
				group("power",
						entry("pole", (node) -> (gc,map,bd) -> {
							var point = Painter.GetPoint(node, map, bd);
							gc.setStroke(Color.BLACK);
							gc.setLineWidth(1);
							gc.strokeOval(point.getX() - 1, point.getY() - 1, 2, 2);
						})
				)
		);
	}

	public static boolean is(Map<String, String> tags, String key, String string) {
		return string.equals(tags.getOrDefault(key, null));
	}

	public OSMNodeInfo getInfo(OSMNode node) {
		if (node.getTags().isEmpty())
			return null;
		
		var painters = new ArrayList<Painter>();
		
		get(nodeConfig, node, painters);
		
		if (painters.isEmpty())
			return null;
		
		return new OSMNodeInfo(painters);
	}

	public OSMWayInfo getInfo(OSMWay way) {
		if (way.getTags().isEmpty())
			return null;
		
		var painters = new ArrayList<Painter>();

		get(wayConfig, way, painters);
		
		if (painters.isEmpty())
			return null;
		
		return new OSMWayInfo(way.getLayer(), painters);
	}

	public OSMRelationInfo getInfo(OSMRelation relation) {
		if (relation.getTags().isEmpty())
			return null;
		
		var painters = new ArrayList<Painter>();

		get(relationConfig, relation, painters);
		
		if (painters.isEmpty())
			return null;
		
		return new OSMRelationInfo(painters);
	}

	public <T extends AOSMItem> void get(Map<String, Map<String, Function<T, Painter>[]>> config, T relation,
			ArrayList<Painter> painters) {
		
		for (var key : config.keySet()) {
			String group;
			if ((group = relation.getTags().getOrDefault(key, null)) != null) {
				Function<T, Painter>[] entry;
				var keyConfig = config.get(key);
				if ((entry = keyConfig.getOrDefault(group, null)) != null) {
					for (var painter : entry) {
						painters.add(painter.apply(relation));
					}
				} else if ((entry = keyConfig.getOrDefault("*", null)) != null) {
					for (var painter : entry) {
						painters.add(painter.apply(relation));
					}
				}
			}
		}
	}

	@SafeVarargs
	public static <T extends AOSMItem> Map<String, Map<String, Function<T, Painter>[]>> filter(Pair<String, Map<String, Function<T,Painter>[]>>...groups) {
		Map<String, Map<String, Function<T,Painter>[]>> map = new HashMap<>();
		
		for (var group : groups) {
			map.put(group.getKey(), group.getValue());
		}
		
		return map;
	}

	@SafeVarargs
	public static <T extends AOSMItem> Pair<String, Map<String, Function<T,Painter>[]>> group(String string, Pair<String, Function<T, Painter>[]>...entries) {
		Map<String, Function<T,Painter>[]> map = new HashMap<>();
		
		Function<T, Painter>[] last = null;
		for (var entry : entries) {
			Function<T, Painter>[] newValue = entry.getValue();
			
			if (newValue != null)
				last = newValue;
			
			map.put(entry.getKey(), last);
			
		}
		
		return new Pair<>(string, map);
	}

	@SafeVarargs
	public static <T extends AOSMItem> Pair<String, Map<String, Function<T,Painter>[]>> groupAny(String string, Function<T, Painter>...generators) {
		Map<String, Function<T,Painter>[]> map = new HashMap<>();
		
		map.put("*", generators);
		
		return new Pair<>(string, map);
	}

	@SafeVarargs
	public static <T extends AOSMItem> Pair<String, Function<T,Painter>[]> entry(String value, Function<T,Painter>...generators) {
		return new Pair<>(value, generators);
	}
	
	public static <T extends AOSMItem> Pair<String, Function<T,Painter>[]> entrySame(String value) {
		return new Pair<>(value, null);
	}
}
