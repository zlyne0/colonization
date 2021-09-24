package net.sf.freecol.common.model.map.path;

import java.util.Comparator;
import java.util.TreeSet;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitMoveType;

import promitech.colonization.Direction;
import promitech.map.Object2dArray;

public class TransportPathFinder {
	
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
	private GoalDecider pathToTileGoalDecider = new GoalDecider() {
	    @Override
	    public boolean hasGoalReached(Node moveNode) {
	        return endTile.getId().equals(moveNode.tile.getId());
	    }
	};
	
	private Object2dArray<Node> grid; 
	private final TreeSet<Node> nodes = new TreeSet<Node>(NODE_WEIGHT_COMPARATOR);
	
	private final CostDecider baseCostDecider = new CostDecider();
	private final NavyCostDecider navyCostDecider = new NavyCostDecider();
	private CostDecider costDecider;
	private GoalDecider goalDecider = pathToTileGoalDecider;

	private Map map;
	private Tile startTile;
	private Tile endTile;
	private Unit moveUnit;
	private final UnitMoveType unitMoveType = new UnitMoveType();
	
	public TransportPathFinder(Map map) {
	    this.map = map;
	}
	
	public Path findToTile(
		final Tile sourceTile,
		final Tile findDestTile,
		final Unit landUnit,
		final Unit potentialTransporter,
		final PathFinder transporterRangeMap
	) {
	    this.startTile = sourceTile;
	    this.endTile = findDestTile;
	    if (sourceTile.getType().isWater()) {
	    	this.moveUnit = potentialTransporter;
	    } else {
	    	this.moveUnit = landUnit;
	    }
	    this.unitMoveType.init(moveUnit);
        this.navyCostDecider.avoidUnexploredTiles = false;
        this.baseCostDecider.avoidUnexploredTiles = false;
	
		resetFinderBeforeSearching(map);
		lowerGridIntegerMaxValue(transporterRangeMap.grid);
		
		navyCostDecider.init(map, potentialTransporter);
		baseCostDecider.init(map, landUnit);
		
		int iDirections = 0, nDirections = Direction.values().length;
		
		Node currentNode = grid.get(startTile.x, startTile.y);
		currentNode.reset(moveUnit.getMovesLeft(), 0);
		currentNode.turns = 0;
		nodes.add(currentNode);

		Node reachedGoalNode = null;
		
		while (true) {
			currentNode = nodes.pollFirst();
			if (currentNode == null) {
				break;
			}
			if (reachedGoalNode != null && reachedGoalNode.hasBetterCostThen(currentNode)) {
				break;
			}
			if (currentNode.tile.getType().isWater()) {
				moveUnit = potentialTransporter;
				unitMoveType.init(moveUnit);
				costDecider = navyCostDecider;
			} else {
				moveUnit = landUnit;
				unitMoveType.init(landUnit);
				costDecider = baseCostDecider;
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
				
				if (goalDecider.hasGoalReached(moveNode)) {
					reachedGoalNode = moveNode;
				}
				
				MoveType moveType = unitMoveType.calculateMoveType(currentNode.tile, moveNode.tile);
				if (moveType == MoveType.MOVE_NO_ACCESS_EMBARK || moveType == MoveType.EMBARK) {
					costDecider.getCost(currentNode.tile, moveNode.tile, currentNode.unitMovesLeft, moveType, moveDirection);
					costDecider.costMovesLeft = navyCostDecider.unitInitialMoves;
					if (currentNode.unitMovesLeft == 0) {
                        costDecider.costNewTurns = 1;
					} else {
					    costDecider.costNewTurns = 0;
					}
					
					Node carrierRangeNode = transporterRangeMap.grid.get(moveTile.x, moveTile.y);
					if (costDecider.improveMove(currentNode, moveNode, carrierRangeNode.totalCost, carrierRangeNode.turns)) {
						nodes.add(moveNode);
					}
				} else {
					if (moveType == MoveType.MOVE_NO_ACCESS_LAND || moveType == MoveType.DISEMBARK) {
						costDecider.getCost(currentNode.tile, moveNode.tile, currentNode.unitMovesLeft, moveType, moveDirection);
						costDecider.costMovesLeft = 0;
						costDecider.costNewTurns = 1;
						if (costDecider.improveMove(currentNode, moveNode)) {
							nodes.add(moveNode);
						}
					} else {
						if (costDecider.calculateAndImproveMove(currentNode, moveNode, moveType, moveDirection)) {
							nodes.add(moveNode);
						}
					}
				}
			}
		}
		if (reachedGoalNode == null) {
			return createPath(landUnit, grid.get(startTile.x, startTile.y));
		} else {
			return createPath(landUnit, reachedGoalNode);
		}
	}

	private void lowerGridIntegerMaxValue(Object2dArray<Node> grid) {
		for (int i = 0; i < grid.getMaxCellIndex(); i++) {
			Node node = grid.get(i);
			if (node.totalCost == Integer.MAX_VALUE) {
				node.totalCost = Integer.MAX_VALUE / 2;
			}
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

		Path path = new Path(moveUnit, startTile, endPathNode.tile, count, 
			endTile == null || endPathNode.tile.equalsCoordinates(endTile)
		);
		n = begining;
		while (n != null) {
			path.add(n.tile, n.turns);
			n = n.next;
		}
		return path;
	}

	private void resetFinderBeforeSearching(Map map) {
		if (grid == null) {
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
	
	private int totalCost(int cellIndex) {
		return grid.get(cellIndex).totalCost;
	}
	
	private int turnsCost(int cellIndex) {
	    return grid.get(cellIndex).turns;
	}

	public void totalCostToStringArrays(String[][] strTab) {
		int v;
	    for (int i=0; i<grid.getMaxCellIndex(); i++) {
	    	v = totalCost(i);
	    	if (v != Integer.MAX_VALUE) {
	    		strTab[grid.toY(i)][grid.toX(i)] = Integer.toString(v);
	    	}
	    }
	}

	public void turnCostToStringArrays(String[][] strTab) {
		int v;
	    for (int i=0; i<grid.getMaxCellIndex(); i++) {
	    	v = turnsCost(i);
	    	if (v != Integer.MAX_VALUE) {
	    		strTab[grid.toY(i)][grid.toX(i)] = Integer.toString(v);
	    	}
	    }
	}
	
	public void toStringArrays(String[][] strTab) {
	    int v;
        for (int i=0; i<grid.getMaxCellIndex(); i++) {
            v = turnsCost(i);
            if (v != Integer.MAX_VALUE) {
                strTab[grid.toY(i)][grid.toX(i)] = Integer.toString(v) + " - " + Integer.toString(totalCost(i)) + " - " + Integer.toString(grid.get(i).unitMovesLeft);
            }
        }
	}

    public void toStringArrays(String[][] strTab, Path path) {
        for (int i=0; i<path.tiles.size; i++) {
            int x = path.tiles.get(i).x;
            int y = path.tiles.get(i).y;
            
            Node node = grid.get(x, y);
            strTab[y][x] = Integer.toString(node.turns) + " - " + Integer.toString(node.totalCost) + " - " + Integer.toString(node.unitMovesLeft);
        }
    }

}
