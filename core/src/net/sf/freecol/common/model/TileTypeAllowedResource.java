package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.WithProbability;
import promitech.colonization.savegame.XmlNodeAttributes;
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
	
	public static class Xml extends XmlNodeParser {

		@Override
		public void startElement(XmlNodeAttributes attr) {
			String resourceTypeId = attr.getStrAttribute("type");
			int probability = attr.getIntAttribute("probability");
			
			ResourceType rt = Specification.instance.resourceTypes.getById(resourceTypeId);
			TileTypeAllowedResource ttr = new TileTypeAllowedResource(rt, probability);
			
			nodeObject = ttr;
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
