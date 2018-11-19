package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Tension;
import net.sf.freecol.common.model.player.Tension.Level;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class IndianSettlement extends Settlement {
    
    public static enum ContactLevel {
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
	protected final List<GoodsType> wantedGoods = new ArrayList<GoodsType>(3); 
    
    private java.util.Map<String,ContactLevel> contactLevelByPlayer = new HashMap<String, IndianSettlement.ContactLevel>();
    private java.util.Map<String, Tension> tensionByPlayer = new HashMap<String, Tension>();
    private final MapIdEntities<Unit> units = MapIdEntities.linkedMapIdEntities();
    private final GoodsContainer goodsContainer;
    
    public IndianSettlement(IdGenerator idGenerator, SettlementType settlementType) {
    	super(idGenerator.nextId(IndianSettlement.class), settlementType);
    	goodsContainer = new GoodsContainer();
    }

    /**
     * constructor used only by xml parser.
     * Xml parser also create {@link goodsContainer}
     * @param id
     * @param settlementType
     */
    private IndianSettlement(String id, SettlementType settlementType) {
		super(id, settlementType);
		goodsContainer = new GoodsContainer();
	}

    public void visitedBy(Player player) {
    	ContactLevel contactLevel = contactLevelByPlayer.get(player.getId());
    	if (contactLevel == null) {
    		contactLevel = ContactLevel.UNCONTACTED;
    	}
    	if (contactLevel == ContactLevel.UNCONTACTED || contactLevel == ContactLevel.CONTACTED) {
    		if (contactLevel == ContactLevel.UNCONTACTED) {
    			setTension(player, getOwner().getTension(player).getValue());
    		}
    		contactLevelByPlayer.put(player.getId(), ContactLevel.VISITED);
    	}
    }
    
    public void scoutedBy(Player player) {
    	ContactLevel contactLevel = contactLevelByPlayer.get(player.getId());
    	if (contactLevel == null) {
    		contactLevel = ContactLevel.UNCONTACTED;
    	}
    	if (contactLevel != ContactLevel.SCOUTED) {
    		if (contactLevel == ContactLevel.UNCONTACTED) {
    			setTension(player, getOwner().getTension(player).getValue());
    		}
    		contactLevelByPlayer.put(player.getId(), ContactLevel.SCOUTED);
    	}
    }
    
    public boolean hasContact(Player player) {
    	ContactLevel level = contactLevelByPlayer.get(player.getId());
    	if (level == null) {
    		return false;
    	}
    	return level != ContactLevel.UNCONTACTED;
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
	
	@Override
	public GoodsContainer getGoodsContainer() {
		return goodsContainer;
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

		visitedBy(demander);
		modifyTensionWithOwnerTension(demander, Tension.TENSION_ADD_NORMAL);		
		lastTribute = turn.getNumber();
		return gold;
	}
	
	public Entry<String, Tension> mostHatedPlayer(MapIdEntities<Player> players) {
		Entry<String, Tension> mostHatedPlayer = null;
		int playerTensionLevel = Integer.MIN_VALUE;

		for (Entry<String, Tension> entry : tensionByPlayer.entrySet()) {
			Tension tension = entry.getValue();
			Player player = players.getById(entry.getKey());
			
			if (player.isNotLiveEuropeanPlayer()) {
				continue;
			}
			if (tension.getLevel() == Tension.Level.HAPPY) {
				continue;
			}
			if (playerTensionLevel < tension.getValue()) {
				mostHatedPlayer = entry;
				playerTensionLevel = tension.getValue();
			}
		}
		return mostHatedPlayer;
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
			
			addNode(GoodsContainer.class, "goodsContainer");
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
	public void addGoods(String goodsTypeId, int quantity) {
	    goodsContainer.increaseGoodsQuantity(goodsTypeId, quantity);
	}

	@Override
	public boolean isContainsTile(Tile improvingTile) {
		throw new IllegalStateException("not implemented");
	}

	@Override
	public void initMaxPossibleProductionOnTile(Tile tile) {
		throw new IllegalStateException("not implemented");
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
	
	@Override
	public boolean canAutoLoadUnit() {
		return false;
	}

	@Override
	public boolean canAutoUnloadUnits() {
		return false;
	}

    private boolean hasMissionary() {
        return missionary != null;
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
    
	public void removeMissionary() {
		Unit m = missionary.unit;
		m.getOwner().removeUnit(m);
		missionary.unit = null;
		missionary = null;
	}

	public List<GoodsType> getWantedGoods() {
		return wantedGoods;
	}
}
