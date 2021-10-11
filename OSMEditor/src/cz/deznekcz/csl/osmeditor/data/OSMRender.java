package cz.deznekcz.csl.osmeditor.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.deznekcz.csl.osmeditor.ui.OSMNodeInfo;
import cz.deznekcz.csl.osmeditor.ui.OSMWayInfo;
import javafx.stage.FileChooser.ExtensionFilter;

public class OSMRender {

	private List<OSMNodeInfo> nodeLayer;
	private Map<Integer, List<OSMWayInfo>> wayLayers;
	private List<OSMRelationInfo> relationLayer;
	private int topLayerIndex;
	private int lowLayerIndex;
	
	public OSMRender() {
		nodeLayer = new ArrayList<>();
		wayLayers = new HashMap<>();
		relationLayer = new ArrayList<>();
		lowLayerIndex = 0;
		topLayerIndex = 0;
	}

	public List<OSMNodeInfo> getNodeLayer() {
		return nodeLayer;
	}

	public List<OSMWayInfo> getWayLayer(int layer) {
		return getWayLayer(layer, true);
	}
	
	public List<OSMWayInfo> getWayLayer(int layer, boolean create) {
		var wayLayer = wayLayers.getOrDefault(layer, null);
		
		if (wayLayer == null && create) {
			topLayerIndex = topLayerIndex < layer ? layer : topLayerIndex;
			lowLayerIndex = lowLayerIndex > layer ? layer : lowLayerIndex;
			
			wayLayer = new ArrayList<OSMWayInfo>();
			wayLayers.put(layer, wayLayer);
		}
		
		return wayLayer;
	}

	public int getLowLayerIndex() {
		return lowLayerIndex;
	}

	public int getTopLayerIndex() {
		return topLayerIndex;
	}

	public List<OSMRelationInfo> getRelationLayer() {
		return relationLayer;
	}

}
