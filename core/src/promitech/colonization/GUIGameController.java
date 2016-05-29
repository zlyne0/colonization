package promitech.colonization;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.UnitIterator;
import net.sf.freecol.common.model.map.Path;
import net.sf.freecol.common.model.map.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import promitech.colonization.GUIGameModel.ChangeStateListener;
import promitech.colonization.actors.colony.ColonyApplicationScreen;
import promitech.colonization.actors.map.MapActor;
import promitech.colonization.actors.map.MapDrawModel;
import promitech.colonization.gamelogic.MoveContext;
import promitech.colonization.gamelogic.MoveType;
import promitech.colonization.math.Point;
import promitech.colonization.savegame.SaveGameParser;

public class GUIGameController {
	private class EndOfUnitDislocationAnimationAction extends RunnableAction {
		MoveContext moveContext;
		
		@Override
		public void run() {
			GUIGameController.this.onEndOfUnitDislocationAnimation(moveContext);
		}
	}

	private GameLogic gameLogic;
	private final GUIGameModel guiGameModel = new GUIGameModel();
	private Game game;
	
	private MapActor mapActor;
	private ApplicationScreenManager screenManager;
	
	private boolean blockUserInteraction = false;
	private PathFinder finder = new PathFinder();
	private final EndOfUnitDislocationAnimationAction endOfUnitDislocationAnimation = new EndOfUnitDislocationAnimationAction();
	
	public void initGameFromSavegame() throws IOException, ParserConfigurationException, SAXException {
        SaveGameParser saveGameParser = new SaveGameParser("maps/savegame_1600.xml");
        game = saveGameParser.parse();
        game.playingPlayer = game.players.getById("player:1");
        System.out.println("game = " + game);
        
        guiGameModel.unitIterator = new UnitIterator(game.playingPlayer, new Unit.ActivePredicate());
        
        gameLogic = new GameLogic(game);
	}
	
    public void setMapActor(MapActor mapActor) {
        this.mapActor = mapActor;
    }
	
    public void skipUnit() {
    	if (blockUserInteraction) {
    		return;
    	}
    	throwExceptionWhenActiveUnitNotSet();
    	Unit unit = guiGameModel.getActiveUnit();
    	unit.setState(UnitState.SKIPPED);
    	
    	logicNextActiveUnit();
    }
    
	public void nextActiveUnit() {
		if (blockUserInteraction) {
			return;
		}
		logicNextActiveUnit();
	}
	
	public void nextActiveUnitWhenActive(Unit unit) {
		if (guiGameModel.isActiveUnitSet() && guiGameModel.getActiveUnit().equalsId(unit)) {
			logicNextActiveUnit();
		}
	}
	
	private void logicNextActiveUnit() {
		if (guiGameModel.unitIterator.hasNext()) {
			Unit nextUnit = guiGameModel.unitIterator.next();
			changeActiveUnit(nextUnit);
			centerOnActiveUnit();
			if (nextUnit.isDestinationSet()) {
				logicAcceptGotoPath();
			}
		} else {
			mapActor.mapDrawModel().setSelectedUnit(null);
			mapActor.mapDrawModel().unitPath = null;
			guiGameModel.setActiveUnit(null);
		}
	}

	public void enterInViewMode() {
		if (blockUserInteraction) {
			return;
		}
		System.out.println("enterInViewMode");
		
		MapDrawModel mapDrawModel = mapActor.mapDrawModel();
		if (mapDrawModel.getSelectedUnit() != null) {
			mapDrawModel.selectedTile = mapDrawModel.getSelectedUnit().getTile();
		} else {
			Point p = mapActor.getCenterOfScreen();
			mapDrawModel.selectedTile = game.map.getTile(p.x, p.y);
		}
		guiGameModel.previewViewModeUnit = guiGameModel.getActiveUnit();
		guiGameModel.setActiveUnit(null);
		mapDrawModel.setSelectedUnit(null);
		guiGameModel.setViewMode(true);
	}

	public void leaveViewMode() {
		if (blockUserInteraction) {
			return;
		}
		System.out.println("leaveInViewMode");
		
		MapDrawModel mapDrawModel = mapActor.mapDrawModel();
		mapDrawModel.selectedTile = null;
		centerOnActiveUnit();
		guiGameModel.setActiveUnit(guiGameModel.previewViewModeUnit);
		mapDrawModel.setSelectedUnit(guiGameModel.previewViewModeUnit);
		guiGameModel.previewViewModeUnit = null;
		guiGameModel.setViewMode(false);
	}

	public void doubleClickOnTile(Point mapPoint) {
		if (blockUserInteraction) {
			return;
		}
		Tile tile = game.map.getTile(mapPoint.x, mapPoint.y);
		if (tile == null) {
			return;
		}
		if (tile.hasSettlement()) {
		    if (tile.getSettlement().isColony()) {
		        ColonyApplicationScreen colonyApplicationScreen = screenManager.getApplicationScreen(ApplicationScreenType.COLONY);
		        colonyApplicationScreen.initColony(tile.getSettlement().getColony(), tile);
		        screenManager.setScreen(ApplicationScreenType.COLONY);
		    }
		}
	}
	
	public void clickOnTile(Point p) {
	    clickOnTileDebugInfo(p);
	    
		if (blockUserInteraction) {
			return;
		}
		
		MapDrawModel mapDrawModel = mapActor.mapDrawModel();
		
		if (guiGameModel.isCreateGotoPathMode()) {
			Tile tile = game.map.getTile(p.x, p.y);
			generateGotoPath(tile);
			return;
		}
		
		if (guiGameModel.isViewMode()) {
			mapDrawModel.selectedTile = game.map.getTile(p.x, p.y);
			mapDrawModel.setSelectedUnit(null);
		} else {
			mapDrawModel.selectedTile = null;
			Tile tile = game.map.getTile(p.x, p.y);

			if (!tile.hasSettlement()) {
				Unit newSelectedUnit = tile.units.first();
				if (newSelectedUnit != null && newSelectedUnit.isOwner(game.playingPlayer)) {
					changeActiveUnit(newSelectedUnit);
				}
			}
		}
	}

	private void clickOnTileDebugInfo(Point p) {
        Tile tile = game.map.getTile(p.x, p.y);
        System.out.println("p = " + p + ", xml x=\"" + p.x + "\" y=\"" + p.y + "\"");
        if (tile != null) {
            System.out.println("tile: " + tile);
        } else {
            System.out.println("tile is null");
        }
        Object tileDrawModel = mapActor.mapDrawModel().getTileDrawModel(p.x, p.y);
        if (tileDrawModel != null) {
            System.out.println("drawmodel = " + tileDrawModel.toString());
        }
	}
	
	public void pressDirectionKey(Direction direction) {
		if (blockUserInteraction) {
			return;
		}
		
		MapDrawModel mapDrawModel = mapActor.mapDrawModel();
		if (guiGameModel.isViewMode()) {
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
			
			Unit selectedUnit = mapDrawModel.getSelectedUnit();
			Tile sourceTile = selectedUnit.getTile();
			
			Tile destTile = game.map.getTile(sourceTile.x, sourceTile.y, direction);
			MoveContext moveContext = new MoveContext(sourceTile, destTile, selectedUnit, direction);
			
			mapActor.mapDrawModel().unitPath = null;
			selectedUnit.clearDestination();
			System.out.println("moveContext = " + moveContext);
			
			if (moveContext.canHandleMove()) {
				moveContext.handleMove();
				guiMoveInteraction(moveContext);
			}
		}
	}

	private void guiMoveInteraction(MoveContext moveContext) {
		if (mapActor.isTileOnScreenEdge(moveContext.destTile)) {
			mapActor.centerCameraOnTile(moveContext.destTile);
		}
		blockUserInteraction = true;
		endOfUnitDislocationAnimation.moveContext = moveContext;
		mapActor.startUnitDislocationAnimation(moveContext, endOfUnitDislocationAnimation);
	}
	
	private void onEndOfUnitDislocationAnimation(MoveContext moveContext) {
		if (moveContext.isMoveType(MoveType.MOVE)) {
			boolean exloredNewTiles = game.playingPlayer.revealMapAfterUnitMove(game.map, moveContext.unit);
			if (exloredNewTiles) {
				mapActor.resetUnexploredBorders();
			}
		}
		
		if (moveContext.isMoveType(MoveType.EMBARK)) {
			mapActor.mapDrawModel().setSelectedUnit(null);
			guiGameModel.setActiveUnit(null);
		}
		
		if (moveContext.isMoveViaPath()) {
			if (game.map.isUnitSeeHostileUnit(moveContext.unit)) {
				blockUserInteraction = false;
				System.out.println("unit: " + moveContext.unit + " see hostile unit");
				return;
			}
			
			moveContext.initNextPathStep();
			System.out.println("moveContext = " + moveContext);
			if (moveContext.canHandleMove()) {
				moveContext.handleMove();
				guiMoveInteraction(moveContext);
			} else {
				if (moveContext.isEndOfPath()) {
					moveContext.unit.clearDestination();
					mapActor.mapDrawModel().unitPath = null;
				} else {
                    moveContext.unit.setState(UnitState.SKIPPED);
					logicNextActiveUnit();
				}
				blockUserInteraction = false;
			}
		} else {
			if (!moveContext.unit.couldMove()) {
				logicNextActiveUnit();
			}
			blockUserInteraction = false;
		}
	}
	
	public void closeColonyView(Colony colony) {
		screenManager.setScreen(ApplicationScreenType.MAP_VIEW);
		if (guiGameModel.isActiveUnitSet()) {
			if (colony.isUnitInColony(guiGameModel.getActiveUnit())) {
				changeActiveUnit(null);
			}
		}
	}

	public void closeColonyViewAndActiveUnit(Colony colony, Unit unit) {
		closeColonyView(colony);
		unit.setState(UnitState.ACTIVE);
		changeActiveUnit(unit);
	}
	
	void changeActiveUnit(Unit unit) {
		guiGameModel.setActiveUnit(unit);
		mapActor.mapDrawModel().setSelectedUnit(unit);
		mapActor.mapDrawModel().unitPath = null;
		if (unit == null) {
			return;
		}
		setDrawableUnitPath(unit);
	}
	
	private void setDrawableUnitPath(Unit unit) {
		mapActor.mapDrawModel().unitPath = null;
		
		if (unit.isDestinationTile()) {
			Tile startTile = unit.getTile();
			Tile endTile = game.map.getTile(unit.getDestinationX(), unit.getDestinationY());
			mapActor.mapDrawModel().unitPath = finder.findToTile(game.map, startTile, endTile, unit);
		}
		if (unit.isDestinationEurope()) {
			Tile startTile = unit.getTile();
			mapActor.mapDrawModel().unitPath = finder.findToEurope(game.map, startTile, unit);
		}
	}
	
    public Game getGame() {
        return game;
    }

	public void setApplicationScreenManager(ApplicationScreenManager screenManager) {
		this.screenManager = screenManager;
	}

	public void enterIntoCreateGotoPathMode() {
		if (guiGameModel.isCreateGotoPathMode()) {
			return;
		}
		if (guiGameModel.isActiveUnitNotSet()) {
			return;
		}
		guiGameModel.setCreateGotoPathMode(true);
		setDrawableUnitPath(guiGameModel.getActiveUnit());		
	}
	
	public void leaveCreateGotoPathMode() {
		guiGameModel.setCreateGotoPathMode(false);
		mapActor.mapDrawModel().unitPath = null;
	}

	private void logicAcceptGotoPath() {
		Path unitPath = mapActor.mapDrawModel().unitPath;
		if (unitPath == null) {
			throw new IllegalStateException("path not generated");
		}
		guiGameModel.setCreateGotoPathMode(false);
		
		MoveContext moveContext = new MoveContext(unitPath);
		moveContext.initNextPathStep();
		if (unitPath.isPathToEurope()) {
			moveContext.unit.setDestinationEurope();
		} else {
			moveContext.unit.setDestination(unitPath.endTile);
		}

		System.out.println("moveContext = " + moveContext);

		if (moveContext.canHandleMove()) {
			moveContext.handleMove();
			guiMoveInteraction(moveContext);
		} else {
            moveContext.unit.clearDestination();
            mapActor.mapDrawModel().unitPath = null;
		}
	}
	
	private void generateGotoPath(Tile destinationTile) {
		throwExceptionWhenActiveUnitNotSet();
		
		Tile startTile = guiGameModel.getActiveUnit().getTile();
		
		Path path = finder.findToTile(game.map, startTile, destinationTile, guiGameModel.getActiveUnit());
		System.out.println("found path: " + path);
		mapActor.mapDrawModel().unitPath = path;
	}

	public void acceptPathToDestination(Tile tile) {
		generateGotoPath(tile);
		logicAcceptGotoPath();
	}
	
	public void acceptPathToEuropeDestination() {
		mapActor.mapDrawModel().unitPath = finder.findToEurope(
				game.map, 
				guiGameModel.getActiveUnit().getTile(), 
				guiGameModel.getActiveUnit()
		);
		logicAcceptGotoPath();
	}
	
	public void acceptAction() {
		if (guiGameModel.isCreateGotoPathMode()) {
			if (!guiGameModel.isCreateGotoPathMode()) {
				throw new IllegalStateException("should be in find path mode");
			}
			throwExceptionWhenActiveUnitNotSet();
			logicAcceptGotoPath();
			return;
		}
	}
	
	public void cancelAction() {
		if (guiGameModel.isCreateGotoPathMode()) {
			leaveCreateGotoPathMode();
		}
	}
	
	public void addGUIGameModelChangeListener(ChangeStateListener listener) {
		guiGameModel.addChangeListener(listener);
	}

	public void onShowGUI() {
		guiGameModel.runListeners();
	}

	public Unit getActiveUnit() {
		return guiGameModel.getActiveUnit();
	}

	public void endTurn(EndOfTurnPhaseListener endOfTurnPhaseListener) {
		blockUserInteraction = true;
		guiGameModel.setAiMove(true);
		System.out.println("end turn");

		game.playingPlayer.endTurn();
		
		List<Player> players = game.players.allToProcessedOrder(game.playingPlayer);
		for (Player player : players) {
			if (player.nation().isUnknownEnemy()) {
				continue;
			}
			
			endOfTurnPhaseListener.nextAIturn(player);
			System.out.println("player " + player);
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		gameLogic.newTurn(game.playingPlayer);
		if (gameLogic.getNewTurnContext().isRequireUpdateMapModel()) {
			mapActor.resetMapModel();
		}
		
		logicNextActiveUnit();
		
		guiGameModel.setAiMove(false);
		blockUserInteraction = false;
		endOfTurnPhaseListener.endOfAIturns();
	}

	public void buildRoad() {
		throwExceptionWhenActiveUnitNotSet();
		Tile tile = guiGameModel.getActiveUnit().getTile();
		Unit unit = guiGameModel.getActiveUnit();
		if (!unit.hasAbility(Ability.IMPROVE_TERRAIN)) {
			return;
		}
		TileImprovementType roadImprovement = Specification.instance.tileImprovementTypes.getById(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID);
		logicMakeImprovement(tile, unit, roadImprovement);
	}

	public void plowOrClearForestImprovement() {
		throwExceptionWhenActiveUnitNotSet();
		Tile tile = guiGameModel.getActiveUnit().getTile();
		Unit unit = guiGameModel.getActiveUnit();
		if (!unit.hasAbility(Ability.IMPROVE_TERRAIN)) {
			return;
		}

		TileImprovementType imprv = null;
		if (tile.getType().isForested()) {
			imprv = Specification.instance.tileImprovementTypes.getById(TileImprovementType.CLEAR_FOREST_IMPROVEMENT_TYPE_ID);
		} else {
			imprv = Specification.instance.tileImprovementTypes.getById(TileImprovementType.PLOWED_IMPROVEMENT_TYPE_ID);
		}
		logicMakeImprovement(tile, unit, imprv);
	}

	private void logicMakeImprovement(Tile tile, Unit unit, TileImprovementType improvement) {
		if (tile.canBeImprovedByUnit(improvement, unit)) {
			unit.startImprovement(tile, improvement);
			System.out.println("unit[" + unit + "] build [" + improvement + "] on tile[" + tile + "]");
			logicNextActiveUnit();
		} else {
			System.out.println("unit[" + unit + "] can not make improvement[" + improvement + "] on tile[" + tile + "]");
		}
	}

	public void sentryUnit() {
		guiGameModel.getActiveUnit().setState(UnitState.SENTRY);
		logicNextActiveUnit();
	}

	public void fortify() {
		throwExceptionWhenActiveUnitNotSet();
		Unit activeUnit = guiGameModel.getActiveUnit();
		activeUnit.setState(UnitState.FORTIFYING);
		logicNextActiveUnit();
	}
	
	public void activeUnit() {
		throwExceptionWhenActiveUnitNotSet();
		guiGameModel.getActiveUnit().setState(UnitState.ACTIVE);
		guiGameModel.runListeners();
	}
	
	private void throwExceptionWhenActiveUnitNotSet() {
		if (guiGameModel.isActiveUnitNotSet()) {
			throw new IllegalStateException("active unit not set");
		}
	}

	public void centerOnActiveUnit() {
		if (guiGameModel.isActiveUnitSet()) {
			mapActor.centerCameraOnTile(guiGameModel.getActiveUnit().getTile());
		}
	}
}
