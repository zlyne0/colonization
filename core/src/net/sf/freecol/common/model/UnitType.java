package net.sf.freecol.common.model;

import java.util.Comparator;

import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.BuildableType;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.UnitTypeChange;
import net.sf.freecol.common.model.specification.UnitTypeChange.ChangeType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class UnitType extends BuildableType {
	
    public static final Comparator<UnitType> UNIT_TYPE_PRICE_COMPARATOR = new Comparator<UnitType>() {
		@Override
		public int compare(UnitType o1, UnitType o2) {
			return o1.getPrice() - o2.getPrice();
		}
	};
	
	public static final String FREE_COLONIST = "model.unit.freeColonist";
	public static final String WAGON_TRAIN = "model.unit.wagonTrain";
	
    private static final int DEFAULT_LINE_OF_SIGHT = 1;
    public static final int DEFAULT_MOVEMENT = 3;
    public static final int DEFAULT_OFFENCE = 0;
    public static final int DEFAULT_DEFENCE = 1;

    public final MapIdEntities<UnitTypeChange> unitTypeChanges = new MapIdEntities<UnitTypeChange>();
    public final MapIdEntities<UnitConsumption> unitConsumption = new MapIdEntities<UnitConsumption>();
    
    /**
     * The offence of this UnitType. Only Units with an offence value
     * greater than zero can attack.
     */
    private int offence = DEFAULT_OFFENCE;

    /** The defence of this UnitType. */
    private int defence = DEFAULT_DEFENCE;

    /** The capacity of this UnitType. */
    private int space = 0;

    /**
     * The number of hit points this UnitType has. At the moment, this
     * is only used for ships. All other UnitTypes are downgraded or
     * destroyed if they lose a battle.
     */
    private int hitPoints = 0;

    /** The space taken by this UnitType. */
    private int spaceTaken = 1;

    /** The skill level of this UnitType. */
    private int skill = Xml.UNDEFINED;

    /** The price of this UnitType. */
    private int price = Xml.UNDEFINED;

    /** The initial moves of this UnitType. */
    private int movement = DEFAULT_MOVEMENT;

    /** The maximum distance of tiles this UnitType can observe. */
    private int lineOfSight = DEFAULT_LINE_OF_SIGHT;

    /** The probability of recruiting a Unit of this type in Europe. */
    private int recruitProbability = 0;

    /** How much a Unit of this type contributes to the Player's score. */
    private int scoreValue = 0;

    /** The maximum experience a unit of this type can accumulate. */
    private int maximumExperience = 0;

    /**
     * The maximum attrition this UnitType can accumulate without
     * being destroyed.
     */
    private int maximumAttrition = Xml.INFINITY;
    
    public String expertProductionForGoodsId;
    
    public UnitType(String id) {
    	super(id);
    }

    public String resourceImageKey() {
    	return getId() + ".image";
    }
    
	public boolean isUnitType() {
		return true;
	}
    
	public int lineOfSight() {
		float base = lineOfSight;
		base = applyModifier(Modifier.LINE_OF_SIGHT_BONUS, base);
		return (int)base;
	}

    public boolean isNaval() {
    	return hasAbility(Ability.NAVAL_UNIT);
    }
	
    public boolean isWagonTrain() {
        return WAGON_TRAIN.equalsIgnoreCase(id);
    }
    
	public int getHitPoints() {
		return hitPoints;
	}

    public boolean isOffensive() {
        return getBaseOffence() > UnitType.DEFAULT_OFFENCE;
    }
	
    public int getBaseOffence() {
        return offence;
    }
    
    /**
     * Can this type of unit be upgraded to another given type by a given
     * educational change type?
     *
     * If the target type is null, return true if the UnitType can be
     * upgraded to any other type by the given means of education.
     *
     * @param newType The <code>UnitType</code> to learn (may be null
     *     in the case of attempting to move to a native settlement
     *     when the skill taught there is still unknown).
     * @param changeType The educational <code>ChangeType</code>.
     * @return True if this unit type can learn.
     */
    public boolean canBeUpgraded(ChangeType changeType) {
    	return canBeUpgraded(null, changeType);
    }

    public boolean canBeUpgraded(UnitType newType, ChangeType changeType) {
        for (UnitTypeChange change : unitTypeChanges.entities()) {
            if ((newType == null || newType.equalsId(change.getNewUnitTypeId())) && change.isPositiveProbability(changeType)) {
                return true;
            }
        }
        return false;
    }

    public int getSpaceTaken() {
		return spaceTaken;
	}
    
    public int getMovement() {
    	return movement;
    }

    public int getSpace() {
		return space;
	}

	public int getSkill() {
		return skill;
	}
    
    /**
     * Does this UnitType have a skill?
     *
     * @return True if this unit type has a skill.
     */
    public boolean hasSkill() {
        return skill != UNDEFINED;
    }
    
	public boolean hasPrice() {
		return price != UNDEFINED;
	}
    
    public String getExpertProductionForGoodsId() {
        return expertProductionForGoodsId;
    }

    protected int getMaximumExperience() {
        return maximumExperience;
    }

	public int getPrice() {
		return price;
	}

    public static class Xml extends XmlNodeParser {
        public Xml() {
        	BuildableType.Xml.abstractAddNodes(this);
            addNodeForMapIdEntities("unitTypeChanges", UnitTypeChange.class);
            addNodeForMapIdEntities("unitConsumption", UnitConsumption.class);
        }

        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute("id");
            
            UnitType ut = new UnitType(id);
            BuildableType.Xml.abstractStartElement(attr, ut);
            
            ut.offence = attr.getIntAttribute("offence", DEFAULT_OFFENCE);
            ut.defence = attr.getIntAttribute("defence", DEFAULT_DEFENCE);
            ut.movement = attr.getIntAttribute("movement", DEFAULT_MOVEMENT);
            ut.lineOfSight = attr.getIntAttribute("lineOfSight", DEFAULT_LINE_OF_SIGHT);
            ut.scoreValue = attr.getIntAttribute("scoreValue", 0);
            ut.space = attr.getIntAttribute("space", 0);
            ut.spaceTaken = attr.getIntAttribute("spaceTaken", 1);
            ut.hitPoints = attr.getIntAttribute("hitPoints", 0);
            ut.maximumExperience = attr.getIntAttribute("maximumExperience", 0);
            ut.recruitProbability = attr.getIntAttribute("recruitProbability", 0);
            ut.expertProductionForGoodsId = attr.getStrAttribute("expert-production");
            ut.skill = attr.getIntAttribute("skill", Xml.UNDEFINED);
            ut.price = attr.getIntAttribute("price", Xml.UNDEFINED);
            
            nodeObject = ut;
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "unit-type";
        }
    }

}
