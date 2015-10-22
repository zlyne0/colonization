package net.sf.freecol.common.model.specification;

import net.sf.freecol.common.model.SettlementType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;


public class EuropeanNationType extends NationType {

    private boolean ref;
    
    @Override
    public boolean isREF() {
        return ref;
    }
    
    public static class Xml extends XmlNodeParser {
        public Xml() {
            addNodeForMapIdEntities("settlementTypes", SettlementType.class);
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            EuropeanNationType nationType = new EuropeanNationType();
            nationType.european = true;
            nationType.id = attr.getStrAttribute("id");
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
