package net.sf.freecol.common.model;

import java.util.List;

import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.RequiredGoods;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class ColonyBuildingQueueItem implements Identifiable {
	private final String id;
	
	private final UnitType unitType;
	private final BuildingType buildingType;
	
	public ColonyBuildingQueueItem(UnitType unitType) {
		this(unitType, null);
	}
	
	public ColonyBuildingQueueItem(BuildingType buildingType) {
		this(null, buildingType);
	}
	
	public ColonyBuildingQueueItem(UnitType unitType, BuildingType buildingType) {
		if (unitType != null) {
			id = unitType.getId();
		} else {
			if (buildingType != null) {
				id = buildingType.getId();
			} else {
				this.id = "should throw exception";
				throw new IllegalStateException("unit type and building type can not be null");
			}
		} 
		this.unitType = unitType;
		this.buildingType = buildingType;
	}
	
	@Override
	public String getId() {
		return id;
	}
	
	public List<RequiredGoods> requiredGoods() {
		if (unitType != null) {
			return unitType.requiredGoods.sortedEntities();
		}
		if (buildingType != null) {
			return buildingType.requiredGoods.sortedEntities();
		}
		throw new IllegalStateException("there is no build item type");
	}
/*
	public boolean doesNotNeedGoodsToBuild() {
		return requiredGoods().isEmpty();
	}
	
	public int getRequiredPopulation() {
		if (unitType != null) {
			return 1;
		}
		return buildingType.getRequiredPopulation();
	}
*/	
	
	public String toString() {
		String st = "";
		if (unitType != null) {
			st = "unit: ";
		}
		if (buildingType != null) {
			st = "building: ";
		}
		return st + id;
	}
	
	public static class Xml extends XmlNodeParser {

		@Override
		public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttribute("id");
			BuildingType buildingType = Specification.instance.buildingTypes.getByIdOrNull(id);
			UnitType unitType = Specification.instance.unitTypes.getByIdOrNull(id);
			
			ColonyBuildingQueueItem item = new ColonyBuildingQueueItem(unitType, buildingType);
			nodeObject = item;
		}

		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "buildQueueItem";
		}
	}

}
