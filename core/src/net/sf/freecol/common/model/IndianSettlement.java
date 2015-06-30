package net.sf.freecol.common.model;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;

public class IndianSettlement extends Settlement {

    public static class Xml extends XmlNodeParser {

        public Xml(XmlNodeParser parent) {
            super(parent);
        }

        @Override
        public void startElement(String qName, Attributes attributes) {
            IndianSettlement is = new IndianSettlement();
            is.name = getStrAttribute(attributes, "name");
            is.type = getStrAttribute(attributes, "settlementType");
            
            Tile.Xml tileXmlParser = getParentXmlParser();
            tileXmlParser.tile.indianSettlement = is;
        }

        @Override
        public String getTagName() {
            return "colony";
        }
        
    }
}
