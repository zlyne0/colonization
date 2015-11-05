package net.sf.freecol.common.model;

import net.sf.freecol.common.model.UnitContainer.NoAddReason;
import net.sf.freecol.common.model.specification.BuildingType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Building extends ObjectWithId {

    public BuildingType buildingType;
    public final MapIdEntities<Unit> workers = new MapIdEntities<Unit>();
    
    public Building(String id) {
        super(id);
    }

	public UnitContainer.NoAddReason getNoAddReason(Unit unit) {
		if (!unit.isPerson()) {
			return UnitContainer.NoAddReason.WRONG_TYPE;
		}
		UnitContainer.NoAddReason reason = buildingType.getNoAddReason(unit.unitType);
		if (reason == NoAddReason.NONE) {
			int workersSpaceTaken = 0;
			for (Unit u : workers.entities()) {
				workersSpaceTaken += u.unitType.getSpaceTaken();
			}
			if (unit.unitType.getSpaceTaken() + workersSpaceTaken > buildingType.getWorkplaces()) {
				return UnitContainer.NoAddReason.CAPACITY_EXCEEDED;
			}
		}
		return reason;
	}    
    
	public void baseProduction(ProductionSummary summary) {
        buildingType.productionInfo.addProductionToSummary(summary, workers.entities());
	}
	
//    public void realProduction(ProductionSummary warehouse) {
//        ProductionSummary baseProd = new ProductionSummary();
//        ProductionSummary baseConsum = new ProductionSummary();
//        
//        for (Production bProd : buildingType.productionInfo.productions) {
//            bProd.sumProductionType(baseProd, workers.entities());
//            bProd.sumProductionType(baseConsum, workers.entities());
//        }
//        if (warehouse.hasMoreOrEquals(baseConsum)) {
//            // realProd = baseProd
//            // realConsum = baseConsum
//        } else {
//            ProductionSummary realConsum = 
//        }
//    }
	
	public String toString() {
	    return "id = " + getId() + ", type = " + buildingType;
	}
	
    public static class Xml extends XmlNodeParser {

        public Xml() {
            addNodeForMapIdEntities("workers", Unit.class);
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute("id");
            Building b = new Building(id);
            
            String buildingTypeId = attr.getStrAttribute("buildingType");
            if (buildingTypeId == null) {
                throw new IllegalStateException("can not find buildingType for building " + id);
            }
            BuildingType buildingType = game.specification.buildingTypes.getById(buildingTypeId);
            b.buildingType = buildingType;
            
            nodeObject = b;
        }

        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "building";
        }
    }
}
