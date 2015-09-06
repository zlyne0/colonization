package promitech.colonization;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import promitech.colonization.actors.MapActor;
import promitech.colonization.actors.MapDrawModel;
import promitech.colonization.actors.UnitDislocationAnimation;
import promitech.colonization.actors.UnitDislocationAnimation.EndOfAnimationListener;
import promitech.colonization.gamelogic.MoveContext;
import promitech.colonization.gamelogic.MoveType;
import promitech.colonization.gamelogic.UnitIterator;
import promitech.colonization.math.Point;

public class GameController {

	private final Game game;
	private final MapActor mapActor;
	
	private final UnitIterator unitIterator;
	private Unit activeUnit;
	private boolean viewMode = false;
	private GameLogic gameLogic = new GameLogic();
	private boolean blockUserInteraction = false;
	
	public GameController(final Game game, MapActor mapActor) {
		this.game = game;
		this.mapActor = mapActor;
		
		unitIterator = new UnitIterator(game.playingPlayer, new Unit.ActivePredicate());
	}

	public void nextActiveUnit() {
		if (blockUserInteraction) {
			return;
		}
		
		if (unitIterator.hasNext()) {
			activeUnit = unitIterator.next();
			mapActor.mapDrawModel().setSelectedUnit(activeUnit);
			if (activeUnit != null) {
				mapActor.centerCameraOnTile(activeUnit.getTile());
			} 
		}
	}

	public void setViewMode(boolean checked) {
		if (blockUserInteraction) {
			return;
		}
		
		MapDrawModel mapDrawModel = mapActor.mapDrawModel();
		if (checked) {
			viewMode = true;
			if (mapDrawModel.getSelectedUnit() != null) {
				mapDrawModel.selectedTile = mapDrawModel.getSelectedUnit().getTile();
			} else {
				Point p = mapActor.getCenterOfScreen();
				mapDrawModel.selectedTile = game.map.getTile(p.x, p.y);
			}
			mapActor.mapDrawModel().setSelectedUnit(null);
		} else {
			viewMode = false;
			mapDrawModel.selectedTile = null;
			mapDrawModel.setSelectedUnit(activeUnit);
			if (activeUnit != null) {
				mapActor.centerCameraOnTile(activeUnit.getTile());
			} 
		}
	}

	public void clickOnTile(Point p) {
		if (blockUserInteraction) {
			return;
		}
		
		MapDrawModel mapDrawModel = mapActor.mapDrawModel();
		
		if (viewMode) {
			mapDrawModel.selectedTile = game.map.getTile(p.x, p.y);
			mapDrawModel.setSelectedUnit(null);
		} else {
			mapDrawModel.selectedTile = null;
			Tile tile = game.map.getTile(p.x, p.y);

			if (!tile.hasSettlement()) {
				Unit newSelectedUnit = tile.units.first();
				if (newSelectedUnit != null && newSelectedUnit.isOwner(game.playingPlayer)) {
					mapDrawModel.setSelectedUnit(newSelectedUnit);
					activeUnit = newSelectedUnit;
				}
			}
		}
	}

	public void pressDirectionKey(Direction direction) {
		if (blockUserInteraction) {
			return;
		}
		
		MapDrawModel mapDrawModel = mapActor.mapDrawModel();
		if (viewMode) {
			int x = direction.stepX(mapDrawModel.selectedTile.x, mapDrawModel.selectedTile.y);
			int y = direction.stepY(mapDrawModel.selectedTile.x, mapDrawModel.selectedTile.y);
			mapDrawModel.selectedTile = game.map.getTile(x, y);
			
			if (mapActor.isTileOnScreenEdge(mapDrawModel.selectedTile)) {
				mapActor.centerCameraOnTile(mapDrawModel.selectedTile);
			}
		} else {
			if (mapDrawModel.getSelectedUnit() == null) {
				return;
			}
			
			MoveContext moveContext = new MoveContext();
			moveContext.direction = direction;
			moveContext.unit = mapDrawModel.getSelectedUnit();
			moveContext.sourceTile = moveContext.unit.getTile();
			moveContext.descTile = game.map.getTile(moveContext.sourceTile.x, moveContext.sourceTile.y, direction);
			
			MoveType moveType = moveContext.unit.getMoveType(moveContext.sourceTile, moveContext.descTile);
			moveContext.moveType = moveType;
			System.out.println("moveType = " + moveType);
			
			switch (moveType) {
				case MOVE: {
					moveUnit(moveContext);
					
					guiMoveInteraction(moveContext);
				} break;
				case EMBARK: {
					mapActor.mapDrawModel().setSelectedUnit(null);
					activeUnit = null;

					embarkUnit(moveContext);

					guiMoveInteraction(moveContext);
				} break;
				default: {
					System.out.println("not handled move type: " + moveType);
				}
			}
		}
	}

	private void guiMoveInteraction(MoveContext moveContext) {
		if (mapActor.isTileOnScreenEdge(moveContext.descTile)) {
			mapActor.centerCameraOnTile(moveContext.descTile);
		}
		blockUserInteraction = true;
		endOfUnitDislocationAnimation.moveContext = moveContext;
		mapActor.startUnitDislocationAnimation(moveContext, endOfUnitDislocationAnimation);
	}
	
	private void moveUnit(MoveContext moveContext) {
		moveContext.unit.setState(UnitState.ACTIVE);
		moveContext.unit.setStateToAllChildren(UnitState.SENTRY);
		int moveCost = moveContext.unit.getMoveCost(moveContext.sourceTile, moveContext.descTile, moveContext.direction);
		System.out.println("moveLeft = " + moveContext.unit.getMovesLeft() + ", moveCost = " + moveCost);
		moveContext.unit.reduceMovesLeft(moveCost);
		moveContext.unit.changeLocation(moveContext.descTile);
	}
	
	private void embarkUnit(MoveContext moveContext) {
		Unit carrier = null;
		for (Unit u : moveContext.descTile.units.entities()) {
			if (u.canAddUnit(moveContext.unit)) {
				carrier = u;
				break;
			}
		}
		if (carrier == null) {
			throw new IllegalStateException("carrier unit unit should exists and check while generate moveType");
		}
		moveContext.unit.setState(UnitState.SKIPPED);
		moveContext.unit.changeLocation(carrier);
		moveContext.unit.reduceMovesLeftToZero();
	}
	
	private UnitDislocationAnimation.EndOfAnimationListener endOfUnitDislocationAnimation = new EndOfAnimationListener() {
		@Override
		public void end(Unit unit) {
			blockUserInteraction = false;
			if (MoveType.MOVE.equals(moveContext.moveType)) { 
				boolean exloredNewTiles = game.playingPlayer.revealMapAfterUnitMove(game.map, unit);
				if (exloredNewTiles) {
					mapActor.resetUnexploredBorders();
				}
			}
		}
	};
	
	public Specification getSpecification() {
		return game.specification;
	}
}
