package net.sf.freecol.common.model;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class ResourceType extends ObjectWithFeatures {
	
	public ResourceType(String id) {
		super(id);
	}

	public static class Xml extends XmlNodeParser {

		public Xml() {
            addNodeForMapIdEntities("modifiers", Modifier.class);
            addNodeForMapIdEntities("abilities", Ability.class);
		}
		
		@Override
        public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttribute("id");
			nodeObject = new ResourceType(id); 
		}

		@Override
		public String getTagName() {
		    return tagName();
		}
		
		public static String tagName() {
		    return "resource-type";
		}
	}
}
