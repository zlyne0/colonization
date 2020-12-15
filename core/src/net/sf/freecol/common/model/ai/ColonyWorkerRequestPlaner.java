package net.sf.freecol.common.model.ai;

import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.utils.ObjectIntMap;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.ai.ObjectsListScore;
import promitech.colonization.ai.ObjectsListScore.ObjectScore;
import promitech.map.isometric.NeighbourIterableTile;

public class ColonyWorkerRequestPlaner {

	private final Map map;
	private final Player player;
	
	
	public ColonyWorkerRequestPlaner(Map map, Player player) {
		this.map = map;
		this.player = player;
	}
	
	public void plan() {
		List<GoodsType> goodsTypeToScoreByPrice = Specification.instance.goodsTypeToScoreByPrice;
		
		ObjectsListScore<ColonyWorkerReq> colonyWorkerReqScores = new ObjectsListScore<ColonyWorkerReq>(player.settlements.size());
		for (Settlement settlement : player.settlements) {
			ColonyWorkerReq colonyWorkerReq = new ColonyWorkerReq(settlement.asColony(), goodsTypeToScoreByPrice);
			ObjectsListScore<UnitType> score = colonyWorkerReq.simulate();
			colonyWorkerReqScores.add(colonyWorkerReq, score.scoreSum());
		}
		colonyWorkerReqScores.sortDescending();
		
		for (ObjectScore<ColonyWorkerReq> colonyWorkerReq : colonyWorkerReqScores) {
			System.out.println(colonyWorkerReq.getScore() + " " + colonyWorkerReq.getObj().getColony().getName());
			ObjectsListScore<UnitType> workersReq = colonyWorkerReq.getObj().simulate();
			for (ObjectScore<UnitType> objectScore : workersReq) {
				System.out.println("  " + objectScore.getScore() + " " + objectScore.getObj());
			}
		}
	}
	
	private Colony colonyWorkerDestination(Unit unit, ObjectsListScore<ColonyWorkerReq> colonyWorkerReqScores) {
		Colony colony = null;
		for (ObjectScore<ColonyWorkerReq> objectScore : colonyWorkerReqScores) {
			ColonyWorkerReq colonyWorkerReqScore = objectScore.getObj();
			colony = colonyWorkerReqScore.getColony();
			if (!hasDeliverWorkerMission(colonyWorkerReqScore)) {
				break;
			}
		}
		return colony;
	}

	private boolean hasDeliverWorkerMission(ColonyWorkerReq colonyWorkerReqScore) {
		colonyWorkerReqScore.simulate().size();
		// TODO: check deliver worker to colony for work, 
		// it is possible to have mission but no more then colonyWorkerReqScore.simulate().size()  
		return false;
	}
	
    public ObjectScore<Tile> scoreTile(UnitType workerType, Tile tile, MapIdEntities<GoodsType> goodsType) {
    	int theBestScore = -1;
    	int unattendedScore = 0;
    	
    	Entry<GoodsType, Integer> unattendedProduction = tile.getType().productionInfo.singleFilteredUnattendedProduction(goodsType);
    	if (unattendedProduction != null) {
			unattendedScore = player.market().getSalePrice(
				unattendedProduction.getKey(), 
				unattendedProduction.getValue()
			);
			
			theBestScore = centerColonyTileScore(unattendedProduction, workerType);
    	}
    	
    	int theBestScoreFromNeighbourTiles = theBestScoreFromNeighbourTiles(tile, workerType, goodsType);
    	return new ObjectScore<Tile>(tile, 
			Math.max(theBestScore, theBestScoreFromNeighbourTiles + unattendedScore)
		);
    }
    
    private int centerColonyTileScore(Entry<GoodsType, Integer> tileProduction, UnitType workerType) {
    	// tile unattended production
    	
    	ObjectIntMap<GoodsType> ps = new ObjectIntMap<GoodsType>(2);
    	
		for (BuildingType buildingType : Specification.instance.buildingTypes) {
			Production buildingInput = buildingType.productionForInput(tileProduction.getKey());
			if (buildingInput != null) {
				Entry<GoodsType, Integer> buildingOutput = buildingInput.singleOutput();
				// output for building
				if (buildingOutput != null) {
					int productionAmount = (int)workerType.applyModifier(
						buildingOutput.getKey().getId(),
						buildingOutput.getValue()
					);
					if (productionAmount >= tileProduction.getValue()) {
						ps.put(buildingOutput.getKey(), tileProduction.getValue());
					} else {
						int tileAddProd = tileProduction.getValue() - productionAmount;
						ps.put(buildingOutput.getKey(), productionAmount);
						ps.put(tileProduction.getKey(), tileAddProd);
					}
				}
			}
		}
		
		int sum = 0;
		for (ObjectIntMap.Entry<GoodsType> entry : ps) {
			sum += player.market().getSalePrice(entry.key, entry.value);
		}
		return sum;
    }
    
    private int theBestScoreFromNeighbourTiles(Tile tile, UnitType workerType, MapIdEntities<GoodsType> goodsType) {
    	int theBestScore = 0;
    	for (NeighbourIterableTile<Tile> neighbourTile : map.neighbourLandTiles(tile)) {
			if (neighbourTile.tile.hasSettlement()) {
				continue;
			}
			List<Production> productions = neighbourTile.tile.getType().productionInfo.getAttendedProductions();
			for (Production production : productions) {
				for (java.util.Map.Entry<GoodsType, Integer> outputEntry : production.outputEntries()) {
					if (goodsType.containsId(outputEntry.getKey())) {
						
						int amount = tileProductionAmount(neighbourTile.tile, workerType, outputEntry);
						int score = player.market().getSalePrice(
							outputEntry.getKey(), 
							amount
						);
						if (score > theBestScore) {
							theBestScore = score;
						}
						
					}
				}
			}
		}
    	return theBestScore;
    }
    
    private int tileProductionAmount(Tile tile, UnitType workerType, java.util.Map.Entry<GoodsType, Integer> goodsTypeProdAmount) {
    	Integer goodInitValue = goodsTypeProdAmount.getValue();
    	GoodsType prodGoodsType = goodsTypeProdAmount.getKey();
    	
    	int goodsQuantity = (int)workerType.applyModifier(prodGoodsType.getId(), goodInitValue);
    	goodsQuantity = (int)player.getFeatures().applyModifier(prodGoodsType.getId(), goodsQuantity);
    	goodsQuantity = tile.applyTileProductionModifier(prodGoodsType.getId(), goodsQuantity);
    	return goodsQuantity;
    }
	
	
}
