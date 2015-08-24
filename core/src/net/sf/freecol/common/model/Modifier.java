package net.sf.freecol.common.model;

import promitech.colonization.savegame.XmlNodeAttributes;
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

    /**
     * The value increments per turn.  This can be used to create
     * Modifiers whose values increase or decrease over time.
     */
    private float increment;
    private ModifierType incrementType;
    private int modifierIndex = DEFAULT_MODIFIER_INDEX;
	
	
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
	
	public static class Xml extends XmlNodeParser {

		@Override
        public void startElement(XmlNodeAttributes attr) {
			Modifier modifier = new Modifier();
			modifier.id = attr.getStrAttribute("id");
			modifier.value = attr.getFloatAttribute("value");
			modifier.modifierType = attr.getEnumAttribute(ModifierType.class, "type");
			modifier.incrementType = attr.getEnumAttribute(ModifierType.class, "incrementType");
			if (modifier.incrementType != null) {
				modifier.increment = attr.getFloatAttribute("increment");
			}
			modifier.modifierIndex = attr.getIntAttribute("index", DEFAULT_MODIFIER_INDEX);
			nodeObject = modifier;
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
