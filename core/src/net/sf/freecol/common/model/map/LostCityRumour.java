package net.sf.freecol.common.model.map;

import net.sf.freecol.common.model.Identifiable;
import promitech.colonization.savegame.XmlNodeAttributes;
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
        public void startElement(XmlNodeAttributes attr) {
            LostCityRumour lcr = new LostCityRumour();
            lcr.id = attr.getStrAttribute("id");
            
            nodeObject = lcr;
        }

        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "lostCityRumour";
        }
    }
}
