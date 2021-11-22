package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.player.Market.MarketTransactionLogger;
import net.sf.freecol.common.model.player.ChooseEmigrantToRecruitNotification;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.TransactionEffectOnMarket;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.RequiredGoods;
import net.sf.freecol.common.model.specification.WithProbability;
import net.sf.freecol.common.model.specification.options.UnitListOption;
import net.sf.freecol.common.model.specification.options.UnitOption;
import promitech.colonization.Randomizer;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;
import promitech.colonization.ui.resources.StringTemplate;

public class Europe extends ObjectWithFeatures implements UnitLocation {

	private static final int MAX_RECRUITABLE_UNITS = 3;
	
    /** The initial recruit price. */
    private static final int RECRUIT_PRICE_INITIAL = 200;

    /** The initial lower bound on recruitment price. */
    private static final int LOWER_CAP_INITIAL = 80;
    
    private Player owner;
    private int recruitPrice = RECRUIT_PRICE_INITIAL;
    private int recruitLowerCap = LOWER_CAP_INITIAL;
    private final MapIdEntities<Unit> units = new MapIdEntities<Unit>();
    private final MapIdEntities<UnitType> recruitables = MapIdEntities.linkedMapIdEntities(MAX_RECRUITABLE_UNITS);
	private final ObjectIntMap<UnitType> unitPrices = new ObjectIntMap<UnitType>();

    public static Europe newStartingEurope(IdGenerator idGenerator, Player player) {
    	Europe europe = new Europe();
        europe.addAbility(new Ability(Ability.DRESS_MISSIONARY, true));
        europe.setOwner(player);
    	
        UnitListOption unitsOptions = Specification.options.getUnitListOption(GameOptions.IMMIGRANTS);
        for (UnitOption uo : unitsOptions.unitOptions.entities()) {
        	europe.recruitables.add(uo.getUnitType());
        }
        europe.generateRecruitablesUnitList();
    	return europe;
    }
    
    public Europe() {
        super("no id for europe");
    }

    @Override
    public String getId() {
    	throw new IllegalAccessError("no id for object");
    }
    
	@Override
	public MapIdEntitiesReadOnly<Unit> getUnits() {
		return units;
	}
    
    @Override
    public void addUnit(Unit unit) {
        units.add(unit);
    }

    @Override
    public void removeUnit(Unit unit) {
        units.removeId(unit);
    }
	
	public MapIdEntities<UnitType> getRecruitables() {
		return recruitables;
	}
	
    public void setOwner(Player owner) {
        this.owner = owner;
    }
	
    public boolean hasOwner() {
    	return owner != null;
    }
    
    public int getNextImmigrantTurns() {
        int production = penalizedImmigration(owner.getImmigrationProduction());
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
    
	public void handleImmigrationOnNewTurn() {
    	int immigrationProduction = owner.getImmigrationProduction();
    	int penalizedImmigration = penalizedImmigration(immigrationProduction);
    	
    	System.out.println("immigration " + 
			"playerImmigration: " + owner.getImmigration() + 
			", prod: " + immigrationProduction + 
			", penalized: " + penalizedImmigration +
			", playerReqImmigration: " + owner.getImmigrationRequired()
    	);
    	
    	owner.modifyImmigration(penalizedImmigration);
    	if (owner.shouldANewColonistEmigrate()) {
    		owner.reduceImmigration();
    		owner.updateImmigrationRequired();
    		
    		if (owner.isAi()) {
    			emigrate();
    			return;
    		}
    		
    		if (owner.getFeatures().hasAbility(Ability.SELECT_RECRUIT)) {
    			owner.eventsNotifications.addMessageNotification(new ChooseEmigrantToRecruitNotification());
    		} else {
    			Unit emigrateUnit = emigrate();
    			StringTemplate st = StringTemplate.template("model.europe.emigrate")
					.addKey("%europe%", owner.getEuropeNameKey())
					.addStringTemplate("%unit%", UnitLabel.getPlainUnitLabel(emigrateUnit));
    			owner.eventsNotifications.addMessageNotification(st);
    		}
    	}
	}
    
    /**
     * Get any immigration produced in Europe.
     *
     * Col1 penalizes immigration by -4 per unit in Europe per turn,
     * but there is a +2 player bonus, which we might as well add
     * here.  Total immigration per turn can not be negative, but that
     * is handled in ServerPlayer.
     *
     * @param production The current total colony production.
     * @return Immigration produced this turn in Europe.
     */
    private int penalizedImmigration(int production) {
        int n = 0;
        for (Unit u : units.entities()) {
            if (u.isPerson()) {
            	n++;
            }
        }
        n *= Specification.options.getIntValue(GameOptions.EUROPEAN_UNIT_IMMIGRATION_PENALTY);
        n += Specification.options.getIntValue(GameOptions.PLAYER_IMMIGRATION_BONUS);
        // Do not allow total production to be negative.
        if (n + production < 0) {
        	n = -production;
        }
        return n;
    }
    
    public int getRecruitImmigrantPrice() {
        if (!owner.isColonial()) {
        	return -1;
        }
        int required = owner.getImmigrationRequired();
        int immigration = owner.getImmigration();
        int difference = Math.max(required - immigration, 0);
        if (required == 0) {
        	required = 1;
        }
        return Math.max((recruitPrice * difference) / required, recruitLowerCap);
    }

    public Unit buyUnit(UnitType unitType, int price) {
    	owner.subtractGold(price);
    	Unit u = createUnit(unitType);
    	increasePrice(unitType, price);
    	return u;
    }
    
	private void increasePrice(UnitType unitType, int actualPrice) {
		String option;
    	if (Specification.options.getBoolean(GameOptions.PRICE_INCREASE_PER_TYPE)) {
    		option = "model.option.priceIncrease." + unitType.idSuffix();
    	} else {
    		option = "model.option.priceIncrease";
    	}
    	int increasePrice = Specification.options.getIntValue(option, 0);
    	if (increasePrice != 0) {
    		unitPrices.getAndIncrement(unitType, actualPrice, increasePrice);
    	}
	}
    
	private Unit emigrate() {
		UnitType emigrateUnitType = Randomizer.instance().randomMember(recruitables);
		return recruitImmigrant(emigrateUnitType);
	}

	public Unit buyImmigrant(UnitType unitType, int price) {
		owner.subtractGold(price);
		recruitPrice += Specification.options.getIntValue(GameOptions.RECRUIT_PRICE_INCREASE);
		recruitLowerCap += Specification.options.getIntValue(GameOptions.LOWER_CAP_INCREASE);
		return recruitImmigrant(unitType);
	}

	public Unit recruitImmigrant(final UnitType unitType) {
		for (UnitType ut : recruitables.copy()) {
			if (ut.equalsId(unitType)) {
				recruitables.removeId(ut);
				break;
			}
		}
		generateRecruitablesUnitList();
		return createUnit(unitType);
	}
	
	private Unit createUnit(UnitType unitType) {
		UnitRole role = (Specification.options.getBoolean(GameOptions.EQUIP_EUROPEAN_RECRUITS))
			? unitType.getDefaultRole()
			: Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID);
		Unit unit = UnitFactory.create(unitType, role, owner, this);
		if (unit.isCarrier()) {
			unit.setState(UnitState.ACTIVE);
		} else {
			unit.setState(UnitState.SENTRY);
		}
		return unit;
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
		int count = MAX_RECRUITABLE_UNITS - recruitables.size();
		for (int i=0; i<count; i++) {
			WithProbability<UnitType> randomOne = Randomizer.instance().randomOne(recruitProbabilities);
			recruitables.add(randomOne.probabilityObject());
		}
	}
	
	public void replaceNotRecruitableUnits() {
		for (UnitType unitType : recruitables.copy()) {
			if (!owner.getFeatures().canApplyAbilityToObject(Ability.CAN_RECRUIT_UNIT, unitType)) {
				recruitables.removeId(unitType);
			}
		}
		generateRecruitablesUnitList();
	}
	
	public void emigrantsFountainOfYoung(int colNumber) {
		List<WithProbability<UnitType>> recruitProbabilities = new ArrayList<WithProbability<UnitType>>(Specification.instance.unitTypeRecruitProbabilities.size());
		for (WithProbability<UnitType> recruitProbability : Specification.instance.unitTypeRecruitProbabilities) { 
			if (owner.getFeatures().canApplyAbilityToObject(Ability.CAN_RECRUIT_UNIT, recruitProbability.probabilityObject())) {
				recruitProbabilities.add(recruitProbability);
			}
		}
		for (int i=0; i<colNumber; i++) {
			UnitType unitType = Randomizer.instance()
				.randomOne(recruitProbabilities)
				.probabilityObject();
			createUnit(unitType);
		}
	}
	
	public boolean isNoNavyInPort() {
		for (Unit unit : units.entities()) {
			if (unit.isNaval()) {
				return false;
			}
		}
		return true;
	}

    public void changeUnitRole(Game game, Unit unit, UnitRole newUnitRole, MarketTransactionLogger marketLog) {
    	if (!newUnitRole.isAvailableTo(unit.unitType, this)) {
    		throw new IllegalStateException("can not change role for unit: " + unit + " from " + unit.unitRole + " to " + newUnitRole);
    	}
    	ProductionSummary requiredGoods = UnitRoleLogic.requiredGoodsToChangeRole(unit, newUnitRole);
    	
    	unit.changeRole(newUnitRole);

    	Player player = unit.getOwner();
		for (Entry<String> reqGoodsEntry : requiredGoods.entries()) {
			GoodsType goodsType = Specification.instance.goodsTypes.getById(reqGoodsEntry.key);
			if (reqGoodsEntry.value == 0) {
				continue;
			}
			if (reqGoodsEntry.value > 0) {
				// buy
				TransactionEffectOnMarket transaction = player.market().buyGoods(game, player, goodsType, reqGoodsEntry.value);
				marketLog.logPurchase(transaction);
			} else {
				// sell
				TransactionEffectOnMarket transaction = player.market().sellGoods(game, player, goodsType, -reqGoodsEntry.value);
				marketLog.logSale(transaction);
			}
		}
    }

    public boolean canAiBuyUnit(UnitType unitType, int budget) {
    	if (!isUnitAvailableToBuyByAI(unitType)) {
    		return false;
		}
		int price = aiUnitPrice(unitType);
		return budget >= price;
    }

	public boolean isUnitAvailableToBuyByAI(UnitType unitType) {
		return unitType.equalsId(UnitType.FREE_COLONIST)
			|| recruitables.containsId(unitType)
			|| Specification.instance.unitTypesTrainedInEurope.containsId(unitType);
	}

	public Unit buyUnitByAI(UnitType unitType) {
    	int recruitImmigrantPrice = getRecruitImmigrantPrice();
    	
    	if (recruitables.containsId(unitType)) {
    		return buyImmigrant(unitType, recruitImmigrantPrice);
    	}
    	// ai has advantage to buy colonist when they want
    	if (unitType.equalsId(UnitType.FREE_COLONIST) && owner.hasGold(recruitImmigrantPrice)) {
    		return buyImmigrant(unitType, recruitImmigrantPrice);
    	}
    	
    	// buy the cheapest trained unit and degrade to colonist 
    	if (unitType.equalsId(UnitType.FREE_COLONIST)) {
    		UnitType theCheapestTrainableUnit = theCheapestTrainableUnit();
    		return buyUnit(unitType, theCheapestTrainableUnit.getPrice());
    	}
    	return buyUnit(unitType, unitType.getPrice());
    }

    public int aiUnitPrice(UnitType unitType) {
		int recruitImmigrantPrice = getRecruitImmigrantPrice();

		// ai has advantage to buy colonist when they want
    	if (unitType.equalsId(UnitType.FREE_COLONIST)) {
			UnitType theCheapestTrainableUnit = theCheapestTrainableUnit();
			// buy the cheapest trained unit and degrade to colonist
			int trainedPrice = getUnitPrice(theCheapestTrainableUnit);
			return Math.min(recruitImmigrantPrice, trainedPrice);
		}

		int trainableUnitPrice = getUnitPrice(unitType);
    	if (recruitables.containsId(unitType)) {
    		return Math.min(trainableUnitPrice, recruitImmigrantPrice);
		}
    	return trainableUnitPrice;
	}

	public int aiTheCheapestUnitPrice() {
		UnitType theCheapestTrainableUnit = theCheapestTrainableUnit();
		int recruitImmigrantPrice = getRecruitImmigrantPrice();
		int trainedPrice = getUnitPrice(theCheapestTrainableUnit);
		return Math.min(recruitImmigrantPrice, trainedPrice);
	}

    private UnitType theCheapestTrainableUnit() {
    	return Specification.instance.unitTypesTrainedInEurope.sortedEntities().iterator().next();
    }
    
    public int getUnitPrice(UnitType unitType) {
    	if (unitPrices.containsKey(unitType)) {
    		return unitPrices.get(unitType, UnitType.DEFAULT_PRICE);
    	}
    	if (!unitType.hasPrice()) {
    		return UnitType.DEFAULT_PRICE;
    	}
    	return unitType.getPrice();
    }
    
	public int getUnitPrice(UnitType unitType, UnitRole unitRole, int amount) {
		if (!unitType.hasPrice()) {
			return Integer.MAX_VALUE;
		}
		int price = unitType.getPrice();
		for (RequiredGoods goods : unitRole.requiredGoods.entities()) {
			price += owner.market().getBidPrice(goods.goodsType, goods.amount * unitRole.getMaximumCount());
		}
		return price * amount;
	}

    public static class Xml extends XmlNodeParser<Europe> {

        private static final String ATTR_UNIT_TYPE_PRICE = "price";
		private static final String ATTR_UNIT_TYPE = "unitType";
		private static final String ELEMENT_RECRUIT = "recruit";
        private static final String ELEMENT_UNITPRICE = "unitPrice";
		private static final String ATTR_RECRUIT_LOWER_CAP = "recruitLowerCap";
		private static final String ATTR_RECRUIT_PRICE = "recruitPrice";

		public Xml() {
            addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
            addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
            
            addNode(Unit.class, new ObjectFromNodeSetter<Europe,Unit>() {
                @Override
                public void set(Europe europe, Unit unit) {
                    unit.changeUnitLocation(europe);
                }
				@Override
				public void generateXml(Europe source, ChildObject2XmlCustomeHandler<Unit> xmlGenerator) throws IOException {
					xmlGenerator.generateXmlFromCollection(source.units.entities());
				}
            });
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            Europe eu = new Europe();
            eu.recruitPrice = attr.getIntAttribute(ATTR_RECRUIT_PRICE, RECRUIT_PRICE_INITIAL);
            eu.recruitLowerCap = attr.getIntAttribute(ATTR_RECRUIT_LOWER_CAP, LOWER_CAP_INITIAL);
            nodeObject = eu;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
        	if (attr.isQNameEquals(ELEMENT_RECRUIT)) {
        		UnitType unitType = Specification.instance.unitTypes.getById(attr.getStrAttribute(ATTR_ID));
        		nodeObject.recruitables.add(unitType);
        	}
        	if (attr.isQNameEquals(ELEMENT_UNITPRICE)) {
        		UnitType unitType = attr.getEntity(ATTR_UNIT_TYPE, Specification.instance.unitTypes);
        		int price = attr.getIntAttribute(ATTR_UNIT_TYPE_PRICE);
        		nodeObject.unitPrices.put(unitType, price);
        	}
        }
        
        @Override
        public void startWriteAttr(Europe eu, XmlNodeAttributesWriter attr) throws IOException {
        	attr.set(ATTR_RECRUIT_PRICE, eu.recruitPrice);
        	attr.set(ATTR_RECRUIT_LOWER_CAP, eu.recruitLowerCap);
        	
        	for (UnitType ut : eu.recruitables) {
        		attr.xml.element(ELEMENT_RECRUIT);
        		attr.set(ATTR_ID, ut);
        		attr.xml.pop();
        	}
        	
        	for (Entry<UnitType> entry : eu.unitPrices) {
				attr.xml.element(ELEMENT_UNITPRICE);
				attr.set(ATTR_UNIT_TYPE, entry.key);
				attr.set(ATTR_UNIT_TYPE_PRICE, entry.value);
				attr.xml.pop();
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
