package net.sf.freecol.common.model.player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.xml.sax.SAXException;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IdGenerator;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Nation;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.NationType;
import promitech.colonization.SpiralIterator;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;
import promitech.map.Boolean2dArray;

public class Player extends ObjectWithId {
	
    public static enum PlayerType {
        NATIVE, COLONIAL, REBEL, INDEPENDENT, ROYAL, UNDEAD, RETIRED
    }
	
    private Nation nation;
    private NationType nationType;
    private PlayerType playerType;
    private Europe europe;
    private Market market;
    private boolean dead = false;
    private boolean ai;
    private int tax = 0;
    private int gold = 0;
    private int interventionBells = 0;
    private int entryLocationX = 0;
    private int entryLocationY = 0;
    private String newLandName;
    private String name;
    
    /**
     * The number of immigration points.  Immigration points are an
     * abstract game concept.  They are generated by but are not
     * identical to crosses.
     */
    protected int immigration = 0;

    /**
     * The amount of immigration needed until the next unit decides
     * to migrate.
     */
    protected int immigrationRequired = 0;
    private boolean attackedByPrivateers = false;
    
    private String independentNationName;
    public final MapIdEntities<Unit> units = new MapIdEntities<Unit>();
    public final MapIdEntities<Settlement> settlements = new MapIdEntities<Settlement>();
    private HighSeas highSeas;
    protected Monarch monarch;
    private final ObjectWithFeatures updatableFeatures;
    public FoundingFathers foundingFathers;
    
    public EventsNotifications eventsNotifications = new EventsNotifications();
    
    public final PlayerForOfWar fogOfWar = new PlayerForOfWar(); 
    private Boolean2dArray exploredTiles;
    
    private final java.util.Map<String, Stance> stance = new HashMap<String, Stance>();
    private final java.util.Map<String, Tension> tension = new HashMap<String, Tension>();
    protected List<String> banMission = null;
    
    public static Player newStartingPlayer(IdGenerator idGenerator, Nation nation, String name) {
    	Player player = new Player(idGenerator.nextId(Player.class));
    	player.ai = true; 
        player.nation = nation;
        player.nationType = nation.nationType;
        player.name = name;
        player.updatableFeatures.addFeaturesAndOverwriteExisted(nation.nationType);
        if (nation.nationType.isEuropean()) {
        	player.changePlayerType(PlayerType.COLONIAL);
        	
        	player.europe = Europe.newStartingEurope(idGenerator, player);
        	player.monarch = Monarch.newStartingMonarch(player);
        	player.highSeas = new HighSeas();
        	
        	player.gold = Specification.options.getIntValue(GameOptions.STARTING_MONEY);
        	player.immigrationRequired = (int)player.updatableFeatures.applyModifier(
    			Modifier.RELIGIOUS_UNREST_BONUS, 
    			Specification.options.getIntValue(GameOptions.INITIAL_IMMIGRATION)
        	);
        } else {
        	player.changePlayerType(PlayerType.NATIVE);
        }
        player.foundingFathers = new FoundingFathers(player);
        
        player.market = new Market(idGenerator.nextId(Market.class));
        player.market.initGoods();
        
    	return player;
    }
    
    public Player(String id) {
    	super(id);
    	updatableFeatures = new ObjectWithFeatures("tmp:" +  id);
    }
    
    public Nation nation() {
    	return nation;
    }
    
    public StringTemplate getNationName() {
        return (playerType == PlayerType.REBEL || playerType == PlayerType.INDEPENDENT)
            ? StringTemplate.name(independentNationName)
            : StringTemplate.key(Messages.nameKey(nation.getId()));
    }
    
	public NationType nationType() {
		return nationType;
	}
    
	public Market market() {
		return market;
	}
	
    /**
     * What is the name of the player's market?
     * Following a declaration of independence we are assumed to trade
     * broadly with any European market rather than a specific port.
     *
     * @return A <code>StringTemplate</code> for the player market.
     */
    public StringTemplate marketName() {
        return (getEurope() == null)
            ? StringTemplate.key("model.market.independent")
            : StringTemplate.key(getEuropeNameKey());
    }
	
    public String toString() {
        return "id = " + id + ", nation = " + nation;
    }
    
    public boolean atWarWith(Player player) {
        return getStance(player) == Stance.WAR;
    }
    
    public boolean atWarWithAnyEuropean(MapIdEntities<Player> players) {
        for (Player p : players.entities()) {
            if (p.isLiveEuropeanPlayer() && p.notEqualsId(this) && this.atWarWith(p)) { 
                return true;
            }
        }
        return false;
    }
    
    public boolean hasContacted(Player player) {
        return getStance(player) != Stance.UNCONTACTED;
    }
    
    public boolean atPeaceOrAlliance(Player player) {
    	Stance s = getStance(player);
    	return s == Stance.PEACE || s == Stance.ALLIANCE;    	
    }
    
    public boolean atPeaceOrCeaseFire(Player player) {
    	Stance s = getStance(player);
    	return s == Stance.PEACE || s == Stance.CEASE_FIRE;
    }
    
    public boolean atWarOrCeaseFire(Player player) {
    	Stance s = getStance(player);
    	return s == Stance.WAR || s == Stance.CEASE_FIRE;
    }
    
	public boolean hasPeaceOrAllianceWithOneOfEuropeanPlayers(Game game) {
		for (Player other : game.players.entities()) {
			if (other.isNotLiveEuropeanPlayer()) {
				continue;
			}
			if (this.equalsId(other)) {
				continue;
			}
			if (this.atPeaceOrAlliance(other)) {
				return true;
			}
		}
		return false;
	}
    
    public Stance getStance(Player player) {
    	if (player == null) {
    		return Stance.UNCONTACTED;
    	}
    	Stance stance = this.stance.get(player.getId());
    	if (stance == null) {
    		return Stance.UNCONTACTED;
    	}
    	return stance;
    }
    
    public void changeStance(final Player otherPlayer, final Stance newStance) {
    	Stance old = getStance(otherPlayer);
    	
    	if (old != newStance) {
            System.out.println("player " + this.getId() + "[" + this.nation.getId() 
                + "] change stance to player " + otherPlayer.getId() + "[" + otherPlayer.nation.getId() 
                + "] from " + old + " to " + newStance 
            );

    		modifyStance(otherPlayer, newStance);
    		int modifier = old.getTensionModifier(newStance);
			modifyTension(otherPlayer, modifier);
    		
			if (old != Stance.UNCONTACTED) {
				String msgBody = Messages.message(StringTemplate.template("model.diplomacy." + newStance + ".declared")
					.addName("%nation%", nation())
				);
				MessageNotification msg = new MessageNotification(Game.idGenerator, msgBody);
				otherPlayer.eventsNotifications.addMessageNotification(msg);
			}
    	}
    	
    	old = otherPlayer.getStance(this);
    	if (old != newStance) {
    		otherPlayer.modifyStance(this, newStance);
    		int modifier = old.getTensionModifier(newStance);
    		otherPlayer.modifyTension(otherPlayer, modifier);
    		
            if (old != Stance.UNCONTACTED) {
				String msgBody = Messages.message(StringTemplate.template("model.diplomacy." + newStance + ".declared")
					.addName("%nation%", otherPlayer.nation())
				);
				MessageNotification msg = new MessageNotification(Game.idGenerator, msgBody);
				this.eventsNotifications.addMessageNotification(msg);
            }
    	}
    }
    
    private void modifyStance(Player p, Stance newStance) {
        if (newStance == null) {
            stance.remove(p.getId());
            return;
        }
        Stance oldStance = stance.get(p.getId());
        if (newStance == oldStance) {
        	return;
        }
        stance.put(p.getId(), newStance);
    }
    
    public void modifyTension(Player p, int val) {
		if (val == 0) {
    		return;
    	}
    	Tension tensionObj = tension.get(p.getId());
    	if (tensionObj == null) {
    		tensionObj = new Tension(Tension.TENSION_MIN);
    	}
    	tensionObj.modify(val);
    }
    
	public void modifyTensionAndPropagateToAllSettlements(Player player, int tensionValue) {
		modifyTension(player, tensionValue);
		for (Settlement settlement : settlements.entities()) {
			IndianSettlement indianSettlement = settlement.getIndianSettlement();
			if (indianSettlement.hasContact(player)) {
				int tension = indianSettlement.settlementType.isCapital() ? tensionValue : tensionValue / 2; 
				indianSettlement.modifyTension(player, tension);
			}
		}
	}
    
    public Tension getTension(Player p) {
        Tension tensionObj = tension.get(p.getId());
        if (tensionObj == null) {
            return new Tension(Tension.TENSION_MIN);
        }
        return tensionObj;
    }
    
    public void addMissionBan(Player player) {
        if (banMission == null) {
            banMission = new ArrayList<String>();
        }
        banMission.add(player.getId());
    }
    
    private void changePlayerType(PlayerType type) {
        if (playerType != PlayerType.REBEL && playerType != PlayerType.INDEPENDENT) {
            switch (type) {
            case REBEL: 
            case INDEPENDENT:
//                addAbility(Ability.INDEPENDENCE_DECLARED, true);
//                addAbility(Ability.INDEPENDENT_NATION, true);
                break;
            default:
                break;
            }
        }
        playerType = type;
    }
    
	public boolean missionsBanned(Player player) {
    	return false;
    }
    
    public boolean isIndian() {
        return playerType == PlayerType.NATIVE;
    }
	
    public boolean isRoyal() {
        return playerType == PlayerType.ROYAL;
    }
    
	public boolean canMoveToEurope() {
		return europe != null;
	}
    
    public String getEuropeNameKey() {
        return (europe == null) ? null : nation.getId() + ".europe";
    }
	
	/**
	 * @return boolean - return true when set some tile as explored 
	 */
	public boolean setTileAsExplored(Tile tile) {
		return exploredTiles.setAndReturnDifference(tile.x, tile.y, true);
	}
	
	public boolean setTileAsExplored(int coordsIndex) {
		return exploredTiles.setAndReturnDifference(coordsIndex, true);
	}
	
	public boolean isTileUnExplored(Tile tile) {
		return !isTileExplored(tile.x, tile.y);
	}
	
	public boolean isTileExplored(int x, int y) {
		return exploredTiles.get(x, y);
	}
	
	public boolean isTileExplored(int coordsIndex) {
		return exploredTiles.get(coordsIndex);
	}
	
	public void initExploredMap(Map map) {
		exploredTiles = new Boolean2dArray(map.width, map.height, false);
	}
	
	public void explorAllTiles() {
		exploredTiles.set(true);
	}
    
	/**
	 * Method populate {@link MoveExploredTiles} <code>exploredTiles</code> with explored
	 * tiles
	 */
	public void revealMapAfterUnitMove(Map map, Unit unit, MoveExploredTiles exploredTiles) {
		Tile unitTileLocation = unit.getTile();
		
		setTileAsExplored(unitTileLocation);
		fogOfWar.removeFogOfWar(unitTileLocation);
		
		int radius = unit.lineOfSight();
		SpiralIterator spiralIterator = new SpiralIterator(map.width, map.height);
		spiralIterator.reset(unitTileLocation.x, unitTileLocation.y, true, radius);
		
		while (spiralIterator.hasNext()) {
			int coordsIndex = spiralIterator.getCoordsIndex();
			
			if (setTileAsExplored(coordsIndex)) {
				exploredTiles.addExploredTile(spiralIterator.getX(), spiralIterator.getY());
			}
			if (fogOfWar.removeFogOfWar(coordsIndex)) {
				exploredTiles.addRemoveFogOfWar(spiralIterator.getX(), spiralIterator.getY());
			}
			
			spiralIterator.next();
		}
	}

	public void revealMapSeeColony(Map map, Colony colony) {
		int radius = (int)getFeatures().applyModifier(
			Modifier.LINE_OF_SIGHT_BONUS, 
			colony.settlementType.getVisibleRadius()
		);
		
		setTileAsExplored(colony.tile);
		for (Tile t : map.neighbourTiles(colony.tile, radius)) {
			setTileAsExplored(t);
		}
	}
	
    public Europe getEurope() {
        return europe;
    }
	
    public int unitTypeCount(UnitType unitType) {
    	int counter = 0;
    	for (Unit unit : units.entities()) {
    		if (unit.unitType.equalsId(unitType)) {
    			counter++;
    		}
    	}
    	return counter;
    }
    
    public boolean hasUnitType(String unitTypeId) {
        for (Unit unit : units.entities()) {
            if (unit.unitType.equalsId(unitTypeId)) {
                return true;
            }
        }
        return false;
    }
    
    public void addGold(int gold) {
    	this.gold += gold;
    }
    
    public void subtractGold(int gold) {
    	this.gold -= gold;
        if (this.gold < 0) {
            this.gold = 0;
        }
    }
    
    public void transferGoldToPlayer(int gold, Player player) {
    	this.subtractGold(gold);
    	player.addGold(gold);
    }
    
    public boolean hasGold() {
        return this.gold > 0;
    }
    
    public boolean hasNotGold(int gold) {
    	return this.gold < gold;
    }
    
    public boolean hasGold(int gold) {
    	return this.gold >= gold;
    }
    
    public int getGold() {
    	return gold;
    }
    
    public boolean isEuropean() {
        return playerType == PlayerType.COLONIAL
            || playerType == PlayerType.REBEL
            || playerType == PlayerType.INDEPENDENT
            || playerType == PlayerType.ROYAL;
    }

    public boolean isColonial() {
        return playerType == PlayerType.COLONIAL;
    }
    
    public boolean isIndependent() {
    	return playerType == PlayerType.INDEPENDENT;
    }
    
    public boolean isRebel() {
        return playerType == PlayerType.REBEL;
    }
    
	public boolean isNotLiveEuropeanPlayer() {
		return isDead() || !isEuropean(); 
	}
    
	public boolean isDead() {
		return dead;
	}
	
	public boolean isLive() {
		return !dead;
	}
	
    public boolean isLiveEuropeanPlayer() {
        return !isDead() && isEuropean();
    }
	
    public boolean isLiveIndianPlayer() {
    	return isIndian() && !isDead();
    }
    
	public void endTurn() {
	}

	public void modifyLiberty(int libertyAmount) {
		if (!canHaveFoundingFathers()) {
			return;
		}
		foundingFathers.modifyLiberty(libertyAmount);
		if (isRebel()) {
			interventionBells += libertyAmount;
		}
	}
	
    public void modifyImmigration(int amount) {
        immigration = Math.max(0, immigration + amount);
    }

    public int getImmigrationProduction() {
        int production = 0;
        for (Settlement settlement : settlements.entities()) {
            ProductionSummary productionSummary = settlement.productionSummary();
            for (GoodsType goodsType : Specification.instance.immigrationGoodsTypeList.entities()) {
                production += productionSummary.getQuantity(goodsType.getId());
            }
        }
        return production;
    }
    
	public int getImmigrationRequired() {
		return immigrationRequired;
	}

	public int getImmigration() {
		return immigration;
	}
	
	public boolean shouldANewColonistEmigrate() {
		return immigrationRequired <= immigration;
	}
	
    public void updateImmigrationRequired() {
        if (!isColonial()) {
        	return;
        }
        final int current = immigrationRequired;
        final int base = Specification.options.getIntValue(GameOptions.CROSSES_INCREMENT);
        // If the religious unrest bonus is present, immigrationRequired
        // has already been reduced.  We want to apply the bonus to the
        // sum of the *unreduced* immigration target and the increment.
        final int unreduced = Math.round(current / updatableFeatures.applyModifier(Modifier.RELIGIOUS_UNREST_BONUS, 1f));
        immigrationRequired = (int)updatableFeatures.applyModifier(Modifier.RELIGIOUS_UNREST_BONUS, unreduced + base);
    }
	
	public void reduceImmigration() {
		if (Specification.options.getBoolean(GameOptions.SAVE_PRODUCTION_OVERFLOW)) {
			immigration = immigrationRequired - immigration;
		} else {
			immigration = 0;
		}
	}
    
    public final boolean canHaveFoundingFathers() {
        return nationType.hasAbility(Ability.ELECT_FOUNDING_FATHER);
    }
	
	public void removeUnit(Unit unit) {
		units.removeId(unit);
		unit.remove();
	}
	
    public HighSeas getHighSeas() {
        return highSeas;
    }
	
    public int getTax() {
        return tax;
    }

	protected void setTax(int tax) {
		this.tax = tax;
	}
    
	public ObjectWithFeatures getFeatures() {
		return updatableFeatures;
	}

	public void addFoundingFathers(Game game, FoundingFather father) {
		foundingFathers.add(game, father);
		updatableFeatures.addFeatures(father);
		
		for (Settlement settlement : settlements.entities()) {
			if (settlement.isColony()) {
				settlement.getColony().updateColonyFeatures();
			}
		}
	}

	public int getEntryLocationX() {
		return entryLocationX;
	}

	public int getEntryLocationY() {
		return entryLocationY;
	}

	public void setEntryLocation(int x, int y) {
		this.entryLocationX = x;
		this.entryLocationY = y;
	}
	
	public String getNewLandName() {
		return newLandName;
	}
	
    public void setNewLandName(String newLandName) {
        this.newLandName = newLandName;
    }
	
    public boolean isAttackedByPrivateers() {
        return attackedByPrivateers;
    }

    public void setAttackedByPrivateers() {
        this.attackedByPrivateers = true;
    }
    
    public Monarch getMonarch() {
        return monarch;
    }

	public void addSettlement(Settlement settlement) {
		settlements.add(settlement);
		settlement.setOwner(this);
	}

	private void afterReadPlayer() {
		if (europe != null) {
			europe.setOwner(this);
		}
		if (monarch != null) {
			monarch.setPlayer(this);
		}
		if (foundingFathers == null) {
			foundingFathers = new FoundingFathers(this);
		} else {
			foundingFathers.setPlayer(this);
		}
		updatableFeatures.addFeatures(foundingFathers.entities());			
	}

	public boolean isAi() {
		return ai;
	}
	
	public boolean isHuman() {
		return !ai;
	}
	
	public void setAi(boolean ai) {
		this.ai = ai;
	}
	
	public void setHuman() {
		this.ai = false; 
	}

	public String getName() {
		return name;
	}
	
	public static class Xml extends XmlNodeParser<Player> {
		private static final String ATTR_AI = "ai";
		private static final String ATTR_PLAYER = "player";
		private static final String ELEMENT_TENSION = "tension";
		private static final String ELEMENT_STANCE = "stance";
		private static final String ELEMENT_BAN_MISSION = "ban-mission";
		private static final String ATTR_PLAYER_TYPE = "playerType";
		private static final String ATTR_ENTRY_LOCATION_Y = "entryLocationY";
		private static final String ATTR_ENTRY_LOCATION_X = "entryLocationX";
		private static final String ATTR_INDEPENDENT_NATION_NAME = "independentNationName";
		private static final String ATTR_IMMIGRATION_REQUIRED = "immigrationRequired";
		private static final String ATTR_IMMIGRATION = "immigration";
		private static final String ATTR_INTERVENTION_BELLS = "interventionBells";
		private static final String ATTR_GOLD = "gold";
		private static final String ATTR_TAX = "tax";
		private static final String ATTR_NEW_LAND_NAME = "newLandName";
		private static final String ATTR_ATTACKED_BY_PRIVATEERS = "attackedByPrivateers";
		private static final String ATTR_DEAD = "dead";
		private static final String ATTR_NATION_TYPE = "nationType";
		private static final String ATTR_NATION_ID = "nationId";
		private static final String ATTR_USERNAME = "username";

		public Xml() {
			addNode(EventsNotifications.class, "eventsNotifications");
            addNode(Market.class, "market");
            addNode(HighSeas.class, "highSeas");
            addNode(Europe.class, "europe");
            addNode(Monarch.class, "monarch");
            addNode(FoundingFathers.class, "foundingFathers");
        }

        @Override
        public void startElement(XmlNodeAttributes attr) {
            Player player = new Player(attr.getId());
            player.dead = attr.getBooleanAttribute(ATTR_DEAD);
            player.ai = attr.getBooleanAttribute(ATTR_AI);
            player.attackedByPrivateers = attr.getBooleanAttribute(ATTR_ATTACKED_BY_PRIVATEERS, false);
            player.newLandName = attr.getStrAttribute(ATTR_NEW_LAND_NAME);
            player.tax = attr.getIntAttribute(ATTR_TAX, 0);
            player.gold = attr.getIntAttribute(ATTR_GOLD, 0);
            player.interventionBells = attr.getIntAttribute(ATTR_INTERVENTION_BELLS, 0);
            player.immigration = attr.getIntAttribute(ATTR_IMMIGRATION, 0);
            player.immigrationRequired = attr.getIntAttribute(ATTR_IMMIGRATION_REQUIRED, 0);
            
            player.name = attr.getStrAttribute(ATTR_USERNAME);

			player.nation = attr.getEntity(ATTR_NATION_ID, Specification.instance.nations);
            NationType nationType = attr.getEntity(ATTR_NATION_TYPE, Specification.instance.nationTypes);
            if (nationType != null) {
            	player.nationType = nationType;
            	player.updatableFeatures.addFeaturesAndOverwriteExisted(player.nationType);
            }
            player.independentNationName = attr.getStrAttribute(ATTR_INDEPENDENT_NATION_NAME);

            player.entryLocationX = attr.getIntAttribute(ATTR_ENTRY_LOCATION_X, 1);
            player.entryLocationY = attr.getIntAttribute(ATTR_ENTRY_LOCATION_Y, 1);
            
            player.changePlayerType(attr.getEnumAttribute(PlayerType.class, ATTR_PLAYER_TYPE));
            nodeObject = player;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
        	if (attr.isQNameEquals(ELEMENT_STANCE)) {
        		String playerId = attr.getStrAttribute(ATTR_PLAYER);
        		Stance stance = attr.getEnumAttribute(Stance.class, ATTR_VALUE);
        		nodeObject.stance.put(playerId, stance);
        	}
        	if (attr.isQNameEquals(ELEMENT_TENSION)) {
        		String playerId = attr.getStrAttribute(ATTR_PLAYER);
        		Tension tension = new Tension(attr.getIntAttribute(ATTR_VALUE));
        		nodeObject.tension.put(playerId, tension);
        	}
        	if (attr.isQNameEquals(ELEMENT_BAN_MISSION)) {
        	    if (nodeObject.banMission == null) {
        	        nodeObject.banMission = new ArrayList<String>();
        	    }
        	    nodeObject.banMission.add(attr.getStrAttributeNotNull(ATTR_PLAYER));
        	}
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
		    if (qName.equals(getTagName())) {
		    	nodeObject.afterReadPlayer();
		    }
        }
        
        @Override
        public void startWriteAttr(Player player, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(player);
        	
        	attr.set(ATTR_DEAD, player.dead);
        	attr.set(ATTR_AI, player.ai);
        	attr.set(ATTR_ATTACKED_BY_PRIVATEERS, player.attackedByPrivateers);
        	attr.set(ATTR_NEW_LAND_NAME, player.newLandName);
        	attr.set(ATTR_TAX, player.tax);
        	attr.set(ATTR_GOLD, player.gold);
        	attr.set(ATTR_INTERVENTION_BELLS, player.interventionBells);
        	attr.set(ATTR_IMMIGRATION, player.immigration);
        	attr.set(ATTR_IMMIGRATION_REQUIRED, player.immigrationRequired);

        	attr.set(ATTR_NATION_ID, player.nation);
        	attr.set(ATTR_NATION_TYPE, player.nationType);
        	attr.set(ATTR_USERNAME, player.name);
        	attr.set(ATTR_INDEPENDENT_NATION_NAME, player.independentNationName);
        	attr.set(ATTR_ENTRY_LOCATION_X, player.entryLocationX);
        	attr.set(ATTR_ENTRY_LOCATION_Y, player.entryLocationY);
        	attr.set(ATTR_PLAYER_TYPE, player.playerType);
        	
        	for (Entry<String, Stance> stance : player.stance.entrySet()) {
				attr.xml.element(ELEMENT_STANCE);
				attr.set(ATTR_PLAYER, stance.getKey());
				attr.set(ATTR_VALUE, stance.getValue());
				attr.xml.pop();
			}
        	for (Entry<String, Tension> tensionEntry : player.tension.entrySet()) {
				attr.xml.element(ELEMENT_TENSION);
				attr.set(ATTR_PLAYER, tensionEntry.getKey());
				attr.set(ATTR_VALUE, tensionEntry.getValue().getValue());
				attr.xml.pop();
			}
        	if (player.banMission != null) {
        	    for (String playerId : player.banMission) {
        	        attr.xml.element(ELEMENT_BAN_MISSION);
        	        attr.set(ATTR_PLAYER, playerId);
        	        attr.xml.pop();
        	    }
        	}
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "player";
        }
    }
}
