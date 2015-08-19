package net.sf.freecol.common.model;

import java.util.HashMap;

import net.sf.freecol.common.model.specification.NationType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Player extends ObjectWithFeatures {
	
    public static enum PlayerType {
        NATIVE, COLONIAL, REBEL, INDEPENDENT, ROYAL, UNDEAD, RETIRED
    }
	
    Nation nation;
    NationType nationType;
    private PlayerType playerType;
    public MapIdEntities<Unit> units = new MapIdEntities<Unit>();
    public MapIdEntities<Settlement> settlements = new MapIdEntities<Settlement>();
    
    protected final java.util.Map<String, Stance> stance = new HashMap<String, Stance>();

    public Player(String id) {
    	super(id);
    }
    
    public Nation getNation() {
    	return nation;
    }
    
    public String toString() {
        return "id = " + id + ", nation = " + nation;
    }
    
    public boolean atWarWith(Player player) {
        return getStance(player) == Stance.WAR;
    }
    
    public boolean hasContacted(Player player) {
        return getStance(player) != Stance.UNCONTACTED;
    }
    
    public Stance getStance(Player player) {
    	if (player == null) {
    		return Stance.UNCONTACTED;
    	}
    	Stance stance = this.stance.get(player.getId());
    	if (stance == null) {
    		return Stance.UNCONTACTED;
    	}
    	return stance;
    }
    
    private void changePlayerType(PlayerType type) {
        if (playerType != PlayerType.REBEL && playerType != PlayerType.INDEPENDENT) {
            switch (type) {
            case REBEL: 
            case INDEPENDENT:
                addAbility(Ability.INDEPENDENCE_DECLARED, true);
                addAbility(Ability.INDEPENDENT_NATION, true);
                break;
            default:
                break;
            }
        }
        playerType = type;
    }
    
	public boolean missionsBanned(Player player) {
    	return false;
    }
    
    public boolean isRoyal() {
        return playerType == PlayerType.ROYAL;
    }
    
	public boolean canMoveToEurope() {
		return false;
	}
    
    public static class Xml extends XmlNodeParser {
        public Xml(XmlNodeParser parent) {
            super(parent);
            
            addNode(new MapIdEntities.Xml(this, "modifiers", Modifier.class));
            addNode(new MapIdEntities.Xml(this, "abilities", Ability.class));
        }

        @Override
        public void startElement(XmlNodeAttributes attr) {
            String idStr = attr.getStrAttribute("id");
            String nationIdStr = attr.getStrAttribute("nationId");
            String nationTypeStr = attr.getStrAttribute("nationType");
            
            Player player = new Player(idStr);
            player.nation = game.specification.nations.getById(nationIdStr);
            if (nationTypeStr != null) {
                player.nationType = game.specification.nationTypes.getById(nationTypeStr);
            }
            
            player.changePlayerType(attr.getEnumAttribute(PlayerType.class, "playerType"));
            nodeObject = player;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
        	if (attr.isQNameEquals("stance")) {
        		String playerId = attr.getStrAttribute("player");
        		Stance stance = Stance.valueOf(attr.getStrAttribute("value").toUpperCase());
        		((Player)nodeObject).stance.put(playerId, stance);
        	}
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "player";
        }
    }
}
