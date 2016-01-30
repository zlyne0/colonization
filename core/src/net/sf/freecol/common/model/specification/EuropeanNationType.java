package net.sf.freecol.common.model.specification;

import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.SettlementType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;


public class EuropeanNationType extends NationType {

	private boolean ref;
    
	public EuropeanNationType(String id) {
		super(id);
	}
    
    @Override
    public boolean isREF() {
        return ref;
    }
    
    public static class Xml extends XmlNodeParser {
        public Xml() {
            addNodeForMapIdEntities("settlementTypes", SettlementType.class);
            addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
            addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            EuropeanNationType nationType = new EuropeanNationType(attr.getStrAttribute("id"));
            nationType.european = true;
            nationType.ref = attr.getBooleanAttribute("ref");
            nodeObject = nationType;
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "european-nation-type";
        }
    }
}
