package net.sf.freecol.common.model;

import java.util.HashMap;

import net.sf.freecol.common.model.specification.NationType;
import promitech.colonization.SpiralIterator;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

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
	
    Nation nation;
    NationType nationType;
    private PlayerType playerType;
    public MapIdEntities<Unit> units = new MapIdEntities<Unit>();
    public MapIdEntities<Settlement> settlements = new MapIdEntities<Settlement>();
    
    private BooleanMap fogOfWar;
    private BooleanMap exploredTiles;
    
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
	
	public void removeFogOfWar(int x, int y, Map map) {
		if (fogOfWar == null) {
			fogOfWar = new BooleanMap(map, true);
		}
		fogOfWar.set(x, y, false);
	}
	
	public boolean hasFogOfWar(Tile tile) {
		return hasFogOfWar(tile.x, tile.y);
	}
	
	public boolean hasFogOfWar(int x, int y) {
		return fogOfWar.isSet(x, y);
	}
	
	public void resetFogOfWar(Map map) {
		if (fogOfWar == null) {
			fogOfWar = new BooleanMap(map, true);
		}
		fogOfWar.reset(true);
	}
	
	/**
	 * @return return true when explore new tiles
	 */
	public boolean revealMapAfterUnitMove(Map map, Unit unit) {
		int radius = unit.lineOfSight();
		SpiralIterator spiralIterator = new SpiralIterator(map.width, map.height);
		spiralIterator.reset(unit.getTile().x, unit.getTile().y, true, radius);
		
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
			removeFogOfWar(tile.x, tile.y, map);
		}
		return unexploredTile;
	}
	
    public static class Xml extends XmlNodeParser {
        public Xml() {
            addNodeForMapIdEntities("modifiers", Modifier.class);
            addNodeForMapIdEntities("abilities", Ability.class);
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
