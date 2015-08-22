package promitech.colonization;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import promitech.colonization.actors.MapActor;
import promitech.colonization.actors.MapDrawModel;
import promitech.colonization.actors.UnitDislocationAnimation;
import promitech.colonization.actors.UnitDislocationAnimation.EndOfAnimation;
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
			mapActor.mapDrawModel().selectedUnit = activeUnit;
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
			if (mapDrawModel.selectedUnit != null) {
				mapDrawModel.selectedTile = mapDrawModel.selectedUnit.getTile();
			} else {
				Point p = mapActor.getCenterOfScreen();
				mapDrawModel.selectedTile = game.map.getTile(p.x, p.y);
			}
			mapActor.mapDrawModel().selectedUnit = null;
		} else {
			viewMode = false;
			mapDrawModel.selectedTile = null;
			mapDrawModel.selectedUnit = activeUnit;
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
			mapDrawModel.selectedUnit = null;
		} else {
			mapDrawModel.selectedTile = null;
			Tile tile = game.map.getTile(p.x, p.y);

			if (!tile.hasSettlement()) {
				Unit first = tile.units.first();
				if (first != null && first.isOwner(game.playingPlayer)) {
					mapDrawModel.selectedUnit = tile.units.first();
					activeUnit = mapDrawModel.selectedUnit;
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
			if (mapDrawModel.selectedUnit == null) {
				return;
			}
			Unit unit = mapDrawModel.selectedUnit;
			Tile sourceTile = unit.getTile();
			Tile descTile = game.map.getTile(sourceTile.x, sourceTile.y, direction);
			
			MoveType moveType = unit.getMoveType(sourceTile, descTile);
			System.out.println("moveType = " + moveType);
			
			if (moveType == MoveType.MOVE) {
				unit.setState(UnitState.ACTIVE);
				unit.setStateToAllChildren(UnitState.SENTRY);
				int moveCost = unit.getMoveCost(sourceTile, descTile, direction, unit.getMovesLeft());
				System.out.println("moveLeft = " + unit.getMovesLeft() + ", moveCost = " + moveCost);
				unit.setMovesLeft(unit.getMovesLeft() - moveCost);
				
				unit.changeLocation(descTile);
				
				if (mapActor.isTileOnScreenEdge(unit.getTile())) {
					mapActor.centerCameraOnTile(unit.getTile());
				}
				blockUserInteraction = true;
				mapActor.startUnitDislocationAnimation(
					unit, 
					sourceTile, descTile,
					endOfUnitDislocationAnimation
				);
			}
		}
	}
	
	private UnitDislocationAnimation.EndOfAnimation endOfUnitDislocationAnimation = new EndOfAnimation() {
		@Override
		public void end(Unit unit) {
			blockUserInteraction = false;
			boolean exloredNewTiles = game.playingPlayer.revealMapAfterUnitMove(game.map, unit);
			if (exloredNewTiles) {
				mapActor.resetUnexploredBorders();
			}
		}
	};
}
