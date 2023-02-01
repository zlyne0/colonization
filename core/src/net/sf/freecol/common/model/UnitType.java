package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.xml.sax.SAXException;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.BuildableType;
import net.sf.freecol.common.model.specification.UnitTypeChange;
import net.sf.freecol.common.model.specification.UnitTypeChange.ChangeType;
import net.sf.freecol.common.model.specification.WithProbability;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class UnitType extends BuildableType {
	
    public static final Comparator<UnitType> UNIT_TYPE_PRICE_COMPARATOR = new Comparator<UnitType>() {
		@Override
		public int compare(UnitType o1, UnitType o2) {
			return o1.getPrice() - o2.getPrice();
		}
	};

	public static final String PETTY_CRIMINAL = "model.unit.pettyCriminal";
	public static final String INDENTURED_SERVANT = "model.unit.indenturedServant";

	public static final String FREE_COLONIST = "model.unit.freeColonist";
	public static final String WAGON_TRAIN = "model.unit.wagonTrain";
	public static final String ARTILLERY = "model.unit.artillery";
	public static final String VETERAN_SOLDIER = "model.unit.veteranSoldier";
	public static final String SCOUT = "model.unit.seasonedScout";
	public static final String CARAVEL = "model.unit.caravel";
	public static final String MERCHANTMAN = "model.unit.merchantman";
	public static final String FRIGATE = "model.unit.frigate";
	public static final String GALLEON = "model.unit.galleon";
	public static final String BRAVE = "model.unit.brave";
	public static final String EXPERT_FISHERMAN = "model.unit.expertFisherman";
	public static final String EXPERT_FARMER = "model.unit.expertFarmer";
	public static final String EXPERT_ORE_MINER = "model.unit.expertOreMiner";
	public static final String EXPERT_SILVER_MINER = "model.unit.expertSilverMiner";
	public static final String EXPERT_FUR_TRAPPER = "model.unit.expertFurTrapper";
	public static final String MASTER_FUR_TRADER = "model.unit.masterFurTrader";
	public static final String MASTER_TOBACCONIST = "model.unit.masterTobacconist";
	public static final String MASTER_TOBACCO_PLANTER = "model.unit.masterTobaccoPlanter";
	public static final String MASTER_WEAVER = "model.unit.masterWeaver";
	public static final String ELDER_STATESMAN = "model.unit.elderStatesman";
	public static final String HARDY_PIONEER = "model.unit.hardyPioneer";
	
    private static final int DEFAULT_LINE_OF_SIGHT = 1;
    public static final int DEFAULT_MOVEMENT = 3;
    public static final int DEFAULT_OFFENCE = 0;
    public static final int DEFAULT_DEFENCE = 1;

	public static final int DEFAULT_PRICE = 100;

    public final MapIdEntities<UnitTypeChange> unitTypeChanges = MapIdEntities.linkedMapIdEntities();
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

    /** The default role for a unit of this type. */
    private UnitRole defaultRole = null;
    private String defaultRoleId = UnitRole.DEFAULT_ROLE_ID;
    
    /**
     * The maximum attrition this UnitType can accumulate without
     * being destroyed.
     */
    private int maximumAttrition = Xml.INFINITY;
    
    private String expertProductionForGoodsId;

	protected String extendsId;
	private boolean naval = false;

    private UnitType(String id) {
    	super(id);
    }

    public String resourceImageKey() {
    	return getId() + ".image";
    }
    
	public boolean isType(String unitTypeId) {
		return getId().equals(unitTypeId);
	}
    
    @Override
	public boolean isUnitType() {
		return true;
	}
    
	public int lineOfSight() {
		return lineOfSight;
	}

	private void updateReferences() {
		naval = hasAbility(Ability.NAVAL_UNIT);
	}
	
    public boolean isNaval() {
    	return naval;
    }
	
    public boolean canCarryUnits() {
    	return hasAbility(Ability.CARRY_UNITS);
    }
    
    public boolean canBuildColony() {
        return hasAbility(Ability.FOUND_COLONY);
    }
    
    public boolean isWagonTrain() {
        return WAGON_TRAIN.equalsIgnoreCase(id);
    }
    
    public boolean isPerson() {
        return hasAbility(Ability.PERSON)
            || hasAbility(Ability.BORN_IN_COLONY)
            || hasAbility(Ability.BORN_IN_INDIAN_SETTLEMENT)
            || hasAbility(Ability.FOUND_COLONY);
    }
    
	public int getHitPoints() {
		return hitPoints;
	}

    public boolean isOffensive() {
        return getBaseOffence() > UnitType.DEFAULT_OFFENCE;
    }
	
	public boolean isDefensive() {
		return getBaseDefence() > UnitType.DEFAULT_DEFENCE;
	}
    
    public int getBaseOffence() {
        return offence;
    }
    
	public int getBaseDefence() {
		return defence;
	}
    
    public boolean canBeUpgraded(ChangeType changeType) {
    	return canBeUpgraded(null, changeType);
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
    public boolean canBeUpgraded(UnitType newType, ChangeType changeType) {
        for (UnitTypeChange change : unitTypeChanges.entities()) {
            if ((newType == null || newType.equalsId(change.getNewUnitTypeId())) && change.isPositiveProbability(changeType)) {
                return true;
            }
        }
        return false;
    }
    
    public UnitTypeChange getUnitTypeChange(ChangeType changeType, Player player) {
    	for (UnitTypeChange change : unitTypeChanges.entities()) {
    		if (change.isPositiveProbability(changeType)) {
    			String newUnitTypeId = change.getNewUnitTypeId();
    			UnitType newUnitType = Specification.instance.unitTypes.getById(newUnitTypeId);
    			if (newUnitType.isAvailableTo(player.getFeatures())) {
    				return change;
    			}
    		}
    	}
    	return null;
    }
    
    public UnitType upgradeByChangeType(ChangeType changeType, Player player) {
    	for (UnitTypeChange change : unitTypeChanges.entities()) {
    		if (change.isPositiveProbability(changeType)) {
    			String newUnitTypeId = change.getNewUnitTypeId();
    			UnitType newUnitType = Specification.instance.unitTypes.getById(newUnitTypeId);
    			if (newUnitType.isAvailableTo(player.getFeatures())) {
    				return newUnitType;
    			}
    		}
    	}
    	return null;
    }
    
    public List<String> getUpgradesUnitTypeIds(ChangeType changeType) {
    	List<String> changeTypeUnitTypes = new ArrayList<String>();
        for (UnitTypeChange change : unitTypeChanges.entities()) {
        	if (change.isPositiveProbability(changeType)) {
        		changeTypeUnitTypes.add(change.getNewUnitTypeId());
        	}
        }
        return changeTypeUnitTypes;
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

	public void updateDefaultRoleReference() {
		defaultRole = Specification.instance.unitRoles.getById(defaultRoleId);
	}

	public UnitRole getDefaultRole() {
		return defaultRole;
	}

    public boolean isRecruitable() {
        return recruitProbability > 0;
    }
	
    public WithProbability<UnitType> createRecruitProbability() {
    	return new WithProbability<UnitType>() {
			@Override
			public int getOccureProbability() {
				return recruitProbability;
			}

			@Override
			public UnitType probabilityObject() {
				return UnitType.this;
			}
		};
    }

    public static class Xml extends XmlNodeParser<UnitType> {
        private static final String ELEMENT_DEFAULT_ROLE = "default-role";
		private static final String ATTR_EXTENDS = "extends";
		private static final String ATTR_PRICE = "price";
		private static final String ATTR_SKILL = "skill";
		private static final String ATTR_EXPERT_PRODUCTION = "expert-production";
		private static final String ATTR_RECRUIT_PROBABILITY = "recruitProbability";
		private static final String ATTR_MAXIMUM_EXPERIENCE = "maximumExperience";
		private static final String ATTR_HIT_POINTS = "hitPoints";
		private static final String ATTR_SPACE_TAKEN = "spaceTaken";
		private static final String ATTR_SPACE = "space";
		private static final String ATTR_SCORE_VALUE = "scoreValue";
		private static final String ATTR_LINE_OF_SIGHT = "lineOfSight";
		private static final String ATTR_MOVEMENT = "movement";
		private static final String ATTR_DEFENCE = "defence";
		private static final String ATTR_OFFENCE = "offence";

		public Xml() {
        	BuildableType.Xml.abstractAddNodes(this);
            addNodeForMapIdEntities("unitTypeChanges", UnitTypeChange.class);
            addNodeForMapIdEntities("unitConsumption", UnitConsumption.class);
        }

        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute(ATTR_ID);
            
            UnitType ut = new UnitType(id);
            BuildableType.Xml.abstractStartElement(attr, ut);
            
            ut.offence = attr.getIntAttribute(ATTR_OFFENCE, DEFAULT_OFFENCE);
            ut.defence = attr.getIntAttribute(ATTR_DEFENCE, DEFAULT_DEFENCE);
            ut.movement = attr.getIntAttribute(ATTR_MOVEMENT, DEFAULT_MOVEMENT);
            ut.lineOfSight = attr.getIntAttribute(ATTR_LINE_OF_SIGHT, DEFAULT_LINE_OF_SIGHT);
            ut.scoreValue = attr.getIntAttribute(ATTR_SCORE_VALUE, 0);
            ut.space = attr.getIntAttribute(ATTR_SPACE, 0);
            ut.spaceTaken = attr.getIntAttribute(ATTR_SPACE_TAKEN, 1);
            ut.hitPoints = attr.getIntAttribute(ATTR_HIT_POINTS, 0);
            ut.maximumExperience = attr.getIntAttribute(ATTR_MAXIMUM_EXPERIENCE, 0);
            ut.recruitProbability = attr.getIntAttribute(ATTR_RECRUIT_PROBABILITY, 0);
            ut.expertProductionForGoodsId = attr.getStrAttribute(ATTR_EXPERT_PRODUCTION);
            ut.skill = attr.getIntAttribute(ATTR_SKILL, Xml.UNDEFINED);
            ut.price = attr.getIntAttribute(ATTR_PRICE, Xml.UNDEFINED);
            ut.defaultRoleId = UnitRole.DEFAULT_ROLE_ID;
            
            ut.extendsId = attr.getStrAttribute(ATTR_EXTENDS);
            if (ut.extendsId != null) {
            	UnitType parent = Specification.instance.unitTypes.getById(ut.extendsId);
            	ut.addFeatures(parent);
            	ut.unitConsumption.addAll(parent.unitConsumption);
            }
            nodeObject = ut;
        }

        @Override
        public void startWriteAttr(UnitType ut, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(ut);
        	BuildableType.Xml.abstractStartWriteAttr(ut, attr);

        	attr.set(ATTR_OFFENCE, ut.offence);
        	attr.set(ATTR_DEFENCE, ut.defence);
        	attr.set(ATTR_MOVEMENT, ut.movement);
        	attr.set(ATTR_LINE_OF_SIGHT, ut.lineOfSight);
        	attr.set(ATTR_SCORE_VALUE, ut.scoreValue);
        	attr.set(ATTR_SPACE, ut.space);
        	attr.set(ATTR_SPACE_TAKEN, ut.spaceTaken);
        	attr.set(ATTR_HIT_POINTS, ut.hitPoints);
        	attr.set(ATTR_MAXIMUM_EXPERIENCE, ut.maximumExperience);
        	attr.set(ATTR_RECRUIT_PROBABILITY, ut.recruitProbability);
        	attr.set(ATTR_EXPERT_PRODUCTION, ut.expertProductionForGoodsId);
        	attr.set(ATTR_SKILL, ut.skill);
        	attr.set(ATTR_PRICE, ut.price);

        	// "extends" (ATTR_EXTENDS) attribute there is only on reading in order to avoid save ObjectFeatures twice
        	// While saving ObjectFeatures it's difficulty to distinguish futures form parent and main object.
        	// extends attribute is used only in specification.xml not directly in save xml        	
        	
        	if (ut.defaultRoleId != null && !UnitRole.DEFAULT_ROLE_ID.equals(ut.defaultRoleId)) {
        		attr.xml.element(ELEMENT_DEFAULT_ROLE);
        		attr.set(ATTR_ID, ut.defaultRoleId);
        		attr.xml.pop();
        	}
        }
        
        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
        	if (attr.isQNameEquals(ELEMENT_DEFAULT_ROLE)) {
        		nodeObject.defaultRoleId = attr.getStrAttributeNotNull(ATTR_ID);
        	}
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
        	if (getTagName().equals(qName)) {
        		nodeObject.updateReferences();
        	}
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
