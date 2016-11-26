package net.sf.freecol.common.model.map.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.SettlementType;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.map.Region;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GameOptions;
import promitech.colonization.Direction;
import promitech.colonization.Randomizer;
import promitech.colonization.SpiralIterator;

class IndianSettlementsGenerator {

	class Territory {
		public final Player player;
		public int numberOfSettlements = 0;
		public Tile centerTile;

		public Territory(Player player, Tile tile) {
			this.player = player;
			this.centerTile = tile;
		}

		public void updateNumberOfSettlements(float share) {
            switch (player.nationType().getSettlementNumber()) {
            case HIGH:
                numberOfSettlements = Math.round(4 * share);
                break;
            case AVERAGE:
                numberOfSettlements = Math.round(3 * share);
                break;
            case LOW:
                numberOfSettlements = Math.round(2 * share);
                break;
            }
		}
	}

	private final MapGenerator mapGenerator;
	private final Map map;
	private final Game game;
	private SpiralIterator spiralIterator;
	
	IndianSettlementsGenerator(MapGenerator mapGenerator, Game game, Map map) {
		this.mapGenerator = mapGenerator;
		this.map = map;
		this.game = game;
		
		spiralIterator = new SpiralIterator(map.width, map.height);
	}
	
	void makeNativeSettlements() {
		float shareSum = 0f;
		
		java.util.Map<String,Territory> territoryByName = new HashMap<String, IndianSettlementsGenerator.Territory>();
		
		List<Player> indians = new ArrayList<Player>(game.players.size());
		for (Player player : game.players.entities()) {
			if (!player.isLiveIndianPlayer()) {
				continue;
			}
            switch (player.nationType().getSettlementNumber()) {
            case HIGH:
                shareSum += 4;
                break;
            case AVERAGE:
                shareSum += 3;
                break;
            case LOW:
                shareSum += 2;
                break;
            }
			indians.add(player);
			
			Territory territory = null;
			Set<String> playerRegionNames = player.nationType().getRegionNames();

			for (String playerRegionName : playerRegionNames) {
				if (territoryByName.get(playerRegionName) == null) {
					Region region = map.regions.getByIdOrNull(playerRegionName);
					if (region != null) {
						territory = new Territory(player, centerOfRegion(map, region));
						territoryByName.put(playerRegionName, territory);
						System.out.println("Allocated region " + playerRegionName + " for " + player + ".");
						break;
					} else {
						System.out.println("can not find region(" + playerRegionName + ") for player " + player);
					}
				}
			}
			
			if (territory == null) {
				System.out.println("Failed to allocate preferred region for " + player);
				
				territory = new Territory(player, mapGenerator.getRandomLandTile(map));
				territoryByName.put(player.getId(), territory);
			}
		}
		if (indians.isEmpty()) {
			return;
		}
		
		List<Tile> settlementTiles = generateSuitableSettlementTiles(map);
		System.out.println("settlementTiles = " + settlementTiles.size());
		float share = settlementTiles.size() / shareSum;
        if (settlementTiles.size() < indians.size()) {
            System.out.println("There are only " + settlementTiles.size() + " settlement sites. This is smaller than " + indians.size() + " the number of tribes.");
        }
        
        for (Entry<String, Territory> entry : territoryByName.entrySet()) {
        	Territory territory = entry.getValue();
        	territory.updateNumberOfSettlements(share);
        	
        	SettlementType capitalType = territory.player.nationType().getSettlementCapitalType();
        	//int radius = capitalType.getClaimableRadius();
        	final Tile center = territory.centerTile;
			Collections.sort(settlementTiles, new Comparator<Tile>() {
	        	public int compare(Tile t1, Tile t2) {
	        		return Map.distance(t1, center) - Map.distance(t2, center);
	        	}
	        });
        	if (!settlementTiles.isEmpty()) {
        		Tile tile = settlementTiles.remove(0);
        		changeTileOwner(Settlement.createIndianSettlement(territory.player, tile, capitalType));
        		territory.centerTile = tile;
        		territory.numberOfSettlements--;
        	}
        }
        
        for (Entry<String, Territory> entry : territoryByName.entrySet()) {
        	final Territory territory = entry.getValue();
        	
        	Collections.sort(settlementTiles, new Comparator<Tile>() {
        		public int compare(Tile t1, Tile t2) {
        			return Map.distance(t1, territory.centerTile) - Map.distance(t2, territory.centerTile);
        		}
        	});
        	
        	SettlementType settlementRegularType = territory.player.nationType().getSettlementRegularType();
        	while (territory.numberOfSettlements > 0 && !settlementTiles.isEmpty()) {
        		Tile tile = settlementTiles.remove(0);
        		changeTileOwner(Settlement.createIndianSettlement(territory.player, tile, settlementRegularType));
        		territory.numberOfSettlements--;
        	}
        }
	}

	private void changeTileOwner(IndianSettlement settlement) {
		spiralIterator.reset(settlement.tile.x, settlement.tile.y, true, settlement.settlementType.getClaimableRadius());
		
		settlement.tile.changeOwner(settlement.getOwner(), settlement);
		
		Tile tile;
		while (spiralIterator.hasNext()) {
			tile = map.getTile(spiralIterator.getX(), spiralIterator.getX());
			if (tile != null && !tile.hasOwnerOrOwningSettlement()) {
				tile.changeOwner(settlement.getOwner(), settlement);
			}
			spiralIterator.next();
		}
	}
	
	public Tile centerOfRegion(Map map, Region region) {
		int x, y;
		for (y=0; y<Map.STANDARD_REGION_NAMES.length; y++) {
			for (x=0; x<Map.STANDARD_REGION_NAMES[0].length; x++) {
				if (region.equalsId(Map.STANDARD_REGION_NAMES[y][x])) {
					return map.getTile(
						(map.width / 3) * x + (map.width / 3) / 2,					 
						(map.height / 3) * y + (map.height / 3) / 2 
					);
				}
			}
		}
		return mapGenerator.getRandomLandTile(map);
	}
	
	private List<Tile> generateSuitableSettlementTiles(Map map) {
		int settlementDistance = Specification.options.getIntValue(GameOptions.SETTLEMENT_NUMBER);
		
		List<Tile> settlementTiles = new ArrayList<Tile>(mapGenerator.landTilesCount);
		boolean tooClose = false;
		Tile tile;
		int x, y, i;
		for (y=0; y<map.height; y++) {
			for (x=0; x<map.width; x++) {
				tile = map.getTile(x, y);
				if (!map.isPolar(tile) && isSuitableForNativeSettlement(tile)) {
					tooClose = false;
					for (i=0; i<settlementTiles.size(); i++) {
						if (Map.distance(tile, settlementTiles.get(i)) < settlementDistance) {
							tooClose = true;
							break;
						}
					}
					if (!tooClose) {
						settlementTiles.add(tile);
					}
				}
			}
		}
		Collections.shuffle(settlementTiles, Randomizer.instance().getRand());
		return settlementTiles;
	}
	
    /**
     * Is a tile suitable for a native settlement?
     * Require the tile be settleable, and at least half its neighbours
     * also be settleable.
     *
     * @param tile The <code>Tile</code> to examine.
     * @return True if this tile is suitable.
     */
    private boolean isSuitableForNativeSettlement(Tile tile) {
        if (!tile.getType().canSettle()) {
        	return false;
        }
        
        int good = 0;
        Tile t;
        for (Direction d : Direction.allDirections) {
        	t = map.getTile(tile, d);
        	if (t != null && t.getType().canSettle()) {
        		good++;
        	}
        }
        return good >= Direction.allDirections.size() / 2;
    }
	
}
