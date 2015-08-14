package net.sf.freecol.common.model.map;

import net.sf.freecol.common.model.Identifiable;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;

public class LostCityRumour implements Identifiable {
    private String id;
    
    @Override
    public String getId() {
        return id;
    }
    
    public static class Xml extends XmlNodeParser {
        public Xml(XmlNodeParser parent) {
            super(parent);
        }

        @Override
        public void startElement(String qName, Attributes attributes) {
            LostCityRumour lcr = new LostCityRumour();
            lcr.id = getStrAttribute(attributes, "id");
            
            nodeObject = lcr;
        }

        @Override
        public String getTagName() {
            return "lostCityRumour";
        }
    }
}
