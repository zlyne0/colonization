package net.sf.freecol.common.model;

import org.xml.sax.Attributes;

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
		public void startElement(String qName, Attributes attributes) {
			String id = getStrAttribute(attributes, "id");
			TileImprovementType tileImprovementType = new TileImprovementType(id);
			
			specification.tileImprovementTypes.add(tileImprovementType);
		}

		@Override
		public String getTagName() {
			return "tileimprovement-type";
		}
	}
}

