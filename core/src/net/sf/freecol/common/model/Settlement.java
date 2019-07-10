package net.sf.freecol.common.model;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;

public abstract class Settlement extends ObjectWithId implements UnitLocation {
    
    public static final int FOOD_PER_COLONIST = 200;
    
	protected String name;
    public final SettlementType settlementType;
    protected Player owner;
    public Tile tile;
    protected boolean coastland = false;

    public Settlement(String id, SettlementType settlementType) {
		super(id);
		this.settlementType = settlementType;
	}
    
    public abstract String getImageKey();

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
	    this.owner = owner;
	}
	
	public void changeOwner(Player newOwner) {
		if (this.owner != null) {
			this.owner.settlements.removeId(this);
		}
		newOwner.addSettlement(this);
		
		for (Unit unit : getUnits().entities()) {
			unit.captureByPlayer(newOwner);
		}
		for (Unit unit : tile.getUnits().entities()) {
			unit.captureByPlayer(newOwner);
		}
	}
	
    public boolean isCoastland() {
    	return coastland;
    }
	
	public boolean isColony() {
		return false;
	}
	
	public boolean isIndianSettlement() {
		return false;
	}

    public Colony asColony() {
        return (Colony)this;
    }

	public IndianSettlement asIndianSettlement() {
	    return (IndianSettlement)this;
	}
    
    public boolean canBombardEnemyShip() {
        return isCoastland() && hasAbility(Ability.BOMBARD_SHIPS);
    }

    public boolean hasGoodsToEquipRole(UnitRole unitRole) {
    	return UnitRoleLogic.hasContainerRequiredGoods(getGoodsContainer(), unitRole);
    }
    
    public abstract GoodsContainer getGoodsContainer();
    
    public abstract boolean hasAbility(String abilityCode);
    
    public abstract int applyModifiers(String abilityCode, int val);
    
    public abstract void addGoods(String goodsTypeId, int quantity);

	public abstract boolean isContainsTile(Tile improvingTile);
	
	public abstract void initMaxPossibleProductionOnTile(Tile tile);

	public abstract ProductionSummary productionSummary();

	public abstract void addModifiersTo(ObjectWithFeatures mods, String modifierCode);

	public void removeFromMap(Game game) {
		for (int x=0; x<game.map.width; x++) {
			for (int y=0; y<game.map.height; y++) {
				Tile t = game.map.getSafeTile(x, y);
				if (t.isOwnBySettlement(this)) {
					t.resetOwningSettlement();
				}
			}
		}
		tile.setSettlement(null);
		tile = null;
	}

	public void removeFromPlayer() {
		owner.settlements.removeId(this);
		owner = null;
	}
}
