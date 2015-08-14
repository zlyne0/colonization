package net.sf.freecol.common.model;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class ResourceType implements Identifiable {
	private final String id;
	
	public ResourceType(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}
	
	public static class Xml extends XmlNodeParser {
		public Xml(XmlNodeParser parent) {
			super(parent);
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttribute("id");
			nodeObject = new ResourceType(id); 
		}

		@Override
		public String getTagName() {
			return "resource-type";
		}
	}
}
