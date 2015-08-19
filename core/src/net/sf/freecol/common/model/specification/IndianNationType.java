package net.sf.freecol.common.model.specification;

import net.sf.freecol.common.model.SettlementType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class IndianNationType extends NationType {

    public static class Xml extends XmlNodeParser {
        public Xml(XmlNodeParser parent) {
            super(parent);
            addNodeForMapIdEntities("settlementTypes", SettlementType.class);
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            NationType nationType = new EuropeanNationType();
            nationType.european = false;
            nationType.id = attr.getStrAttribute("id");
            nodeObject = nationType;
        }
        
        @Override
        public String getTagName() {
            return "indian-nation-type";
        }
    }
}
