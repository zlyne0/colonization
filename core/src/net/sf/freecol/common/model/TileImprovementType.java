package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.Scope;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class TileImprovementType extends ObjectWithFeatures {
	public static final String ROAD_MODEL_IMPROVEMENT_TYPE_ID = "model.improvement.road";
	public static final String RIVER_IMPROVEMENT_TYPE_ID = "model.improvement.river";
	public static final String PLOWED_IMPROVEMENT_TYPE_ID = "model.improvement.plow";
	public static final String CLEAR_FOREST_IMPROVEMENT_TYPE_ID = "model.improvement.clearForest";
	
	private int addWorkTurns;
	private int movementCost = 0;
	private boolean natural = false;
	private String requiredRoleId;
	
	/** The amount of the equipment expended in making this improvement. */
	private int expendedAmount;
	
	public TileImprovementType(String id) {
		super(id);
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
    
	public boolean isNatural() {
		return natural;
	}
    
	public boolean isSatisfyUnitRole(UnitRole unitRole) {
		return unitRole.getId().equals(requiredRoleId);
	}

	public int getExpendedAmount() {
		return expendedAmount;
	}

	public int getAddWorkTurns() {
		return addWorkTurns;
	}
	
	public static class Xml extends XmlNodeParser {
		public Xml() {
            addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
            addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
            addNode(Scope.class, ObjectWithFeatures.OBJECT_SCOPE_NODE_SETTER);
		}
		
		@Override
        public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttribute("id");
			TileImprovementType entity = new TileImprovementType(id);
			entity.movementCost = attr.getIntAttribute("movement-cost", 0);
			entity.natural = attr.getBooleanAttribute("natural");
			entity.requiredRoleId = attr.getStrAttribute("required-role");
			entity.expendedAmount = attr.getIntAttribute("expended-amount", 0);
			entity.addWorkTurns = attr.getIntAttribute("add-work-turns", 0);
			
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

