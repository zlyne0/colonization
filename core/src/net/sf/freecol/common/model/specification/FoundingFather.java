package net.sf.freecol.common.model.specification;

import java.io.IOException;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Turn;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class FoundingFather extends ObjectWithFeatures {

    public static final String FERDINAND_MAGELLAN = "model.foundingFather.ferdinandMagellan";
    public static final String PETER_MINUIT = "model.foundingFather.peterMinuit";
    
    public static enum FoundingFatherType {
        TRADE,
        EXPLORATION,
        MILITARY,
        POLITICAL,
        RELIGIOUS
    }

    /** The type of this FoundingFather. */
    private final FoundingFatherType type;
    
    private final int[] weight = new int[Turn.NUMBER_OF_AGES];    
    
    public final MapIdEntities<FoundingFatherEvent> events = new MapIdEntities<FoundingFatherEvent>();
    
    public FoundingFather(String id, FoundingFatherType type) {
        super(id);
        this.type = type;
    }

    public FoundingFatherType getType() {
        return type;
    }
    
    public static class Xml extends XmlNodeParser<FoundingFather> {

        private static final String ATTR_WEIGHT_PREFIX = "weight";
		private static final String ATTR_TYPE = "type";

		public Xml() {
            addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
            addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
            addNodeForMapIdEntities("events", FoundingFatherEvent.class);
        }
        
		private String weightAttr(int i) {
			return ATTR_WEIGHT_PREFIX + (i+1);
		}
		
        @Override
        public void startElement(XmlNodeAttributes attr) {
            FoundingFatherType type = attr.getEnumAttribute(FoundingFatherType.class, ATTR_TYPE);
            FoundingFather ff = new FoundingFather(attr.getStrAttributeNotNull(ATTR_ID), type);
            
            for (int i=0; i<ff.weight.length; i++) {
                ff.weight[i] = attr.getIntAttribute(weightAttr(i), 0);
            }
            nodeObject = ff;
        }

        @Override
        public void startWriteAttr(FoundingFather ff, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(ff);
        	attr.set(ATTR_TYPE, ff.type);
        	
        	for (int i=0; i<ff.weight.length; i++) {
        		attr.set(weightAttr(i), ff.weight[i]);
        	}
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "founding-father";
        }
    }
}
