package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Tension;
import net.sf.freecol.common.model.player.Tension.Level;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.IndianNationType;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.UnitTypeChange.ChangeType;
import promitech.colonization.Randomizer;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class IndianSettlement extends Settlement {

    /** How far to search for a colony to add an Indian convert to. */
    private static final int MAX_CONVERT_DISTANCE = 10;

    private static final int ALARM_RADIUS = 2;
    private static final int ALARM_TILE_IN_USE = 2;
    private static final int ALARM_MISSIONARY_PRESENT = -10;
    
    public enum ContactLevel {
        UNCONTACTED,     // Nothing known other than location?
        CONTACTED,       // Name, wanted-goods now visible
        VISITED,         // Skill now known
        SCOUTED          // Scouting bonus consumed
    };
	
    /** Radius of native tales map reveal. */
    public static final int TALES_RADIUS = 6;
    
	/** The missionary at this settlement. */
    private IndianSettlementMissionary missionary;
    
    /** The number of the turn during which the last tribute was paid. */
    private int lastTribute = 0;
    
    /**
     * This is the skill that can be learned by Europeans at this
     * settlement.  Its value will be null when the
     * skill has already been taught to a European.  
     */
    protected UnitType learnableSkill = null;
    private int convertProgress;
	protected final List<GoodsType> wantedGoods = new ArrayList<GoodsType>(IndianSettlementWantedGoods.MAX_WANTED_GOODS); 
    
    private java.util.Map<String,ContactLevel> contactLevelByPlayer = new HashMap<String, IndianSettlement.ContactLevel>();
    private java.util.Map<String, Tension> tensionByPlayer = new HashMap<String, Tension>();
    private final MapIdEntities<Unit> units = MapIdEntities.linkedMapIdEntities();
    
    public IndianSettlement(IdGenerator idGenerator, SettlementType settlementType) {
    	super(idGenerator.nextId(IndianSettlement.class), settlementType);
    	goodsContainer = new GoodsContainer();
    }

    /**
     * constructor used only by xml parser.
     * @param id
     * @param settlementType
     */
    private IndianSettlement(String id, SettlementType settlementType) {
		super(id, settlementType);
	}

    public boolean isVisitedBy(Player player) {
    	ContactLevel contactLevel = contactLevel(player);
    	return contactLevel == ContactLevel.VISITED || contactLevel == ContactLevel.SCOUTED;
    }
    
    private ContactLevel contactLevel(Player player) {
    	ContactLevel contactLevel = contactLevelByPlayer.get(player.getId());
    	if (contactLevel == null) {
    		return ContactLevel.UNCONTACTED;
    	}
    	return contactLevel;
    }
    
    public void visitBy(Player player) {
    	ContactLevel contactLevel = contactLevel(player);
    	if (contactLevel == ContactLevel.UNCONTACTED || contactLevel == ContactLevel.CONTACTED) {
    		if (contactLevel == ContactLevel.UNCONTACTED) {
    			setTension(player, getOwner().getTension(player).getValue());
    		}
    		contactLevelByPlayer.put(player.getId(), ContactLevel.VISITED);
    	}
    }
    
    public void scoutBy(Player player) {
    	ContactLevel contactLevel = contactLevel(player);
    	if (contactLevel != ContactLevel.SCOUTED) {
    		if (contactLevel == ContactLevel.UNCONTACTED) {
    			setTension(player, getOwner().getTension(player).getValue());
    		}
    		contactLevelByPlayer.put(player.getId(), ContactLevel.SCOUTED);
    	}
    }
    
    public boolean hasContact(Player player) {
    	return contactLevel(player) != ContactLevel.UNCONTACTED;
    }
    
    public boolean hasAnyScouted() {
    	for (Entry<String, ContactLevel> entrySet : contactLevelByPlayer.entrySet()) {
    		if (entrySet.getValue() == ContactLevel.SCOUTED) {
    			return true;
    		}
    	}
    	return false;
    }

	public void modifyTensionWithOwnerTension(Player player, int tensionValue) {
		modifyTension(player, tensionValue);
		getOwner().modifyTension(player, settlementType.isCapital() ? tensionValue : tensionValue / 2);
	}
    
	public void modifyTension(Player player, int tensionValue) {
		Tension tension = tensionByPlayer.get(player.getId());
		if (tension == null) {
			tension = new Tension(tensionValue);
			tensionByPlayer.put(player.getId(), tension);
		} else {
			tension.modify(tensionValue);
		}
	}

    public void setTension(Player player, int tensionValue) {
        Tension tension = tensionByPlayer.get(player.getId());
        if (tension == null) {
            tension = new Tension(tensionValue);
            tensionByPlayer.put(player.getId(), tension);
        } else {
            tension.setValue(tensionValue);
        }
    }
	
    public Tension getTension(Player player) {
    	Tension tension = tensionByPlayer.get(player.getId());
    	if (tension == null) {
    		tension = new Tension();
    		tensionByPlayer.put(player.getId(), tension);
    	}
    	return tension;
    }
    
    public void generateTension(Game game) {
		ObjectIntMap<Player> alarms = new ObjectIntMap<Player>();
		ObjectMap<Player, Level> oldTension = new ObjectMap<Player, Tension.Level>();
		
		generateTensionFromNeighbourTiles(game.map, alarms);
		generateTensionFromMissionary(alarms);
		
		for (ObjectIntMap.Entry<Player> alarmsEntry : alarms.entries()) {
			int change = alarmsEntry.value;
			if (change != 0) {
				Player enemy = alarmsEntry.key;
				change = (int)enemy.getFeatures().applyModifier(Modifier.NATIVE_ALARM_MODIFIER, change);
				oldTension.put(enemy, getTension(enemy).getLevel());
				modifyTensionWithOwnerTension(enemy, change);
			}
		}
		
		// Calm down a bit at the whole-tribe level.
		for (Player enemy : game.players.entities()) {
			if (!enemy.isLiveEuropeanPlayer()) {
				continue;
			}
			Tension enemyTension = owner.getTension(enemy);
			if (enemyTension.getValue() > 0) {
				int change = enemyTension.getValue() / 100 + 4;
				owner.modifyTensionAndPropagateToAllSettlements(enemy, -change);
			}
			
			Level newLevel = getTension(enemy).getLevel();
			if (owner.hasContacted(enemy) && newLevel.worst(oldTension.get(enemy))) {
				String key = "indianSettlement.alarmIncrease.tension." + newLevel.name().toLowerCase();
				if (Messages.containsKey(key)) {
					StringTemplate st = StringTemplate.template(key)
                        .addStringTemplate("%nation%", owner.getNationName())
                        .addStringTemplate("%enemy%", enemy.getNationName())
                        .add("%settlement%", getName());
					enemy.eventsNotifications.addMessageNotification(st);
				}
			}
		}
    }

    private void generateTensionFromMissionary(ObjectIntMap<Player> alarms) {
		if (hasMissionary()) {
			Player enemy = missionaryOwner();
			if (enemy.isLiveEuropeanPlayer()) {
				int missionAlarm = ALARM_MISSIONARY_PRESENT;
				if (getMissionary().isMissionaryExpert()) {
					missionAlarm *= 2;
				}
				alarms.getAndIncrement(enemy, 0, missionAlarm);
			}
		}
	}

	private void generateTensionFromNeighbourTiles(Map map, ObjectIntMap<Player> alarms) {
		int radius = settlementType.getClaimableRadius() + ALARM_RADIUS;
		for (Tile neighbourTile : map.neighbourTiles(tile.x, tile.y, radius)) {
			if (neighbourTile.getUnits().isNotEmpty()) {
				Unit firstUnit = neighbourTile.getUnits().first();
				Player enemy = firstUnit.getOwner();
				
				if (enemy.isLiveEuropeanPlayer()) {
					int alarm = 0;
					for (Unit unit : neighbourTile.getUnits().entities()) {
						if (!unit.isNaval() && UnitMethods.isOffensiveUnit(unit)) {
							alarm += unit.unitType.getBaseOffence(); 
						}
					}
					alarms.getAndIncrement(enemy, 0, alarm);
				}
			} else if (neighbourTile.hasSettlement() && neighbourTile.getSettlement().isColony()) {
				Colony colony = neighbourTile.getSettlement().asColony();
				alarms.getAndIncrement(colony.getOwner(), 0, ALARM_TILE_IN_USE + colony.getColonyUnitsCount());
			} else if (neighbourTile.getOwner() != null) {
				Player enemy = neighbourTile.getOwner();
				if (neighbourTile.getOwner().isLiveEuropeanPlayer()) {
					alarms.getAndIncrement(enemy, 0, ALARM_TILE_IN_USE);
				}
			}
		}
    }
    
    @Override
    public boolean hasAbility(String abilityCode) {
        return false;
    }

	@Override
	public void addModifiersTo(ObjectWithFeatures mods, String modifierCode) {
		mods.addModifierFrom(settlementType, modifierCode);
	}
    
    public String getImageKey() {
    	String st = owner.nation().getId();
    	
    	if (settlementType.isCapital()) {
    		st += ".capital";
    	} else {
    		st += ".settlement";
    	}
    	
    	if (hasMissionary()) {
    		st += "";
    	} else {
    		st += ".mission";
    	}
    	st += ".image";
        return st;
    }
    
	@Override
	public boolean isIndianSettlement() {
		return true;
	}
	
	public int plunderGold(Unit attacker) {
		if (settlementType == null) {
			return 0;
		}
		return settlementType.plunderGold(attacker);
	}

	public int demandTribute(Turn turn, Player demander) {
		final int TURNS_PER_TRIBUTE = 5;
		
		int gold = 0;
		SettlementTypeGift giftRange = settlementType.getGift();
		if (lastTribute + TURNS_PER_TRIBUTE < turn.getNumber() && giftRange != null) {
			Level tensionLevel = owner.getTension(demander).getLevel();
			switch (tensionLevel) {
			case HAPPY:
			case CONTENT:
				gold = Math.min(giftRange.randomValue() / 10, 100);
				break;
			case DISPLEASED:
				gold = Math.min(giftRange.randomValue() / 20, 100);
				break;
			case ANGRY:
			case HATEFUL:
				gold = 0;
			default:
				break;
			}
		}

		visitBy(demander);
		modifyTensionWithOwnerTension(demander, Tension.TENSION_ADD_NORMAL);		
		lastTribute = turn.getNumber();
		return gold;
	}
	
	public void spreadMilitaryGoods() {
		Settlement randomSettlement = Randomizer.instance().randomMember(owner.settlements);
		if (randomSettlement == null || randomSettlement.equalsId(this)) {
			return;
		}
		for (GoodsType goodsType : Specification.instance.goodsTypes.entities()) {
			if (goodsType.isMilitary()) {
				int goodsInStock = goodsContainer.goodsAmount(goodsType);
				if (goodsInStock > 50) {
					goodsContainer.transferGoods(goodsType, goodsInStock / 2, randomSettlement.getGoodsContainer());
				}
			}
		}
	}
	
	public void equipMilitaryRoles() {
		List<Unit> unitsToEquip = null;
		
		for (UnitRole milUnitRole : Specification.instance.nativeMilitaryRoles) {
			if (hasGoodsToEquipRole(milUnitRole)) {
				if (unitsToEquip == null) {
					// little of optymalization
					unitsToEquip = new ArrayList<Unit>(getUnits().size() + tile.getUnits().size());
					unitsToEquip.addAll(getUnits().entities());
					unitsToEquip.addAll(tile.getUnits().entities());
				}
				
				for (Unit unit : unitsToEquip) {
					if (unit.isRoleAvailable(milUnitRole) 
							&& hasGoodsToEquipRole(milUnitRole) 
							&& milUnitRole.hasMoreOffensivePower(unit.unitRole)
					) {
						changeUnitRole(unit, milUnitRole, owner.getFeatures());
					}
				}
			}
		}
	}
	
	public Entry<String, Tension> mostHatedPlayer(MapIdEntities<Player> players) {
		Entry<String, Tension> mostHatedPlayer = null;
		int playerTensionLevel = Integer.MIN_VALUE;

		for (Entry<String, Tension> entry : tensionByPlayer.entrySet()) {
			Tension tension = entry.getValue();
			Player player = players.getById(entry.getKey());
			
			if (player.isNotLiveEuropeanPlayer() || tension.getLevel() == Tension.Level.HAPPY) {
				continue;
			}
			if (playerTensionLevel < tension.getValue()) {
				mostHatedPlayer = entry;
				playerTensionLevel = tension.getValue();
			}
		}
		return mostHatedPlayer;
	}
	
	public void learnSkill(Unit unit, boolean enhancedMissionaries) {
		unit.changeUnitType(learnableSkill);
		if (!settlementType.isCapital() && !(hasMissionary(unit.getOwner()) && enhancedMissionaries)) {
			learnableSkill = null;
		}
	}
	
	public IndianNationType indianNationType() {
		return (IndianNationType)owner.nationType();
	}
	
	public void conversion(Map map) {
		if (!hasMissionary()) {
			return;
		}
		
		Unit missionary = getMissionary().unit;
		float conversionSkill = missionary.unitType.applyModifier(Modifier.CONVERSION_SKILL, missionary.unitType.getSkill());
		int alarm = Math.min(getTension(missionary.getOwner()).getValue(), Tension.TENSION_MAX);
		int conversionAlarm = (int)missionary.unitType.applyModifier(Modifier.CONVERSION_ALARM_RATE, alarm);
		int convert = convertProgress + (int)conversionSkill + (conversionAlarm - alarm);
		
		if (convert >= settlementType.getConvertThreshold() && (getUnits().size() + tile.getUnits().size() > 2)) {
			Colony colony = map.findColonyInRange(tile, MAX_CONVERT_DISTANCE, missionary.getOwner()); 
			if (colony == null) {
				convertProgress = convert;
				System.out.println("IndianConversion[" + getId() + "].no " + convert);
				return;
			}
			convertProgress = 0;
			
			System.out.println("IndianConversion[" + getId() + "].conversion " + convert + " to " + colony);
			
			convertToDest(colony.tile, missionary.getOwner());
			colony.owner.eventsNotifications.addMessageNotification(
				StringTemplate.template("model.colony.newConvert")
					.addStringTemplate("%nation%", owner.getNationName())
					.add("%colony%", colony.getName())
			);
		} else {
			convertProgress = convert;
			System.out.println("IndianConversion[" + getId() + "].no " + convert);
		}
	}

    public Unit convertToDest(Tile toTile, Player toPlayer) {
		List<Unit> unitsToConvert = new ArrayList<Unit>();
		unitsToConvert.addAll(tile.getUnits().entities());
		unitsToConvert.addAll(getUnits().entities());
		
		Unit convert = Randomizer.instance().randomMember(unitsToConvert);
		convert.changeOwner(toPlayer);
		convert.changeUnitType(ChangeType.CONVERSION);
		convert.changeRole(Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID));
        convert.reduceMovesLeftToZero();
        convert.setState(Unit.UnitState.ACTIVE);
        convert.changeUnitLocation(toTile);
    	return convert;
    }

	public void resetConvertProgress() {
		convertProgress = 0;
	}

	public void setConvertProgress(int convertProgress) {
		this.convertProgress = convertProgress;
	}
    
	public List<GoodsType> getWantedGoods() {
		return wantedGoods;
	}
	
	@Override
    public int warehouseCapacity() {
		return ProductionSummary.CARRIER_SLOT_MAX_QUANTITY * settlementType.getClaimableRadius();
	}
	
	public static class Xml extends XmlNodeParser<IndianSettlement> {

        private static final String ATTR_LEVEL = "level";
		private static final String ATTR_PLAYER = "player";
		private static final String ELEMENT_ALARM = "alarm";
		private static final String ELEMENT_CONTACT_LEVEL = "contactLevel";
		private static final String ATTR_SETTLEMENT_TYPE = "settlementType";
		private static final String ATTR_OWNER = "owner";
		private static final String ATTR_NAME = "name";
		private static final String ATTR_LAST_TRIBUTE = "lastTribute";		
		private static final String ATTR_LEARNABLE_SKILL = "learnableSkill";		
		private static final String ATTR_WANTED_GOODS = "wantedGoods";
		private static final String ATTR_CONVERT_PROGRESS = "convertProgress";
		
		public Xml() {
            addNode(Unit.class, new ObjectFromNodeSetter<IndianSettlement, Unit>() {
                @Override
                public void set(IndianSettlement target, Unit entity) {
                    entity.changeUnitLocation(target);
                }

                @Override
                public void generateXml(IndianSettlement source, ChildObject2XmlCustomeHandler<Unit> xmlGenerator) throws IOException {
                    xmlGenerator.generateXmlFromCollection(source.units.entities());
                }
            });
            addNode(GoodsContainer.class, new ObjectFromNodeSetter<IndianSettlement, GoodsContainer>() {
				@Override
				public void set(IndianSettlement target, GoodsContainer entity) {
					target.goodsContainer = entity;
				}

				@Override
				public void generateXml(IndianSettlement source, ChildObject2XmlCustomeHandler<GoodsContainer> xmlGenerator)
						throws IOException {
					xmlGenerator.generateXml(source.goodsContainer);
				}
            });
			addNode(IndianSettlementMissionary.class, "missionary");
		}
		
		@Override
        public void startElement(XmlNodeAttributes attr) {
			Player owner = game.players.getById(attr.getStrAttribute(ATTR_OWNER));
            SettlementType settlementType = owner.nationType()
        		.settlementTypes
        		.getById(attr.getStrAttribute(ATTR_SETTLEMENT_TYPE));
            
			IndianSettlement is = new IndianSettlement(
        		attr.getStrAttributeNotNull(ATTR_ID),
        		settlementType
    		);
            is.name = attr.getStrAttribute(ATTR_NAME);
            is.owner = owner;
            is.lastTribute = attr.getIntAttribute(ATTR_LAST_TRIBUTE, 0);
            is.learnableSkill = attr.getEntity(ATTR_LEARNABLE_SKILL, Specification.instance.unitTypes);
            is.convertProgress = attr.getIntAttribute(ATTR_CONVERT_PROGRESS, 0);
            
            owner.settlements.add(is);
            
            String goodsId = null;
            int goodsIdIndex = 0;
			while ((goodsId = attr.getStrAttribute(ATTR_WANTED_GOODS + goodsIdIndex)) != null) {
            	is.wantedGoods.add(Specification.instance.goodsTypes.getById(goodsId));
            	goodsIdIndex++;
            }
            
            nodeObject = is;
        }
		

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
        	if (attr.isQNameEquals(ELEMENT_CONTACT_LEVEL)) {
        		nodeObject.contactLevelByPlayer.put(
    				attr.getStrAttribute(ATTR_PLAYER), 
    				attr.getEnumAttribute(ContactLevel.class, ATTR_LEVEL)
        		);
        	}
        	if (attr.isQNameEquals(ELEMENT_ALARM)) {
        		nodeObject.tensionByPlayer.put(
        			attr.getStrAttribute(ATTR_PLAYER), 
        			new Tension(attr.getIntAttribute(ATTR_VALUE))
        		);
        	}
        }
        
        @Override
        public void startWriteAttr(IndianSettlement is, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(is);
        	attr.set(ATTR_NAME, is.name);
        	attr.set(ATTR_OWNER, is.owner);
        	attr.set(ATTR_SETTLEMENT_TYPE, is.settlementType);
        	attr.set(ATTR_LAST_TRIBUTE, is.lastTribute, 0);
        	attr.set(ATTR_LEARNABLE_SKILL, is.learnableSkill);
        	attr.set(ATTR_CONVERT_PROGRESS, is.convertProgress);
        	
        	for (int i=0; i<is.wantedGoods.size(); i++) {
        		attr.set(ATTR_WANTED_GOODS + i, is.wantedGoods.get(i));
        	}
        	
        	for (Entry<String, ContactLevel> contactEntry : is.contactLevelByPlayer.entrySet()) {
				attr.xml.element(ELEMENT_CONTACT_LEVEL);
				attr.set(ATTR_PLAYER, contactEntry.getKey());
				attr.set(ATTR_LEVEL, contactEntry.getValue());
				attr.xml.pop();
			}
        	for (Entry<String, Tension> tensionEntry : is.tensionByPlayer.entrySet()) {
				attr.xml.element(ELEMENT_ALARM);
				attr.set(ATTR_PLAYER, tensionEntry.getKey());
				attr.set(ATTR_VALUE, tensionEntry.getValue().getValue());
				attr.xml.pop();
			}
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "indianSettlement";
        }
    }

	@Override
	public int applyModifiers(String modifierCode, int val) {
		throw new IllegalStateException("not implemented");
	}

	@Override
	public void updateProductionToMaxPossible(Tile tile) {
	}
	
	@Override
	public ProductionSummary productionSummary() {
		throw new IllegalStateException("not implemented");
	}

	public UnitType getLearnableSkill() {
		return learnableSkill;
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
	
    public boolean hasMissionary() {
        return missionary != null && missionary.unit != null;
    }

    public boolean hasMissionary(Player player) {
        return missionary != null && missionary.unit != null && missionary.unit.getOwner().equalsId(player);
    }
    
    public boolean hasMissionaryNotPlayer(Player player) {
    	return missionary != null 
			&& missionary.unit != null 
			&& missionary.unit.getOwner().notEqualsId(player);
    }

    public IndianSettlementMissionary getMissionary() {
    	return missionary;
    }
    
    public Player missionaryOwner() {
    	return missionary.unit.getOwner();
    }

    public void changeMissionary(Unit newMissionary) {
    	if (missionary != null && missionary.unit != null) {
    		missionary.unit.getOwner().eventsNotifications.addMessageNotification(
				StringTemplate.template("indianSettlement.mission.denounced")
					.add("%settlement%", getName())
			);
    		removeMissionary();
    	}
    	if (missionary == null) {
    		missionary = new IndianSettlementMissionary();
    	}
    	newMissionary.changeUnitLocation(missionary);
    	newMissionary.reduceMovesLeftToZero();
    	resetConvertProgress();
    	modifyTensionWithOwnerTension(newMissionary.getOwner(), Tension.ALARM_NEW_MISSIONARY);
    	newMissionary.getOwner().fogOfWar.fogOfWarForMissionary(this, newMissionary.getOwner());
    }
    
	public void removeMissionary() {
		Unit m = missionary.unit;
		m.getOwner().removeUnit(m);
		missionary.unit = null;
		missionary = null;
	}
}
