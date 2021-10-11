package cz.deznekcz.csl.osmeditor.data;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class AOSMItem {

	private long id;
	private Map<String, String> tags;
	private long version;
	private long changeset;
	private long uid;
	private String user;
	private String timestamp;

	public long getId() {
		return id;
	}
	
	public Map<String, String> getTags() {
		return tags;
	}
	
	public long getVersion() {
		return version;
	}
	
	public long getChangeset() {
		return changeset;
	}
	
	public long getUid() {
		return uid;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public AOSMItem(Element element) {
		id = Long.parseLong(element.getAttribute("id"));
		version = Long.parseLong(element.getAttribute("version"));
		changeset = Long.parseLong(element.getAttribute("changeset"));
		uid = Long.parseLong(element.getAttribute("uid"));
		user = element.getAttribute("user");
		timestamp = element.getAttribute("timestamp");
		
		this.tags = new HashMap<>();
		
		NodeList tags = element.getElementsByTagName("tag");
		int tagsCount = tags.getLength();
		
		for (int i = 0; i < tagsCount; i++) {
			Element tag = (Element) tags.item(i);
			this.tags.put(tag.getAttribute("k"), tag.getAttribute("v"));
		}
	}
}
