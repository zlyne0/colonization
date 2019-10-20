package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.GoodsType;

public class GoodMaxProductionLocation {
	
    public static GoodMaxProductionLocation updateFromColonyTile(
    		GoodsType goodsType, 
    		int goodsQuantity,
    		GoodMaxProductionLocation maxProd,
    		ColonyTile colonyTile
	) {
    	if (goodsQuantity > 0) {
    		if (maxProd == null) {
    			maxProd = new GoodMaxProductionLocation(goodsType, goodsQuantity, colonyTile);
    		} else {
    			if (maxProd.hasLessProduction(goodsQuantity)) {
    				maxProd.setProduction(goodsQuantity, colonyTile);
    			}
    		}
    	}
    	return maxProd;
    }
	
    public static GoodMaxProductionLocation updateFromBuilding(
		GoodsType goodsType, 
		int goodsQuantity,
		GoodMaxProductionLocation maxProd,
		Building building
	) {
        if (goodsQuantity > 0) {
            if (maxProd == null) {
                maxProd = new GoodMaxProductionLocation(goodsType, goodsQuantity, building);
            } else {
                if (maxProd.hasLessProduction(goodsQuantity)) {
                    maxProd.setProduction(goodsQuantity, building);
                }
            }
        }
    	return maxProd;
    }
    
    private final GoodsType goodsType;
    private int production;
    public Production tileTypeInitProduction;
    private ColonyTile colonyTile;
    private Building building;

    GoodMaxProductionLocation(GoodsType goodsType, int goodQuantity, Building building) {
        this.goodsType = goodsType;
        this.production = goodQuantity;
        setLocation(building);
    }
    
    GoodMaxProductionLocation(GoodsType goodsType, int goodQuantity, ColonyTile colonyTile) {
        this.goodsType = goodsType;
        this.production = goodQuantity;
        setLocation(colonyTile);
    }
    
    private void setLocation(ColonyTile colonyTile) {
        this.colonyTile = colonyTile;
        this.building = null;
    }
    
    private void setLocation(Building building) {
        this.colonyTile = null;
        this.building = building;
    }

    public boolean hasLessProduction(int goodQuantity) {
        return production < goodQuantity;
    }

    public void setProduction(int goodQuantity, ColonyTile colonyTile) {
        production = goodQuantity;
        setLocation(colonyTile);
    }
    
    public void setProduction(int goodQuantity, Building building) {
        production = goodQuantity;
        setLocation(building);
    }
    
    public String toString() {
        String st = "goods: " + goodsType + " quantity: " + production;
        if (colonyTile != null) {
            st += " colonyTile " + this.colonyTile.getId();
        }
        if (building != null) {
            st += " building " + this.building.getId();
        }
        return st;
    }

    public GoodsType getGoodsType() {
        return goodsType;
    }

    public int getProduction() {
        return production;
    }

	public Building getBuilding() {
		return building;
	}

	public ColonyTile getColonyTile() {
		return colonyTile;
	}
	
	public ProductionLocation getProductionLocation() {
	    if (colonyTile != null) {
	        return colonyTile;
	    }
	    return building;
	}
}
