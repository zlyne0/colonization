package net.sf.freecol.common.model.specification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.ObjectWithFeatures;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class Modifier implements Identifiable {

	public static final String AMPHIBIOUS_ATTACK = "model.modifier.amphibiousAttack";
	public static final String ARTILLERY_AGAINST_RAID = "model.modifier.artilleryAgainstRaid";
	public static final String ARTILLERY_IN_THE_OPEN = "model.modifier.artilleryInTheOpen";
	public static final String ATTACK_BONUS = "model.modifier.attackBonus";
	public static final String BIG_MOVEMENT_PENALTY = "model.modifier.bigMovementPenalty";
	public static final String BOMBARD_BONUS = "model.modifier.bombardBonus";
	public static final String BREEDING_DIVISOR = "model.modifier.breedingDivisor";
	public static final String BREEDING_FACTOR = "model.modifier.breedingFactor";
	public static final String BUILDING_PRICE_BONUS = "model.modifier.buildingPriceBonus";
	public static final String COLONY_GOODS_PARTY = "model.modifier.colonyGoodsParty";
	public static final String CONSUME_ONLY_SURPLUS_PRODUCTION = "model.modifier.consumeOnlySurplusProduction";
	public static final String CONVERSION_ALARM_RATE = "model.modifier.conversionAlarmRate";
	public static final String CONVERSION_SKILL = "model.modifier.conversionSkill";
	public static final String DEFENCE = "model.modifier.defence";
	public static final String EXPLORE_LOST_CITY_RUMOUR = "model.modifier.exploreLostCityRumour";
	public static final String FORTIFIED = "model.modifier.fortified";
	public static final String IMMIGRATION = "model.modifier.immigration";
	public static final String LAND_PAYMENT_MODIFIER = "model.modifier.landPaymentModifier";
	public static final String LIBERTY = "model.modifier.liberty";
	public static final String LINE_OF_SIGHT_BONUS = "model.modifier.lineOfSightBonus";
	public static final String MINIMUM_COLONY_SIZE = "model.modifier.minimumColonySize";
	public static final String MISSIONARY_TRADE_BONUS = "model.modifier.missionaryTradeBonus";
	public static final String MOVEMENT_BONUS = "model.modifier.movementBonus";
	public static final String NATIVE_ALARM_MODIFIER = "model.modifier.nativeAlarmModifier";
	public static final String NATIVE_CONVERT_BONUS = "model.modifier.nativeConvertBonus";
	public static final String OFFENCE = "model.modifier.offence";
	public static final String OFFENCE_AGAINST = "model.modifier.offenceAgainst";
	public static final String PEACE_TREATY = "model.modifier.peaceTreaty";
	public static final String POPULAR_SUPPORT = "model.modifier.popularSupport";
	public static final String RELIGIOUS_UNREST_BONUS = "model.modifier.religiousUnrestBonus";
	public static final String SAIL_HIGH_SEAS = "model.modifier.sailHighSeas";
	public static final String SHIP_TRADE_PENALTY = "model.modifier.shipTradePenalty";
	public static final String SMALL_MOVEMENT_PENALTY = "model.modifier.smallMovementPenalty";
	public static final String SOL = "model.modifier.SoL";
	public static final String TILE_TYPE_CHANGE_PRODUCTION = "model.modifier.tileTypeChangeProduction";
	public static final String TRADE_BONUS = "model.modifier.tradeBonus";
	public static final String TRADE_VOLUME_PENALTY = "model.modifier.tradeVolumePenalty";
	public static final String TREASURE_TRANSPORT_FEE = "model.modifier.treasureTransportFee";
	public static final String WAREHOUSE_STORAGE = "model.modifier.warehouseStorage";

	public static final float UNKNOWN = Float.MIN_VALUE;
	public static final int DEFAULT_MODIFIER_INDEX = 0;
	public static final int DEFAULT_PRODUCTION_INDEX = 100;

	// Specific combat indicies
	public static final int BASE_COMBAT_INDEX = 10;
	public static final int UNIT_ADDITIVE_COMBAT_INDEX = 20;
	public static final int UNIT_NORMAL_COMBAT_INDEX = 40;
	public static final int ROLE_COMBAT_INDEX = 30;
	public static final int GENERAL_COMBAT_INDEX = 50;

	public static enum ModifierType {
		ADDITIVE, MULTIPLICATIVE, PERCENTAGE
	}

	private String id;
    private ModifierType modifierType;
    private float value;
    private final List<Scope> scopes = new ArrayList<Scope>();

    /**
     * The value increments per turn.  This can be used to create
     * Modifiers whose values increase or decrease over time.
     */
    private float increment;
    private ModifierType incrementType;
    private int modifierIndex = DEFAULT_MODIFIER_INDEX;
	
    public Modifier(String id) {
    	this.id = id;
    }
	
	public Modifier(String id, ModifierType modifierType, float value) {
		this(id);
		this.modifierType = modifierType;
		this.value = value;
	}

	@Override
	public String getId() {
		return id;
	}

	public float apply(float base) {
        switch (modifierType) {
        case ADDITIVE:
            return base + value;
        case MULTIPLICATIVE:
            return base * value;
        case PERCENTAGE:
            return base + (base * value) / 100;
        default:
            throw new IllegalArgumentException("can not recognize modifierType: " + modifierType);
        }
	}

    public boolean canAppliesTo(ObjectWithFeatures obj) {
        if (scopes.isEmpty()) {
            return true;
        }
        for (Scope s : scopes) {
            if (s.isAppliesTo(obj)) {
                return true;
            }
        }
        return false;
    }
	
	public String toString() {
	    return "modifier id: " + id + ", modifierType: " + modifierType;
	}
	
	public static class Xml extends XmlNodeParser<Modifier> {

		private static final String INDEX_TYPE = "index";
		private static final String INCREMENT_TYPE = "increment";
		private static final String INCREMENT_TYPE_ATTR = "incrementType";
		private static final String TYPE_ATTR = "type";

		public Xml() {
            addNode(Scope.class, new ObjectFromNodeSetter<Modifier, Scope>() {
                @Override
                public void set(Modifier target, Scope entity) {
                    target.scopes.add(entity);
                }
				@Override
				public void generateXml(Modifier source, ChildObject2XmlCustomeHandler<Scope> xmlGenerator) throws IOException {
					xmlGenerator.generateXmlFromCollection(source.scopes);
				}
            });
        }

	    @Override
        public void startElement(XmlNodeAttributes attr) {
			Modifier modifier = new Modifier(attr.getStrAttribute(ATTR_ID));
			modifier.value = attr.getFloatAttribute(ATTR_VALUE, 0);
			modifier.modifierType = attr.getEnumAttribute(ModifierType.class, TYPE_ATTR);
			modifier.incrementType = attr.getEnumAttribute(ModifierType.class, INCREMENT_TYPE_ATTR);
			if (modifier.incrementType != null) {
				modifier.increment = attr.getFloatAttribute(INCREMENT_TYPE);
			}
			modifier.modifierIndex = attr.getIntAttribute(INDEX_TYPE, DEFAULT_MODIFIER_INDEX);
			nodeObject = modifier;
		}

	    @Override
	    public void startWriteAttr(Modifier node, XmlNodeAttributesWriter attr) throws IOException {
	    	attr.setId(node);
	    	attr.set(ATTR_VALUE, node.value);
	    	attr.set(TYPE_ATTR, node.modifierType);
	    	if (node.incrementType != null) {
	    		attr.set(INCREMENT_TYPE_ATTR, node.incrementType);
	    		attr.set(INCREMENT_TYPE, node.increment);
	    	}
	    	attr.set(INDEX_TYPE, node.modifierIndex);
	    }
	    
		@Override
		public String getTagName() {
		    return tagName();
		}
		
        public static String tagName() {
            return "modifier";
        }
	}
}
