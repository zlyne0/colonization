package net.sf.freecol.common.model;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class TileImprovementType implements Identifiable {
	public static final String ROAD_MODEL_IMPROVEMENT_TYPE_ID = "model.improvement.road";
	public static final String RIVER_IMPROVEMENT_TYPE_ID = "model.improvement.river";
	public static final String PLOWED_IMPROVEMENT_TYPE_ID = "model.improvement.plow";
	
	public final String id;
	
	public TileImprovementType(String id) {
		this.id = id;
	}

    public boolean isRiver() {
        return RIVER_IMPROVEMENT_TYPE_ID.equals(id);
    }

    public boolean isRoad() {
		return ROAD_MODEL_IMPROVEMENT_TYPE_ID.equals(id);
    }

    public boolean isPlowed() {
    	return PLOWED_IMPROVEMENT_TYPE_ID.equals(id);
    }
    
    public String getId() {
        return id;
    }
	
	public static class Xml extends XmlNodeParser {
		public Xml(XmlNodeParser parent) {
			super(parent);
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttribute("id");
			nodeObject = new TileImprovementType(id);
		}

		@Override
		public String getTagName() {
			return "tileimprovement-type";
		}
	}
}

