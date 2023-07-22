package cz.deznekcz.csl.osmeditor.data;

import java.util.List;

import cz.deznekcz.csl.osmeditor.data.config.Painter;

public class OSMRelationInfo {

	private List<Painter> painters;

	public OSMRelationInfo(List<Painter> painters) {
		this.painters = painters;
	}
	
	public List<Painter> getPainters() {
		return painters;
	}

}
