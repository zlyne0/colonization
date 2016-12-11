package net.sf.freecol.common.model.specification;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Turn;
import promitech.colonization.savegame.XmlNodeAttributes;
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
    
    public static class Xml extends XmlNodeParser {

        public Xml() {
            addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
            addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
            addNodeForMapIdEntities("events", FoundingFatherEvent.class);
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            FoundingFatherType type = attr.getEnumAttribute(FoundingFatherType.class, "type");
            FoundingFather ff = new FoundingFather(attr.getStrAttributeNotNull("id"), type);
            
            for (int i=0; i<ff.weight.length; i++) {
                ff.weight[0] = attr.getIntAttribute("weight" + (i+1), 0);
            }
            nodeObject = ff;
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
