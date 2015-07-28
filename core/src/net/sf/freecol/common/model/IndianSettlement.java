package net.sf.freecol.common.model;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;

public class IndianSettlement extends Settlement {
    
    /** The missionary at this settlement. */
    protected Unit missionary = null;
    
    public String getImageKey() {
    	String st = owner.getNation().getId();
    	
    	if (settlementType.isCapital()) {
    		st += ".capital";
    	} else {
    		st += ".settlement";
    	}
    	
    	if (hasMissionary()) {
    		st += "";
    	} else {
    		st += ".mission";
    	}
    	st += ".image";
        return st;
    }
    
    private boolean hasMissionary() {
        return missionary != null;
    }

    public static class Xml extends XmlNodeParser {

        public Xml(XmlNodeParser parent) {
            super(parent);
        }

        @Override
        public void startElement(String qName, Attributes attributes) {
            IndianSettlement is = new IndianSettlement();
            is.name = getStrAttribute(attributes, "name");
            Player owner = game.players.getById(getStrAttribute(attributes, "owner"));
            is.owner = owner;
            is.settlementType = owner.nationType.settlementTypes.getById(getStrAttribute(attributes, "settlementType"));
            
            Tile.Xml tileXmlParser = getParentXmlParser();
            tileXmlParser.tile.indianSettlement = is;
            is.tile = tileXmlParser.tile;
   
            owner.settlements.add(is);
        }

        @Override
        public String getTagName() {
            return "indianSettlement";
        }
        
    }
}
