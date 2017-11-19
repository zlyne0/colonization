package promitech.colonization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.map.path.TransportPathFinder;
import promitech.colonization.actors.map.MapActor;
import promitech.colonization.actors.map.MapDrawModel;
import promitech.colonization.gamelogic.MoveContext;
import promitech.colonization.ui.QuestionDialog;
import promitech.colonization.ui.resources.StringTemplate;

public class MoveController {

	private final MoveDrawerSemaphore moveDrawerSemaphore = new MoveDrawerSemaphore(this);
	
	private MapActor mapActor;
	private MoveLogic moveLogic;
	private GUIGameModel guiGameModel;
	private GUIGameController guiGameController;

	private final PathFinder finder = new PathFinder();
	
	public MoveController() {
	}
	
	public void inject(MoveLogic moveLogic, GUIGameModel guiGameModel, GUIGameController guiGameController) {
		this.moveLogic = moveLogic;
		this.guiGameModel = guiGameModel;
		this.guiGameController = guiGameController;
	}
	
	public void pressDirectionKey(Direction direction) {
		
		MapDrawModel mapDrawModel = mapActor.mapDrawModel();
		if (guiGameModel.isViewMode()) {
			int x = direction.stepX(mapDrawModel.selectedTile.x, mapDrawModel.selectedTile.y);
			int y = direction.stepY(mapDrawModel.selectedTile.x, mapDrawModel.selectedTile.y);
			mapDrawModel.selectedTile = guiGameModel.game.map.getTile(x, y);
			
			if (mapActor.isTileOnScreenEdge(mapDrawModel.selectedTile)) {
				mapActor.centerCameraOnTile(mapDrawModel.selectedTile);
			}
		} else {
			if (guiGameModel.isActiveUnitNotSet()) {
				return;
			}
			
			Unit selectedUnit = guiGameModel.getActiveUnit();
			Tile sourceTile = selectedUnit.getTile();
			
			Tile destTile = guiGameModel.game.map.getTile(sourceTile.x, sourceTile.y, direction);
			MoveContext moveContext = new MoveContext(sourceTile, destTile, selectedUnit, direction);
			
			mapActor.mapDrawModel().unitPath = null;
			selectedUnit.clearDestination();
			System.out.println("moveContext.pressDirectionKey = " + moveContext);
			
			moveLogic.forGuiMove(moveContext);
		}
	}

	public void disembarkUnitToLocation(Unit carrier, Unit unitToDisembark, Tile destTile) {
		MoveContext mc = new MoveContext(carrier.getTileLocationOrNull(), destTile, unitToDisembark);
		
		moveLogic.forGuiMoveOnlyReallocation(mc, null);
	}
	
	public void disembarkUnitsToLocation(Unit carrier, Collection<Unit> unitsToDisembark, Tile destTile) {
		List<MoveContext> disembarkMoves = new ArrayList<MoveContext>(unitsToDisembark.size()); 
		
		for (Unit u : unitsToDisembark) {
			MoveContext mc = new MoveContext(carrier.getTileLocationOrNull(), destTile, u);
			System.out.println("disembark unit move: " + mc);
			if (mc.isMoveType()) {
				disembarkMoves.add(mc);
			}
		}
		
		moveLogic.forGuiMoveOnlyReallocation(disembarkMoves, null);
	}

	protected void startAnimateMove(MoveContext moveContext) {
		if (mapActor.isTileOnScreenEdge(moveContext.destTile)) {
			mapActor.centerCameraOnTile(moveContext.destTile);
		}
		mapActor.startUnitDislocationAnimation(moveContext, moveDrawerSemaphore);
	}
	
	public boolean showMoveOnPlayerScreen(MoveContext moveContext) {
		return !guiGameModel.game.playingPlayer.fogOfWar.hasFogOfWar(moveContext.sourceTile)
				|| !guiGameModel.game.playingPlayer.fogOfWar.hasFogOfWar(moveContext.destTile);
	}

	public void acceptPathToDestination(Tile tile) {
		generateGotoPath(tile);
		logicAcceptGotoPath();
	}
	
	public void acceptPathToEuropeDestination() {
		mapActor.mapDrawModel().unitPath = finder.findToEurope(
				guiGameModel.game.map, 
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
			guiGameModel.throwExceptionWhenActiveUnitNotSet();
			logicAcceptGotoPath();
			return;
		}
	}
	
	public void logicAcceptGotoPath() {
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

		System.out.println("path.moveContext = " + moveContext);

		moveLogic.forGuiMove(moveContext);
	}

	public void removeDrawableUnitPath() {
		mapActor.mapDrawModel().unitPath = null;
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

	public void generateGotoPath(Tile destinationTile) {
		guiGameModel.throwExceptionWhenActiveUnitNotSet();
		
		Tile startTile = guiGameModel.getActiveUnit().getTile();
		
		Path path = finder.findToTile(guiGameModel.game.map, startTile, destinationTile, guiGameModel.getActiveUnit());
		System.out.println("found path: " + path);
		mapActor.mapDrawModel().unitPath = path;
	}

	public void setDrawableUnitPath(Unit unit) {
		mapActor.mapDrawModel().unitPath = null;
		
		if (unit.isDestinationTile()) {
			Tile startTile = unit.getTile();
			Tile endTile = guiGameModel.game.map.getTile(unit.getDestinationX(), unit.getDestinationY());
			mapActor.mapDrawModel().unitPath = finder.findToTile(guiGameModel.game.map, startTile, endTile, unit);
		}
		if (unit.isDestinationEurope()) {
			Tile startTile = unit.getTile();
			mapActor.mapDrawModel().unitPath = finder.findToEurope(guiGameModel.game.map, startTile, unit);
		}
	}

	public void showHighSeasQuestion(MoveContext moveContext) {
		final QuestionDialog.OptionAction<MoveContext> sailHighSeasYesAnswer = new QuestionDialog.OptionAction<MoveContext>() {
			@Override
			public void executeAction(MoveContext payload) {
				moveLogic.forGuiMoveOnlyReallocation(payload, new MoveLogic.AfterMoveProcessor() {
					@Override
					void afterMove(MoveContext moveContext) {
						moveContext.unit.moveUnitToHighSea();
						guiGameController.logicNextActiveUnit();
					}
				});
			}
		};
		
        final QuestionDialog.OptionAction<MoveContext> moveToHighSeaAnswer = new QuestionDialog.OptionAction<MoveContext>() {
            @Override
            public void executeAction(MoveContext payload) {
                payload.setMoveViaHighSea();
                // invoke forGuiMove like in logicAcceptGotoPath
                moveLogic.forGuiMove(payload);
            }
        };
		
        QuestionDialog questionDialog = new QuestionDialog();
		questionDialog.addQuestion(StringTemplate.template("highseas.text")
            .addAmount("%number%", moveContext.unit.getSailTurns())
        );
        questionDialog.addAnswer("highseas.yes", sailHighSeasYesAnswer, moveContext);
        questionDialog.addAnswer("highseas.no", moveToHighSeaAnswer, moveContext);
        
        guiGameController.showDialog(questionDialog);
	}
	
	public MoveDrawerSemaphore getMoveDrawerSemaphore() {
		return moveDrawerSemaphore;
	}

	public void setMapActor(MapActor mapActor) {
		this.mapActor = mapActor;
	}
	
}
