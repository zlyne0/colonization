package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.List;

import promitech.colonization.Direction;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class TileImprovement implements Identifiable {
    
    private final String id;
    
	public final TileImprovementType type;
	private String style;
	public int magnitude = 0;
	private int turns = 0;
	private long connected = 0L;

	public TileImprovement(String id, TileImprovementType type, String style) {
		this.id = id;
		this.magnitude = type.getMagnitude();
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
		connected = getConnectionsFromStyle();
	}

	public TileImprovement(IdGenerator idGenerator, TileImprovementType type) {
		this.id = idGenerator.nextId(TileImprovement.class);
		this.type = type;
		this.magnitude = type.getMagnitude();
		this.style = null;
	}

	@Override
	public String getId() {
	    return id;
	}
	
	public boolean isComplete() {
		return turns <= 0;
	}

	public String getStyle() {
		return style;
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
    	if (type.isRoad()) {
    		return Direction.allDirections;
    	} 
    	if (type.isRiver()) {
    		return Direction.longSides;
    	}
    	throw new IllegalStateException("can not generate connection directions for tile improvement " + this);
    }

    public String toString() {
    	return id + " type: " + type.getId();
    }

    private void setConnection(Direction direction, boolean val) {
    	if (val) {
    		connected |= 1 << direction.ordinal();
    	} else {
    		connected &= ~(1 << direction.ordinal());
    	}
    }

	public TileImprovement addConnection(Direction direction) {
		setConnection(direction, true);
		return this;
	}

	public TileImprovement removeConnection(Direction direction) {
		setConnection(direction, false);
		return this;
	}
    
	public void updateStyle() {
		this.style = encodeConnections();
	}
	
    private String encodeConnections() {
        List<Direction> dirns = getConnectionDirections();
        if (dirns == null) {
        	return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Direction d : dirns) {
            sb.append((isConnectedTo(d)) ? Integer.toString(magnitude) : "0");
        }
        return sb.toString();
    }
	
	public static class Xml extends XmlNodeParser<TileImprovement> {

		private static final String ATTR_TURNS = "turns";
		private static final String ATTR_MAGNITUDE = "magnitude";
		private static final String ATTR_STYLE = "style";
		private static final String ATTR_TYPE = "type";

		@Override
        public void startElement(XmlNodeAttributes attr) {
			String typeStr = attr.getStrAttribute(ATTR_TYPE);
			String style = attr.getStrAttribute(ATTR_STYLE);
			String id = attr.getStrAttribute(ATTR_ID);
			TileImprovementType type = Specification.instance.tileImprovementTypes.getById(typeStr);
			TileImprovement tileImprovement = new TileImprovement(id, type, style);
			tileImprovement.magnitude = attr.getIntAttribute(ATTR_MAGNITUDE, 0);
			tileImprovement.turns = attr.getIntAttribute(ATTR_TURNS);
			
			nodeObject = tileImprovement;
		}

		@Override
		public void startWriteAttr(TileImprovement ti, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(ti);

			attr.set(ATTR_TYPE, ti.type);
			attr.set(ATTR_STYLE, ti.style);
			attr.set(ATTR_MAGNITUDE, ti.magnitude, 0);
			attr.set(ATTR_TURNS, ti.turns);
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
