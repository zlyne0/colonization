package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.IndianNationType;
import net.sf.freecol.common.model.specification.RandomChoice;
import promitech.colonization.Randomizer;
import promitech.colonization.ui.resources.Messages;
import promitech.map.isometric.NeighbourIterableTile;

public class SettlementFactory {

	private final Map map;
	
	public SettlementFactory(Map map) {
		this.map = map;
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
	
	public IndianSettlement create(Player player, Tile tile, SettlementType settlementType) {
    	String settlmentName = generateSettlmentName(player);
    	
    	TileImprovementType roadImprovement = Specification.instance.tileImprovementTypes.getById(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID);
    	TileImprovement tileImprovement = new TileImprovement(Game.idGenerator, roadImprovement);
    	tile.addImprovement(tileImprovement);
    	
		IndianSettlement indianSettlement = new IndianSettlement(Game.idGenerator, settlementType);
		indianSettlement.name = settlmentName;
		indianSettlement.tile = tile;
		indianSettlement.learnableSkill = generateLearnableSkill(map, tile, (IndianNationType)player.nationType());
		tile.setSettlement(indianSettlement);
		
		player.addSettlement(indianSettlement);
		
		generateIndianUnits(player, indianSettlement);
		changeTileOwner(indianSettlement);
		createInitialGoods(indianSettlement);
		return indianSettlement;
    }

	private void generateIndianUnits(Player player, IndianSettlement indianSettlement) {
		int settlementUnitsNumber = Randomizer.instance().randomInt(
			indianSettlement.settlementType.getMinimumSize(), 
			indianSettlement.settlementType.getMaximumSize()
		);
		final UnitType brave = Specification.instance.unitTypes.getById("model.unit.brave");
		for (int i=0; i<settlementUnitsNumber; i++) {
			Unit unit = new Unit(Game.idGenerator.nextId(Unit.class), brave, brave.getDefaultRole(), player);
			player.units.add(unit);
			unit.setIndianSettlement(indianSettlement);
			unit.changeUnitLocation(indianSettlement);
		}
	}

	private UnitType generateLearnableSkill(Map map, Tile tile, IndianNationType nationType) {
		ObjectIntMap<String> scale = new ObjectIntMap<String>(nationType.getSkills().size());
		for (RandomChoice<UnitType> randomChoice : nationType.getSkills()) {
			if (randomChoice.probabilityObject().getExpertProductionForGoodsId() != null) {
				scale.put(randomChoice.probabilityObject().getExpertProductionForGoodsId(), 1);
			}
		}
		
		for (NeighbourIterableTile<Tile> it : map.neighbourTiles(tile)) {
			ProductionInfo productionInfo = it.tile.getType().productionInfo;
			for (Entry<String> scaleEntry : scale.entries()) {
				int goodsAmount = productionInfo.unattendedProductions(scaleEntry.key);
				scale.put(scaleEntry.key, scaleEntry.value + goodsAmount);
			}
		}
		
		List<RandomChoice<UnitType>> skills = new ArrayList<RandomChoice<UnitType>>(nationType.getSkills().size());
		for (RandomChoice<UnitType> randomChoice : nationType.getSkills()) {
			int scaleVal = 1;
			if (randomChoice.probabilityObject().getExpertProductionForGoodsId() != null) {
				scaleVal = scale.get(randomChoice.probabilityObject().getExpertProductionForGoodsId(), 1);
			}
			skills.add(new RandomChoice<UnitType>(
				randomChoice.probabilityObject(), 
				randomChoice.getOccureProbability() * scaleVal
			));
		}
		if (skills.isEmpty()) {
			return Specification.instance.unitTypes.getById(UnitType.SCOUT);
		}
		return Randomizer.instance().randomOne(skills).probabilityObject(); 
	}
	
	private void changeTileOwner(IndianSettlement settlement) {
		settlement.tile.changeOwner(settlement.getOwner(), settlement);
		
		for (Tile tile : map.neighbourTiles(settlement.tile, settlement.settlementType.getClaimableRadius()+1)) {
		    if (!tile.hasOwnerOrOwningSettlement() && !tile.getType().isWater()) {
		        tile.changeOwner(settlement.getOwner(), settlement);
		    }
        }
	}
	
    /**
     * Add some initial goods to a newly generated settlement.
     * After all, they have been here for some time.
	 */
	public void createInitialGoods(IndianSettlement settlement) {
		IndianSettlementProduction isp = new IndianSettlementProduction();
		isp.createInitialGoods(map, settlement);
	}
}
