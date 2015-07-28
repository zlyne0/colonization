package net.sf.freecol.common.model;

public abstract class Settlement implements Identifiable {
	protected String id;
    protected String name;
    public SettlementType settlementType;
    protected Player owner;
    public Tile tile;
    
    protected MapIdEntities<SettlementType> settlementTypes = new MapIdEntities<SettlementType>();
    
    public abstract String getImageKey();

	public String getId() {
		return id;
	}
}
