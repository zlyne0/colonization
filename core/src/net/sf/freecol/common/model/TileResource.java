package net.sf.freecol.common.model;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;

public class TileResource implements Identifiable {
	private static final int UNLIMITED = -1;
	
	private String id;
	private final ResourceType resourceType;
	
	private int quantity = UNLIMITED;
	
	public TileResource(ResourceType resourceType) {
		this.resourceType = resourceType;
	}
	
	@Override
	public String getId() {
	    return id;
	}
	
	public ResourceType getResourceType() {
	    return resourceType;
	}
	
	public static class Xml extends XmlNodeParser {

		public Xml(XmlNodeParser parent) {
			super(parent);
		}

		@Override
		public void startElement(String qName, Attributes attributes) {
			String resourceTypeStr = getStrAttribute(attributes, "type");
			int quantity = getIntAttribute(attributes, "quantity");
			ResourceType resourceType = game.specification.resourceTypes.getById(resourceTypeStr);

			TileResource tileResource = new TileResource(resourceType);
			tileResource.quantity = quantity;
			tileResource.id = getStrAttribute(attributes, "id");
			
			nodeObject = tileResource;
		}

		@Override
		public String getTagName() {
			return "resource";
		}
	}
}
