package net.sf.freecol.common.model.player;

import java.util.HashMap;

import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Nation;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Stance;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.FoundingFather;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.NationType;
import promitech.colonization.SpiralIterator;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

class BooleanMap {
	private boolean tab[][];
	private int width;
	private int height;
	
	public BooleanMap(Map map, boolean defaultVal) {
		this.width = map.width;
		this.height = map.height;
		
		tab = new boolean[height][width];
		reset(defaultVal);
	}
	
	public boolean isSet(int x, int y) {
		if (tab == null) {
			return false;
		}
		if (isCordsValid(x, y)) {
			return tab[y][x];
		}
		return false;
	}

	public boolean isCordsValid(int x, int y) {
		return x >= 0 && x < width && y >= 0 && y < height;
	}
	
	/**
	 * @return return true when value change
	 */
	public boolean set(int x, int y, boolean value) {
		if (isCordsValid(x, y)) {
			boolean c = tab[y][x] ^ value;
			tab[y][x] = value;
			return c;
		}
		return false;
	}

	public void reset(boolean defaultVal) {
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				tab[y][x] = defaultVal;
			}
		}
	}
}

public class Player extends ObjectWithFeatures {
	
    public static enum PlayerType {
        NATIVE, COLONIAL, REBEL, INDEPENDENT, ROYAL, UNDEAD, RETIRED
    }
	
    private Nation nation;
    private NationType nationType;
    private PlayerType playerType;
    private Europe europe;
    private Market market;
    private boolean dead = false;
    private int tax;
    private int gold;
    private int liberty;
    private int interventionBells;
    private int immigration;
    private String independentNationName;
    public final MapIdEntities<Unit> units = new MapIdEntities<Unit>();
    public final MapIdEntities<Settlement> settlements = new MapIdEntities<Settlement>();
    public final MapIdEntities<FoundingFather> foundingFathers = new MapIdEntities<FoundingFather>();
    
    public EventsNotifications eventsNotifications = new EventsNotifications();
    
    public final PlayerForOfWar fogOfWar = new PlayerForOfWar(); 
    private BooleanMap exploredTiles;
    
    protected final java.util.Map<String, Stance> stance = new HashMap<String, Stance>();

    public Player(String id) {
    	super(id);
    }
    
    public Nation nation() {
    	return nation;
    }
    
    public StringTemplate getNationName() {
        return (playerType == PlayerType.REBEL || playerType == PlayerType.INDEPENDENT)
            ? StringTemplate.name(independentNationName)
            : StringTemplate.key(Messages.nameKey(nation.getId()));
    }
    
	public NationType nationType() {
		return nationType;
	}
    
	public Market market() {
		return market;
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
		return europe != null;
	}
    
    public String getEuropeNameKey() {
        return (europe == null) ? null : nation.getId() + ".europe";
    }
	
	/**
	 * @return boolean - return true when set some tile as explored 
	 */
	public boolean setTileAsExplored(Tile tile, Map map) {
		if (exploredTiles == null) {
			exploredTiles = new BooleanMap(map, false);
		}
		return exploredTiles.set(tile.x, tile.y, true);
	}
	
	public boolean isTileUnExplored(Tile tile) {
		return !isTileExplored(tile.x, tile.y);
	}
	
	public boolean isTileExplored(int x, int y) {
		if (exploredTiles == null) {
			return false;
		}
		return exploredTiles.isSet(x, y);
	}
	
	/**
	 * @return return true when explore new tiles
	 */
	public boolean revealMapAfterUnitMove(Map map, Unit unit) {
		Tile unitTileLocation = unit.getTile();
		
		int radius = unit.lineOfSight();
		SpiralIterator spiralIterator = new SpiralIterator(map.width, map.height);
		spiralIterator.reset(unitTileLocation.x, unitTileLocation.y, true, radius);
		
		boolean unexploredTile = false;
		while (spiralIterator.hasNext()) {
			Tile tile = map.getTile(spiralIterator.getX(), spiralIterator.getY());
			spiralIterator.next();
			if (tile == null) {
				continue;
			}
			if (setTileAsExplored(tile, map)) {
				unexploredTile = true;
			}
			fogOfWar.removeFogOfWar(tile.x, tile.y);
		}
		return unexploredTile;
	}

    public Europe getEurope() {
        return europe;
    }
	
    public int unitTypeCount(UnitType unitType) {
    	int counter = 0;
    	for (Unit unit : units.entities()) {
    		if (unit.unitType.equalsId(unitType)) {
    			counter++;
    		}
    	}
    	return counter;
    }
    
    public void addGold(int gold) {
    	this.gold += gold;
    }
    
    public void subtractGold(int gold) {
    	this.gold -= gold;
    }
    
    public boolean hasNotGold(int gold) {
    	return this.gold < gold;
    }
    
    public boolean hasGold(int gold) {
    	return this.gold >= gold;
    }
    
    public boolean isEuropean() {
        return playerType == PlayerType.COLONIAL
            || playerType == PlayerType.REBEL
            || playerType == PlayerType.INDEPENDENT
            || playerType == PlayerType.ROYAL;
    }

    public boolean isRebel() {
        return playerType == PlayerType.REBEL;
    }
    
	public boolean isNotLiveEuropeanPlayer() {
		return nation.isUnknownEnemy() || isDead() || !isEuropean(); 
	}
    
	public boolean isDead() {
		return dead;
	}
	
	public void endTurn() {
	}

	public void modifyLiberty(int libertyAmount) {
		if (!canHaveFoundingFathers()) {
			return;
		}
		this.liberty = Math.max(0, this.liberty + libertyAmount);
		if (isRebel()) {
			interventionBells += libertyAmount;
		}
	}
	
    public void modifyImmigration(int amount) {
        immigration = Math.max(0, immigration + amount);
    }
	
    public boolean canHaveFoundingFathers() {
        return nationType.hasAbility(Ability.ELECT_FOUNDING_FATHER);
    }
	
	public void removeSettlement(Settlement settlement) {
		settlement.tile.setSettlement(null);
		settlements.removeId(settlement.getId());
	}
    
    public static class Xml extends XmlNodeParser {
        public Xml() {
            addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
            addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
            addNode(Europe.class, "europe");
            addNode(Market.class, "market");
            addNode(EventsNotifications.class, "eventsNotifications");
        }

        @Override
        public void startElement(XmlNodeAttributes attr) {
            String idStr = attr.getStrAttribute("id");
            String nationIdStr = attr.getStrAttribute("nationId");
            String nationTypeStr = attr.getStrAttribute("nationType");
            
            Player player = new Player(idStr);
            player.dead = attr.getBooleanAttribute("dead");
            player.tax = attr.getIntAttribute("tax", 0);
            player.gold = attr.getIntAttribute("gold", 0);
            player.liberty = attr.getIntAttribute("liberty", 0);
            player.interventionBells = attr.getIntAttribute("interventionBells", 0);
            player.immigration = attr.getIntAttribute("immigration", 0);
            player.nation = Specification.instance.nations.getById(nationIdStr);
            if (nationTypeStr != null) {
                player.nationType = Specification.instance.nationTypes.getById(nationTypeStr);
            }
            player.independentNationName = attr.getStrAttribute("independentNationName");
            
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
        	if (attr.isQNameEquals("foundingFathers")) {
        	    int count = attr.getIntAttribute("xLength", 0);
        	    for (int i=0; i<count; i++) {
        	        String fatherId = attr.getStrAttributeNotNull("x" + i);
        	        FoundingFather father = Specification.instance.foundingFathers.getById(fatherId);
        	        ((Player)nodeObject).foundingFathers.add(father);
        	    }
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
