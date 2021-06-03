package net.sf.freecol.common.model.ai.missions.workerrequest;

import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.utils.ObjectIntMap;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.ai.ObjectsListScore;
import promitech.colonization.ai.ObjectsListScore.ObjectScore;
import promitech.map.isometric.NeighbourIterableTile;

class CreateColonyReqScore {

	private final Map map;
	private final Player player;
	private final UnitType colonistUnitType;
	private final MapIdEntities<GoodsType> goodsType;
	
	CreateColonyReqScore(Map map, Player player, MapIdEntities<GoodsType> goodsType) {
		this.map = map;
		this.player = player;
		this.colonistUnitType = Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST);
		this.goodsType = goodsType;
	}

	public void score(ObjectsListScore<TileUnitType> tileScore, Tile[] tiles) {
		for (Tile tile : tiles) {
			if (tile == null) {
				continue;
			}
			score(tileScore, tile);
		}
	}

    public void score(ObjectsListScore<TileUnitType> tileScore, Tile tile) {
    	int unattendedScore = 0;
    	
    	Entry<GoodsType, Integer> unattendedProduction = tile.getType().productionInfo.singleFilteredUnattendedProduction(goodsType);
    	if (unattendedProduction != null) {
			unattendedScore = player.market().getSalePrice(
				unattendedProduction.getKey(), 
				unattendedProduction.getValue()
			);
			centerColonyTileScore(tileScore, unattendedProduction, tile);
    	}
    	theBestScoreFromNeighbourTiles(tileScore, unattendedScore, tile, goodsType);
    }
    
    private void centerColonyTileScore(ObjectsListScore<TileUnitType> tileScore, Entry<GoodsType, Integer> tileProduction, Tile tile) {
    	// tile unattended production
    	
    	Entry<GoodsType, Integer> buildingOutput = null;
    	
		for (BuildingType buildingType : Specification.instance.buildingTypes) {
			Production buildingInput = buildingType.productionForInput(tileProduction.getKey());
			if (buildingInput != null) {
				buildingOutput = buildingInput.singleOutput();
				break;
			}
		}
		if (buildingOutput != null) {
			{
				int score = scoreProductionForBuilding(colonistUnitType, buildingOutput, tileProduction);
				tileScore.add(new ObjectScore<TileUnitType>(new TileUnitType(tile, colonistUnitType), score));
			}
			
			UnitType specialist = Specification.instance.expertUnitTypeByGoodType.get(buildingOutput.getKey().getId());
			if (specialist != null) {
				int score = scoreProductionForBuilding(specialist, buildingOutput, tileProduction);
				tileScore.add(new ObjectScore<TileUnitType>(new TileUnitType(tile, specialist), score));
			}
		}
    }

    private int scoreProductionForBuilding(UnitType unitType, Entry<GoodsType, Integer> buildingOutput, Entry<GoodsType, Integer> tileProduction) {
		ObjectIntMap<GoodsType> ps = new ObjectIntMap<GoodsType>(2);
		
		int productionAmount = (int)unitType.applyModifier(
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
		
		int sum = 0;
		for (ObjectIntMap.Entry<GoodsType> entry : ps) {
			sum += player.market().getSalePrice(entry.key, entry.value);
		}
		return sum;
    }
    
    private void theBestScoreFromNeighbourTiles(ObjectsListScore<TileUnitType> tileScore, int colonyCenterTileScore, Tile tile, MapIdEntities<GoodsType> goodsType) {
    	int colonistTheBestScore = 0;
    	int expertTheBestScore = 0;
    	UnitType theBestExpertType = null;
    	
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
						if (score > colonistTheBestScore) {
							colonistTheBestScore = score;
							
							UnitType expert = Specification.instance.expertUnitTypeByGoodType.get(outputEntry.getKey().getId());
							if (expert != null) {
								amount = tileProductionAmount(neighbourTile.tile, expert, outputEntry);
								score = player.market().getSalePrice(
									outputEntry.getKey(), 
									amount
								);
								if (score > expertTheBestScore && score > colonistTheBestScore) {
									expertTheBestScore = score;
									theBestExpertType = expert;
								}
							}
						}
					}
				}
			}
		}
    	if (colonistTheBestScore > 0) {
    		tileScore.add(
				new ObjectScore<TileUnitType>(new TileUnitType(tile, colonistUnitType), colonistTheBestScore + colonyCenterTileScore)
			);
    	}
    	if (theBestExpertType != null) {
    		tileScore.add(
				new ObjectScore<TileUnitType>(new TileUnitType(tile, theBestExpertType), expertTheBestScore + colonyCenterTileScore)
			);
    	}
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
