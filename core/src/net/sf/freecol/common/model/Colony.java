package net.sf.freecol.common.model;

import promitech.colonization.savegame.XmlNodeAttributes;
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
        public void startElement(XmlNodeAttributes attr) {
            String strAttribute = attr.getStrAttribute("settlementType");
            Player owner = game.players.getById(attr.getStrAttribute("owner"));
            
            Colony colony = new Colony();
            colony.id = attr.getStrAttribute("id");
            colony.name = attr.getStrAttribute("name");
            colony.owner = owner;
            colony.settlementType = owner.nationType.settlementTypes.getById(strAttribute);
            owner.settlements.add(colony);
            
            nodeObject = colony;
        }

        @Override
        public String getTagName() {
            return "colony";
        }
    }
}
