package promitech.colonization;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.map.Path;
import net.sf.freecol.common.model.map.PathFinder;
import promitech.colonization.GUIGameModel.ChangeStateListener;
import promitech.colonization.actors.colony.ColonyApplicationScreen;
import promitech.colonization.actors.map.MapActor;
import promitech.colonization.actors.map.MapDrawModel;
import promitech.colonization.actors.map.UnitDislocationAnimation;
import promitech.colonization.actors.map.UnitDislocationAnimation.EndOfAnimationListener;
import promitech.colonization.gamelogic.MoveContext;
import promitech.colonization.gamelogic.MoveType;
import promitech.colonization.gamelogic.UnitIterator;
import promitech.colonization.math.Point;
import promitech.colonization.savegame.SaveGameParser;

public class GUIGameController {

	private final GUIGameModel guiGameModel = new GUIGameModel();
	private Game game;
	private UnitIterator unitIterator;
	
	private MapActor mapActor;
	private ApplicationScreenManager screenManager;
	
	private boolean blockUserInteraction = false;
	
	public void initGameFromSavegame() throws IOException, ParserConfigurationException, SAXException {
        SaveGameParser saveGameParser = new SaveGameParser("maps/savegame_1600.xml");
        game = saveGameParser.parse();
        game.playingPlayer = game.players.getById("player:1");
        System.out.println("game = " + game);
        
        unitIterator = new UnitIterator(game.playingPlayer, new Unit.ActivePredicate());
	}
	
    public void setMapActor(MapActor mapActor) {
        this.mapActor = mapActor;
    }
	
	public void nextActiveUnit() {
		if (blockUserInteraction) {
			return;
		}
		
		if (unitIterator.hasNext()) {
			changeActiveUnit(unitIterator.next());
			if (guiGameModel.isActiveUnitSet()) {
				mapActor.centerCameraOnTile(guiGameModel.getActiveUnit().getTile());
			} 
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
		if (guiGameModel.isActiveUnitSet()) {
			mapActor.centerCameraOnTile(guiGameModel.getActiveUnit().getTile());
		} 
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
			generateGotoPath(p);
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
        System.out.println("p = " + p);
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
	
	private UnitDislocationAnimation.EndOfAnimationListener endOfUnitDislocationAnimation = new EndOfAnimationListener() {
		@Override
		public void end(Unit unit) {
			
			if (moveContext.isMoveType(MoveType.MOVE)) {
				boolean exloredNewTiles = game.playingPlayer.revealMapAfterUnitMove(game.map, unit);
				if (exloredNewTiles) {
					mapActor.resetUnexploredBorders();
				}
			}

			if (moveContext.isMoveType(MoveType.EMBARK)) {
				mapActor.mapDrawModel().setSelectedUnit(null);
				guiGameModel.setActiveUnit(null);
			}
			
			if (moveContext.isMoveViaPath()) {
				moveContext.initNextPathStep();
				System.out.println("moveContext = " + moveContext);
				if (moveContext.canHandleMove()) {
					moveContext.handleMove();
					guiMoveInteraction(moveContext);
				} else {
					blockUserInteraction = false;
					
					if (moveContext.isEndOfPath()) {
						mapActor.mapDrawModel().unitPath = null;
					}
				}
			} else {
				blockUserInteraction = false;
			}
		}
	};
	
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
		mapActor.mapDrawModel().unitPath = null;
		guiGameModel.setCreateGotoPathMode(true);
	}
	
	public void leaveCreateGotoPathMode() {
		guiGameModel.setCreateGotoPathMode(false);
		mapActor.mapDrawModel().unitPath = null;
	}

	private void acceptGotoPath() {
		if (!guiGameModel.isCreateGotoPathMode()) {
			throw new IllegalStateException("should be in find path mode");
		}
		if (guiGameModel.isActiveUnitNotSet()) {
			throw new IllegalStateException("active unit should be set");
		}
		Path unitPath = mapActor.mapDrawModel().unitPath;
		if (unitPath == null) {
			throw new IllegalStateException("path not generated");
		}
		guiGameModel.setCreateGotoPathMode(false);
		
		MoveContext moveContext = new MoveContext(unitPath);
		moveContext.initNextPathStep();

		System.out.println("moveContext = " + moveContext);

		if (moveContext.canHandleMove()) {
			moveContext.handleMove();
			guiMoveInteraction(moveContext);
		}
	}
	
	private void generateGotoPath(Point tileCoords) {
		if (guiGameModel.isActiveUnitNotSet()) {
			throw new IllegalStateException("activeUnit should be set to generate goto path");
		}
		
		PathFinder finder = new PathFinder();
		Tile startTile = guiGameModel.getActiveUnit().getTile();
		Tile endTile = game.map.getTile(tileCoords.x, tileCoords.y);
		
		Path path = finder.findToTile(game.map, startTile, endTile, guiGameModel.getActiveUnit());
		System.out.println("found path: " + path);
		mapActor.mapDrawModel().unitPath = path;
	}

	public void acceptAction() {
		if (guiGameModel.isCreateGotoPathMode()) {
			acceptGotoPath();
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
}
