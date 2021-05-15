package net.sf.freecol.common.model;

import net.sf.freecol.common.model.UnitContainer.NoAddReason;
import net.sf.freecol.common.model.specification.BuildingType;

import java.io.IOException;

import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class Building extends ObjectWithId implements ProductionLocation, UnitLocation {

    public BuildingType buildingType;
    private final MapIdEntities<Unit> workers = new MapIdEntities<Unit>();

    public Building(IdGenerator idGenerator, BuildingType aBuildingType) {
    	this(idGenerator.nextId(Building.class), aBuildingType);
    }
    
    private Building(String id, BuildingType aBuildingType) {
        super(id);
        this.buildingType = aBuildingType;
    }

	@Override
	public String productionLocationId() {
		return buildingType.getId();
	}

	public UnitContainer.NoAddReason getNoAddReason(UnitType unitType) {
		int workersSpaceTaken = 0;
		for (Unit u : workers.entities()) {
			workersSpaceTaken += u.unitType.getSpaceTaken();
		}
		return buildingType.addWorkerToBuildingReason(unitType, workersSpaceTaken);
	}    
    
    public boolean canAddWorker(UnitType unitType) {
        NoAddReason reason = getNoAddReason(unitType);
//        if (NoAddReason.NONE != reason) {
//            System.out.println("can not add unit to " + buildingType + " because " + reason);
//        }
        return NoAddReason.NONE == reason;
    }

	public String toString() {
	    return "id = " + getId() + ", type = " + buildingType;
	}
	
	public void upgrade(BuildingType aBuildingType) {
		this.buildingType = aBuildingType;
	}
	
	public MapIdEntities<Unit> damageBuilding() {
		buildingType = buildingType.getUpgradesFrom();
		if (workers.isEmpty()) {
			return MapIdEntities.unmodifiableEmpty();
		}
		
		MapIdEntities<Unit> eject = new MapIdEntities<Unit>();
		getWorkersToEject(eject);
		return eject;
	}

	/**
	 * Method return workers which can not work in building because of type or workspace capacity
	 * @param ejectWorkers
	 */
	public void getWorkersToEject(MapIdEntities<Unit> ejectWorkers) {
		if (workers.isEmpty()) {
			return;
		}
		
		int workersSpaceTaken = 0;
		for (Unit worker : workers.entities()) {
			UnitContainer.NoAddReason reason = buildingType.getNoAddReason(worker.unitType);
			if (reason != NoAddReason.NONE) {
				ejectWorkers.add(worker);
				continue;
			}
			if (workersSpaceTaken + worker.unitType.getSpaceTaken() > buildingType.getWorkplaces()) {
				ejectWorkers.add(worker);
			} else {
				workersSpaceTaken += worker.unitType.getSpaceTaken();
			}
		}
	}
	
    @Override
    public MapIdEntitiesReadOnly<Unit> getUnits() {
        return workers;
    }

    @Override
    public void addUnit(Unit unit) {
        workers.add(unit);
    }

    @Override
    public void removeUnit(Unit unit) {
    	unit.reduceMovesLeftToZero();
        workers.removeId(unit);
    }
    
    public static class Xml extends XmlNodeParser<Building> {

        private static final String ATTR_BUILDING_TYPE = "buildingType";

        public Xml() {
            addNode(Unit.class, new ObjectFromNodeSetter<Building, Unit>() {
                @Override
                public void set(Building target, Unit entity) {
                    entity.changeUnitLocation(target);
                }

                @Override
                public void generateXml(Building source, ChildObject2XmlCustomeHandler<Unit> xmlGenerator) throws IOException {
                    xmlGenerator.generateXmlFromCollection(source.workers.entities());
                }
            });
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute(ATTR_ID);
            String buildingTypeId = attr.getStrAttributeNotNull(ATTR_BUILDING_TYPE);
            BuildingType buildingType = Specification.instance.buildingTypes.getById(buildingTypeId);
            Building b = new Building(id, buildingType);
            nodeObject = b;
        }

        @Override
        public void startWriteAttr(Building n, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(n);
        	attr.set(ATTR_BUILDING_TYPE, n.buildingType);
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
