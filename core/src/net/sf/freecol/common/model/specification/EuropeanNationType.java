package net.sf.freecol.common.model.specification;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.SettlementType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;


public class EuropeanNationType extends NationType {

    public static class Xml extends XmlNodeParser {
        public Xml(XmlNodeParser parent) {
            super(parent);
            addNode(new MapIdEntities.Xml(this, "settlementTypes", SettlementType.class));
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            NationType nationType = new EuropeanNationType();
            nationType.european = true;
            nationType.id = attr.getStrAttribute("id");
            nodeObject = nationType;
        }
        
        @Override
        public String getTagName() {
            return "european-nation-type";
        }
    }
}
