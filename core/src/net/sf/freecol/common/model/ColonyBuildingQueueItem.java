package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.BuildableType;
import net.sf.freecol.common.model.specification.BuildingType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class ColonyBuildingQueueItem implements Identifiable {
	
	private final BuildableType item;
	
	public ColonyBuildingQueueItem(BuildableType item) {
		this.item = item;
	}
	
	@Override
	public String getId() {
		return item.getId();
	}
	
	public BuildableType getType() {
		return item;
	}
	
	public String toString() {
		String st = "";
		if (item.isUnitType()) {
			st = "unit: ";
		}
		if (item.isBuildingType()) {
			st = "building: ";
		}
		return st + getId();
	}
	
	public static class Xml extends XmlNodeParser {

		@Override
		public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttribute("id");
			BuildableType item = null;
			
			BuildingType buildingType = Specification.instance.buildingTypes.getByIdOrNull(id);
			if (buildingType != null) {
				item = buildingType;
			}
			UnitType unitType = Specification.instance.unitTypes.getByIdOrNull(id);
			if (unitType != null) {
				item = unitType;
			}
			if (item == null) {
				throw new IllegalStateException("can not find unitType or buildingType by id: " + id);
			}
			ColonyBuildingQueueItem qitem = new ColonyBuildingQueueItem(item);
			nodeObject = qitem;
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
