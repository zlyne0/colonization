package net.sf.freecol.common.model;

import promitech.colonization.savegame.XmlNodeAttributes;
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

		@Override
        public void startElement(XmlNodeAttributes attr) {
			String resourceTypeStr = attr.getStrAttribute("type");
			int quantity = attr.getIntAttribute("quantity");
			ResourceType resourceType = Specification.instance.resourceTypes.getById(resourceTypeStr);

			TileResource tileResource = new TileResource(resourceType);
			tileResource.quantity = quantity;
			tileResource.id = attr.getStrAttribute("id");
			
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
