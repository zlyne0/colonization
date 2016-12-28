package net.sf.freecol.common.model;

import java.util.HashMap;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Tension;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class IndianSettlement extends Settlement {
    
    public static enum ContactLevel {
        UNCONTACTED,     // Nothing known other than location?
        CONTACTED,       // Name, wanted-goods now visible
        VISITED,         // Skill now known
        SCOUTED          // Scouting bonus consumed
    };
	
	/** The missionary at this settlement. */
    protected Unit missionary = null;
    
    private java.util.Map<String,ContactLevel> contactLevelByPlayer = new HashMap<String, IndianSettlement.ContactLevel>();
    private java.util.Map<String, Tension> tensionByPlayer = new HashMap<String, Tension>();

    public IndianSettlement(IdGenerator idGenerator) {
    	super(idGenerator.nextId(IndianSettlement.class));
    }
    
    public IndianSettlement(String id) {
		super(id);
	}

    public boolean hasContact(Player player) {
    	ContactLevel level = contactLevelByPlayer.get(player.getId());
    	if (level == null) {
    		return false;
    	}
    	return level != ContactLevel.UNCONTACTED;
    }
    
	public void modifyTension(Player player, int tensionValue) {
		Tension tension = tensionByPlayer.get(player.getId());
		if (tension == null) {
			tension = new Tension(tensionValue);
			tensionByPlayer.put(player.getId(), tension);
		} else {
			tension.modify(tensionValue);
		}
	}
    
    @Override
    public boolean hasAbility(String abilityCode) {
        return false;
    }
    
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
            IndianSettlement is = new IndianSettlement(attr.getStrAttributeNotNull("id"));
            is.name = attr.getStrAttribute("name");
            Player owner = game.players.getById(attr.getStrAttribute("owner"));
            is.owner = owner;
            is.settlementType = owner.nationType().settlementTypes.getById(attr.getStrAttribute("settlementType"));
            
            owner.settlements.add(is);
            
            nodeObject = is;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
        	if (attr.isQNameEquals("contactLevel")) {
        		((IndianSettlement)nodeObject).contactLevelByPlayer.put(
    				attr.getStrAttribute("player"), 
    				attr.getEnumAttribute(ContactLevel.class, "level")
        		);
        	}
        	if (attr.isQNameEquals("alarm")) {
        		((IndianSettlement)nodeObject).tensionByPlayer.put(
        			attr.getStrAttribute("player"), 
        			new Tension(attr.getIntAttribute("value"))
        		);
        	}
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "indianSettlement";
        }
    }

	@Override
	public int applyModifiers(String abilityCode, int val) {
		throw new IllegalStateException("not implemented");
	}

	@Override
	public void addGoods(String goodsTypeId, int quantity) {
		throw new IllegalStateException("not implemented");
	}

	@Override
	public boolean isContainsTile(Tile improvingTile) {
		throw new IllegalStateException("not implemented");
	}

	@Override
	public void initMaxPossibleProductionOnTile(Tile tile) {
		throw new IllegalStateException("not implemented");
	}

	@Override
	public ProductionSummary productionSummary() {
		throw new IllegalStateException("not implemented");
	}
}
