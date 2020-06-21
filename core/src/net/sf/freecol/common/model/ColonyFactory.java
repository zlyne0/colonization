package net.sf.freecol.common.model;

import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import promitech.colonization.ai.ColonyProductionPlaner;

public class ColonyFactory {

	private final Game game;
	private final PathFinder pathFinder;
	
	public ColonyFactory(Game game, PathFinder pathFinder) {
		this.game = game;
		this.pathFinder = pathFinder;
	}

	public Colony buildColonyByAI(Unit buildByUnit, Tile tile) {
        String colonyName = SettlementFactory.generateSettlmentName(buildByUnit.getOwner());
        Colony colony = buildColony(buildByUnit, tile, colonyName);
        ColonyProductionPlaner productionPlaner = new ColonyProductionPlaner();
        productionPlaner.createPlan(colony);
        return colony;
	}
	
	public Colony buildColony(Unit buildByUnit, Tile tile, String colonyName) {
		Colony colony = createColonyObject(game.map, buildByUnit, tile, colonyName);
		determineEuropeSeaConnection(colony);
		seeColonyByOtherPlayers(colony);
		return colony;
	}

	private void seeColonyByOtherPlayers(Colony colony) {
		for (Player player : game.players.entities()) {
			if (player.notEqualsId(colony.getOwner()) 
				&& player.isLiveEuropeanPlayer() 
				&& player.getFeatures().hasAbility(Ability.SEE_ALL_COLONIES)
			) {
				player.revealMapSeeColony(game.map, colony);
			}
		}
	}

    protected Colony createColonyObject(Map map, Unit buildByUnit, Tile tile, String name) {
    	Colony colony = new Colony(
			Game.idGenerator,
			buildByUnit.getOwner().nationType().getSettlementRegularType()
		);
    	colony.name = name;
    	
    	tile.setSettlement(colony);
    	tile.changeOwner(buildByUnit.getOwner(), colony);
    	buildByUnit.getOwner().addSettlement(colony);
    	
    	colony.createColonyTiles(map, tile);
    	
    	colony.initDefaultBuildings();
    	colony.updateColonyFeatures();
    	
    	colony.initColonyBuilderUnit(buildByUnit);
    	
    	TileImprovementType roadImprovement = Specification.instance.tileImprovementTypes.getById(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID);
    	TileImprovement tileImprovement = new TileImprovement(Game.idGenerator, roadImprovement);
    	tile.addImprovement(tileImprovement);
    	
    	return colony;
    }
	
	private void determineEuropeSeaConnection(Colony colony) {
		// tmp unit only for find path to europe
    	Unit tmpGalleon = new Unit(
			"tmp:buildColony:findToEurope:-1",
			Specification.instance.unitTypes.getById(UnitType.GALLEON),
			Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID),
			colony.owner
		);
    	tmpGalleon.changeUnitLocation(colony.tile);
		colony.getOwner().units.add(tmpGalleon);
    	tmpGalleon.setOwner(colony.getOwner());
    	Path path = pathFinder.findToEurope(game.map, colony.tile, tmpGalleon, false);

    	boolean foundPath = false;
    	if (!path.tiles.isEmpty()) {
    		Tile lastPathStep = path.tiles.peek();
    		if (lastPathStep.getType().isHighSea()) {
    			foundPath = true;
    		}
    	}
    	tmpGalleon.changeUnitLocation(colony.tile);
    	colony.getOwner().removeUnit(tmpGalleon);
    	
    	colony.seaConnectionToEurope = foundPath;
	}
}
