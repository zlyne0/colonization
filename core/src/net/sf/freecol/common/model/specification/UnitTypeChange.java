package net.sf.freecol.common.model.specification;

import java.util.EnumMap;
import java.util.Map;

import net.sf.freecol.common.model.Identifiable;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class UnitTypeChange implements Identifiable {
    public static enum ChangeType {
        EDUCATION,
        NATIVES,
        EXPERIENCE,
        LOST_CITY,
        PROMOTION,
        CREATION,
        ENTER_COLONY,
        INDEPENDENCE,
        CLEAR_SKILL,
        DEMOTION,
        CAPTURE,
        CONVERSION,
        UNDEAD
    }

	public static final Map<ChangeType, String> tags = new EnumMap<ChangeType, String>(ChangeType.class);
	static {
		tags.put(ChangeType.EDUCATION, "learnInSchool");
		tags.put(ChangeType.NATIVES, "learnFromNatives");
		tags.put(ChangeType.EXPERIENCE, "learnFromExperience");
		tags.put(ChangeType.LOST_CITY, "learnInLostCity");
		tags.put(ChangeType.PROMOTION, "promotion");
		tags.put(ChangeType.CLEAR_SKILL, "clearSkill");
		tags.put(ChangeType.DEMOTION, "demotion");
		tags.put(ChangeType.CAPTURE, "capture");
		tags.put(ChangeType.CREATION, "creation");
		tags.put(ChangeType.ENTER_COLONY, "enterColony");
		tags.put(ChangeType.INDEPENDENCE, "independence");
		tags.put(ChangeType.CONVERSION, "conversion");
		tags.put(ChangeType.UNDEAD, "undead");
	}
    
	private String id;
    protected final Map<ChangeType, Integer> changeTypes = new EnumMap<ChangeType, Integer>(ChangeType.class);
	
    protected int turnsToLearn = 0;

	@Override
	public String getId() {
		return id;
	}

	public String getNewUnitTypeId() {
		return id;
	}
	
    public final int getProbability(ChangeType type) {
        Integer result = changeTypes.get(type);
        return (result == null) ? 0 : result;
    }

	public boolean isPositiveProbability(ChangeType changeType) {
		return getProbability(changeType) > 0;
	}
    
	public static class Xml extends XmlNodeParser {
		public Xml(XmlNodeParser parent) {
			super(parent);
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
			UnitTypeChange unitTypeChange = new UnitTypeChange();
			unitTypeChange.id = attr.getStrAttribute("unit");

			unitTypeChange.turnsToLearn = attr.getIntAttribute("turnsToLearn", UNDEFINED);
			if (unitTypeChange.turnsToLearn > 0) {
            	unitTypeChange.changeTypes.put(ChangeType.EDUCATION, 100);
            }
            
			for (ChangeType type : ChangeType.values()) {
                int value = attr.getIntAttribute(tags.get(type), -1);
                if (value >= 0) {
                	unitTypeChange.changeTypes.put(type, Math.min(100, value));
                }
            }
			nodeObject = unitTypeChange;
		}

		@Override
		public String getTagName() {
			return "upgrade";
		}
	}
}
