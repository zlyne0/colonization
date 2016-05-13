package promitech.colonization;

import java.util.LinkedList;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.ResourceType;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileResource;
import net.sf.freecol.common.model.TileTypeTransformation;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.Goods;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.UnitTypeChange.ChangeType;

public class GameLogic {

	private final Game game;
	
	private boolean requireUpdateMapModel = false;
	
	public GameLogic(Game game) {
		this.game = game;
	}

	public void newTurn(Player player) {
		requireUpdateMapModel = false;
		
		for (Unit unit : player.units.entities()) {
			newTurnForUnit(unit);
		}
		
		for (Settlement settlement : player.settlements.entities()) {
			if (!settlement.isColony()) {
				continue;
			}
			Colony colony = (Colony)settlement;
			// TODO: refactoring
			System.out.println("###### colony " + colony);
			for (ColonyTile colonyTile : colony.colonyTiles.entities()) {
			    if (colonyTile.getWorker() != null && !colonyTile.getWorker().isExpert()) {
			        Unit worker = colonyTile.getWorker();
			        ProductionSummary realProduction = colony.productionSummaryForTerrain(colonyTile).realProduction;
		            
			        System.out.println("goods: " + realProduction);
			        System.out.println("tile " + colonyTile + " worker " + colonyTile.getWorker() + " expert " + worker.isExpert());
			        
                    for (Entry<String> entry : realProduction.entries()) {
                        UnitType goodsExpertUnitType = Specification.instance.expertUnitTypeByGoodType.get(entry.key);
                        if (goodsExpertUnitType != null && worker.unitType.canBeUpgraded(goodsExpertUnitType, ChangeType.EXPERIENCE)) {
                            System.out.println("experience from " + worker.unitType + " to " + goodsExpertUnitType);
                            
                            int experience = entry.value;
                            worker.gainExperience(experience);
                        }
                    }
			    }
			}
			for (Building building : colony.buildings.entities()) {
			    ProductionSummary realProduction = colony.productionSummary(building).realProduction;
		        for (Unit worker : building.workers.entities()) {
		            if (worker.isExpert()) {
		                continue;
		            }
		            
		            System.out.println("goods: " + realProduction);
		            System.out.println("building " + building + " worker " + worker + " expert " + worker.isExpert());
		            
		            for (Entry<String> entry : realProduction.entries()) {
		                UnitType goodsExpertUnitType = Specification.instance.expertUnitTypeByGoodType.get(entry.key);
		                if (goodsExpertUnitType != null && worker.unitType.canBeUpgraded(goodsExpertUnitType, ChangeType.EXPERIENCE)) {
                            System.out.println("experience from " + worker.unitType + " to " + goodsExpertUnitType);
                            
                            int experience = entry.value / building.workers.size();
                            worker.gainExperience(experience);
		                }
		            }
		        }
			}
			
			//colony.increaseWarehouseByProduction();
		}
		
		player.fogOfWar.resetFogOfWar(player);
	}
	
	public void newTurnForUnit(Unit unit) {
		unit.resetMovesLeftOnNewTurn();
		switch (unit.getState()) {
			case IMPROVING:
				workOnImprovement(unit);
				break;
			case FORTIFYING:
				unit.setState(UnitState.FORTIFIED);
				break;
			case SKIPPED:
				unit.setState(UnitState.ACTIVE);
				break;
			default:
				break;
		}
	}

	private void workOnImprovement(Unit unit) {
		if (unit.workOnImprovement()) {
			Tile improvingTile = unit.getTile();
			TileImprovementType improvementType = unit.getTileImprovementType();
			
			LinkedList<Settlement> neighbouringSettlements = game.map.findSettlements(improvingTile, unit.getOwner(), 2);
			
			TileTypeTransformation changedTileType = improvementType.changedTileType(improvingTile.getType());
			if (changedTileType != null) {
				improvingTile.changeTileType(changedTileType.getToType());
				
				Goods production = changedTileType.getProduction();
				int prodAmount = production.getAmount();
				if (unit.unitType.hasAbility(Ability.EXPERT_PIONEER)) {
					prodAmount *= 2;
				}
				if (!neighbouringSettlements.isEmpty()) {
					Settlement settlement = neighbouringSettlements.getFirst();
					prodAmount = settlement.applyModifiers(Modifier.TILE_TYPE_CHANGE_PRODUCTION, prodAmount);
					settlement.addGoods(production.getId(), prodAmount);
				}
			} else {
				TileImprovement tileImprovement = new TileImprovement(game.idGenerator, improvementType);
				improvingTile.addImprovement(tileImprovement);
				if (improvementType.isRoad()) {
					improvingTile.updateRoadConnections(game.map);
				}
			}
			
			for (Settlement settlement : neighbouringSettlements) {
				if (settlement.isContainsTile(improvingTile)) {
					settlement.initMaxPossibleProductionOnTile(improvingTile);
					break;
				}
			}
			
			for (Unit u : improvingTile.units.entities()) {
				if (u.getState() == UnitState.IMPROVING && u.getTileImprovementType().equalsId(improvementType)) {
					u.setState(UnitState.ACTIVE);
				}
			}
			
			// does improvement expose resource
			if (isExposedResourceAfterImprovement(improvingTile, improvementType)) {
				ResourceType resourceType = improvingTile.getType().exposeResource();
				int initQuantity = resourceType.initQuantity();
				improvingTile.addResource(new TileResource(game.idGenerator, resourceType, initQuantity));
			}
			
			requireUpdateMapModel = true;
			unit.setState(UnitState.ACTIVE);
		}
	}

	private boolean isExposedResourceAfterImprovement(Tile tile, TileImprovementType improvementType) {
		return !tile.hasTileResource() 
				&& Randomizer.getInstance().isHappen(improvementType) 
				&& tile.getType().allowedResourceTypes.isNotEmpty();
		
	}
	
	public boolean isRequireUpdateMapModel() {
		return requireUpdateMapModel;
	}
}
