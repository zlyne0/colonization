package net.sf.freecol.common.model.specification;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.SettlementType;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;


public class EuropeanNationType extends NationType {

    public static class Xml extends XmlNodeParser {
        public Xml(XmlNodeParser parent) {
            super(parent);
            addNode(new MapIdEntities.Xml(this, "settlementTypes", SettlementType.class));
        }
        
        @Override
        public void startElement(String qName, Attributes attributes) {
            NationType nationType = new EuropeanNationType();
            nationType.european = true;
            nationType.id = getStrAttribute(attributes, "id");
            nodeObject = nationType;
        }
        
        @Override
        public String getTagName() {
            return "european-nation-type";
        }
    }
}
