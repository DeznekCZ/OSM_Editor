package cz.deznekcz.csl.osmeditor.data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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

	public boolean isProposal() {
		return getTags().containsKey("proposed");
	}

	public String getProposeType() {
		for (var tag : tags.entrySet()) {
			if (tag.getValue().equals("proposed"))
				return tag.getKey();
		}
		return null;
	}

	public <T> T getTag(String tag, Function<String, T> converter, T value) {
		try {
			String v = getTags().getOrDefault(tag, null);
			return v == null ? value : converter.apply(v);
		} catch (Throwable e) {
			return value;
		}
	}

	public String getStringTag(String tag, String defaultValue) {
		return getTag(tag, Function.identity(), defaultValue);
	}

	public int getIntTag(String tag, int defaultValue) {
		return getTag(tag, Integer::parseInt, defaultValue);
	}

	public double getDoubleTag(String tag, double defaultValue) {
		return getTag(tag, Double::parseDouble, defaultValue);
	}

	public boolean getBooleanTag(String tag, boolean defaultValue) {
		return getTag(tag, Boolean::parseBoolean, defaultValue);
	}

    public boolean hasTag(String key) {
		return getTags().containsKey(key);
    }
}
