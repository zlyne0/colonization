package net.sf.freecol.common.model.ai.missions.workerrequest;

import java.util.Map.Entry;
import java.util.Set;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.MapTileDebugInfo;
import net.sf.freecol.common.model.ai.missions.workerrequest.TilePlayer.NoClaimReason;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Stance;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;
import promitech.colonization.Direction;
import promitech.colonization.SpiralIterator;
import promitech.map.Int2dArray;

class TilePlayer {
    public static enum NoClaimReason {
        NONE,            // Actually, tile can be claimed
        TERRAIN,         // Not on settleable terrain
        RUMOUR,          // Europeans can not claim tiles with LCR
        WATER,           // Natives do not claim water
        OCCUPIED,        // Hostile unit present.
        SETTLEMENT,      // Settlement present
        WORKED,          // One of our settlements is working this tile
        EUROPEANS,       // Owned by Europeans and not for sale
        NATIVES,         // Owned by natives and they want payment for it
    };
	
    private final Map map;
    private final Player player;
    private final boolean hasZeroSettlements;
    private final boolean landPaymentModifier;
    
    TilePlayer(Map map, Player player) {
    	this.map = map;
    	this.player = player;
    	this.hasZeroSettlements = player.settlements.isEmpty();
    	this.landPaymentModifier = player.getFeatures().hasModifier(Modifier.LAND_PAYMENT_MODIFIER);
    }
    
    NoClaimReason terrainClaim(Tile tile) {
    	if (!tile.getType().canSettle()) {
    		return NoClaimReason.TERRAIN;
    	}
    	if (tile.getType().isWater()) {
    		return NoClaimReason.WATER;
    	}
    	if (map.isPolar(tile)) {
    		return NoClaimReason.TERRAIN;
    	}
    	if (map.isOnMapEdge(tile)) {
    		return NoClaimReason.TERRAIN;
    	}
    	if (tile.hasSettlement()) {
    		return NoClaimReason.SETTLEMENT;
    	}
    	if (map.hasColonyInRange(tile, 1)) {
    		return NoClaimReason.SETTLEMENT;
    	}
    	return NoClaimReason.NONE;
    }
    
    NoClaimReason canOwnTileToFoundSettlement(Tile tile) {
    	if (tile.getOwner() == null) {
    		return NoClaimReason.NONE;
    	}
    	if (tile.getOwner().isEuropean()) {
    		return NoClaimReason.EUROPEANS;
    	} 
    	if (landPaymentModifier) {
    		return NoClaimReason.NONE;
    	}
    	// tile is owned by natives
		if (canClaimFreeCenterTile(tile)) {
			return NoClaimReason.NONE;
		}
    	return NoClaimReason.NATIVES;
    }
    
    private boolean canClaimFreeCenterTile(Tile tile) {
    	String buildOption = Specification.options.getStringValue(GameOptions.BUILD_ON_NATIVE_LAND);
    	return player.isEuropean() && tile.getOwner() != null && tile.getOwner().isIndian() &&
			(
    				GameOptions.BUILD_ON_NATIVE_LAND_ALWAYS.equals(buildOption) 
				|| (GameOptions.BUILD_ON_NATIVE_LAND_FIRST.equals(buildOption) && hasZeroSettlements)
				|| (GameOptions.BUILD_ON_NATIVE_LAND_FIRST_AND_UNCONTACTED.equals(buildOption) &&
						hasZeroSettlements && player.getStance(tile.getOwner()) == Stance.UNCONTACTED) 
			);
    }
    
    boolean hasEnemyUnits(Tile tile) {
    	if (tile.getUnits().isEmpty()) {
    		return false;
    	}
    	Unit tileUnit = tile.getUnits().first();
    	return tileUnit.getOwner().notEqualsId(player);
    }
    
}

public class BuildColony {

    public static enum TileSelection {
        WITHOUT_UNEXPLORED,
        ONLY_SEASIDE
    }
    
//    public static enum ColonyValueCategory {
//        A_OVERRIDE, // override slot containing showstopper NoValueType values
//        A_PROD,     // general production level
//        A_TILE,     // strangeness with the tile
//        A_EUROPE,   // proximity to Europe
//        A_RESOURCE, // penalize building on top of a resource
//        A_ADJACENT, // penalize adjacent units and settlement-owned-tiles
//        A_FOOD,     // penalize food shortage
//        A_LEVEL,    // reward high production potential
//        A_NEARBY,   // penalize nearby units and settlements
//        A_GOODS;    // check sufficient critical goods available (e.g. lumber)
//        // A_GOODS must be last, the spec is entitled to require checks on
//        // as many goods types as it likes
//
//    }
	
    /** Special return values for showstopper getColonyValue fail. */
	public static enum NoValueType {
		BOGUS(-1), TERRAIN(-2), RUMOUR(-3), SETTLED(-4), FOOD(-5), INLAND(-6), POLAR(-7);

		private final double value;

		private NoValueType(int value) {
			this.value = value;
		}

		public double getDouble() {
			return value;
		}
	}    
    
	public class TileBCWeight {
		public double resource;
		public double override;
		public double stealLand;
		
		public double nearby;
		public double europe;
		public double prod;
		public double adjacent;
		public double level;
		public double food;
		public double goods;

		public int foodProduction = 0;
		public boolean unitOccupied;
		public int weight = 0;
		public NoClaimReason terrainClaim;

		public void reset() {
			resource = 1;
			override = 1;
			stealLand = 1;
			nearby = 1;
			europe = 1;
			prod = 1;
			adjacent = 1;
			level = 1;
			food = 1;
			goods = 1;
			
			foodProduction = 0;
			unitOccupied = false;
			weight = 0;
			terrainClaim = null;
		}
		
		public void calculate() {
			weight = (int)(values.weight() * 100);			
		}
		
		private double weight() {
			return (
				resource *
				override *
				stealLand *
				nearby *
				europe *
				prod *
				adjacent *
				level *
				food *
				goods
			);
		}

		public boolean isWithoutUnits() {
			return !unitOccupied;
		}
	}
	
    // Want a few settlements before taking risks
    final int LOW_SETTLEMENT_NUMBER = 3;

    // Would like a caravel to reach high seas in 3 moves
    final int LONG_PATH_TILES = 12;
    // wagon train move range
    final int LONG_INLAND_PATH = 6; 
    
    // Applied once
    final double MOD_HAS_RESOURCE           = 0.75;
    final double MOD_FOOD_LOW               = 0.75;
    final double MOD_INITIAL_FOOD           = 2.0;
    final double MOD_STEAL                  = 0.5;
    final double MOD_STEAL_NEXT_SETTLEMENT  = 0.25;
    final double MOD_INLAND                 = 0.5;

    // Applied per surrounding tile
    final double MOD_OWNED_EUROPEAN         = 0.67;
    final double MOD_OWNED_NATIVE           = 0.8;

    // Applied per goods production, per surrounding tile
    final double MOD_HIGH_PRODUCTION        = 1.2;
    final double MOD_GOOD_PRODUCTION        = 1.1;

    // Applied per occurrence (own colony only one-time), range-dependent.
    final int DISTANCE_MAX = 5;
    final double[] MOD_OWN_COLONY     = {0.0, 0.0, 0.5, 1.50, 1.25};
    final double[] MOD_ENEMY_COLONY   = {0.0, 0.0, 0.4, 0.50, 0.70};
    final double[] MOD_NEUTRAL_COLONY = {0.0, 0.0, 0.7, 0.80, 1.00};
    final double[] MOD_ENEMY_UNIT     = {0.4, 0.5, 0.6, 0.75, 0.90};

    // Goods production in excess of this on a tile counts as good/high
    final int GOOD_PRODUCTION = 4;
    final int HIGH_PRODUCTION = 8;

    // Counting "high" production as 2, "good" production as 1
    // overall food production is considered low/very low if less than...
    final int FOOD_LOW = 4;
    final int FOOD_VERY_LOW = 1;

	private final Map map;
	private final Int2dArray tileWeights;
	private final TileBCWeight values = new TileBCWeight();
	
	private final HighSeaDistanceGenerator highSeaDistance;
	private final ColonyLandDistance colonyLandDistance;
	
	private Player player;
	private TilePlayer tilePlayer;
	private int settlementCount;
	private double development;
	private GoodsType foodGoodsType;
	
	private final ProductionSummary potentialColonyProduction = new ProductionSummary();
	private final ProductionSummary potentialGoodProduction = new ProductionSummary();
	private final ProductionSummary potentialHighProduction = new ProductionSummary();
	private final SpiralIterator spiralIterator;
	

	public BuildColony(Map map) {
		this.map = map;
		
		tileWeights = new Int2dArray(map.width, map.height);
		highSeaDistance = new HighSeaDistanceGenerator(map);
		colonyLandDistance = new ColonyLandDistance(map);
		spiralIterator = new SpiralIterator(map.width, map.height);
		
		foodGoodsType = Specification.instance.goodsTypes.getById(GoodsType.FOOD);
		
		highSeaDistance.generate();
	}
	
	private void init(Player player) {
		this.player = player;
		
		colonyLandDistance.generate(player);
		tilePlayer = new TilePlayer(map, player);
		settlementCount = player.settlements.size();
		
		// Penalize certain problems more in the initial colonies.
		development = Math.min(LOW_SETTLEMENT_NUMBER, settlementCount) / (double)LOW_SETTLEMENT_NUMBER;
	}
	
	public void generateWeights(Player player, Set<TileSelection> tileFilter) {
		init(player);
		
        boolean withoutUnexplored = tileFilter.contains(TileSelection.WITHOUT_UNEXPLORED);
        boolean onlySeaside = tileFilter.contains(TileSelection.ONLY_SEASIDE);
        int x, y;
        for (y=0; y<map.height; y++) {
            for (x=0; x<map.width; x++) {
                Tile tile = map.getSafeTile(x, y);
                if (tile.getType().isWater() || tile.hasLostCityRumour()) {
                	continue;
                }
                if (withoutUnexplored && player.isTileUnExplored(tile)) {
                    continue;
                }
                if (onlySeaside && !tile.isOnSeaSide()) {
                    continue;
                }
                tileWeightCalculation(tile);
                values.calculate();
                tileWeights.set(x, y, values.weight);
            }
        }
	}

	public TileBCWeight tileWeight(Tile tile) {
		tileWeightCalculation(tile);
		values.calculate();
		return values;
	}
	
	private void tileWeightCalculation(Tile tile) {
		values.reset();
		
		if (tileClaims(tile)) {
			return;
		}
		
		potentialColonyProduction.makeEmpty();
		potentialGoodProduction.makeEmpty();
		potentialHighProduction.makeEmpty();
		
		if (centerTileProductionWeight(tile)) {
			return;
		}
		
        if (highSeasAndInlandWeight(tile)) {
        	return;
        }
        
        // Penalty for building on a resource tile, because production
        // can not be improved much.
        values.resource = tile.hasTileResource() ? MOD_HAS_RESOURCE : 1.0;
        
        productionWeight(tile);
        nearbySettlementsAndUnits(tile);
	}

	private boolean tileClaims(Tile tile) {
		values.terrainClaim = tilePlayer.terrainClaim(tile);
		
		if (tilePlayer.hasEnemyUnits(tile)) {
			values.unitOccupied = true;
		}
		
		NoClaimReason claimReason = tilePlayer.canOwnTileToFoundSettlement(tile);
		if (NoClaimReason.NATIVES == claimReason) {
			if (isNextToNativeSettlement(tile)) {
				values.stealLand = MOD_STEAL;
				//values.terrainClaim = NoClaimReason.SETTLEMENT;
			} else {
				values.stealLand = MOD_STEAL; 
			}
		}
		
		if (values.terrainClaim != NoClaimReason.NONE) {
			values.override = NoValueType.TERRAIN.getDouble();
			return true;
		}
		
		return false;
	}
	
	private boolean isNextToNativeSettlement(Tile tile) {
		for (Direction d : Direction.allDirections) {
			Tile neighbourTile = map.getTile(tile, d);
			if (neighbourTile != null && neighbourTile.hasSettlement() && !neighbourTile.getSettlement().isColony()) {
				return true;
			}
		}
		return false;
	}
	
	public void nearbySettlementsAndUnits(Tile tile) {
		boolean supportingColony = false;
		for (int radius=2; radius<DISTANCE_MAX; radius++) {
			spiralIterator.reset(tile.x, tile.y, false, radius);
			while (spiralIterator.hasNext()) {
				Tile nTile = map.getSafeTile(spiralIterator.getX(), spiralIterator.getY());
				if (nTile.hasSettlement()) {
					if (nTile.getSettlement().getOwner().equalsId(player)) {
						if (!supportingColony) {
							supportingColony = true;
							values.nearby *= MOD_OWN_COLONY[radius];
						}
					} else {
						if (nTile.getSettlement().getOwner().atWarWith(player)) {
							values.nearby *= MOD_ENEMY_COLONY[radius];
						} else {
							values.nearby *= MOD_NEUTRAL_COLONY[radius];
						}
					}
				}
				if (nTile.getUnits().isNotEmpty()) {
					Unit unit = nTile.getUnits().first();
					if (unit.getOwner().notEqualsId(player) && unit.isOffensiveUnit() && player.atWarWith(unit.getOwner())) {
						values.nearby *= MOD_ENEMY_UNIT[radius];
					}
				}
				spiralIterator.next();
			}
		}
	}
	
	/**
	 * Return true when negative weight
	 */
	private boolean highSeasAndInlandWeight(Tile tile) {
		int highseaDistance = highSeaDistance.getDistance(tile);
		if (highseaDistance == HighSeaDistanceGenerator.LACK_CONNECTION) {
			int inlandColonyDistance = colonyLandDistance.getDistance(tile);
			if (inlandColonyDistance > LONG_INLAND_PATH) {
				values.override = NoValueType.INLAND.getDouble();
				return true;
			}
			values.europe = MOD_INLAND;
		} else {
			if (highseaDistance >= LONG_PATH_TILES) {
	            // Normally penalize in direct proportion to length of
	            // path, but scale up to penalizing by the square of the
	            // path length for the first colony.
	            double trip = (double)LONG_PATH_TILES / highseaDistance;
	            values.europe = Math.pow(trip, 2.0 - development);
			} else {
				values.europe = 1.0 + 0.25 * ((double)LONG_PATH_TILES / (LONG_PATH_TILES - highseaDistance));
			}
		}
		return false;
	}

	private boolean centerTileProductionWeight(Tile tile) {
		int initialFood = 0;
		
		for (Production unattendedProduction : tile.getType().productionInfo.getUnattendedProductions()) {
			for (Entry<GoodsType, Integer> prodOutput : unattendedProduction.outputEntries()) {
				int amount = prodOutput.getValue();
				amount = tile.applyTileProductionModifier(prodOutput.getKey().getId(), amount);
				
				if (prodOutput.getKey().isFood()) {
					initialFood += amount;
					potentialColonyProduction.addGoods(GoodsType.FOOD, amount);
				} else {
					potentialColonyProduction.addGoods(prodOutput.getKey().getId(), amount);
				}
			}
		}
		
        if (initialFood <= FOOD_VERY_LOW) {
            values.override = NoValueType.FOOD.getDouble();
            return true;
        }
        values.prod = ((float)initialFood) * foodGoodsType.getProductionWeight();
        return false;
	}
	
	private void productionWeight(Tile tile) {
		for (Direction direction : Direction.allDirections) {
			Tile nTile = map.getTile(tile, direction);
			if (nTile == null) {
				continue;
			}
			double pf = 1.0;
			if (nTile.getOwner() != null && nTile.getOwner().notEqualsId(player)) {
				if (nTile.getOwner().isEuropean()) {
                    values.adjacent *= MOD_OWNED_EUROPEAN * development;
                    continue; // Always ignore production from this tile 
				} else {
					pf = MOD_OWNED_NATIVE;
				}
			}
			for (Production production : nTile.getType().productionInfo.getAttendedProductions()) {
				for (Entry<GoodsType, Integer> outputGood : production.outputEntries()) {
					int amount = outputGood.getValue();
					GoodsType goodsType = outputGood.getKey();
					if (goodsType.isFood()) {
						goodsType = foodGoodsType;
					}
					amount = tile.applyTileProductionModifier(goodsType.getId(), amount);
					
					if (nTile.getType().isWater()) {
						amount *= development; 
					}
					
					potentialColonyProduction.addGoods(goodsType.getId(), amount);
					values.prod += amount * goodsType.getProductionWeight() * pf;
					
					if (amount > HIGH_PRODUCTION) {
						potentialHighProduction.addGoods(goodsType.getId(), amount);
					} else {
						if (amount > GOOD_PRODUCTION) {
							potentialGoodProduction.addGoods(goodsType.getId(), amount);
						}
					}
				}
			}
			for (com.badlogic.gdx.utils.ObjectIntMap.Entry<String> highProd : potentialHighProduction.entries()) {
				values.level *= MOD_HIGH_PRODUCTION;
				potentialGoodProduction.setZero(highProd.key);
			}
			int goodSizeAmount = 0;
			for (com.badlogic.gdx.utils.ObjectIntMap.Entry<String> goodProd : potentialGoodProduction.entries()) {
				if (goodProd.value > 0) {
					goodSizeAmount++;
				}
			}
			if (goodSizeAmount > 0) {
				values.level *= MOD_GOOD_PRODUCTION * goodSizeAmount;
			}
		}
		
        if (potentialColonyProduction.getQuantity(GoodsType.FOOD) < FOOD_LOW) {
            values.food *= MOD_FOOD_LOW;
        }
		
		for (com.badlogic.gdx.utils.ObjectIntMap.Entry<String> prodEntry : potentialColonyProduction.entries()) {
			GoodsType goodsType = Specification.instance.goodsTypes.getById(prodEntry.key);
			if (goodsType.getLowProductionThreshold() > 0 && prodEntry.value < goodsType.getLowProductionThreshold()) {
                float fraction = (float)prodEntry.value / goodsType.getLowProductionThreshold();
                double zeroValue = goodsType.getZeroProductionFactor();
				values.goods *= (1.0 - fraction) * zeroValue + fraction;
			}
		}
	}
	
	public void toStringValues(MapTileDebugInfo mapFileInfo) {
        int x, y, cost;
        for (int i=0; i<tileWeights.size(); i++) {
            x = tileWeights.toX(i);
            y = tileWeights.toY(i);
            cost = tileWeights.get(i);
            
            if (cost > 0 && cost != Integer.MAX_VALUE) {
            	mapFileInfo.str(x, y, Integer.toString(cost));
            }
        }
		
	}

	public Int2dArray getTileWeights() {
		return tileWeights;
	}

}
