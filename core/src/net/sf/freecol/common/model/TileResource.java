package net.sf.freecol.common.model;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;

public class TileResource {
	private static final int UNLIMITED = -1;
	private final ResourceType resourceType;
	
	private int quantity = UNLIMITED;
	
	public TileResource(ResourceType resourceType) {
		this.resourceType = resourceType;
	}
	
	public static class Xml extends XmlNodeParser {

		public Xml(Tile.Xml parent) {
			super(parent);
		}

		@Override
		public void startElement(String qName, Attributes attributes) {
			String resourceTypeStr = getStrAttribute(attributes, "type");
			int quantity = getIntAttribute(attributes, "quantity");
			ResourceType resourceType = rootGame.specification.getResourceTypeByStr(resourceTypeStr);

			TileResource tileResource = new TileResource(resourceType);
			tileResource.quantity = quantity;
			
			Tile.Xml parentXmlParser = getParentXmlParser();
			parentXmlParser.tile.addTileResources(tileResource);
		}

		@Override
		public String getTagName() {
			return "resource";
		}
	}

	public ResourceType getResourceType() {
		return resourceType;
	}
	
	
}
