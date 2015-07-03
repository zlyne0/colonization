package net.sf.freecol.common.model;

public abstract class Settlement {
    protected String name;
    public SettlementType settlementType;
    protected Player owner;
    
    protected MapIdEntities<SettlementType> settlementTypes = new MapIdEntities<SettlementType>();
    
    public abstract String getImageKey();
}
