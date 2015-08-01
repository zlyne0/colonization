package net.sf.freecol.common.model;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;

public class Colony extends Settlement {

    private boolean isUndead() {
        return false;
    }
    
    public int getDisplayUnitCount() {
        return 1;
    }
    
    private String getStockadeKey() {
        return null;
    }

	@Override
	public boolean isColony() {
		return true;
	}
    
    public String getImageKey() {
        if (isUndead()) {
            return "undead";
        }
        int count = getDisplayUnitCount();
        String key = (count <= 3) ? "small"
            : (count <= 7) ? "medium"
            : "large";
        String stockade = getStockadeKey();
        if (stockade != null) {
            key += "." + stockade;
        }
        return "model.settlement." + key + ".image";
    }
    
    public static class Xml extends XmlNodeParser {
        public Xml(XmlNodeParser parent) {
            super(parent);
        }

        @Override
        public void startElement(String qName, Attributes attributes) {
            String strAttribute = getStrAttribute(attributes, "settlementType");
            Player owner = game.players.getById(getStrAttribute(attributes, "owner"));
            
            Colony colony = new Colony();
            colony.id = getStrAttribute(attributes, "id");
            colony.name = getStrAttribute(attributes, "name");
            colony.owner = owner;
            colony.settlementType = owner.nationType.settlementTypes.getById(strAttribute);
            
            Tile.Xml tileXmlParser = getParentXmlParser();
            tileXmlParser.tile.settlement = colony;
            
            colony.tile = tileXmlParser.tile;
            owner.settlements.add(colony);
        }

        @Override
        public String getTagName() {
            return "colony";
        }
        
    }

}
