package net.sf.freecol.common.model;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import promitech.colonization.Randomizer;
import promitech.colonization.ui.resources.Messages;

public abstract class Settlement extends ObjectWithId implements UnitLocation {
    
    public static final int FOOD_PER_COLONIST = 200;
    
	protected String name;
    public final SettlementType settlementType;
    protected Player owner;
    public Tile tile;
    protected boolean coastland = false;
    
    public static IndianSettlement createIndianSettlement(Player player, Tile tile, SettlementType settlementType) {
    	String settlmentName = generateSettlmentName(player);
    	
    	TileImprovementType roadImprovement = Specification.instance.tileImprovementTypes.getById(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID);
    	TileImprovement tileImprovement = new TileImprovement(Game.idGenerator, roadImprovement);
    	tile.addImprovement(tileImprovement);
    	
		IndianSettlement indianSettlement = new IndianSettlement(Game.idGenerator, settlementType);
		indianSettlement.name = settlmentName;
		indianSettlement.tile = tile;
		tile.setSettlement(indianSettlement);
		
		player.addSettlement(indianSettlement);
		
		generateIndianUnits(player, indianSettlement);
		return indianSettlement;
    }

	private static void generateIndianUnits(Player player, IndianSettlement indianSettlement) {
		int settlementUnitsNumber = Randomizer.instance().randomInt(
			indianSettlement.settlementType.getMinimumSize(), 
			indianSettlement.settlementType.getMaximumSize()
		);
		final UnitType brave = Specification.instance.unitTypes.getById("model.unit.brave");
		for (int i=0; i<settlementUnitsNumber; i++) {
			Unit unit = new Unit(Game.idGenerator.nextId(Unit.class), brave, brave.getDefaultRole(), player);
			unit.setIndianSettlement(indianSettlement);
			unit.changeUnitLocation(indianSettlement);
		}
	}
    
    public static Colony buildColony(Map map, Unit buildByUnit, Tile tile, String name) {
    	Colony colony = new Colony(
			Game.idGenerator,
			buildByUnit.getOwner().nationType().getSettlementRegularType()
		);
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
    
    public Settlement(String id, SettlementType settlementType) {
		super(id);
		this.settlementType = settlementType;
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
	
	public void changeOwner(Player newOwner) {
		if (this.owner != null) {
			this.owner.settlements.removeId(this);
		}
		newOwner.addSettlement(this);
		
		for (Unit unit : getUnits().entities()) {
			unit.captureByPlayer(newOwner);
		}
		for (Unit unit : tile.getUnits().entities()) {
			unit.captureByPlayer(newOwner);
		}
	}
	
    public boolean isCoastland() {
    	return coastland;
    }
	
	public abstract boolean isColony();

	public Colony getColony() {
		return (Colony)this;
	}
	
    public boolean canBombardEnemyShip() {
        return isCoastland() && hasAbility(Ability.BOMBARD_SHIPS);
    }

    public boolean hasGoodsToEquipRole(UnitRole unitRole) {
    	return UnitRoleLogic.hasContainerRequiredGoods(getGoodsContainer(), unitRole);
    }
    
    public abstract GoodsContainer getGoodsContainer();
    
    public abstract boolean hasAbility(String abilityCode);
    
    public abstract int applyModifiers(String abilityCode, int val);
    
    public abstract void addGoods(String goodsTypeId, int quantity);

	public abstract boolean isContainsTile(Tile improvingTile);
	
	public abstract void initMaxPossibleProductionOnTile(Tile tile);

	public abstract ProductionSummary productionSummary();

	public abstract void addModifiersTo(ObjectWithFeatures mods, String modifierCode);
}
