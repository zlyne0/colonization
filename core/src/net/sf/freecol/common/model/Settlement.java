package net.sf.freecol.common.model;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.ui.resources.StringTemplate;

public abstract class Settlement extends ObjectWithId implements UnitLocation {
    
    public static final int FOOD_PER_COLONIST = 200;
    
	protected String name;
    public final SettlementType settlementType;
    protected Player owner;
    public Tile tile;
    protected boolean coastland = false;
    protected GoodsContainer goodsContainer;

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
    	return goodsContainer.hasGoodsQuantity(unitRole.requiredGoods);
    }
    
    public void changeUnitRole(Unit unit, UnitRole newUnitRole, ObjectWithFeatures unitLocationFeatures) {
    	if (!newUnitRole.isAvailableTo(unit.unitType, unitLocationFeatures)) {
    		throw new IllegalStateException("can not change role for unit: " + unit + " from " + unit.unitRole + " to " + newUnitRole);
    	}

    	ProductionSummary required = new ProductionSummary();
		int maxAvailableRoleCount = UnitRoleLogic.maximumAvailableRequiredGoods(unit, newUnitRole, goodsContainer, required);
    	unit.changeRole(newUnitRole, maxAvailableRoleCount);
    	goodsContainer.decreaseGoodsQuantity(required);
    }
    
    
    public GoodsContainer getGoodsContainer() {
        return goodsContainer;
    }
    
	public void addGoods(String goodsTypeId, int quantity) {
		goodsContainer.increaseGoodsQuantity(goodsTypeId, quantity);
	}
	
    public void removeExcessedStorableGoods() {
        int warehouseCapacity = warehouseCapacity();
        for (GoodsType gt : Specification.instance.goodsTypes.entities()) {
            if (!gt.isStorable() || gt.isFood()) {
                continue;
            }
            int goodsAmount = goodsContainer.goodsAmount(gt);
            if (goodsAmount > warehouseCapacity) {
                int wasteAmount = goodsAmount - warehouseCapacity;
                goodsContainer.decreaseGoodsQuantity(gt, wasteAmount);
                
                if (owner.isHuman()) {
                	StringTemplate st = StringTemplate.template("model.building.warehouseWaste")
            			.add("%colony%", getName())
            			.addName("%goods%", gt)
            			.addAmount("%waste%", wasteAmount);
                	owner.eventsNotifications.addMessageNotification(st);
                }
            }
        }
    }
	
	public int maxGoodsAmountToFillWarehouseCapacity(GoodsType goodsType, int goodsAmount) {
		int freeSpace = warehouseCapacity() - goodsContainer.goodsAmount(goodsType);
		if (freeSpace < 0) {
			freeSpace = 0;
		}
		if (freeSpace < goodsAmount) {
			return freeSpace;
		}
		return goodsAmount;
	}
    
	public abstract int warehouseCapacity();
    
    public abstract boolean hasAbility(String abilityCode);
    
    public abstract int applyModifiers(String abilityCode, int val);
    
	public abstract void updateProductionToMaxPossible(Tile tile);
    
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
