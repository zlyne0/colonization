package net.sf.freecol.common.model;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class ColonyBuildingQueueItem implements Identifiable {
	private final String id;
	
	public ColonyBuildingQueueItem(String id) {
		this.id = id;
	}
	
	@Override
	public String getId() {
		return id;
	}
	
	public String toString() {
		return id;
	}
	
	public static class Xml extends XmlNodeParser {

		@Override
		public void startElement(XmlNodeAttributes attr) {
			ColonyBuildingQueueItem item = new ColonyBuildingQueueItem(attr.getStrAttribute("id"));
			nodeObject = item;
		}

		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "buildQueueItem";
		}
	}

}
