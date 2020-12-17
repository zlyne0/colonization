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
	private final UnitType colonistUnitType;
	
	public ColonyWorkerRequestPlaner(Map map, Player player) {
		this.map = map;
		this.player = player;
		this.colonistUnitType = Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST);
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
	
    public ObjectScore<UnitType> scoreTile(Tile tile, MapIdEntities<GoodsType> goodsType) {
    	int unattendedScore = 0;
    	ObjectScore<UnitType> centerColonyTileProd = null;
    	
    	Entry<GoodsType, Integer> unattendedProduction = tile.getType().productionInfo.singleFilteredUnattendedProduction(goodsType);
    	if (unattendedProduction != null) {
			unattendedScore = player.market().getSalePrice(
				unattendedProduction.getKey(), 
				unattendedProduction.getValue()
			);
			
			centerColonyTileProd = centerColonyTileScore(unattendedProduction);
    	}
    	
    	ObjectScore<UnitType> neighbourTile = theBestScoreFromNeighbourTiles(unattendedScore, tile, goodsType);
    	
    	if (centerColonyTileProd == null) {
    		return neighbourTile;
    	}
    	return ObjectScore.max(centerColonyTileProd, neighbourTile);
    }
    
    private ObjectScore<UnitType> centerColonyTileScore(Entry<GoodsType, Integer> tileProduction) {
    	// tile unattended production
    	
    	UnitType ut = colonistUnitType;
    	ObjectIntMap<GoodsType> ps = new ObjectIntMap<GoodsType>(2);
    	
		for (BuildingType buildingType : Specification.instance.buildingTypes) {
			Production buildingInput = buildingType.productionForInput(tileProduction.getKey());
			if (buildingInput != null) {
				Entry<GoodsType, Integer> buildingOutput = buildingInput.singleOutput();
				// output for building
				if (buildingOutput != null) {
					int productionAmount = (int)colonistUnitType.applyModifier(
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
					ut = Specification.instance.expertUnitTypeForGoodsType(
						buildingOutput.getKey(), 
						colonistUnitType
					); 
				}
			}
		}
		
		int sum = 0;
		for (ObjectIntMap.Entry<GoodsType> entry : ps) {
			sum += player.market().getSalePrice(entry.key, entry.value);
		}
		return new ObjectScore<UnitType>(ut, sum);
    }
    
    private ObjectScore<UnitType> theBestScoreFromNeighbourTiles(int colonyCenterTileScore, Tile tile, MapIdEntities<GoodsType> goodsType) {
    	UnitType scoreUnitType = colonistUnitType;
    	int theBestScore = 0;
    	
    	for (NeighbourIterableTile<Tile> neighbourTile : map.neighbourLandTiles(tile)) {
			if (neighbourTile.tile.hasSettlement()) {
				continue;
			}
			List<Production> productions = neighbourTile.tile.getType().productionInfo.getAttendedProductions();
			for (Production production : productions) {
				for (java.util.Map.Entry<GoodsType, Integer> outputEntry : production.outputEntries()) {
					if (goodsType.containsId(outputEntry.getKey())) {
						int amount = tileProductionAmount(neighbourTile.tile, colonistUnitType, outputEntry);
						int score = player.market().getSalePrice(
							outputEntry.getKey(), 
							amount
						);
						if (score > theBestScore) {
							theBestScore = score;
							scoreUnitType = Specification.instance.expertUnitTypeForGoodsType(
								outputEntry.getKey(), 
								colonistUnitType
							); 
						}
					}
				}
			}
		}
    	return new ObjectScore<UnitType>(scoreUnitType, theBestScore + colonyCenterTileScore);
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
