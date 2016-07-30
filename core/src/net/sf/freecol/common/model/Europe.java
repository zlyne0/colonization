package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.WithProbability;
import promitech.colonization.Randomizer;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Europe extends ObjectWithFeatures implements UnitLocation {

	private static final int MAX_RECRUITABLE_UNITS = 3;
	
    /** The initial recruit price. */
    private static final int RECRUIT_PRICE_INITIAL = 200;

    /** The initial lower bound on recruitment price. */
    private static final int LOWER_CAP_INITIAL = 80;
    
    private Player owner;
    private int recruitPrice;
    private int recruitLowerCap;
    private final MapIdEntities<Unit> units = new MapIdEntities<Unit>();
    protected final List<UnitType> recruitables = new ArrayList<UnitType>(MAX_RECRUITABLE_UNITS);
    
    public Europe(String id) {
        super(id);
    }

	@Override
	public MapIdEntities<Unit> getUnits() {
		return units;
	}
    
	public List<UnitType> getRecruitables() {
		return recruitables;
	}
	
    public void setOwner(Player owner) {
        this.owner = owner;
    }
	
    public int getNextImmigrantTurns() {
        int production = getTotalImmigrationProduction();
        int turns = 100;
        if (production > 0) {
            int immigrationRequired = owner.getImmigrationRequired() - owner.getImmigration();
            turns = immigrationRequired / production;
            if (immigrationRequired % production > 0) {
                turns++;
            }
        }
        return turns;
    }
    
    public int getRecruitImmigrantPrice() {
        if (!owner.isColonial()) {
        	return -1;
        }
        int required = owner.getImmigrationRequired();
        int immigration = owner.getImmigration();
        int difference = Math.max(required - immigration, 0);
        return Math.max((recruitPrice * difference) / required, recruitLowerCap);
    }
    
    private int getTotalImmigrationProduction() {
        int prod = owner.getImmigrationProduction() + getImmigrationProduction();
        if (prod < 0) {
            prod = 0;
        }
        return prod;
    }
    
    private int getImmigrationProduction() {
        int n = 0;
        for (Unit u : units.entities()) {
            if (u.isPerson()) { 
                n++;
            }
        }
        n *= Specification.options.getIntValue(GameOptions.EUROPEAN_UNIT_IMMIGRATION_PENALTY);
        n += Specification.options.getIntValue(GameOptions.PLAYER_IMMIGRATION_BONUS);
        return n;
    }

	public void buyUnit(UnitType unitType, int price) {
		owner.subtractGold(price);
		
        UnitRole role = (Specification.options.getBoolean(GameOptions.EQUIP_EUROPEAN_RECRUITS))
                ? unitType.getDefaultRole()
                : Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID);
        Unit unit = new Unit(Game.idGenerator.nextId(Unit.class), unitType, role, owner);
        unit.changeUnitLocation(this);
        if (unit.isCarrier()) {
        	unit.setState(UnitState.ACTIVE);
        } else {
        	unit.setState(UnitState.SENTRY);
        }
	}

	public void recruitImmigrant(UnitType unitType, int price) {
		buyUnit(unitType, price);
		
		increaseRecruitmentDifficulty();
		for (UnitType ut : recruitables) {
			if (ut.equalsId(unitType)) {
				recruitables.remove(ut);
				break;
			}
		}
		generateRecruitablesUnitList();
	}

	private void generateRecruitablesUnitList() {
		if (recruitables.size() >= MAX_RECRUITABLE_UNITS) {
			return;
		}
		List<WithProbability<UnitType>> recruitProbabilities = new ArrayList<WithProbability<UnitType>>(Specification.instance.unitTypeRecruitProbabilities.size());
		for (WithProbability<UnitType> recruitProbability : Specification.instance.unitTypeRecruitProbabilities) { 
			if (owner.getFeatures().canApplyAbilityToObject(Ability.CAN_RECRUIT_UNIT, recruitProbability.probabilityObject())) {
				recruitProbabilities.add(recruitProbability);
			}
		}
		for (int i=0; i<MAX_RECRUITABLE_UNITS - recruitables.size(); i++) {
			WithProbability<UnitType> randomOne = Randomizer.getInstance().randomOne(recruitProbabilities);
			recruitables.add(randomOne.probabilityObject());
		}
	}
	
    private void increaseRecruitmentDifficulty() {
        recruitPrice += Specification.options.getIntValue(GameOptions.RECRUIT_PRICE_INCREASE);
        recruitLowerCap += Specification.options.getIntValue(GameOptions.LOWER_CAP_INCREASE);
    }
	
	public boolean isNoNavyInPort() {
		for (Unit unit : units.entities()) {
			if (unit.isNaval()) {
				return false;
			}
		}
		return true;
	}

    public static class Xml extends XmlNodeParser {

        public Xml() {
            addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
            addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
            
            addNode(Unit.class, new ObjectFromNodeSetter<Europe,Unit>() {
                @Override
                public void set(Europe europe, Unit unit) {
                    unit.changeUnitLocation(europe);
                }
            });
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute("id");
            Europe eu = new Europe(id);
            eu.recruitPrice = attr.getIntAttribute("recruitPrice", RECRUIT_PRICE_INITIAL);
            eu.recruitLowerCap = attr.getIntAttribute("recruitLowerCap", LOWER_CAP_INITIAL);
            nodeObject = eu;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
        	if (attr.isQNameEquals("recruit")) {
        		UnitType unitType = Specification.instance.unitTypes.getById(attr.getStrAttribute("id"));
        		((Europe)nodeObject).recruitables.add(unitType);
        	}
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "europe";
        }
    }

}
