package net.sf.freecol.common.model.map;

import java.util.Comparator;
import java.util.TreeSet;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.specification.Ability;
import promitech.colonization.Direction;
import promitech.colonization.gamelogic.MoveContext;
import promitech.colonization.gamelogic.MoveType;

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
	    newTotalPathCost = TURN_FACTOR * (currentNode.turns + costNewTurns) + moveCost;
	    if (moveNode.totalCost > newTotalPathCost) {
	        moveNode.totalCost = newTotalPathCost;
	        moveNode.unitMovesLeft = costMovesLeft;
	        moveNode.turns = currentNode.turns + costNewTurns;
	        return true;
	    }
	    return false;
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
	    if (!MoveType.ENTER_SETTLEMENT_WITH_CARRIER_AND_GOODS.equals(moveType)) {
	        // consider only moves without actions, actions can only happen on find path goal
	        if (!moveType.isProgress()) {
	            moveCost = ILLEGAL_MOVE_COST;
	            return true;
	        }
	    }
	    if (moveUnit.getOwner().isTileUnExplored(newTile)) {
	        moveCost = ILLEGAL_MOVE_COST;
	        return true;
	    }
	    return false;
	}
}

class NavyCostDecider extends CostDecider {
    
    @Override
    boolean calculateAndImproveMove(Node currentNode, Node moveNode, MoveType moveType, Direction moveDirection) {
        // check whether tile accessible
        // if moveNode.tile is land moveType == MoveType.MOVE_NO_ACCESS_LAND
        if (isMoveIllegal(moveNode.tile, moveType)) {
        	return false;
        }
        // check whether tile can be bombarded by colony or hostile ship
        if (isTileThreatForUnit(moveNode.tile)) {
            // when there is threat it use all moves points
            moveCost = currentNode.unitMovesLeft;
        } else {
            // tile is not bombarded so use common move cost
            getCost(currentNode.tile, moveNode.tile, currentNode.unitMovesLeft, moveType, moveDirection);
        }
        return improveMove(currentNode, moveNode);
    }

    public boolean isTileThreatForUnit(Tile moveTile) {
        for (int i=0; i<Direction.values().length; i++) {
            Direction direction = Direction.values()[i];
            Tile neighbourToMoveTile = map.getTile(moveTile.x, moveTile.y, direction);
            if (neighbourToMoveTile.hasSettlement()) {
                if (isTileHasBombardedColony(neighbourToMoveTile)) {
                    costMovesLeft = 0;
                    costNewTurns = 1;
                    return true;
                }
            } else {
                boolean useAllMove = hasTileBombardedUnit(neighbourToMoveTile);
                if (useAllMove) {
                    costMovesLeft = 0;
                    costNewTurns = 1;
                    return true;
                }                
            }
        }
        return false;
    }
    
    // TODO: move method to Tile
    private boolean hasTileBombardedUnit(Tile tile) {
        if (tile.units.isEmpty()) {
            return false;
        }
        for (Unit unit : tile.units.entities()) {
            if (unit.getOwner().equalsId(moveUnit.getOwner())) {
                // TODO: ustawienie metadanych ze brak zagrozenia
                // jesli brak jakichkolwiek jednostek to tez brak zagrozenia
                break;
            }
            if (moveUnitPiracy) {
                if (unit.isOffensiveUnit()) {
                    return true;
                }
            } else {
                if ((unit.isOffensiveUnit() && moveUnit.getOwner().atWarWith(unit.getOwner())) || unit.unitType.hasAbility(Ability.PIRACY) ) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // TODO: move method to Tile
    private boolean isTileHasBombardedColony(Tile tile) {
        if (!tile.hasSettlement()) {
            return false;
        }
        Settlement settlement = tile.getSettlement();
        if (settlement.getOwner().equalsId(moveUnit.getOwner())) {
            return false;
        }
        if (settlement.canBombardEnemyShip() && (settlement.getOwner().atWarWith(moveUnit.getOwner()) || moveUnitPiracy)) {
            return true;
        }
        return false;
    }
    
}

class Node {	
    int totalCost;
	int turns;
	int unitMovesLeft;
	boolean noMove;
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
		turns = 0;
		this.unitMovesLeft = unitMovesLeft;
		this.noMove = false;
		this.preview = null;
		this.next = null;
	}
	
	public String toString() {
		return "[" + tile.x + "," + tile.y + "], turns = " + turns + ", totalCost = " + totalCost;
	}
}

public class PathFinder {
	
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
	
	private Node grid[][];
	private final TreeSet<Node> nodes = new TreeSet<Node>(NODE_WEIGHT_COMPARATOR);
	private CostDecider costDecider = new CostDecider();

	public PathFinder() {
	}
	
	public Path find(Map map, Tile startTile, Tile endTile, Unit moveUnit) {
		resetFinderBeforeSearching(map);
		
		int iDirections = 0, nDirections = Direction.values().length;
		costDecider.init(map, moveUnit);
		
		Node currentNode = grid[startTile.y][startTile.x];
		currentNode.reset(moveUnit.getMovesLeft(), 0);
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
				
				Tile moveTile = map.getTile(currentNode.tile.x, currentNode.tile.y, moveDirection);
				Node moveNode = grid[moveTile.y][moveTile.x];
				if (moveNode.noMove) {
					continue;
				}
				
				MoveType moveType = moveUnit.getMoveType(currentNode.tile, moveNode.tile);
				if (endTile.getId().equals(moveNode.tile.getId())) {
					reachedGoalNode = moveNode;
					// change moveType to default move. Sometimes goal can be indian settlement 
					// and moveType should be used only to find path
					moveType = MoveType.MOVE;
				}
				
				if (costDecider.calculateAndImproveMove(currentNode, moveNode, moveType, moveDirection)) {
					if (oneOfTheBest == null || moveNode.hasBetterCostThen(oneOfTheBest)) {
						oneOfTheBest = moveNode;
					}
					moveNode.preview = currentNode;
					nodes.add(moveNode);
				} else {
					if (costDecider.isMoveIllegal()) {
						moveNode.noMove = true;
					}
				}
			}
		}

		if (reachedGoalNode != null) {
			return createPath(startTile, reachedGoalNode);
		} else {
			return createPath(startTile, oneOfTheBest);
		}
	}
	
	private Path createPath(Tile startTile, final Node endPathNode) {
		Node begining = null;
		Node n = endPathNode;
		int count = 1;
		while (n != null) {
			n.next = begining;
			begining = n;
			n = n.preview;
			count++;
		}

		Path path = new Path(startTile, endPathNode.tile, count);
		n = begining;
		while (n != null) {
			path.add(n.tile, n.turns);
			n = n.next;
		}
		return path;
	}

	private void resetFinderBeforeSearching(Map map) {
		if (grid == null) {
			grid = new Node[map.height][map.width];
		}
		for (int y=0; y<map.height; y++) {
			for (int x=0; x<map.width; x++) {
				if (grid[y][x] == null) {
					grid[y][x] = new Node(map.getTile(x, y));
				}
				grid[y][x].reset(0, INFINITY);
			}
		}
		
		nodes.clear();
	}
	
}
