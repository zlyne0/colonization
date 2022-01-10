package net.sf.freecol.common.model.colonyproduction;

import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;

import java.util.Comparator;

public class MaxGoodsProductionLocation {

    public static final Comparator<MaxGoodsProductionLocation> quantityComparator = new Comparator<MaxGoodsProductionLocation>() {
        @Override
        public int compare(MaxGoodsProductionLocation o1, MaxGoodsProductionLocation o2) {
            return o2.getProduction() - o1.getProduction();
        }
    };

    public static MaxGoodsProductionLocation max(MaxGoodsProductionLocation a, MaxGoodsProductionLocation b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        if (a.production > b.production) {
            return a;
        } else {
            return b;
        }
    }

    GoodsType goodsType;
    int production;
    Production tileTypeInitProduction;
    Tile colonyTile;
    BuildingType buildingType;
	
    public String toString() {
        String st = "goods: " + goodsType + " quantity: " + production;
        if (colonyTile != null) {
            st += " colonyTile(" + this.colonyTile.getId() + ") " + this.colonyTile.getType().getId();
        }
        if (buildingType != null) {
            st += " buildingType " + this.buildingType.getId();
        }
        return st;
    }

    public String productionLocationId() {
        if (colonyTile != null) {
            return colonyTile.getId();
        }
        return buildingType.getId();
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

    public BuildingType getBuildingType() {
        return buildingType;
    }

    public Production getTileTypeInitProduction() {
        return tileTypeInitProduction;
    }

}
