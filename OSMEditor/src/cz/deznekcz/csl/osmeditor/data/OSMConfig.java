package cz.deznekcz.csl.osmeditor.data;

import cz.deznekcz.csl.osmeditor.data.config.*;
import cz.deznekcz.csl.osmeditor.ui.*;
import javafx.scene.paint.Color;

import java.util.*;

public class OSMConfig {

    private final IFilter<OSMRelation> relationConfig;
    private final IFilter<OSMWay> wayConfig;
    private final IFilter<OSMNode> nodeConfig;

    public class Colors {
        public static final Drawer AREA_FOREST = Drawer.of(Color.DARKGREEN);
        public static final Drawer AREA_MEADOW = Drawer.of(Color.YELLOWGREEN);
        public static final Drawer AREA_FARMLAND = Drawer.of(Color.YELLOW);
        public static final Drawer AREA_BUILDING = Drawer.of(Color.RED.brighter());
        public static final Drawer AREA_WATER = Drawer.of(Color.LIGHTSKYBLUE);
        public static final Drawer LINE_WATER = Drawer.of(Color.LIGHTSKYBLUE.darker());
        public static final Drawer LINE_POWER = Drawer.of(Color.BLACK.interpolate(Color.TRANSPARENT, 0.5));
        public static final Drawer LINE_ROAD_MOTORWAY = Drawer.of(Color.PINK.darker());
        public static final Drawer LINE_ROAD_TRUNK = Drawer.of(Color.RED.darker());
        public static final Drawer LINE_ROAD_PRIMARY = Drawer.of(Color.ORANGE);
        public static final Drawer LINE_ROAD_SECONDARY = Drawer.of(Color.YELLOW);
        public static final Drawer LINE_ROAD_TERTIARY = Drawer.of(Color.LIGHTYELLOW);
        public static final Drawer LINE_ROAD_COMMON = Drawer.of(Color.WHITE);
        public static final Drawer LINE_TRACK = Drawer.of(Color.BROWN);
        public static final Drawer LINE_RAIL = Drawer.of(Color.BLACK).dashed(Color.WHITE, 5, 5, 5);
        public static final Drawer LINE_RAIL_DISUSED = Drawer.of(Color.GRAY.interpolate(Color.GREEN, 0.5))
                .dashed(Color.WHITE.interpolate(Color.WHITE, 0.5), 5, 5, 5);
        public static final Drawer LINE_PLATFORM = Drawer.of(Color.GRAY);
        public static final Drawer NODE_POWER = Drawer.of(Color.BLACK);
    }

    public OSMConfig() {
        relationConfig = IFilter.of(
                IGroup.of("landuse",
                        IEntry.of("forest", Zone.of(Colors.AREA_FOREST)),
                        IEntry.of("meadow", Zone.of(Colors.AREA_MEADOW)),
                        IEntry.of("farmland", Zone.of(Colors.AREA_FARMLAND))
                ),
                IGroup.of("natural",
                        IEntry.of(List.of("water", "pond"), Zone.of(Colors.AREA_WATER))
                )
        );

        wayConfig = IFilter.of(
                IGroup.of("highway",
                        IEntry.of(List.of("motorway", "motorway_link"), Road.of(3f, true, Colors.LINE_ROAD_MOTORWAY)),
                        IEntry.of(List.of("trunk", "trunk_link"), Road.of(2.5f, true, Colors.LINE_ROAD_TRUNK)),
                        IEntry.of(List.of("primary", "primary_link"), Road.of(2, true, Colors.LINE_ROAD_PRIMARY)),
                        IEntry.of(List.of("secondary", "secondary_link"), Road.of(2, true, Colors.LINE_ROAD_SECONDARY)),
                        IEntry.of(List.of("tertiary", "tertiary_link"), Road.of(3.5f, Colors.LINE_ROAD_TERTIARY)),
                        IEntry.of(List.of("unclassified", "residential", "service"), Road.of(4, Colors.LINE_ROAD_COMMON))
                                .when(IEntry.isStringTag("service", "emergency_access"),
                                        Line.of(1, Colors.LINE_ROAD_COMMON.dashed(1, 1))),
                        IEntry.of("track", Line.of(1, Colors.LINE_TRACK.dashed(2, 1)))
                ),
                IGroup.of("railway",
                        IEntry.of("rail", Road.of(2, Colors.LINE_RAIL)),
                        IEntry.of(List.of("disused", "dismantled"), Road.of(2, Colors.LINE_RAIL_DISUSED)),
                        IEntry.of("platform", Road.of(1, Colors.LINE_PLATFORM))
                ),
                IGroup.of("power",
                        IEntry.of("line", Line.of(2, Colors.LINE_POWER)),
                        IEntry.of("minor_line", Line.of(1, Colors.LINE_POWER))
                ),
                IGroup.of("building", Shape.of(Colors.AREA_BUILDING)),
                IGroup.of("waterway",
                        IEntry.of("river", Line.of(3, Colors.LINE_WATER).tunelable()),
                        IEntry.of(List.of("stream", "canal"), Line.of(2, Colors.LINE_WATER).tunelable()),
                        IEntry.of("ditch", Line.of(1, Colors.LINE_WATER).tunelable())
                ),
                IGroup.of("natural",
                        IEntry.of("water", Shape.of(Colors.AREA_WATER))
                ),
                IGroup.of("landuse",
                        IEntry.of("forest", Shape.of(Colors.AREA_FOREST)),
                        IEntry.of("meadow", Shape.of(Colors.AREA_MEADOW)),
                        IEntry.of("farmland", Shape.of(Colors.AREA_FARMLAND))
                )
        );

        nodeConfig = IFilter.of(
                IGroup.of("place",
                        IEntry.of("village", (node) -> (gc, map, bd, bg) -> {
                            if (bg) return;
                            var point = Painter.GetPoint(node, map, bd);
                            var name = node.getStringTag("name", "<UNSET>");
                            gc.setStroke(Color.BLACK);
                            gc.setLineWidth(1);
                            gc.strokeText(name, point.getX(), point.getY());
                        })
                ),
                IGroup.of("power",
                        IEntry.of("pole", Point.of(Colors.NODE_POWER))
                )
        );
    }

    public static boolean is(Map<String, String> tags, String key, String string) {
        return string.equals(tags.getOrDefault(key, null));
    }

    public OSMNodeInfo getInfo(OSMNode node) {
        if (node.getTags().isEmpty())
            return null;

        var painters = nodeConfig.apply(node);

        if (painters.isEmpty())
            return null;

        return new OSMNodeInfo(painters);
    }

    public OSMWayInfo getInfo(OSMWay way) {
        if (way.getTags().isEmpty())
            return null;

        var painters = wayConfig.apply(way);

        if (painters.isEmpty())
            return null;

        return new OSMWayInfo(way.getLayer(), painters);
    }

    public OSMRelationInfo getInfo(OSMRelation relation) {
        if (relation.getTags().isEmpty())
            return null;

        var painters = relationConfig.apply(relation);

        if (painters.isEmpty())
            return null;

        return new OSMRelationInfo(painters);
    }
}
