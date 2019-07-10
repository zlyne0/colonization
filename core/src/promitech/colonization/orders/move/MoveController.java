package promitech.colonization.orders.move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GameOptions;
import promitech.colonization.Direction;
import promitech.colonization.orders.LostCityRumourService;
import promitech.colonization.orders.move.MoveService.AfterMoveProcessor;
import promitech.colonization.screen.map.MapActor;
import promitech.colonization.screen.map.hud.ChooseUnitsToDisembarkDialog;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.screen.map.hud.GUIGameModel;
import promitech.colonization.screen.map.hud.NewLandNameDialog;
import promitech.colonization.screen.map.unitanimation.MoveView;
import promitech.colonization.ui.QuestionDialog;
import promitech.colonization.ui.QuestionDialog.OptionAction;
import promitech.colonization.ui.resources.StringTemplate;

public class MoveController {
    private final MoveDrawerSemaphore unitAnimationSemaphore = new MoveDrawerSemaphore();
	
	private MapActor mapActor;
	private MoveView moveView;
	private MoveService moveService;
	private MoveInThreadService moveInThreadService;
	private GUIGameModel guiGameModel;
	private GUIGameController guiGameController;

	private PathFinder finder;
	
	public MoveController() {
	}
	
	public void inject(
		GUIGameModel guiGameModel, 
		GUIGameController guiGameController, MoveView moveView, MoveService moveService,
		MoveInThreadService moveInThreadService,
		PathFinder pathFinder
	) {
		this.moveView = moveView;
		this.guiGameModel = guiGameModel;
		this.guiGameController = guiGameController;
		this.moveService = moveService;
		this.moveInThreadService = moveInThreadService;
		this.finder = pathFinder;
	}
	
	public void pressDirectionKey(Direction direction) {
		if (guiGameModel.isViewMode()) {
			// no cursor move by direction key in view mode
			return;
		}
		
		if (guiGameModel.isActiveUnitNotSet()) {
			return;
		}
		
		Unit selectedUnit = guiGameModel.getActiveUnit();
		Tile sourceTile = selectedUnit.getTile();
		
		Tile destTile = guiGameModel.game.map.getTile(sourceTile.x, sourceTile.y, direction);
		if (destTile == null) {
			return;
		}
		MoveContext moveContext = new MoveContext(sourceTile, destTile, selectedUnit, direction);
		
		mapActor.mapDrawModel().unitPath = null;
		selectedUnit.clearDestination();
		System.out.println("moveContext.pressDirectionKey = " + moveContext);
		
		moveInThreadService.executeMove(moveContext, guiGameController.ifRequiredNextActiveUnit());
	}
	
	public void disembarkUnitToLocation(Unit carrier, Unit unitToDisembark, Tile destTile) {
		MoveContext mc = new MoveContext(carrier.getTileLocationOrNull(), destTile, unitToDisembark);
		moveInThreadService.confirmedMoveProcessor(mc, AfterMoveProcessor.DO_NOTHING);
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
		moveInThreadService.processMultipleMoves(disembarkMoves, AfterMoveProcessor.DO_NOTHING);
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

		moveInThreadService.executeMove(moveContext, new AfterMoveProcessor() {
		    @Override
		    public void afterMove(MoveContext moveContext) {
		        if (moveContext.isEndOfPath() && moveContext.unit.isCarrier() && moveContext.destTile.hasSettlement()) {
                    guiGameController.showColonyScreen(moveContext.destTile);
		        }
		        guiGameController.nextActiveUnitWhenNoMovePointsAsGdxPostRunnable();
		    }
        });
	}

	public void executeTradeRoute(Unit tradeRouteUnit) {
		moveInThreadService.executeTradeRoute(tradeRouteUnit, guiGameController.ifRequiredNextActiveUnit());
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
				moveInThreadService.confirmedMoveProcessor(payload, new MoveService.AfterMoveProcessor() {
			        @Override
			        public void afterMove(MoveContext moveContext) {
			            moveContext.unit.sailUnitToEurope(moveContext.destTile);
			            guiGameController.nextActiveUnitAsGdxPostRunnable();
			        }
                });
			}
		};
		
        final QuestionDialog.OptionAction<MoveContext> moveToHighSeaAnswer = new QuestionDialog.OptionAction<MoveContext>() {
            @Override
            public void executeAction(MoveContext payload) {
                payload.setMoveViaHighSea();
                moveInThreadService.executeMove(payload, guiGameController.ifRequiredNextActiveUnit());
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

    public void showCashInTreasureConfirmation(Unit treasureWagon) {
    	QuestionDialog questionDialog = new QuestionDialog();
    	
    	int fee = treasureWagon.treasureTransportFee();
    	if (fee == 0) {
    		questionDialog.addQuestion(StringTemplate.template("cashInTreasureTrain.free"));
    	} else {
    		int percent = Specification.options.getIntValue(GameOptions.TREASURE_TRANSPORT_FEE);
    		StringTemplate st = StringTemplate.template("cashInTreasureTrain.pay")
    				.addAmount("%fee%", percent);
    		questionDialog.addQuestion(st);
    	}
    	
    	questionDialog.addAnswer("cashInTreasureTrain.yes", new OptionAction<Unit>() {
    		@Override
    		public void executeAction(Unit payload) {
    			moveService.cashInTreasure(payload);
    		}
    	}, treasureWagon);
    	questionDialog.addAnswer("cashInTreasureTrain.no", QuestionDialog.DO_NOTHING_ACTION, treasureWagon);
    	guiGameController.showDialog(questionDialog);
    }

    public void showLostCityRumourConfirmation(MoveContext moveContext) {
        new LostCityRumourService(guiGameController, moveInThreadService, guiGameModel.game)
            .showLostCityRumourConfirmation(moveContext);
    }
}
