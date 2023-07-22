package cz.deznekcz.csl.osmeditor.ui;

import cz.deznekcz.csl.osmeditor.data.config.Painter;

import java.util.List;

public class OSMWayInfo {

	private List<Painter> painters;
	private int layer;

	public int getLayer() {
		return layer;
	}

	public OSMWayInfo(int layer, List<Painter> painters) {
		this.layer = layer;
		this.painters = painters;
	}
	
	public List<Painter> getPainters() {
		return painters;
	}

}
