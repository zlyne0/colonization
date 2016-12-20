package net.sf.freecol.common.model;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import promitech.colonization.ui.resources.Messages;

public abstract class Settlement extends ObjectWithId {
    
    public static final int FOOD_PER_COLONIST = 200;
    
	protected String name;
    public SettlementType settlementType;
    protected Player owner;
    public Tile tile;
    protected boolean coastland = false;
    
    public static IndianSettlement createIndianSettlement(Player player, Tile tile, SettlementType settlementType) {
    	String settlmentName = generateSettlmentName(player);
    	
    	TileImprovementType roadImprovement = Specification.instance.tileImprovementTypes.getById(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID);
    	TileImprovement tileImprovement = new TileImprovement(Game.idGenerator, roadImprovement);
    	tile.addImprovement(tileImprovement);
    	
		IndianSettlement indianSettlement = new IndianSettlement(Game.idGenerator);
		indianSettlement.settlementType = settlementType;
		indianSettlement.name = settlmentName;
		
		indianSettlement.tile = tile;
		tile.setSettlement(indianSettlement);
		
		player.addSettlement(indianSettlement);
		
		return indianSettlement;
    }
    
    public static Colony buildColony(Map map, Unit buildByUnit, Tile tile, String name) {
    	Colony colony = new Colony(Game.idGenerator);
    	colony.settlementType = buildByUnit.getOwner().nationType().getSettlementRegularType();
    	colony.name = name;
    	
    	tile.setSettlement(colony);
    	buildByUnit.getOwner().addSettlement(colony);
    	
    	colony.createColonyTiles(map, tile);
    	
    	colony.initDefaultBuildings();
    	colony.updateColonyFeatures();
    	
    	colony.initColonyBuilderUnit(buildByUnit);
    	tile.changeOwner(buildByUnit.getOwner(), colony);
    	return colony;
    }
    
    public static String generateSettlmentName(Player player) {
    	String key = "" + player.nation().getId() + ".settlementName." + player.settlements.size();
    	
    	if (!Messages.containsKey(key)) {
    		key = player.nation().getId() + ".settlementName.freecol." + player.settlements.size();
    	}
    	if (Messages.containsKey(key)) {
    		return Messages.msg(key);
    	}
		return Integer.toString(player.settlements.size());
    }
    
    public Settlement(String id) {
		super(id);
	}
    
    public abstract String getImageKey();

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
	    this.owner = owner;
	}
	
    public boolean isCoastland() {
    	return coastland;
    }
	
	public abstract boolean isColony();

	public Colony getColony() {
		return (Colony)this;
	}
	
    public boolean canBombardEnemyShip() {
        return hasAbility(Ability.BOMBARD_SHIPS);
    }

    public abstract boolean hasAbility(String abilityCode);
    
    public abstract int applyModifiers(String abilityCode, int val);
    
    public abstract void addGoods(String goodsTypeId, int quantity);

	public abstract boolean isContainsTile(Tile improvingTile);
	
	public abstract void initMaxPossibleProductionOnTile(Tile tile);

	public abstract ProductionSummary productionSummary();	
}
