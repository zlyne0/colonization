package net.sf.freecol.common.model;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public final class TileType implements Identifiable, SortableEntity {

	private static final String MODEL_TILE_LAKE = "model.tile.lake";
	private static final String GREAT_RIVER = "model.tile.greatRiver";
    private static final String OCEAN = "model.tile.ocean";
	private static final String HIGH_SEAS = "model.tile.highSeas";
	
	private final String id;
	private int order;
	boolean isForest;
	
	public TileType(String id, boolean isForest) {
		this.id = id;
		this.isForest = isForest;
	}

    @Override
    public String getId() {
        return id;
    }
    
	public String toString() {
		return id + ":" + order;
	}
	
	public boolean isWater() {
		return id.equals(OCEAN) || id.equals(HIGH_SEAS) || id.equals(GREAT_RIVER) || id.equals(MODEL_TILE_LAKE);
	}

	public boolean isHighSea() {
		return id.equals(HIGH_SEAS);
	}
	
	public boolean isLand() {
		return !isWater();
	}

	public boolean hasTheSameTerain(TileType tType) {
	    return !hasDifferentTerain(tType);
	}
	
	public boolean hasDifferentTerain(TileType tType) {
		return !this.id.equals(tType.id);
	}

	public boolean isForested() {
		return isForest;
	}

	public int getOrder() {
		return order;
	}
	
	public void setOrder(int order) {
		this.order = order;
	}
	
	public static class Xml extends XmlNodeParser {
		public Xml(XmlNodeParser parent) {
			super(parent);
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttribute("id");
			boolean isForest = attr.getBooleanAttribute("is-forest");
			nodeObject = new TileType(id, isForest);
		}

		@Override
		public String getTagName() {
			return "tile-type";
		}
		
		public static String tagName() {
		    return "tile-type";
		}
	}
}
