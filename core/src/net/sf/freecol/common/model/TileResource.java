package net.sf.freecol.common.model;

import java.io.IOException;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
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
	
	public boolean reduceQuantityResource(int q) {
		if (quantity == UNLIMITED) {
			return false;
		}
		quantity -= q;
		if (quantity <= 0) {
			quantity = 0;
			return true;
		}
		return false;
	}
	
	public static class Xml extends XmlNodeParser<TileResource> {

		private static final String ATTR_QUANTITY = "quantity";
		private static final String ATTR_TYPE = "type";

		@Override
        public void startElement(XmlNodeAttributes attr) {
			String resourceTypeStr = attr.getStrAttribute(ATTR_TYPE);
			int quantity = attr.getIntAttribute(ATTR_QUANTITY, UNLIMITED);
			ResourceType resourceType = Specification.instance.resourceTypes.getById(resourceTypeStr);
			TileResource tileResource = new TileResource(attr.getStrAttribute(ATTR_ID), resourceType, quantity);
			nodeObject = tileResource;
		}

		@Override
		public void startWriteAttr(TileResource n, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(n);
			attr.set(ATTR_TYPE, n.resourceType);
			attr.set(ATTR_QUANTITY, n.quantity);
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
