package promitech.colonization.screen.map.diplomacy;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ProductionConsumption;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.player.Market;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.RequiredGoods;
import promitech.colonization.Direction;
import promitech.colonization.orders.combat.OffencePower;

// In Freecol: EuropeanAIPlayer.acceptDiplomaticTrade
public class ScoreService {

	private OffencePower offencePower = new OffencePower();
	
	/**
	 * Method score Colony from player point of view
	 * @param colony
	 * @param player
	 */
	public int scoreColony(Game game, Colony colony, Player player) {
		Europe unitMarket = player.getEurope();
		Market market = player.market();
		if (unitMarket == null) {
			throw new IllegalStateException("no unit market for player " + player.getId());
		}
		
		int sum = 0;
		if (colony.getOwner().equalsId(player)) {
			for (ColonyTile colonyTile : colony.colonyTiles.entities()) {
				if (colonyTile.hasWorker()) {
					sum += scoreUnit(unitMarket, colonyTile.getWorker());
				}
				ProductionConsumption productionSummary = colony.productionSummary(colonyTile);
				for (Entry<String> goodsEntry : productionSummary.realProduction.entries()) {
					GoodsType goodsType = Specification.instance.goodsTypes.getById(goodsEntry.key);
					sum += market.getSalePrice(goodsType, goodsEntry.value);
				}
			}
			for (Building building : colony.buildings.entities()) {
				for (Unit unit : building.getUnits().entities()) {
					sum += scoreUnit(unitMarket, unit);
				}
				for (RequiredGoods requiredGoods : building.buildingType.requiredGoods.entities()) {
					if (requiredGoods.goodsType.isStorable()) {
						sum += market.buildingGoodsPrice(requiredGoods.goodsType, requiredGoods.amount);
					}
				} 
			}
			for (Entry<String> entry : colony.getGoodsContainer().entries()) {
				if (entry.value > 0) {
					GoodsType goodsType = Specification.instance.goodsTypes.getById(entry.key);
					sum += market.getSalePrice(goodsType, entry.value);
				}
			}
			
		} else {
			sum += colony.getColonyUnitsCount() * 1000;
			sum += 500; // Some useful goods?
			
			for (Direction direction : Direction.allDirections) {
				Tile tile = game.map.getTile(colony.tile, direction);
				if (tile == null) {
					continue;
				}
				if (colony.getId().equals(tile.getOwningSettlementId())) {
					sum += 200;
				}
			}
			sum *= colony.getStockadeLevel();
		}
		return sum;
	}

	int scoreUnit(Europe unitMarket, Unit unit) {
		return unitMarket.getUnitPrice(unit.unitType);
	}

	void scoreOffencePower(Player player, NationSummary nationSummary) {
		nationSummary.navyPower = 0;
		nationSummary.militaryPower = 0;
		
		for (Unit unit : player.units.entities()) {
			if (unit.isNaval()) {
				nationSummary.navyPower += offencePower.calculateUnitOffencePower(unit);
			} else {
				nationSummary.militaryPower += offencePower.calculateUnitOffencePower(unit);
			}
		}
	}
	
	public double strengthRatio(Player p1, Player p2) {
		NationSummary ns1 = new NationSummary();
		NationSummary ns2 = new NationSummary();
		
		scoreOffencePower(p1, ns1);
		scoreOffencePower(p2, ns2);
		
		return (double)ns1.militaryPower / ((double)ns1.militaryPower + ns2.militaryPower);
	}
}