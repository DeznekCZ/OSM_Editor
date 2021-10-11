package cz.deznekcz.csl.osmeditor.data;

import org.w3c.dom.Element;

public class OSMNode extends AOSMItem {

	private double lat;
	private double lon;

	public OSMNode(Element element) {
		super(element);

		lat = Double.parseDouble(element.getAttribute("lat"));
		lon = Double.parseDouble(element.getAttribute("lon"));
	}

	public double getLat() {
		return lat;
	}
	
	public double getLon() {
		return lon;
	}
}
