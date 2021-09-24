package net.sf.freecol.common.model.map.path;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.TreeSet;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitMoveType;

import promitech.colonization.Direction;
import promitech.map.Object2dArray;

public class PathFinder {

	public static final EnumSet<FlagTypes> excludeUnexploredTiles = EnumSet.of(FlagTypes.AvoidUnexploredTiles);
	public static final EnumSet<FlagTypes> includeUnexploredTiles = EnumSet.noneOf(FlagTypes.class);

	public static final EnumSet<FlagTypes> includeNavyThreat = EnumSet.of(FlagTypes.IncludeNavyThreatTiles);
	public static final EnumSet<FlagTypes> excludeNavyThreat = EnumSet.noneOf(FlagTypes.class);

	public static final EnumSet<FlagTypes> includeUnexploredAndNavyThreatTiles = EnumSet.of(FlagTypes.IncludeNavyThreatTiles);
	public static final EnumSet<FlagTypes> includeUnexploredAndExcludeNavyThreatTiles = EnumSet.of(FlagTypes.IncludeNavyThreatTiles);

	public static final EnumSet<FlagTypes> excludeUnexploredAndIncludeNavyThreatTiles = EnumSet.of(
		FlagTypes.AvoidUnexploredTiles,
		FlagTypes.IncludeNavyThreatTiles
	);

	private enum FlagTypes {
		// move only through explored tiles
		AvoidUnexploredTiles,

		IncludeNavyThreatTiles
	}

    static final int INFINITY = Integer.MAX_VALUE;
    static final int UNDEFINED = Integer.MIN_VALUE;
	
	private static Comparator<Node> NODE_WEIGHT_COMPARATOR = new Comparator<Node>() {
		@Override
		public int compare(Node o1, Node o2) {
			if (o1.totalCost > o2.totalCost) {
				return 1;
			} else {
				return -1;
			}
		}
	};
	private GoalDecider rangeMapGoalDecider = new GoalDecider() {
	    @Override
	    public boolean hasGoalReached(Node moveNode) {
	        return false;
	    }
	};
	private GoalDecider pathToEuropeGoalDecider = new GoalDecider() {
	    @Override
	    public boolean hasGoalReached(Node moveNode) {
	        return moveNode.tile.getType().isHighSea();
	    }
	};
	private GoalDecider pathToTileGoalDecider = new GoalDecider() {
	    @Override
	    public boolean hasGoalReached(Node moveNode) {
	        return endTile.getId().equals(moveNode.tile.getId());
	    }
	};
	
	protected Object2dArray<Node> grid; 
	private final TreeSet<Node> nodes = new TreeSet<Node>(NODE_WEIGHT_COMPARATOR);
	
	private final CostDecider baseCostDecider = new CostDecider();
	private final NavyCostDecider navyCostDecider = new NavyCostDecider();
	private final NavyWithoutThreatCostDecider navyWithoutThreatCostDecider = new NavyWithoutThreatCostDecider();
	private CostDecider costDecider;
	private GoalDecider goalDecider;

	private Map map;
	private Tile startTile;
	private Tile endTile;
	private Unit moveUnit;
	private final UnitMoveType unitMoveType = new UnitMoveType();
	
	private boolean findPossibilities = false;
	
	public PathFinder() {
	}

	public Path findToEurope(final Map map, final Tile startTile, final Unit moveUnit, EnumSet<FlagTypes> flags) {
	    goalDecider = pathToEuropeGoalDecider;
        this.map = map;
        this.startTile = startTile;
        this.endTile = null;
        this.moveUnit = moveUnit;
        this.unitMoveType.init(moveUnit);
        this.findPossibilities = false;
        this.navyWithoutThreatCostDecider.avoidUnexploredTiles = flags.contains(FlagTypes.AvoidUnexploredTiles);
        this.navyCostDecider.avoidUnexploredTiles = flags.contains(FlagTypes.AvoidUnexploredTiles);
        this.baseCostDecider.avoidUnexploredTiles = flags.contains(FlagTypes.AvoidUnexploredTiles);
        
        determineCostDecider(false);
        Path path = find();
        path.toEurope = true;
		return path;
	}

	public Path findToTile(final Map map, final Tile startTile, final Tile endTile, final Unit moveUnit, EnumSet<FlagTypes> flags) {
        goalDecider = pathToTileGoalDecider;
        this.map = map;
        this.startTile = startTile;
        this.endTile = endTile;
        this.moveUnit = moveUnit;
        this.unitMoveType.init(moveUnit);
        this.findPossibilities = false;
		this.navyWithoutThreatCostDecider.avoidUnexploredTiles = flags.contains(FlagTypes.AvoidUnexploredTiles);
		this.navyCostDecider.avoidUnexploredTiles = flags.contains(FlagTypes.AvoidUnexploredTiles);
		this.baseCostDecider.avoidUnexploredTiles = flags.contains(FlagTypes.AvoidUnexploredTiles);
        
        determineCostDecider(flags.contains(FlagTypes.IncludeNavyThreatTiles));
        Path path = find();
        path.toEurope = false;
        return path;
    }
	
	public Path findTheQuickestToTile(final Map map, final Tile startTile, final List<Tile> endTiles, final Unit moveUnit, EnumSet<FlagTypes> flags) {
		if (endTiles.isEmpty()) {
			throw new IllegalArgumentException("endTiles can not be empty");
		}
		Path theBestPath = null;
		for (Tile oneTile : endTiles) {
			Path onePath = findToTile(map, startTile, oneTile, moveUnit, flags);
			if (theBestPath == null || onePath.isQuickestThan(theBestPath)) {
				theBestPath = onePath;
			}
		}
		return theBestPath;
	}
	
	public void generateRangeMap(final Map map, final Tile startTile, final Unit moveUnit, EnumSet<FlagTypes> flags) {
	    goalDecider = rangeMapGoalDecider;
        this.map = map;
        this.startTile = startTile;
        this.endTile = null;
        this.moveUnit = moveUnit;
        this.unitMoveType.init(moveUnit);
        this.findPossibilities = true;
		this.navyWithoutThreatCostDecider.avoidUnexploredTiles = flags.contains(FlagTypes.AvoidUnexploredTiles);
		this.navyCostDecider.avoidUnexploredTiles = flags.contains(FlagTypes.AvoidUnexploredTiles);
		this.baseCostDecider.avoidUnexploredTiles = flags.contains(FlagTypes.AvoidUnexploredTiles);
		
        determineCostDecider(false);
        find();
	}
	
	private void determineCostDecider(boolean includeNavyThreat) {
	    if (moveUnit.isNaval()) {
	        if (includeNavyThreat) {
	            costDecider = navyWithoutThreatCostDecider;
	        } else {
	            costDecider = navyCostDecider;
	        }
	    } else {
	        costDecider = baseCostDecider;
	    }
	}
	
	private Path find() {
		resetFinderBeforeSearching(map);
		
		int iDirections = 0; 
		int nDirections = Direction.values().length;
		costDecider.init(map, moveUnit);
		
		Node currentNode = grid.get(startTile.x, startTile.y);
		currentNode.reset(moveUnit.getMovesLeft(), 0);
		currentNode.turns = 0;
		nodes.add(currentNode);

		Node oneOfTheBest = null;
		Node reachedGoalNode = null;
		while (true) {
			currentNode = nodes.pollFirst();
			if (currentNode == null) {
				break;
			}
			if (reachedGoalNode != null && reachedGoalNode.hasBetterCostThen(currentNode)) {
				break;
			}
			for (iDirections=0; iDirections<nDirections; iDirections++) {
				Direction moveDirection = Direction.values()[iDirections];
				
				Tile moveTile = map.getTile(currentNode.tile, moveDirection);
				if (moveTile == null) {
					continue;
				}
				Node moveNode = grid.get(moveTile.x, moveTile.y);
				if (moveNode.noMove) {
					continue;
				}
				
				MoveType moveType = unitMoveType.calculateMoveType(currentNode.tile, moveNode.tile);
				if (goalDecider.hasGoalReached(moveNode)) {
					reachedGoalNode = moveNode;
					// change moveType to default move. Sometimes goal can be indian settlement 
					// and moveType should be used only to find path
					moveType = MoveType.MOVE;
				}
				
				if (costDecider.calculateAndImproveMove(currentNode, moveNode, moveType, moveDirection)) {
					if (oneOfTheBest == null || moveNode.hasBetterCostThen(oneOfTheBest)) {
						oneOfTheBest = moveNode;
					}
					nodes.add(moveNode);
				} else {
					if (costDecider.isMarkDestTileAsUnaccessible(currentNode, moveNode, moveType)) {
						moveNode.noMove = true;
					}
				}
			}
		}

		if (findPossibilities) {
			return null;
		}
		
		if (reachedGoalNode != null) {
			return createPath(moveUnit, reachedGoalNode);
		} else {
			return createPath(moveUnit, oneOfTheBest);
		}
	}
	
	private Path createPath(final Unit moveUnit, final Node endPathNode) {
		Node begining = null;
		Node n = endPathNode;
		int count = 1;
		while (n != null) {
			n.next = begining;
			begining = n;
			n = n.preview;
			count++;
		}

		if (endPathNode == null) {
			return new Path(
				moveUnit, 
				startTile, startTile, 
				0, false
			);  
		}
		
		Path path = new Path(
			moveUnit, 
			startTile, endPathNode.tile, 
			count, endTile == null || endPathNode.tile.equalsCoordinates(endTile)
		);
		n = begining;
		while (n != null) {
			path.add(n.tile, n.turns);
			n = n.next;
		}
		return path;
	}

	public void reset() {
		nodes.clear();
		if (grid != null) {
			for (int cellIndex=0; cellIndex<grid.getMaxCellIndex(); cellIndex++) {
				grid.get(cellIndex).reset(0, INFINITY);
			}
			grid = null;
		}
	}
	
	private void resetFinderBeforeSearching(Map map) {
		if (grid == null || !grid.sizeEquals(map.width, map.height)) {
		    grid = new Object2dArray<Node>(map.width, map.height);
		    
		    for (int cellIndex=0; cellIndex<grid.getMaxCellIndex(); cellIndex++) {
		        Node n = new Node(map.getTile(grid.toX(cellIndex), grid.toY(cellIndex)));
		        n.reset(0, INFINITY);
		        grid.set(cellIndex, n);
		    }		
		} else {
            for (int cellIndex=0; cellIndex<grid.getMaxCellIndex(); cellIndex++) {
                grid.get(cellIndex).reset(0, INFINITY);
            }       
		}
		nodes.clear();
	}
	
	public int totalCost(int cellIndex) {
		return grid.get(cellIndex).totalCost;
	}
	
	public int turnsCost(int cellIndex) {
	    return grid.get(cellIndex).turns;
	}

	public int turnsCost(Tile tile) {
		return grid.get(tile.x, tile.y).turns;
	}
	
	public void totalCostToStringArrays(String[][] strTab) {
	    for (int i=0; i<grid.getMaxCellIndex(); i++) {
	        strTab[grid.toY(i)][grid.toX(i)] = Integer.toString(totalCost(i));
	    }
	}

	public void turnCostToStringArrays(String[][] strTab) {
	    for (int i=0; i<grid.getMaxCellIndex(); i++) {
	        strTab[grid.toY(i)][grid.toX(i)] = Integer.toString(turnsCost(i));
	    }
	}
	
	public Direction getDirectionInto(int cellIndex) {
		Node oneBefore = null;
		Node n = grid.get(cellIndex);
		
		while (n != null) {
			if (n.preview != null) { 
				oneBefore = n;
			}
			n = n.preview;
		}
		
		if (oneBefore == null) {
			return null;
		}
		
		return Direction.fromCoordinates(
			startTile.x, startTile.y, 
			oneBefore.tile.x, oneBefore.tile.y
		);
	}

    public Path getPathInto(int cellIndex) {
        Node node = grid.get(cellIndex);
        return createPath(moveUnit, node);
    }

	public Path getPathInto(Tile dest) {
		return getPathInto(grid.toIndex(dest.x, dest.y));
	}
}
