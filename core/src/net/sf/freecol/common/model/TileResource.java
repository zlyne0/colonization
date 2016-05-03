package net.sf.freecol.common.model;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class TileResource extends ObjectWithId {
	
	private final ResourceType resourceType;
	private int quantity;
	
	public TileResource(String id, ResourceType resourceType, int quantity) {
		super(id);
		this.resourceType = resourceType;
		this.quantity = quantity;
	}

	public TileResource(IdGenerator idGenerator, ResourceType resourceType, int quantity) {
		super(idGenerator.nextId(ResourceType.class));
		this.resourceType = resourceType;
		this.quantity = quantity;
	}
	
	public ResourceType getResourceType() {
	    return resourceType;
	}
	
	public static class Xml extends XmlNodeParser {

		@Override
        public void startElement(XmlNodeAttributes attr) {
			String resourceTypeStr = attr.getStrAttribute("type");
			int quantity = attr.getIntAttribute("quantity", UNLIMITED);
			ResourceType resourceType = Specification.instance.resourceTypes.getById(resourceTypeStr);
			TileResource tileResource = new TileResource(attr.getStrAttribute("id"), resourceType, quantity);
			nodeObject = tileResource;
		}

		@Override
		public String getTagName() {
		    return tagName();
		}
		
        public static String tagName() {
            return "resource";
        }
		
	}
}
