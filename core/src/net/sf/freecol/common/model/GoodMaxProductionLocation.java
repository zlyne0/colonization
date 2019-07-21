package net.sf.freecol.common.model;

import java.util.Comparator;

import net.sf.freecol.common.model.specification.GoodsType;

public class GoodMaxProductionLocation {
    public static final Comparator<GoodMaxProductionLocation> GOODS_INSERT_ORDER_ASC_COMPARATOR = new Comparator<GoodMaxProductionLocation>() {
        @Override
        public int compare(GoodMaxProductionLocation o1, GoodMaxProductionLocation o2) {
            return ObjectWithId.INSERT_ORDER_ASC_COMPARATOR.compare(o1.getGoodsType(), o2.getGoodsType());
        }
    };
    
    private final GoodsType goodsType;
    private int production;
    public Production tileTypeInitProduction;
    ColonyTile colonyTile;
    Building building;

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
}
