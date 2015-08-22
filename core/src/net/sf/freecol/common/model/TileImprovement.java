package net.sf.freecol.common.model;

import java.util.List;

import promitech.colonization.Direction;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class TileImprovement implements Identifiable {
    
    private String id;
    
	public final TileImprovementType type;
	public final String style;
	public int magnitude = 0;
	private int turns = 0;
	private long connected = 0L;
	
	public TileImprovement(TileImprovementType type, String style) {
		this.type = type;
		if (style != null) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < style.length(); i++) {
				char c = style.charAt(i);
				if (Character.digit(c, Character.MAX_RADIX) < 0) {
					break;
				}
				sb.append((c == '0') ? "0" : "1");
			}
			this.style = sb.toString();		
		} else {
			this.style = style;
		}
	}

	@Override
	public String getId() {
	    return id;
	}
	
	public boolean isComplete() {
		return turns <= 0;
	}
	
    public int getMoveCost(Direction direction, int moveCost) {
        return (isComplete() && isConnectedTo(direction))
            ? type.getMoveCost(moveCost)
            : moveCost;
    }
	
    public boolean isConnectedTo(Direction direction) {
        return (connected & (1 << direction.ordinal())) != 0;
    }

    private long getConnectionsFromStyle() {
        long conn = 0L;
        if (style != null) {
            List<Direction> directions = getConnectionDirections();
            if (directions != null) {
                for (int i = 0; i < directions.size(); i++) {
                    if (style.charAt(i) != '0') {
                        conn |= 1L << directions.get(i).ordinal();
                    }
                }
            }
        }
        return conn;
    }
    
    private List<Direction> getConnectionDirections() {
    	if (isRoad()) {
    		return Direction.allDirections;
    	} 
    	if (isRiver()) {
    		return Direction.longSides;
    	}
    	return null;
    }

    public boolean isRiver() {
        return "model.improvement.river".equals(type.getId());
    }
    
    public boolean isRoad() {
        return "model.improvement.road".equals(type.getId());
    }
    
    public String toString() {
    	return id + " type: " + type.getId();
    }
    
	public static class Xml extends XmlNodeParser {

		@Override
        public void startElement(XmlNodeAttributes attr) {
			String typeStr = attr.getStrAttribute("type");
			String style = attr.getStrAttribute("style");
			TileImprovementType type = game.specification.tileImprovementTypes.getById(typeStr);
			TileImprovement tileImprovement = new TileImprovement(type, style);
			tileImprovement.magnitude = attr.getIntAttribute("magnitude", 0);
			tileImprovement.turns = attr.getIntAttribute("turns");
			tileImprovement.id = attr.getStrAttribute("id");
			tileImprovement.connected = tileImprovement.getConnectionsFromStyle();
			
			nodeObject = tileImprovement;
		}

		@Override
		public String getTagName() {
		    return tagName();
		}
		
		public static String tagName() {
		    return "tileimprovement";
		}
	}
}
