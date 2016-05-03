package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.Modifier;
import promitech.colonization.Randomizer;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class ResourceType extends ObjectWithFeatures {
	
	private int minValue = UNLIMITED;
	private int maxValue = UNLIMITED;
	
	public ResourceType(String id) {
		super(id);
	}

	public int initQuantity() {
		return Randomizer.getInstance().randomInt(minValue, maxValue);		
	}
	
	public static class Xml extends XmlNodeParser {

		public Xml() {
            addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
            addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
		}
		
		@Override
        public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttribute("id");
			ResourceType rt = new ResourceType(id);
			rt.minValue = attr.getIntAttribute("minimum-value", UNLIMITED);
			rt.maxValue = attr.getIntAttribute("maximum-value", UNLIMITED);
			nodeObject = rt;
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
