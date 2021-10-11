package cz.deznekcz.csl.osmeditor.data;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class OSMRelation extends AOSMItem {

	public class OSMRelationMember {

		private String type;
		private long ref;
		private String role;
		
		public String getType() {
			return type;
		}
		
		public String getRole() {
			return role;
		}
		
		public long getRef() {
			return ref;
		}

		public OSMRelationMember(String type, long ref, String role) {
			this.type = type;
			this.ref = ref;
			this.role = role;
		}

	}

	private List<OSMRelationMember> members;

	public OSMRelation(Element element) {
		super(element);
		
		members = new ArrayList<>();
		
		NodeList tags = element.getElementsByTagName("member");
		int tagsCount = tags.getLength();
		
		for (int i = 0; i < tagsCount; i++) {
			Element tag = (Element) tags.item(i);
			this.members.add(
				new OSMRelationMember(
						tag.getAttribute("type"),
						Long.parseLong(tag.getAttribute("ref")),
						tag.getAttribute("role")
						)
				);
		}
	}

	public List<OSMRelationMember> getMembers() {
		return members;
	}
}
