package net.sf.freecol.common.model;

import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class IndianSettlement extends Settlement {
    
    /** The missionary at this settlement. */
    protected Unit missionary = null;
    
    public String getImageKey() {
    	String st = owner.nation().getId();
    	
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
    
	@Override
	public boolean isColony() {
		return false;
	}
    
    private boolean hasMissionary() {
        return missionary != null;
    }

    public static class Xml extends XmlNodeParser {

        @Override
        public void startElement(XmlNodeAttributes attr) {
            IndianSettlement is = new IndianSettlement();
            is.name = attr.getStrAttribute("name");
            Player owner = game.players.getById(attr.getStrAttribute("owner"));
            is.owner = owner;
            is.settlementType = owner.nationType().settlementTypes.getById(attr.getStrAttribute("settlementType"));
            
            owner.settlements.add(is);
            
            nodeObject = is;
        }

        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "indianSettlement";
        }
    }
}
