package cz.deznekcz.csl.osmeditor.ui;

import cz.deznekcz.csl.osmeditor.data.OSM;
import cz.deznekcz.csl.osmeditor.data.OSMNode;
import cz.deznekcz.csl.osmeditor.util.TriConsumer;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

@FunctionalInterface
public interface Painter extends TriConsumer<GraphicsContext, OSM, Bounds> {

	static Point2D GetPoint(OSMNode node, OSM map, Bounds bd) {
		var mapWidth = map.getMaxlon() - map.getMinlon();
		var mapHeight = map.getMaxlat() - map.getMinlat();
		var posX = node.getLon() - map.getMinlon();
		var posY = node.getLat() - map.getMinlat();
		var rX = bd.getWidth() * posX / mapWidth;
		var rY = bd.getHeight() - bd.getHeight() * posY / mapHeight;
		return new Point2D(rX, rY);
	}
	
}
