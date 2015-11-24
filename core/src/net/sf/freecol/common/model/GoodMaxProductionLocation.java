package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.GoodsType;

public class GoodMaxProductionLocation {
    final GoodsType goodsType;
    int production;
    ColonyTile colonyTile;
    Building building;
    
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
}
