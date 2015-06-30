package net.sf.freecol.common.model;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;

public class Colony extends Settlement {

    public static class Xml extends XmlNodeParser {

        public Xml(XmlNodeParser parent) {
            super(parent);
        }

        @Override
        public void startElement(String qName, Attributes attributes) {
            Colony colony = new Colony();
            colony.name = getStrAttribute(attributes, "name");
            colony.type = getStrAttribute(attributes, "settlementType");
            
            Tile.Xml tileXmlParser = getParentXmlParser();
            tileXmlParser.tile.colony = colony;
        }

        @Override
        public String getTagName() {
            return "colony";
        }
        
    }
}
