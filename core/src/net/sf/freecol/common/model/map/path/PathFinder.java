package net.sf.freecol.common.model.map.path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.ai.MapTileDebugInfo;
import net.sf.freecol.common.model.player.Player;

import promitech.colonization.Direction;
import promitech.map.Object2dArray;

import static java.util.Collections.*;

/**
 * Welcome to hell
 */
public class PathFinder {

	public interface SumPolicy {
		public static final SumPolicy SIMPLY_SUM = new SumPolicy() {
			@Override
			public int sum(int a, int b) {
				return a + b;
			}
		};

		public static final SumPolicy PRIORITY_SUM = new SumPolicy() {
			@Override
			public int sum(int a, int b) {
				return 100 * a + b;
			}
		};

		int sum(int a, int b);
	}

	public static final Set<FlagTypes> excludeUnexploredTiles = unmodifiableSet(EnumSet.of(FlagTypes.AvoidUnexploredTiles));
	public static final Set<FlagTypes> includeUnexploredTiles = unmodifiableSet(EnumSet.noneOf(FlagTypes.class));

	public static final Set<FlagTypes> includeNavyThreat = unmodifiableSet(EnumSet.of(FlagTypes.IncludeNavyThreatTiles));
	public static final Set<FlagTypes> excludeNavyThreat = unmodifiableSet(EnumSet.noneOf(FlagTypes.class));

	public static final Set<FlagTypes> includeUnexploredAndNavyThreatTiles = unmodifiableSet(EnumSet.of(FlagTypes.IncludeNavyThreatTiles));
	public static final Set<FlagTypes> includeUnexploredAndExcludeNavyThreatTiles = unmodifiableSet(EnumSet.of(FlagTypes.IncludeNavyThreatTiles));

	public static final Set<FlagTypes> excludeUnexploredAndIncludeNavyThreatTiles = unmodifiableSet(EnumSet.of(
		FlagTypes.AvoidUnexploredTiles,
		FlagTypes.IncludeNavyThreatTiles
	));

	public enum FlagTypes {
		// move only through explored tiles
		AvoidUnexploredTiles,
		IncludeNavyThreatTiles,
		AvoidDisembark,
		AllowEmbark,
		AllowCarrierEnterWithGoods
	}

    public static final int INFINITY = Integer.MAX_VALUE;
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
	
	private final DefaultCostDecider baseCostDecider = new DefaultCostDecider();
	private final NavyCostDecider navyCostDecider = new NavyCostDecider();
	private final NavyWithoutThreatCostDecider navyWithoutThreatCostDecider = new NavyWithoutThreatCostDecider();
	private final MaxTurnRangeCostDecider maxTurnRangeCostDecider = new MaxTurnRangeCostDecider();
	private CostDecider costDecider;
	private GoalDecider goalDecider;
	private TileConsumer tileConsumer = TileConsumer.EMPTY;

	private final PathUnitFactory pathUnitFactory = new PathUnitFactory();

	private Map map;
	private Tile startTile;
	private Tile endTile;
	private PathUnit pathUnit;
	private List<Tile> startTiles = new ArrayList<Tile>();

	private Node oneOfTheBest = null;
	private Node reachedGoalNode = null;

	public PathFinder() {
	}

	private PathUnit createPathUnit(Unit unit) {
		return pathUnitFactory.obtain(unit);
	}

	public PathUnit createPathUnit(Player owner, UnitType unitType) {
		return pathUnitFactory.obtain(owner, unitType, Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID));
	}

	public PathUnit createPathUnit(Player owner, UnitType unitType, UnitRole unitRole) {
		return pathUnitFactory.obtain(owner, unitType, unitRole	);
	}

	public Path findToEurope(Map map, Tile startTile, Unit unit, Set<FlagTypes> flags) {
		return findToEurope(map, startTile, createPathUnit(unit), flags);
	}

	public Path findToEurope(final Map map, final Tile startTile, PathUnit pathUnit, Set<FlagTypes> flags) {
	    goalDecider = pathToEuropeGoalDecider;
        this.map = map;
        this.startTile = startTile;
        this.endTile = null;
        this.pathUnit = pathUnit;

		setCostDeciderFlags(flags);
        determineCostDecider(false, INFINITY);

        Path path = find();
        path.toEurope = true;
		return path;
	}

	public Path findToTile(Map map, Unit unit, Tile endTile, Set<FlagTypes> flags) {
		return findToTile(map, unit.getTile(), endTile, createPathUnit(unit), flags);
	}

	public Path findToTile(final Map map, final Tile startTile, final Tile endTile, final Unit unit, Set<FlagTypes> flags) {
		return findToTile(map, startTile, endTile, createPathUnit(unit), flags);
	}

	public Path findToTile(final Map map, final Tile startTile, final Tile endTile, final PathUnit pathUnit, Set<FlagTypes> flags) {
        goalDecider = pathToTileGoalDecider;
        this.map = map;
        this.startTile = startTile;
        this.endTile = endTile;
        this.pathUnit = pathUnit;

		setCostDeciderFlags(flags);
        determineCostDecider(flags.contains(FlagTypes.IncludeNavyThreatTiles), INFINITY);

        Path path = find();
        path.toEurope = false;
        return path;
    }

	public Path findTheQuickestPath(final Map map, final Tile startTile, final Collection<Tile> endTiles, final Unit unit, Set<FlagTypes> flags) {
		Tile theQuickestTile = findTheQuickestTile(map, startTile, endTiles, unit, flags);
		if (theQuickestTile != null) {
			return createPath(theQuickestTile);
		}
		return null;
	}

	public Tile findTheQuickestTile(final Map map, final Tile startTile, final Collection<Tile> endTiles, final Unit unit, Set<FlagTypes> flags) {
		if (endTiles.isEmpty()) {
			throw new IllegalArgumentException("endTiles can not be empty");
		}
		generateRangeMap(map, startTile, unit, flags);

		Tile theBestTile = null;
		int theBestCost = INFINITY;

		for (Tile tile : endTiles) {
			int cost = turnsCost(tile);
			if (cost < theBestCost) {
				theBestCost = cost;
				theBestTile = tile;
			}
		}
		return theBestTile;
	}

	public void generateRangeMap(final Map map, final Tile aStartTile, final Unit unit, Set<FlagTypes> flags) {
		this.startTiles.clear();
		this.startTiles.add(aStartTile);
		generateRangeMap(map, createPathUnit(unit), flags, INFINITY, TileConsumer.EMPTY);
	}

	public void generateRangeMap(final Map map, final Tile aStartTile, final Unit unit, Set<FlagTypes> flags, int maxTurnsRange) {
		this.startTiles.clear();
		this.startTiles.add(aStartTile);
		generateRangeMap(map, createPathUnit(unit), flags, maxTurnsRange, TileConsumer.EMPTY);
	}

	public void generateRangeMap(final Map map, final Unit unit, Set<FlagTypes> flags) {
		this.startTiles.clear();
		this.startTiles.add(unit.getTile());
		generateRangeMap(map, createPathUnit(unit), flags, INFINITY, TileConsumer.EMPTY);
	}

	public void generateRangeMap(final Map map, Collection<Tile> startTiles, final PathUnit pathUnit, Set<FlagTypes> flags) {
		this.startTiles.clear();
		this.startTiles.addAll(startTiles);
		generateRangeMap(map, pathUnit, flags, INFINITY, TileConsumer.EMPTY);
	}

	public void generateRangeMap(final Map map, Collection<Tile> startTiles, final PathUnit pathUnit, Set<FlagTypes> flags, TileConsumer tileConsumer) {
		this.startTiles.clear();
		this.startTiles.addAll(startTiles);
		generateRangeMap(map, pathUnit, flags, INFINITY, tileConsumer);
	}

	public void generateRangeMap(final Map map, final Tile aStartTile, final PathUnit pathUnit, Set<FlagTypes> flags) {
		this.startTiles.clear();
		this.startTiles.add(aStartTile);
		generateRangeMap(map, pathUnit, flags, INFINITY, TileConsumer.EMPTY);
	}

	public void generateRangeMap(final Map map, final Tile aStartTile, final PathUnit pathUnit, Set<FlagTypes> flags, int maxTurnRange) {
		this.startTiles.clear();
		this.startTiles.add(aStartTile);
		generateRangeMap(map, pathUnit, flags, maxTurnRange, TileConsumer.EMPTY);
	}

	public void generateRangeMap(final Map map, final Tile aStartTile, final PathUnit pathUnit, Set<FlagTypes> flags, TileConsumer tileConsumer) {
		this.startTiles.clear();
		this.startTiles.add(aStartTile);
		generateRangeMap(map, pathUnit, flags, INFINITY, tileConsumer);
	}

	private void generateRangeMap(final Map map, final PathUnit pathUnit, Set<FlagTypes> flags, int maxTurnsRange, TileConsumer tileConsumer) {
	    this.goalDecider = rangeMapGoalDecider;
        this.map = map;
        this.endTile = null;
        this.pathUnit = pathUnit;
		this.startTile = startTiles.get(0);
		this.tileConsumer = tileConsumer;

        setCostDeciderFlags(flags);
        determineCostDecider(false, maxTurnsRange);

		calculatePaths();
	}

	private void setCostDeciderFlags(Set<FlagTypes> flags) {
		this.navyWithoutThreatCostDecider.avoidUnexploredTiles = flags.contains(FlagTypes.AvoidUnexploredTiles);
		this.navyWithoutThreatCostDecider.allowDisembark = !flags.contains(FlagTypes.AvoidDisembark);
		this.navyWithoutThreatCostDecider.allowCarrierEnterWithGoods = flags.contains(FlagTypes.AllowCarrierEnterWithGoods);

		this.navyCostDecider.avoidUnexploredTiles = flags.contains(FlagTypes.AvoidUnexploredTiles);
		this.navyCostDecider.allowDisembark = !flags.contains(FlagTypes.AvoidDisembark);
		this.navyCostDecider.allowCarrierEnterWithGoods = flags.contains(FlagTypes.AllowCarrierEnterWithGoods);

		this.baseCostDecider.avoidUnexploredTiles = flags.contains(FlagTypes.AvoidUnexploredTiles);
		this.baseCostDecider.allowEmbark = flags.contains(FlagTypes.AllowEmbark);
		this.baseCostDecider.allowCarrierEnterWithGoods = flags.contains(FlagTypes.AllowCarrierEnterWithGoods);
	}

	private void determineCostDecider(boolean includeNavyThreat, int maxTurnsRange) {
	    if (pathUnit.isNaval()) {
	        if (includeNavyThreat) {
	            costDecider = navyWithoutThreatCostDecider;
	        } else {
	            costDecider = navyCostDecider;
	        }
	    } else {
	        costDecider = baseCostDecider;
	    }
	    if (maxTurnsRange != INFINITY) {
	    	maxTurnRangeCostDecider.init(maxTurnsRange, costDecider);
	    	costDecider = maxTurnRangeCostDecider;
		}
	}

	private Path find() {
		this.startTiles.clear();
		this.startTiles.add(startTile);
		calculatePaths();

		if (reachedGoalNode != null) {
			return createPath(reachedGoalNode);
		} else {
			return createPath(oneOfTheBest);
		}
	}

	private void calculatePaths() {
		resetFinderBeforeSearching(map);
		for (Tile tile : startTiles) {
			Node currentNode = grid.get(tile.x, tile.y);
			currentNode.reset(pathUnit.movesLeft, 0);
			currentNode.turns = 0;
			nodes.add(currentNode);
		}
		startTiles.clear();

		costDecider.init(map, pathUnit.unitMove);

		oneOfTheBest = null;
		reachedGoalNode = null;

		Node currentNode;
		int iDirections = 0;
		int nDirections = Direction.values().length;

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

				MoveType moveType = pathUnit.unitMove.calculateMoveType(currentNode.tile, moveNode.tile);
				if (goalDecider.hasGoalReached(moveNode)) {
					if (
						((moveType != MoveType.ENTER_SETTLEMENT_WITH_CARRIER_AND_GOODS
						&& moveType != MoveType.MOVE_NO_ACCESS_GOODS)
						|| costDecider.isAllowCarrierEnterWithGoods()) && moveType != MoveType.MOVE_NO_ACCESS_LAND
					) {
						reachedGoalNode = moveNode;
						// change moveType to default move. Sometimes goal can be indian settlement
						// and moveType should be used only to find path
						moveType = MoveType.MOVE;
					}
				}

				if (costDecider.calculateAndImproveMove(currentNode, moveNode, moveType, moveDirection)) {
					if (oneOfTheBest == null || moveNode.hasBetterCostThen(oneOfTheBest)) {
						// TODO: remove - no sense, always next to start tile because everyone has greater cost
						oneOfTheBest = moveNode;
					}
					nodes.add(moveNode);
					if (tileConsumer.consume(moveNode.tile, moveNode.turns) == TileConsumer.Status.END) {
						nodes.clear();
						break;
					}
				} else {
					if (!moveType.isProgress() && costDecider.isMoveImproved()) {
						if (tileConsumer.consume(moveNode.tile, moveNode.turns) == TileConsumer.Status.END) {
							nodes.clear();
							break;
						}
					}
					if (costDecider.isMarkDestTileAsUnaccessible(currentNode, moveNode, moveType)) {
						moveNode.noMove = true;
					}
				}
			}
		}
		pathUnitFactory.free(pathUnit);
		pathUnit = null;
	}

	public Path createPath(int cellIndex) {
		Node node = grid.get(cellIndex);
		return createPath(node);
	}

	public Path createPath(Tile destTile) {
		Node destNode = grid.get(destTile.x, destTile.y);
		return createPath(destNode);
	}

	private Path createPath(final Node endPathNode) {
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
				startTile, startTile,
				0, false
			);  
		}
		
		Path path = new Path(
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
		if (grid == null || !grid.isSizeEquals(map.width, map.height)) {
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

	public int totalCost(Tile tile) {
		return grid.get(tile.x, tile.y).totalCost;
	}

	public int turnsCost(int cellIndex) {
	    return grid.get(cellIndex).turns;
	}

	public int turnsCost(Tile tile) {
		return grid.get(tile.x, tile.y).turns;
	}

	public boolean isTurnCostAbove(Tile tile, int turns) {
		int distance = grid.get(tile.x, tile.y).turns;
		return distance == PathFinder.INFINITY || distance > turns;
	}

	public void printCost(MapTileDebugInfo mapTileDebugInfo) {
		int cost;
	    for (int i=0; i<grid.getMaxCellIndex(); i++) {
			cost = totalCost(i);
			if (cost != INFINITY) {
				mapTileDebugInfo.str(grid.toX(i), grid.toY(i), Integer.toString(cost));
			}
	    }
	}

	public void printTurnCost(MapTileDebugInfo mapTileDebugInfo) {
		int cost;
	    for (int i=0; i<grid.getMaxCellIndex(); i++) {
			cost = turnsCost(i);
			if (cost != INFINITY) {
				mapTileDebugInfo.str(grid.toX(i), grid.toY(i), Integer.toString(cost));
			}
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

	public void printSumTurnCost(PathFinder b, SumPolicy sumPolicy, MapTileDebugInfo mapTileDebugInfo) {
		checkMapSizes(this, b);

		int aCost;
		int bCost;
		int sum;
		for (int cellIndex = 0; cellIndex < grid.getMaxCellIndex(); cellIndex++) {
			aCost = turnsCost(cellIndex);
			bCost = b.turnsCost(cellIndex);
			if (aCost == INFINITY || bCost == INFINITY) {
				continue;
			}
			sum = sumPolicy.sum(aCost, bCost);
			mapTileDebugInfo.str(grid.toX(cellIndex), grid.toY(cellIndex), Integer.toString(sum));
		}
	}

	public Tile findFirstTheBestSumTurnCost(PathFinder b, SumPolicy sumPolicy) {
		checkMapSizes(this, b);
		int theBestSum = INFINITY;
		Node theBestNode = null;

		int aCost;
		int bCost;
		int sum;
		for (int cellIndex = 0; cellIndex < grid.getMaxCellIndex(); cellIndex++) {
			aCost = turnsCost(cellIndex);
			bCost = b.turnsCost(cellIndex);
			if (aCost == INFINITY || bCost == INFINITY) {
				continue;
			}
			sum = sumPolicy.sum(aCost, bCost);
			if (sum < theBestSum) {
				theBestSum = sum;
				theBestNode = grid.get(cellIndex);
			}
		}
		if (theBestNode == null) {
			return null;
		}
		return theBestNode.tile;
	}

	private void checkMapSizes(PathFinder a, PathFinder b) {
		if (!a.grid.isSizeEquals(b.grid)) {
			throw new IllegalStateException(String.format(
				"grid sizes not equals: first [%d, %d] second [%d, %d]",
				a.grid.width, a.grid.height,
				b.grid.width, b.grid.height
			));
		}
	}
}
