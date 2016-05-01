package net.sf.freecol.common.model;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;

public abstract class Settlement implements Identifiable {
	protected String id;
    protected String name;
    public SettlementType settlementType;
    protected Player owner;
    public Tile tile;
    protected boolean coastland = false;
    
    protected MapIdEntities<SettlementType> settlementTypes = new MapIdEntities<SettlementType>();
    
    public abstract String getImageKey();

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
	    this.owner = owner;
	}
	
    public boolean isCoastland() {
    	return coastland;
    }
	
	public abstract boolean isColony();

	public Colony getColony() {
		return (Colony)this;
	}
	
    public boolean canBombardEnemyShip() {
        return hasAbility(Ability.BOMBARD_SHIPS);
    }

    public abstract boolean hasAbility(String abilityCode);
    
    public abstract int applyModifiers(String abilityCode, int val);
    
    public abstract void addGoods(String goodsTypeId, int quantity);

	public abstract boolean isContainsTile(Tile improvingTile);
	
	public abstract void initMaxPossibleProductionOnTile(Tile tile);

}
