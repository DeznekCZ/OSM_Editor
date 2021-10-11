package cz.deznekcz.csl.osmeditor.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class OSM {

	private double minlat;
	private double maxlat;
	private double minlon;
	private double maxlon;

	public static OSM Load(File osmDataFile) {
		OSM osm = new OSM();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			Document doc = db.parse(osmDataFile);
			
			NodeList nodes = doc.getFirstChild().getChildNodes();
			final int count = nodes.getLength();
			for (int i = 0; i < count; i++) {
				Node node = nodes.item(i);
				if (node instanceof Element element) {
					switch (element.getNodeName()) {
					case "node": {
						long id = Long.parseLong(element.getAttribute("id"));
						osm.getNodes().put(id, new OSMNode(element));
						break;
					}
					case "way": {
						long id = Long.parseLong(element.getAttribute("id"));
						osm.getWays().put(id, new OSMWay(element));
						break;
					}
					case "relation": {
						long id = Long.parseLong(element.getAttribute("id"));
						osm.getRelations().put(id, new OSMRelation(element));
						break;
					}
					case "note": {
						System.out.println(element.getTextContent());
						break;
					}
					case "meta": {
						System.out.println("Generation date: " + element.getAttribute("osm_base"));
						break;
					}
					case "bounds": {
						osm.minlat = Double.parseDouble(element.getAttribute("minlat"));
						osm.maxlat = Double.parseDouble(element.getAttribute("maxlat"));
						osm.minlon = Double.parseDouble(element.getAttribute("minlon"));
						osm.maxlon = Double.parseDouble(element.getAttribute("maxlon"));
						break;
					}
					default:
						throw new IllegalArgumentException("Unexpected value: " + element.getNodeName());
					}
				}
			}
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		return osm;
	}

	private Map<Long,OSMNode> nodes;
	private Map<Long,OSMWay> ways;
	private Map<Long,OSMRelation> relations;
	
	public OSM() {
		nodes = new HashMap<>();
		ways = new HashMap<>();
		relations = new HashMap<>();
	}

	public Map<Long,OSMNode> getNodes() {
		return nodes;
	}
	
	public Map<Long,OSMWay> getWays() {
		return ways;
	}

	public Map<Long,OSMRelation> getRelations() {
		return relations;
	}
	
	public double getMaxlat() {
		return maxlat;
	}
	
	public double getMaxlon() {
		return maxlon;
	}
	
	public double getMinlat() {
		return minlat;
	}
	
	public double getMinlon() {
		return minlon;
	}
	
	@Override
	public String toString() {
		return String.format(
				"OSM map data, Bounds: <%f,%f,%f,%f>, Nodes: %l, Ways: %l, Rels: %l", 
				minlat, maxlat, minlon, maxlon, nodes.size(), ways.size(), relations.size()
				);
	}
}
