package net.sf.freecol.common.model.specification;

import net.sf.freecol.common.model.SettlementType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class IndianNationType extends NationType {

    public IndianNationType(String id) {
		super(id);
	}

	public boolean isREF() {
        return false;
    }
    
    public static class Xml extends XmlNodeParser {
        public Xml() {
            addNodeForMapIdEntities("settlementTypes", SettlementType.class);
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            NationType nationType = new EuropeanNationType(attr.getStrAttribute("id"));
            nationType.european = false;
            nodeObject = nationType;
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "indian-nation-type";
        }
    }
}
