package promitech.colonization.orders.move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.Direction;
import promitech.colonization.orders.move.MoveService.AfterMoveProcessor;
import promitech.colonization.screen.map.MapActor;
import promitech.colonization.screen.map.MapDrawModel;
import promitech.colonization.screen.map.hud.ChooseUnitsToDisembarkDialog;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.screen.map.hud.GUIGameModel;
import promitech.colonization.screen.map.hud.NewLandNameDialog;
import promitech.colonization.screen.map.unitanimation.MoveView;
import promitech.colonization.ui.QuestionDialog;
import promitech.colonization.ui.resources.StringTemplate;

public class MoveController {
    private final MoveDrawerSemaphore unitAnimationSemaphore = new MoveDrawerSemaphore();
	
	private MapActor mapActor;
	private MoveView moveView;
	private MoveService moveService;
	private GUIGameModel guiGameModel;
	private GUIGameController guiGameController;

	private final PathFinder finder = new PathFinder();
	
	public MoveController() {
	}
	
	public void inject(
		GUIGameModel guiGameModel, 
		GUIGameController guiGameController, MoveView moveView, MoveService moveService
	) {
		this.moveView = moveView;
		this.guiGameModel = guiGameModel;
		this.guiGameController = guiGameController;
		this.moveService = moveService;
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
			
            moveService.preMoveProcessorInNewThread(moveContext, guiGameController.ifRequiredNextActiveUnit());
		}
	}
	
	public void disembarkUnitToLocation(Unit carrier, Unit unitToDisembark, Tile destTile) {
		MoveContext mc = new MoveContext(carrier.getTileLocationOrNull(), destTile, unitToDisembark);
		
		moveService.confirmedMoveProcessorInNewThread(mc, AfterMoveProcessor.DO_NOTHING);
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
		moveService.confirmedMultipleMoveProcessorInNewThread(disembarkMoves, AfterMoveProcessor.DO_NOTHING);
	}

	public void blockedShowMove(MoveContext moveContext) {
	    moveView.showMoveUnblocked(moveContext, unitAnimationSemaphore);
	    unitAnimationSemaphore.waitForUnitAnimation();
	}
	
    public void blockedShowFailedAttackMove(MoveContext moveContext) {
        moveView.showFailedAttackMoveUnblocked(moveContext, unitAnimationSemaphore);
        unitAnimationSemaphore.waitForUnitAnimation();
    }

    public void blockedShowAttackRetreat(MoveContext moveContext) {
        moveView.showAttackRetreat(moveContext, unitAnimationSemaphore);
        unitAnimationSemaphore.waitForUnitAnimation();
    }
    
    public void blockedShowSuccessfulAttackWithMove(MoveContext moveContext, Unit loser) {
        moveView.showSuccessfulAttackWithMove(moveContext, loser, unitAnimationSemaphore);
        unitAnimationSemaphore.waitForUnitAnimation();
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

		moveService.preMoveProcessorInNewThread(moveContext, new AfterMoveProcessor() {
		    @Override
		    public void afterMove(MoveContext moveContext) {
		        if (moveContext.isEndOfPath() && moveContext.unit.isCarrier() && moveContext.destTile.hasSettlement()) {
                    guiGameController.showColonyScreen(moveContext.destTile);
		        }
		        guiGameController.nextActiveUnitWhenNoMovePointsAsGdxPostRunnable(moveContext);
		    }
        });
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
			    moveService.confirmedMoveProcessorInNewThread(payload, new MoveService.AfterMoveProcessor() {
			        @Override
			        public void afterMove(MoveContext moveContext) {
			            moveContext.unit.moveUnitToHighSea();
			            guiGameController.nextActiveUnitAsGdxPostRunnable();
			        }
                });
			}
		};
		
        final QuestionDialog.OptionAction<MoveContext> moveToHighSeaAnswer = new QuestionDialog.OptionAction<MoveContext>() {
            @Override
            public void executeAction(MoveContext payload) {
                payload.setMoveViaHighSea();
                moveService.preMoveProcessorInNewThread(payload, guiGameController.ifRequiredNextActiveUnit());
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
    
	public void setMapActor(MapActor mapActor) {
		this.mapActor = mapActor;
	}

	public void showDisembarkConfirmation(MoveContext moveContext) {
        ChooseUnitsToDisembarkDialog chooseUnitsDialog = new ChooseUnitsToDisembarkDialog(moveContext, this);
        guiGameController.showDialog(chooseUnitsDialog);
	}

    public void showNewLandNameDialog(Player player, String defaultName) {
        guiGameController.showDialog(new NewLandNameDialog(player, defaultName));
    }
}
