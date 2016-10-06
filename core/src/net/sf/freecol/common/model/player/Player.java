package net.sf.freecol.common.model.player;

import java.util.HashMap;

import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
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
import net.sf.freecol.common.model.map.BooleanMap;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.FoundingFather;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.NationType;
import promitech.colonization.SpiralIterator;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

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
    private int tax;
    private int gold;
    private int liberty;
    private int interventionBells;
    private int entryLocationX;
    private int entryLocationY;
    private String newLandName;
    
    /**
     * The number of immigration points.  Immigration points are an
     * abstract game concept.  They are generated by but are not
     * identical to crosses.
     */
    protected int immigration;

    /**
     * The amount of immigration needed until the next unit decides
     * to migrate.
     */
    protected int immigrationRequired;
    private boolean attackedByPrivateers = false;
    
    private String independentNationName;
    public final MapIdEntities<Unit> units = new MapIdEntities<Unit>();
    public final MapIdEntities<Settlement> settlements = new MapIdEntities<Settlement>();
    public final MapIdEntities<FoundingFather> foundingFathers = new MapIdEntities<FoundingFather>();
    private HighSeas highSeas;
    protected Monarch monarch;
    private final ObjectWithFeatures updatableFeatures;
    
    public EventsNotifications eventsNotifications = new EventsNotifications();
    
    public final PlayerForOfWar fogOfWar = new PlayerForOfWar(); 
    private BooleanMap exploredTiles;
    
    private final java.util.Map<String, Stance> stance = new HashMap<String, Stance>();
    private final java.util.Map<String, Tension> tension = new HashMap<String, Tension>();

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
    		modifyStance(otherPlayer, newStance);
    		int modifier = old.getTensionModifier(newStance);
			modifyTension(otherPlayer, modifier);
    		
			if (old != Stance.UNCONTACTED) {
				String msgBody = Messages.message(StringTemplate.template("model.diplomacy." + newStance + ".declared")
					.addName("%nation%", nation())
				);
				MessageNotification msg = new MessageNotification(Game.idGenerator.nextId(MessageNotification.class), msgBody);
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
				MessageNotification msg = new MessageNotification(Game.idGenerator.nextId(MessageNotification.class), msgBody);
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
    
    private void modifyTension(Player p, int val) {
		if (val == 0) {
    		return;
    	}
    	Tension tensionObj = tension.get(p.getId());
    	if (tensionObj == null) {
    		tensionObj = new Tension(Tension.TENSION_MIN);
    	}
    	tensionObj.modify(val);
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
	public boolean setTileAsExplored(Tile tile, Map map) {
		if (exploredTiles == null) {
			exploredTiles = new BooleanMap(map, false);
		}
		return exploredTiles.set(tile.x, tile.y, true);
	}
	
	public boolean isTileUnExplored(Tile tile) {
		return !isTileExplored(tile.x, tile.y);
	}
	
	public boolean isTileExplored(int x, int y) {
		if (exploredTiles == null) {
			return false;
		}
		return exploredTiles.isSet(x, y);
	}
	
	/**
	 * @return return true when explore new tiles
	 */
	public boolean revealMapAfterUnitMove(Map map, Unit unit) {
		Tile unitTileLocation = unit.getTile();
		
		int radius = unit.lineOfSight();
		SpiralIterator spiralIterator = new SpiralIterator(map.width, map.height);
		spiralIterator.reset(unitTileLocation.x, unitTileLocation.y, true, radius);
		
		boolean unexploredTile = false;
		while (spiralIterator.hasNext()) {
			Tile tile = map.getTile(spiralIterator.getX(), spiralIterator.getY());
			spiralIterator.next();
			if (tile == null) {
				continue;
			}
			if (setTileAsExplored(tile, map)) {
				unexploredTile = true;
			}
			fogOfWar.removeFogOfWar(tile.x, tile.y);
		}
		return unexploredTile;
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
    
    public void addGold(int gold) {
    	this.gold += gold;
    }
    
    public void subtractGold(int gold) {
    	this.gold -= gold;
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
    
    public boolean isRebel() {
        return playerType == PlayerType.REBEL;
    }
    
	public boolean isNotLiveEuropeanPlayer() {
		return nation.isUnknownEnemy() || isDead() || !isEuropean(); 
	}
    
	public boolean isDead() {
		return dead;
	}
	
    public boolean isLiveEuropeanPlayer() {
        return !nation.isUnknownEnemy() && !isDead() && isEuropean();
    }
	
	public void endTurn() {
	}

	public void modifyLiberty(int libertyAmount) {
		if (!canHaveFoundingFathers()) {
			return;
		}
		this.liberty = Math.max(0, this.liberty + libertyAmount);
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
    
    public boolean canHaveFoundingFathers() {
        return nationType.hasAbility(Ability.ELECT_FOUNDING_FATHER);
    }
	
	public void removeSettlement(Settlement settlement) {
		settlement.tile.setSettlement(null);
		settlements.removeId(settlement.getId());
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
    
	public void addFoundingFathers(FoundingFather father) {
		foundingFathers.add(father);
		updatableFeatures.addFeatures(father);
	}

	public int getEntryLocationX() {
		return entryLocationX;
	}

	public int getEntryLocationY() {
		return entryLocationY;
	}

	public String getNewLandName() {
		return newLandName;
	}
	
    public boolean isAttackedByPrivateers() {
        return attackedByPrivateers;
    }

    public Monarch getMonarch() {
        return monarch;
    }

	public BooleanMap getExploredTiles() {
		return exploredTiles;
	}
    
	public static class Xml extends XmlNodeParser {
        public Xml() {
            addNode(Europe.class, new ObjectFromNodeSetter<Player, Europe>() {
                @Override
                public void set(Player target, Europe entity) {
                    target.europe = entity;
                    entity.setOwner(target);
                }
            });
            addNode(Monarch.class, new ObjectFromNodeSetter<Player, Monarch>() {
                @Override
                public void set(Player target, Monarch entity) {
                    target.monarch = entity;
                    entity.setPlayer(target);
                }
            });
            addNode(Market.class, "market");
            addNode(EventsNotifications.class, "eventsNotifications");
            addNode(HighSeas.class, "highSeas");
        }

        @Override
        public void startElement(XmlNodeAttributes attr) {
            String idStr = attr.getStrAttribute("id");
            String nationIdStr = attr.getStrAttribute("nationId");
            String nationTypeStr = attr.getStrAttribute("nationType");
            
            Player player = new Player(idStr);
            player.dead = attr.getBooleanAttribute("dead");
            player.attackedByPrivateers = attr.getBooleanAttribute("attackedByPrivateers", false);
            player.newLandName = attr.getStrAttribute("newLandName");
            player.tax = attr.getIntAttribute("tax", 0);
            player.gold = attr.getIntAttribute("gold", 0);
            player.liberty = attr.getIntAttribute("liberty", 0);
            player.interventionBells = attr.getIntAttribute("interventionBells", 0);
            player.immigration = attr.getIntAttribute("immigration", 0);
            player.immigrationRequired = attr.getIntAttribute("immigrationRequired", 0);
            player.nation = Specification.instance.nations.getById(nationIdStr);
            if (nationTypeStr != null) {
                player.nationType = Specification.instance.nationTypes.getById(nationTypeStr);
                player.updatableFeatures.addFeaturesAndOverwriteExisted(player.nationType);
            }
            player.independentNationName = attr.getStrAttribute("independentNationName");

            player.entryLocationX = attr.getIntAttribute("entryLocationX", 1);
            player.entryLocationY = attr.getIntAttribute("entryLocationY", 1);
            
            player.changePlayerType(attr.getEnumAttribute(PlayerType.class, "playerType"));
            nodeObject = player;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
        	if (attr.isQNameEquals("stance")) {
        		String playerId = attr.getStrAttribute("player");
        		Stance stance = Stance.valueOf(attr.getStrAttribute("value").toUpperCase());
        		((Player)nodeObject).stance.put(playerId, stance);
        	}
        	if (attr.isQNameEquals("tension")) {
        		String playerId = attr.getStrAttribute("player");
        		Tension tension = new Tension(attr.getIntAttribute("value"));
        		((Player)nodeObject).tension.put(playerId, tension);
        	}
        	if (attr.isQNameEquals("foundingFathers")) {
        	    int count = attr.getIntAttribute("xLength", 0);
        	    for (int i=0; i<count; i++) {
        	        String fatherId = attr.getStrAttributeNotNull("x" + i);
        	        FoundingFather father = Specification.instance.foundingFathers.getById(fatherId);
        	        ((Player)nodeObject).addFoundingFathers(father);
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
