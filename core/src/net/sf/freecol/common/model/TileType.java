package net.sf.freecol.common.model;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public final class TileType extends ObjectWithFeatures {

	private static final String MODEL_TILE_LAKE = "model.tile.lake";
	private static final String GREAT_RIVER = "model.tile.greatRiver";
    private static final String OCEAN = "model.tile.ocean";
	private static final String HIGH_SEAS = "model.tile.highSeas";
	
	boolean isForest;
	
	public TileType(String id, boolean isForest) {
		super(id);
		this.isForest = isForest;
	}
    
	public String toString() {
		return id + ":" + getInsertOrder();
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

    public boolean isDirectlyHighSeasConnected() {
        return hasAbility(Ability.MOVE_TO_EUROPE);
    }
	
	public static class Xml extends XmlNodeParser {
		public Xml() {
            addNodeForMapIdEntities("modifiers", Modifier.class);
            addNodeForMapIdEntities("abilities", Ability.class);
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
