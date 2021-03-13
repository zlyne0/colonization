package net.sf.freecol.common.model.colonyproduction;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;

public class MaxGoodsProductionLocation {

    GoodsType goodsType;
    int production;
    Production tileTypeInitProduction;
    Tile colonyTile;
    BuildingType buildingType;
	
    public String toString() {
        String st = "goods: " + goodsType + " quantity: " + production;
        if (colonyTile != null) {
            st += " colonyTile " + this.colonyTile.getId();
        }
        if (buildingType != null) {
            st += " buildingType " + this.buildingType.getId();
        }
        return st;
    }

	public GoodsType getGoodsType() {
		return goodsType;
	}

	public int getProduction() {
		return production;
	}

    public Tile getColonyTile() {
        return colonyTile;
    }
}
