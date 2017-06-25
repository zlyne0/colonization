package net.sf.freecol.common.model.map.t;

import java.util.Comparator;
import java.util.TreeSet;

import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.Path;
import net.sf.freecol.common.model.specification.Ability;
import promitech.colonization.Direction;
import promitech.colonization.gamelogic.MoveType;
import promitech.map.Object2dArray;

// cost for move by civilian unit into foreign settlement is delt by moveType
// the same case for move to foreign unit
// the same case for defender unit
class CostDecider {
    private static final int TURN_FACTOR = 100;
	private static final int ILLEGAL_MOVE_COST = -1;

	protected Map map;
	protected Unit moveUnit;
	protected int unitInitialMoves;
	protected boolean moveUnitPiracy = false;

	protected int costNewTurns;
	protected int costMovesLeft;
	
	protected int moveCost;
	private int newTotalPathCost;
	protected boolean avoidUnexploredTiles = true;

    void init(Map map, Unit moveUnit) {
        this.map = map;
        this.unitInitialMoves = moveUnit.getInitialMovesLeft();
        this.moveUnit = moveUnit;
        moveUnitPiracy = moveUnit.unitType.hasAbility(Ability.PIRACY);
    }
	
    /**
     * @return boolean - return true when can improve move
     */
	boolean calculateAndImproveMove(Node currentNode, Node moveNode, MoveType moveType, Direction moveDirection) {
        if (isMoveIllegal(moveNode.tile, moveType)) {
        	return false;
        }
		getCost(currentNode.tile, moveNode.tile, currentNode.unitMovesLeft, moveType, moveDirection);
		return improveMove(currentNode, moveNode);
	}
	
	protected boolean improveMove(Node currentNode, Node moveNode) {
	    newTotalPathCost = TURN_FACTOR * (currentNode.turns + costNewTurns) + moveCost + currentNode.totalCost;
	    if (moveNode.totalCost > newTotalPathCost) {
		    moveNode.totalCost = newTotalPathCost;
	        moveNode.unitMovesLeft = costMovesLeft;
	        moveNode.turns = currentNode.turns + costNewTurns;
            moveNode.preview = currentNode;
	        return true;
	    }
	    return false;
	}
	
	boolean isMarkDestTileAsUnaccessible(Node source, Node dest, MoveType moveType) {
		return isMoveIllegal();
	}
	
	boolean isMoveIllegal() {
		return moveCost == ILLEGAL_MOVE_COST;
	}
	
	protected void getCost(Tile oldTile, Tile newTile, final int movesLeftBefore, MoveType moveType, Direction moveDirection) {
		int cost = moveUnit.getMoveCost(oldTile, newTile, moveDirection, movesLeftBefore);
		if (cost > movesLeftBefore) {
            final int moveCostNextTurn = moveUnit.getMoveCost(oldTile, newTile, moveDirection, unitInitialMoves);
            cost = movesLeftBefore + moveCostNextTurn;
            costMovesLeft = unitInitialMoves - moveCostNextTurn;
            costNewTurns = 1;
		} else {
			costMovesLeft = movesLeftBefore - cost;
			costNewTurns = 0;
		}
		moveCost = cost;
	}
	
	protected boolean isMoveIllegal(Tile newTile, MoveType moveType) {
		if (MoveType.ENTER_SETTLEMENT_WITH_CARRIER_AND_GOODS.equals(moveType)) {
			if (newTile.getSettlement() != null && !newTile.getSettlement().getOwner().equalsId(moveUnit.getOwner())) {
				moveCost = ILLEGAL_MOVE_COST;
				return true;
			}
		} else {
			// consider only moves without actions, actions can only happen on find path goal
			if (!moveType.isProgress()) {
				moveCost = ILLEGAL_MOVE_COST;
				return true;
			}
		}
		
	    if (avoidUnexploredTiles && moveUnit.getOwner().isTileUnExplored(newTile)) {
	        moveCost = ILLEGAL_MOVE_COST;
	        return true;
	    }
	    return false;
	}
}

class NavyCostDecider extends CostDecider {
    
    private boolean moveToSeaside = false;
    
    @Override
    boolean calculateAndImproveMove(Node currentNode, Node moveNode, MoveType moveType, Direction moveDirection) {
        moveToSeaside = false;
        if ((moveType == MoveType.DISEMBARK || moveType == MoveType.MOVE_NO_ACCESS_LAND) && currentNode.tile.getType().isWater()) {
            moveType = MoveType.MOVE;
            moveToSeaside = true;
        }
        // check whether tile accessible
        // if moveNode.tile is land moveType == MoveType.MOVE_NO_ACCESS_LAND
        if (isMoveIllegal(moveNode.tile, moveType)) {
        	return false;
        }
        // check whether tile can be bombarded by colony or hostile ship
        if (isTileThreatForUnit(moveNode)) {
            // when there is threat it use all moves points
            moveCost = currentNode.unitMovesLeft;
        } else {
            // tile is not bombarded so use common move cost
            getCost(currentNode.tile, moveNode.tile, currentNode.unitMovesLeft, moveType, moveDirection);
        }
        boolean improveMove = improveMove(currentNode, moveNode);
        if (moveToSeaside) {
            // return false so no process other tiles from source tile (currentNode)
            return false;
        }
        return improveMove;
    }

	boolean isMarkDestTileAsUnaccessible(Node source, Node dest, MoveType moveType) {
		if (moveType == MoveType.MOVE_NO_ACCESS_LAND && source.tile.hasSettlement() && dest.tile.getType().isLand()) {
			return false;
		}
		return isMoveIllegal();
	}
    
    private boolean isTileThreatForUnit(Node moveNode) {
        if (moveNode.tileBombardedMetaData) {
            return moveNode.tileBombarded;
        }
        moveNode.tileBombardedMetaData = true;
        
        for (int i=0; i<Direction.values().length; i++) {
            Direction direction = Direction.values()[i];
            Tile neighbourToMoveTile = map.getTile(moveNode.tile, direction);
            if (neighbourToMoveTile == null) {
            	continue;
            }
            if (neighbourToMoveTile.hasSettlement()) {
                if (neighbourToMoveTile.isColonyOnTileThatCanBombardNavyUnit(moveUnit.getOwner(), moveUnitPiracy)) {
                    costMovesLeft = 0;
                    costNewTurns = 1;
                    
                    moveNode.tileBombarded = true;
                    return true;
                }
            } else {
                boolean useAllMove = neighbourToMoveTile.isTileHasNavyUnitThatCanBombardUnit(moveUnit.getOwner(), moveUnitPiracy);
                if (useAllMove) {
                    costMovesLeft = 0;
                    costNewTurns = 1;
                    moveNode.tileBombarded = true;
                    return true;
                }                
            }
        }
        moveNode.tileBombarded = false;
        return false;
    }
}

class Node {	
    int totalCost;
	int turns;
	int unitMovesLeft;
	boolean noMove;
	boolean tileBombardedMetaData = false; // whether meta data set
	boolean tileBombarded = false;
	Node preview;
	Node next;
	
	final Tile tile;
	
	public Node(Tile tile) {
		this.tile = tile;
	}
    
	boolean hasBetterCostThen(Node node) {
		return totalCost < node.totalCost;
	}
	
	void reset(int unitMovesLeft, int totalCost) {
		this.totalCost = totalCost;
		turns = Integer.MAX_VALUE;
		this.unitMovesLeft = unitMovesLeft;
		this.noMove = false;
		this.preview = null;
		this.next = null;
		this.tileBombardedMetaData = false;
		this.tileBombarded = false;
	}
	
	public String toString() {
		return "[" + tile.x + "," + tile.y + "], unitMovesLeft = " + unitMovesLeft + ", turns = " + turns + ", totalCost = " + totalCost;
	}
}

public class TransportPathFinder {
	
    interface GoalDecider {
        boolean hasGoalReached(Node moveNode);
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
	
	public TransportPathFinder() {
	}
	
	public Path findToTile(Map map, Tile sourceTile, Tile findDestTile, Unit landUnit, Unit potentialTransporter) {
	    this.map = map;
	    this.startTile = sourceTile;
	    this.endTile = findDestTile;
	    this.moveUnit = landUnit;
        this.navyCostDecider.avoidUnexploredTiles = false;
        this.baseCostDecider.avoidUnexploredTiles = false;
	
		resetFinderBeforeSearching(map);
		
		navyCostDecider.init(map, potentialTransporter);
		baseCostDecider.init(map, landUnit);
		costDecider = baseCostDecider;
		
		int iDirections = 0, nDirections = Direction.values().length;
		
		Node currentNode = grid.get(startTile.x, startTile.y);
		currentNode.reset(moveUnit.getMovesLeft(), 0);
		currentNode.turns = 0;
		nodes.add(currentNode);

		Node reachedGoalNode = null;
		
		int iterationDebug = 0;
		Direction[] dirDebug = new Direction[] {
			Direction.W,
			Direction.SW,
			Direction.S
		};
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
				costDecider = navyCostDecider;
			} else {
				moveUnit = landUnit;
				costDecider = baseCostDecider;
			}
			
//			if (iterationDebug >= dirDebug.length) {
//				break;
//			} else {
//				iterationDebug++;
//			}
			
			for (iDirections=0; iDirections<nDirections; iDirections++) {
				Direction moveDirection = Direction.values()[iDirections];
				
//				if (dirDebug[iterationDebug-1] != moveDirection) {
//					continue;
//				}
				
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
				
				MoveType moveType = moveUnit.getMoveType(currentNode.tile, moveNode.tile);
				if (moveType == MoveType.EMBARK || moveType == MoveType.MOVE_NO_ACCESS_EMBARK) {
					costDecider.getCost(currentNode.tile, moveNode.tile, currentNode.unitMovesLeft, moveType, moveDirection);
					costDecider.costMovesLeft = navyCostDecider.unitInitialMoves;
					costDecider.costNewTurns = 0;
					if (costDecider.improveMove(currentNode, moveNode)) {
						nodes.add(moveNode);
					}
				} else {
					if (moveType == MoveType.MOVE_NO_ACCESS_LAND) {
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
			return createPath(landUnit, sourceTile, grid.get(sourceTile.x, sourceTile.y));
		} else {
			return createPath(landUnit, sourceTile, reachedGoalNode);
		}
	}
	
	private Path createPath(final Unit moveUnit, final Tile startTile, final Node endPathNode) {
		Node begining = null;
		Node n = endPathNode;
		int count = 1;
		while (n != null) {
			n.next = begining;
			begining = n;
			n = n.preview;
			count++;
		}

		Path path = new Path(moveUnit, startTile, endPathNode.tile, count);
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

}
