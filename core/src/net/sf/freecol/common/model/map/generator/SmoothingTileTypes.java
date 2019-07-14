package net.sf.freecol.common.model.map.generator;

import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileType;
import promitech.colonization.Direction;
import promitech.map.Map2DArray;

public class SmoothingTileTypes {

	private final String plains = "model.tile.plains";
	private final String grassland = "model.tile.grassland";
	private final String prairie = "model.tile.prairie";
	private final String savannah = "model.tile.savannah";

	private final Map map;
	private final MapIdEntities<TileType> tileTypes;
	private final Map2DArray<String> newTilesType;
	private final ObjectIntMap<String> counter = new ObjectIntMap<String>();
	private int maxTypeCount = 0;
	private String maxType = null;
	
	public SmoothingTileTypes(Map map, MapIdEntities<TileType> tileTypes) {
		this.map = map;
		this.tileTypes = tileTypes;
		this.newTilesType = new Map2DArray<String>(String.class, map.width, map.height);
	}
	
	public void smoothing(int smoothingCount) {
		int x, y;
		Tile tile;
		
		for (int i=0; i<smoothingCount; i++) {
			if (i != 0) {
				for (y=0; y<map.height; y++) {
					for (x=0; x<map.width; x++) {
						newTilesType.createTile(x, y, null);
					}
				}
			}
			
			for (y=0; y<map.height; y++) {
				for (x=0; x<map.width; x++) {
					tile = map.getSafeTile(x, y);
					
					if (isCountable(tile.getType())) {
						String newTileTypeId = determineMaxOccurTileType(tile);
						if (!tile.getType().equalsId(newTileTypeId)) {
							newTilesType.createTile(x, y, newTileTypeId);
						}
					}
				}
			}
			
			for (y=0; y<map.height; y++) {
				for (x=0; x<map.width; x++) {
					String newTileType = newTilesType.getSafeTile(x, y);
					if (newTileType != null) {
						tile = map.getSafeTile(x, y);
						tile.changeTileType(tileTypes.getById(newTileType));
					}
				}
			}
		}
	}

	private String determineMaxOccurTileType(Tile tile) {
		counter.clear();
		counter.getAndIncrement(tile.getType().getId(), 0, 1);
		for (Direction direction : Direction.allDirections) {
			Tile neighbourTile = map.getTile(tile, direction);
			if (neighbourTile != null && isCountable(neighbourTile.getType())) {
				counter.getAndIncrement(neighbourTile.getType().getId(), 0, 1);
			}
		}
		
		maxTypeCount = 1;
		maxType = tile.getType().getId();
		for (Entry<String> entry : counter.iterator()) {
			if (entry.value > maxTypeCount) {
				maxTypeCount = entry.value;
				maxType = entry.key;
			}
		}
		return maxType;
	}
	
	private boolean isCountable(TileType tileType) {
		return plains.equals(tileType.getId()) 
			|| grassland.equals(tileType.getId()) 
			|| prairie.equals(tileType.getId()) 
			|| savannah.equals(tileType.getId());
	}
}
