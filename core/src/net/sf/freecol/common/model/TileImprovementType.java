package net.sf.freecol.common.model;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class TileImprovementType implements Identifiable {
	public static final String ROAD_MODEL_IMPROVEMENT_TYPE_ID = "model.improvement.road";
	public static final String RIVER_IMPROVEMENT_TYPE_ID = "model.improvement.river";
	public static final String PLOWED_IMPROVEMENT_TYPE_ID = "model.improvement.plow";
	
	public final String id;
	
	private int movementCost = 0;	
	
	@Override
	public String getId() {
		return id;
	}
	
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
	
    public int getMoveCost(int originalCost) {
        if (movementCost > 0 && movementCost < originalCost) {
            return movementCost;
        } else {
            return originalCost;
        }
    }
    
	public static class Xml extends XmlNodeParser {

		@Override
        public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttribute("id");
			TileImprovementType entity = new TileImprovementType(id);
			entity.movementCost = attr.getIntAttribute("movement-cost", 0);
			
			nodeObject = entity;
		}

		@Override
		public String getTagName() {
		    return tagName();
		}
		
        public static String tagName() {
            return "tileimprovement-type";
        }
	}
}

