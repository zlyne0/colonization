package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Tension;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
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
    public final List<Unit> units = new ArrayList<Unit>();
    
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

    public static class Xml extends XmlNodeParser<IndianSettlement> {

        private static final String ATTR_LEVEL = "level";
		private static final String ATTR_PLAYER = "player";
		private static final String ELEMENT_ALARM = "alarm";
		private static final String ELEMENT_CONTACT_LEVEL = "contactLevel";
		private static final String ATTR_SETTLEMENT_TYPE = "settlementType";
		private static final String ATTR_OWNER = "owner";
		private static final String ATTR_NAME = "name";

		@Override
        public void startElement(XmlNodeAttributes attr) {
            IndianSettlement is = new IndianSettlement(attr.getStrAttributeNotNull(ATTR_ID));
            is.name = attr.getStrAttribute(ATTR_NAME);
            Player owner = game.players.getById(attr.getStrAttribute(ATTR_OWNER));
            is.owner = owner;
            is.settlementType = owner.nationType().settlementTypes.getById(attr.getStrAttribute(ATTR_SETTLEMENT_TYPE));
            
            owner.settlements.add(is);
            
            nodeObject = is;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
        	if (attr.isQNameEquals(ELEMENT_CONTACT_LEVEL)) {
        		nodeObject.contactLevelByPlayer.put(
    				attr.getStrAttribute(ATTR_PLAYER), 
    				attr.getEnumAttribute(ContactLevel.class, ATTR_LEVEL)
        		);
        	}
        	if (attr.isQNameEquals(ELEMENT_ALARM)) {
        		nodeObject.tensionByPlayer.put(
        			attr.getStrAttribute(ATTR_PLAYER), 
        			new Tension(attr.getIntAttribute(ATTR_VALUE))
        		);
        	}
        }
        
        @Override
        public void startWriteAttr(IndianSettlement is, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(is);
        	attr.set(ATTR_NAME, is.name);
        	attr.set(ATTR_OWNER, is.owner);
        	attr.set(ATTR_SETTLEMENT_TYPE, is.settlementType);
        	
        	for (Entry<String, ContactLevel> contactEntry : is.contactLevelByPlayer.entrySet()) {
				attr.xml.element(ELEMENT_CONTACT_LEVEL);
				attr.set(ATTR_PLAYER, contactEntry.getKey());
				attr.set(ATTR_LEVEL, contactEntry.getValue());
				attr.xml.pop();
			}
        	for (Entry<String, Tension> tensionEntry : is.tensionByPlayer.entrySet()) {
				attr.xml.element(ELEMENT_ALARM);
				attr.set(ATTR_PLAYER, tensionEntry.getKey());
				attr.set(ATTR_VALUE, tensionEntry.getValue().getValue());
				attr.xml.pop();
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
