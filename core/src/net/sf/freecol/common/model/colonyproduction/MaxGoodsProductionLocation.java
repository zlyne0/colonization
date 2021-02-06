package net.sf.freecol.common.model.colonyproduction;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.specification.GoodsType;

class MaxGoodsProductionLocation {

    GoodsType goodsType;
    int production;
    Production tileTypeInitProduction;
    Tile colonyTile;
    Building building;
	
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
