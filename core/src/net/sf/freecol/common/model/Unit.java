package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.xml.sax.SAXException;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Unit implements Identifiable, Location {

    /** A state a Unit can have. */
    public static enum UnitState {
        ACTIVE,
        FORTIFIED,
        SENTRY,
        IN_COLONY,
        IMPROVING,
        // @compat 0.10.0
        TO_EUROPE,
        TO_AMERICA,
        // end @compat
        FORTIFYING,
        SKIPPED
    }
	
	
	private String id;
	private Player owner;
    private UnitType unitType;
    private UnitRole unitRole;
    private List<Unit> containedUnits;
    private Tile tile;

    private UnitState state = UnitState.ACTIVE;
    private int movesLeft;
    private int hitPoints;
    private boolean disposed = false;
    private int visibleGoodsCount = -1;
    
	@Override
	public String getId() {
		return id;
	}
    
    public String toString() {
        String st = "unitType = " + unitType;
        if (containedUnits != null) {
            st += ", contains[";
            for (Unit u : containedUnits) {
                st += u.unitType + ", "; 
            }
            st += "]";
        }
        return st;
    }

    public void addUnit(Unit containerUnit) {
        if (containedUnits == null) {
            containedUnits = new ArrayList<Unit>();
        }
        containedUnits.add(containerUnit);
    }
    
    public String resourceImageKey() {
    	if (!owner.nationType.isEuropean()) {
    		if (UnitType.FREE_COLONIST.equals(unitType.getId())) {
    			return unitType.getId() + unitRole.getRoleSuffix() + ".native.image";
    		}
    	}
    	return unitType.getId() + unitRole.getRoleSuffix() + ".image"; 
    }

	public Tile getTile() {
		return tile;
	}
    
	public Player getOwner() {
		return owner;
	}

	public boolean isOwner(Player player) {
		return owner.equals(player);
	}
	
	public int lineOfSight() {
		return unitType.lineOfSight();
	}
	
    public boolean isDamaged() {
        return hitPoints < unitType.getHitPoints();
    }
	
    public boolean isOnCarrier() {
        return getLocation() instanceof Unit;
    }
    
    public Location getLocation() {
		return tile;
	}

	public boolean couldMove() {
        return state == UnitState.ACTIVE
            && movesLeft > 0
            //&& destination == null // Can not reach next tile
            //&& tradeRoute == null
            && !isDamaged()
            && !disposed
            //&& !isAtSea()
            && !isOnCarrier();
    }

    public final Object getTradeRoute() {
        return null;
    }
	
    public Location getDestination() {
        return null;
    }
    
    public UnitState getState() {
        return state;
    }
    
    public TileImprovement getWorkImprovement() {
        return null;
    }
    
    public int getMovesLeft() {
        return movesLeft;
    }
    
    public boolean isNaval() {
    	return unitType != null && unitType.isNaval();
    }
    
    /**
     * Get the visible amount of goods that is carried by this unit.
     *
     * @return The visible amount of goods carried by this <code>Unit</code>.
     */
    public int getVisibleGoodsCount() {
        return (visibleGoodsCount >= 0) ? visibleGoodsCount : getGoodsSpaceTaken();
    }
    
    public int getGoodsSpaceTaken() {
    	return 0;
//        if (!canCarryGoods()) {
//        	return 0;
//        }
//        GoodsContainer gc = getGoodsContainer();
//        return (gc == null) ? 0 : gc.getSpaceTaken();
    }
    
    public String getOccupationKey(Player player) {
        return (player.equals(owner))
            ? ((isDamaged())
                ? "model.unit.occupation.underRepair"
                : (getTradeRoute() != null)
                ? "model.unit.occupation.inTradeRoute"
                : (getDestination() != null)
                ? "model.unit.occupation.goingSomewhere"
                : (getState() == Unit.UnitState.IMPROVING && getWorkImprovement() != null)
                ? (getWorkImprovement().type.getId() + ".occupationString")
                : (getState() == Unit.UnitState.ACTIVE && getMovesLeft() <= 0)
                ? "model.unit.occupation.activeNoMovesLeft"
                : ("model.unit.occupation."
                    + getState().toString().toLowerCase(Locale.US)))
            : (isNaval())
            ? Integer.toString(getVisibleGoodsCount())
            : "model.unit.occupation.activeNoMovesLeft";
    }
	
    public static class Xml extends XmlNodeParser {
        
        private boolean secoundLevel = false;
        private Unit containerUnit = null;
        
        public Xml(XmlNodeParser parent) {
            super(parent);
        }

        @Override
        public void startElement(XmlNodeAttributes attr) {
            String unitTypeStr = attr.getStrAttribute("unitType");
            String unitRoleStr = attr.getStrAttribute("role");
            
            UnitType unitType = game.specification.unitTypes.getById(unitTypeStr);
            Unit unit = new Unit();
            unit.id = attr.getStrAttribute("id");
            unit.unitRole = game.specification.unitRoles.getById(unitRoleStr);
            unit.unitType = unitType;
            unit.state = UnitState.valueOf(attr.getStrAttribute("state").toUpperCase());
            unit.movesLeft = attr.getIntAttribute("movesLeft");
            unit.hitPoints = attr.getIntAttribute("hitPoints");
            unit.visibleGoodsCount = attr.getIntAttribute("visibleGoodsCount", -1);
            
            Tile.Xml tileXmlParser = getParentXmlParser();
            unit.tile = tileXmlParser.tile;
            if (containerUnit == null) {
                containerUnit = unit;
                tileXmlParser.tile.units.add(unit);
            } else {
                secoundLevel = true;
                containerUnit.addUnit(unit);
            }
            
            String ownerStr = attr.getStrAttribute("owner");
            Player owner = game.players.getById(ownerStr);
            unit.owner = owner;
            owner.units.add(unit);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (secoundLevel) {
                secoundLevel = false;
            } else {
                containerUnit = null;
            }
        }
        
        @Override
        public String getTagName() {
            return "unit";
        }
    }
    
    /**
     * A predicate that can be applied to a unit.
     */
    public static abstract class UnitPredicate {
        public abstract boolean obtains(Unit unit);
    }

    /**
     * A predicate for determining active units.
     */
    public static class ActivePredicate extends UnitPredicate {

        /**
         * Is the unit active and going nowhere, and thus available to
         * be moved by the player?
         *
         * @return True if the unit can be moved.
         */
        public boolean obtains(Unit unit) {
            return unit.couldMove();
        }
    }
}
