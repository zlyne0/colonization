package net.sf.freecol.common.model;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;

public class ResourceType {

	private final String resourceTypeId;
	
	public ResourceType(String resourceTypeId) {
		this.resourceTypeId = resourceTypeId;
	}

	public String getResourceTypeId() {
		return resourceTypeId;
	}
	
	public static class Xml extends XmlNodeParser {

		public Xml(XmlNodeParser parent) {
			super(parent);
		}

		@Override
		public void startElement(String qName, Attributes attributes) {
			String id = getStrAttribute(attributes, "id");
			ResourceType resourceType = new ResourceType(id);
			((Specification.Xml)this.parentXmlNodeParser).specification.addResourceType(resourceType);
		}

		@Override
		public String getTagName() {
			return "resource-type";
		}
	}
	
}
