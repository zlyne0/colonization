package net.sf.freecol.common.model;

import java.io.IOException;

import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.Modifier;
import promitech.colonization.Randomizer;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class ResourceType extends ObjectWithFeatures {

	public static final String GAME = "model.resource.game";
	public static final String MINERALS = "model.resource.minerals";

	private int minValue = UNLIMITED;
	private int maxValue = UNLIMITED;
	
	public ResourceType(String id) {
		super(id);
	}

	public int initQuantity() {
	    if (minValue == UNLIMITED && maxValue == UNLIMITED) {
	        return UNLIMITED;
	    }
		return Randomizer.instance().randomInt(minValue, maxValue);		
	}
	
	public static class Xml extends XmlNodeParser<ResourceType> {

		private static final String ATTR_MINIMUM_VALUE = "minimum-value";
		private static final String ATTR_MAXIMUM_VALUE = "maximum-value";

		public Xml() {
            addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
            addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
		}
		
		@Override
        public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttribute(ATTR_ID);
			ResourceType rt = new ResourceType(id);
			rt.minValue = attr.getIntAttribute(ATTR_MINIMUM_VALUE, UNLIMITED);
			rt.maxValue = attr.getIntAttribute(ATTR_MAXIMUM_VALUE, UNLIMITED);
			nodeObject = rt;
		}

		@Override
		public void startWriteAttr(ResourceType rt, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(rt);
			attr.set(ATTR_MINIMUM_VALUE, rt.minValue, UNLIMITED);
			attr.set(ATTR_MAXIMUM_VALUE, rt.maxValue, UNLIMITED);
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
