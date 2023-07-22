package cz.deznekcz.csl.osmeditor.data;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class OSMWay extends AOSMItem {

	private List<Long> nodes;

	public List<Long> getNodes() {
		return nodes;
	}
	
	public OSMWay(Element element) {
		super(element);
		
		nodes = new ArrayList<>();
		
		NodeList tags = element.getElementsByTagName("nd");
		int tagsCount = tags.getLength();
		
		for (int i = 0; i < tagsCount; i++) {
			Element tag = (Element) tags.item(i);
			this.nodes.add(Long.parseLong(tag.getAttribute("ref")));
		}
	}

	public int getLayer() {
		// TODO jinak
		return Integer.parseInt(getTags().getOrDefault("layer", "0"));
	}
}
