package net.sf.freecol.common.model;

public final class TileType {

	private static final String GREAT_RIVER = "model.tile.greatRiver";
    private static final String OCEAN = "model.tile.ocean";
	private static final String HIGH_SEAS = "model.tile.highSeas";
	
	private int order;
	private String type;
	boolean isForest;
	
	public TileType(String id, boolean isForest) {
		this.type = id;
		this.isForest = isForest;
	}

	public String toString() {
		return type + ":" + order;
	}
	
	public boolean isWater() {
		return type.equals(OCEAN) || type.equals(HIGH_SEAS) || type.equals(GREAT_RIVER);
	}

	public boolean isLand() {
		return !isWater();
	}

	public boolean hasTheSameTerain(TileType tType) {
	    return !hasDifferentTerain(tType);
	}
	
	public boolean hasDifferentTerain(TileType tType) {
		return !this.type.equals(tType.type);
	}

	public boolean isForested() {
		return type.indexOf("Forest") != -1;
	}
	
	public String getTypeStr() {
		return type;
	}

	public int getOrder() {
		return order;
	}
	
	public void setOrder(int order) {
		this.order = order;
	}
}
