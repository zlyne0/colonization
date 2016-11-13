package net.sf.freecol.common.model.map.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovementType;
import promitech.colonization.Direction;
import promitech.colonization.Randomizer;

class RiverFlowGenerator {
	private final Map map;
	private final TileImprovementType riverImprovement;
	final Set<Tile> riverTiles = new HashSet<Tile>();
	
	private final List<List<Direction>> randomDirectionLists;
	
	public RiverFlowGenerator(Map map, TileImprovementType riverImprovement) {
		this.map = map;
		this.riverImprovement = riverImprovement;
		
		randomDirectionLists = new ArrayList<List<Direction>>(2);
		for (int i=0; i<2; i++) {
			List<Direction> l = new ArrayList<Direction>(Direction.longSides);
			Collections.shuffle(l, Randomizer.instance().getRand());
			randomDirectionLists.add(l);
		}
	}
	
	private boolean contains(Tile tile) {
		return riverTiles.contains(tile);
	}
	
	private boolean isNextToSelf(Tile source, Tile tile) {
		for (Direction d : Direction.longSides) {
			Tile t = map.getTile(tile, d);
			if (t != source && this.contains(t)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean startFlow(Tile source) {
		Direction direction = Randomizer.instance().randomMember(Direction.longSides);
		return flow(source, direction, 0);
	}
	
	public boolean flow(final Tile source, final Direction previewDirection, final int segmentFlowCount) {
		riverTiles.add(source);
		int flowCount = segmentFlowCount + 1;
		
		Tile nextTile = null;
		if (flowCount <= (2 + Randomizer.instance().randomInt(2))) {
			// the same direction unless it can not flow
			nextTile = map.getTile(source, previewDirection);
			if (canFlow(source, nextTile)) {
				if (isNeighbourWater(nextTile)) {
					riverTiles.add(nextTile);
					return true;
				}
				return flow(nextTile, previewDirection, flowCount);
			}
		}
		
		flowCount = 0;
		List<Direction> randomDirections = randomDirectionLists.get(Randomizer.instance().randomInt(randomDirectionLists.size()));
		for (Direction nextDirection : randomDirections) {
			if (nextDirection == previewDirection || nextDirection == previewDirection.getReverseDirection()) {
				continue;
			}
			nextTile = map.getTile(source, nextDirection);
			if (canNotFlow(source, nextTile)) {
				continue;
			}
			if (isNeighbourWater(nextTile)) {
				riverTiles.add(nextTile);
				return true;
			} else {
				boolean flow = flow(nextTile, nextDirection, flowCount);
				if (flow) {
					return true;
				}
			}
		}
		
		riverTiles.remove(source);
		return false;
	}
	
	private boolean isNeighbourWater(Tile tile) {
		for (Direction d : Direction.longSides) {
			Tile nextTile = map.getTile(tile, d);
			if (nextTile != null && (nextTile.getType().isWater() || nextTile.hasImprovementType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID))) {
				return true;
			}
		}
		return false;
	}
	
	private boolean canFlow(Tile source, Tile nextTile) {
		return !canNotFlow(source, nextTile);
	}
	
	private boolean canNotFlow(Tile source, Tile nextTile) {
		if (nextTile == null) {
			return true;
		}
		if (!nextTile.getType().isTileImprovementAllowed(riverImprovement)) {
			// is the tile suitable for this river?
			return true;
		} 
		if (this.contains(nextTile)) {
			// Tile is already in river.
			return true;
		}
		if (this.isNextToSelf(source, nextTile)) {
			//Tile is next to the river.
			return true;
		}
		return false;
	}
}