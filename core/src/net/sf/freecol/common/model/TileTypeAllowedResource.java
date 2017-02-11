package net.sf.freecol.common.model;

import java.io.IOException;

import net.sf.freecol.common.model.specification.WithProbability;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

class TileTypeAllowedResource implements Identifiable, WithProbability<TileTypeAllowedResource> {

	public final ResourceType resourceType;
	public final int probability;
	
	public TileTypeAllowedResource(ResourceType resourceType, int probability) {
		this.resourceType = resourceType;
		this.probability = probability;
	}
	
	@Override
	public String getId() {
		return resourceType.getId();
	}

	@Override
	public int getOccureProbability() {
		return probability;
	}

	@Override
	public TileTypeAllowedResource probabilityObject() {
		return this;
	}
	
	public static class Xml extends XmlNodeParser<TileTypeAllowedResource> {

		private static final String ATTR_TYPE = "type";
		private static final String ATTR_PROBABILITY = "probability";

		@Override
		public void startElement(XmlNodeAttributes attr) {
			String resourceTypeId = attr.getStrAttribute(ATTR_TYPE);
			int probability = attr.getIntAttribute(ATTR_PROBABILITY);
			
			ResourceType rt = Specification.instance.resourceTypes.getById(resourceTypeId);
			TileTypeAllowedResource ttr = new TileTypeAllowedResource(rt, probability);
			
			nodeObject = ttr;
		}
		
		@Override
		public void startWriteAttr(TileTypeAllowedResource ttr, XmlNodeAttributesWriter attr) throws IOException {
			attr.set(ATTR_TYPE, ttr.resourceType.getId());
			attr.set(ATTR_PROBABILITY, ttr.probability);
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
