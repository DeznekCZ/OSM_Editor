package cz.deznekcz.csl.osmeditor.ui;

import java.util.List;

public class OSMNodeInfo {

	private List<Painter> painters;

	public OSMNodeInfo(List<Painter> painters) {
		this.painters = painters;
	}
	
	public List<Painter> getPainters() {
		return painters;
	}

}
