package promitech.colonization.screen.map.hud;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.EventListener;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyFactory;
import net.sf.freecol.common.model.SettlementFactory;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.MarketSnapshoot;
import net.sf.freecol.common.model.player.MoveExploredTiles;
import net.sf.freecol.common.model.player.Notification;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import promitech.colonization.EndOfTurnPhaseListener;
import promitech.colonization.GameLogic;
import promitech.colonization.ai.AILogic;
import promitech.colonization.math.Point;
import promitech.colonization.orders.BuildColonyOrder;
import promitech.colonization.orders.BuildColonyOrder.OrderStatus;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveController;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.orders.move.MoveService.AfterMoveProcessor;
import promitech.colonization.screen.ApplicationScreenManager;
import promitech.colonization.screen.ApplicationScreenType;
import promitech.colonization.screen.colony.ColonyApplicationScreen;
import promitech.colonization.screen.europe.EuropeApplicationScreen;
import promitech.colonization.screen.map.ColonyNameDialog;
import promitech.colonization.screen.map.MapActor;
import promitech.colonization.screen.map.MapDrawModel;
import promitech.colonization.screen.map.diplomacy.IndianSettlementInformationDialog;
import promitech.colonization.screen.ui.IndianLandDemandQuestionsDialog;
import promitech.colonization.ui.ModalDialog;
import promitech.colonization.ui.QuestionDialog;
import promitech.colonization.ui.QuestionDialog.OptionAction;

public class GUIGameController {
	private GUIGameModel guiGameModel;
	private MoveController moveController;
	private GameLogic gameLogic;
	private MoveService moveService;
	private PathFinder pathFinder;
	
	private MapActor mapActor;
	private HudStage mapHudStage;
	private ApplicationScreenManager screenManager;
	
	private boolean blockUserInteraction = false;

	public GUIGameController() {
	}
	
	public void inject(
		GUIGameModel guiGameModel, MoveController moveController, GameLogic gameLogic, 
		MoveService moveService, PathFinder pathFinder
	) {
		this.guiGameModel = guiGameModel;
		this.moveController = moveController;
		this.gameLogic = gameLogic;
		this.moveService = moveService;
		this.pathFinder = pathFinder;
	}
	
    public void skipUnit() {
    	if (blockUserInteraction) {
    		return;
    	}
    	guiGameModel.throwExceptionWhenActiveUnitNotSet();
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
	
	protected void logicNextActiveUnit() {
		if (guiGameModel.unitIterator.hasNext()) {
			Unit nextUnit = guiGameModel.unitIterator.next();
			changeActiveUnit(nextUnit);
			centerOnActiveUnit();
			if (nextUnit.isDestinationSet()) {
				moveController.logicAcceptGotoPath();
			} else {
				if (nextUnit.isTradeRouteSet()) {
					moveController.executeTradeRoute(nextUnit);
				}
			}
		} else {
			mapActor.mapDrawModel().setSelectedUnit(null);
			mapActor.mapDrawModel().unitPath = null;
			guiGameModel.setActiveUnit(null);
		}
	}

	public void centerMapOnEntryPoint() {
		mapActor.centerCameraOnTile(
			guiGameModel.game.playingPlayer.getEntryLocationX(), 
			guiGameModel.game.playingPlayer.getEntryLocationY()
		);
	}
	
	public void nextActiveUnitAsGdxPostRunnable() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                logicNextActiveUnit();
            }
        });
	}
	
    private final AfterMoveProcessor ifRequiredNextActiveUnit = new AfterMoveProcessor() {
        @Override
        public void afterMove(MoveContext moveContext) {
        	nextActiveUnitWhenNoMovePointsAsGdxPostRunnable();
        }
        @Override
        public void afterMove(Unit unit) {
        	nextActiveUnitWhenNoMovePointsAsGdxPostRunnable();
        }
    };
    public AfterMoveProcessor ifRequiredNextActiveUnit() {
    	return ifRequiredNextActiveUnit;
    }
    
    private final Runnable ifRequiredNextActiveUnitRunnable = new Runnable() {
		@Override
		public void run() {
			nextActiveUnitWhenNoMovePointsAsGdxPostRunnable();
		}
	};
    public Runnable ifRequiredNextActiveUnitRunnable() {
    	return ifRequiredNextActiveUnitRunnable;
    }
    
	public void nextActiveUnitWhenNoMovePointsAsGdxPostRunnable() {
        if (!guiGameModel.getActiveUnit().couldMove() || guiGameModel.getActiveUnit().isDisposed()) {
        	nextActiveUnitAsGdxPostRunnable();
        }
	}
	
	public void enterInViewMode() {
		if (blockUserInteraction) {
			return;
		}
		System.out.println("enterInViewMode");
		
		MapDrawModel mapDrawModel = mapActor.mapDrawModel();
		if (guiGameModel.isActiveUnitSet()) {
			mapDrawModel.selectedTile = guiGameModel.getActiveUnit().getTile();
		} else {
			Point p = mapActor.getCenterOfScreen();
			mapDrawModel.selectedTile = guiGameModel.game.map.getTile(p.x, p.y);
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
		Tile tile = guiGameModel.game.map.getTile(mapPoint.x, mapPoint.y);
		if (tile == null) {
			return;
		}
		if (tile.hasSettlement()) {
		    if (tile.getSettlement().isColony()) {
		    	showColonyScreen(tile);
		    } else {
		    	showDialog(new IndianSettlementInformationDialog(
	    			guiGameModel.game,
	    			tile.getSettlement().asIndianSettlement(), 
	    			guiGameModel.game.playingPlayer)
    			);
		    }
		}
	}
	
	public void showColonyScreen(final Tile tile) {
    	Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				ColonyApplicationScreen colonyApplicationScreen = screenManager.getApplicationScreen(ApplicationScreenType.COLONY);
				colonyApplicationScreen.initColony(tile.getSettlement().asColony(), tile);
				screenManager.setScreen(ApplicationScreenType.COLONY);
			}
    	});
	}

	public void showColonyScreenSpyMode(final Tile tile, final EventListener onCloseColonyListener) {
    	Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				ColonyApplicationScreen colonyApplicationScreen = screenManager.getApplicationScreen(ApplicationScreenType.COLONY);
				colonyApplicationScreen.addOneHitOnLeaveListener(onCloseColonyListener);
				colonyApplicationScreen.initColony(tile.getSettlement().asColony(), tile);
				screenManager.setScreen(ApplicationScreenType.COLONY);
				colonyApplicationScreen.setColonySpyMode();
			}
    	});
	}
	
	public void rightClickOnTile(Point p) {
	}
	
	public void clickOnTile(Point p) {
	    clickOnTileDebugInfo(p);
	    
		if (blockUserInteraction) {
			return;
		}
		Tile clickedTile = guiGameModel.game.map.getTile(p.x, p.y);
		if (clickedTile == null) {
			return;
		}
		
		MapDrawModel mapDrawModel = mapActor.mapDrawModel();
		
		if (guiGameModel.isCreateGotoPathMode()) {
			moveController.generateGotoPath(clickedTile);
			return;
		}
		
		if (guiGameModel.isViewMode()) {
			mapDrawModel.selectedTile = clickedTile;
			mapDrawModel.setSelectedUnit(null);
		} else {
			mapDrawModel.selectedTile = null;
			if (!clickedTile.hasSettlement()) {
				Unit newSelectedUnit = clickedTile.getUnits().first();
				if (newSelectedUnit != null && newSelectedUnit.isOwner(guiGameModel.game.playingPlayer)) {
					changeActiveUnit(newSelectedUnit);
				}
			}
		}
	}

	private void clickOnTileDebugInfo(Point p) {
        Tile tile = guiGameModel.game.map.getTile(p.x, p.y);
        if (tile != null) {
        	System.out.println("p = " + p + ", xml x=\"" + p.x + "\" y=\"" + p.y + "\"");
            System.out.println("tile: " + tile);
        	System.out.println("drawmodel = " + mapActor.mapDrawModel().tileDrawModelToString(p.x, p.y));
        } else {
            System.out.println("tile is null at " + p);
        }
	}
	
    public void showMapScreenAndActiveNextUnit() {
    	screenManager.setScreen(ApplicationScreenType.MAP_VIEW);
    	logicNextActiveUnit();
    }
    
	public void showEuropeScreen() {
        EuropeApplicationScreen screen = screenManager.getApplicationScreen(ApplicationScreenType.EUROPE);
        screen.init(guiGameModel.game.playingPlayer, guiGameModel.game);
		screenManager.setScreen(ApplicationScreenType.EUROPE);		
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
		moveController.setDrawableUnitPath(unit);
	}
	
	public void cancelAction() {
		if (guiGameModel.isCreateGotoPathMode()) {
			moveController.leaveCreateGotoPathMode();
		}
	}
	
	public void onShowGUI() {
		if (guiGameModel.isActiveUnitNotSet()) {
			logicNextActiveUnit();
		}
		guiGameModel.runListeners();
	}

	public Unit getActiveUnit() {
		return guiGameModel.getActiveUnit();
	}

	public void endTurn(EndOfTurnPhaseListener endOfTurnPhaseListener) {
		blockUserInteraction = true;
		guiGameModel.setAiMove(true);
		System.out.println("end turn");

		endOfTurnPhaseListener.nextAIturn(guiGameModel.game.playingPlayer);
		guiGameModel.game.playingPlayer.endTurn();
		
		MarketSnapshoot marketSnapshoot = new MarketSnapshoot(guiGameModel.game.playingPlayer.market());
		
		AILogic aiLogic = new AILogic(guiGameModel.game, gameLogic, moveService);
		
		List<Player> players = guiGameModel.game.players.allToProcessedOrder(guiGameModel.game.playingPlayer);
		for (Player player : players) {			
			endOfTurnPhaseListener.nextAIturn(player);
			System.out.println("new turn for player " + player);
			
			aiLogic.aiNewTurn(player);
		}
		
		gameLogic.comparePrices(guiGameModel.game.playingPlayer, marketSnapshoot);
		
		gameLogic.newTurn(guiGameModel.game.playingPlayer);
		guiGameModel.game.getTurn().increaseTurnNumber();
		if (gameLogic.getNewTurnContext().isRequireUpdateMapModel()) {
		}
		mapActor.resetMapModel();
		mapActor.resetUnexploredBorders();
		
		logicNextActiveUnit();
		
		guiGameModel.setAiMove(false);
		blockUserInteraction = false;
		endOfTurnPhaseListener.endOfAIturns();
	}

	public void buildRoad() {
		guiGameModel.throwExceptionWhenActiveUnitNotSet();
		Tile tile = guiGameModel.getActiveUnit().getTile();
		Unit unit = guiGameModel.getActiveUnit();
		if (!unit.hasAbility(Ability.IMPROVE_TERRAIN)) {
			return;
		}
		String improvementTypeId = TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID;
		confirmTileImprovement(tile, unit, improvementTypeId);
	}

	public void plowOrClearForestImprovement() {
		guiGameModel.throwExceptionWhenActiveUnitNotSet();
		Tile tile = guiGameModel.getActiveUnit().getTile();
		Unit unit = guiGameModel.getActiveUnit();
		if (!unit.hasAbility(Ability.IMPROVE_TERRAIN)) {
			return;
		}
		String improvementTypeId = null;
		if (tile.getType().isForested()) {
			improvementTypeId = TileImprovementType.CLEAR_FOREST_IMPROVEMENT_TYPE_ID;
		} else {
			improvementTypeId = TileImprovementType.PLOWED_IMPROVEMENT_TYPE_ID;
		}
		confirmTileImprovement(tile, unit, improvementTypeId);
	}
	
	private void confirmTileImprovement(final Tile tile, final Unit unit, final String improvementTypeId) {
		if (tile.getOwner() == null || unit.getOwner().equalsId(tile.getOwner())) {
			makeTileImprovement(tile, unit, improvementTypeId);
			return;
		}
		int landPrice = -1;
		if (unit.getOwner().hasContacted(tile.getOwner())) {
			landPrice = unit.getTile().getLandPriceForPlayer(unit.getOwner());
		}
		if (landPrice == 0) {
			makeTileImprovement(tile, unit, improvementTypeId);
			return;
		}
		OptionAction<Unit> buildRoad = new OptionAction<Unit>() {
			@Override
			public void executeAction(Unit payload) {
				tile.removeOwner();
				makeTileImprovement(tile, unit, improvementTypeId);
			}
		};
		QuestionDialog questionDialog = new IndianLandDemandQuestionsDialog(landPrice, unit, tile, buildRoad);
    	mapHudStage.showDialog(questionDialog);
	}

	private void makeTileImprovement(Tile tile, Unit unit, String improvementId) {
		TileImprovementType improvement = Specification.instance.tileImprovementTypes.getById(improvementId);
		
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
		guiGameModel.throwExceptionWhenActiveUnitNotSet();
		Unit activeUnit = guiGameModel.getActiveUnit();
		activeUnit.setState(UnitState.FORTIFYING);
		logicNextActiveUnit();
	}
	
	public void activeUnit() {
		guiGameModel.throwExceptionWhenActiveUnitNotSet();
		guiGameModel.getActiveUnit().setState(UnitState.ACTIVE);
		guiGameModel.runListeners();
	}
	
	public void centerOnActiveUnit() {
		if (guiGameModel.isActiveUnitSet()) {
			mapActor.centerCameraOnTile(guiGameModel.getActiveUnit().getTile());
		}
	}
	
	public void centerOnTile(int x, int y) {
		mapActor.centerCameraOnTile(x, y);
	}

	public Notification getFirstNotification() {
		Notification firstNotification = guiGameModel.game.playingPlayer.eventsNotifications.firstNotification();
		guiGameModel.runListeners();
		return firstNotification;
	}

	public void resetMapModelOnTile(Tile tile) {
		mapActor.resetMapModel();
	}
	
	public void resetMapModel() {
		mapActor.resetMapModel();
	}

	public void buildColony() {
		Unit unit = guiGameModel.getActiveUnit();
		Tile tile = unit.getTile();
	
		BuildColonyOrder buildColonyOrder = new BuildColonyOrder(guiGameModel.game.map);
		
		OrderStatus orderStatus = buildColonyOrder.check(unit, tile);
		if (orderStatus != OrderStatus.OK) {
			System.out.println("can not build colony status[" + orderStatus + "] tile[" + tile + "], unit[" + unit + "]");
			return;
		}
		
		// simplicity - european can be owner only on settlement and neighbour tiles 
		// so it is not possible settle on european own tile
		// so check only native owner
		
		if (tile.getOwner() == null || unit.getOwner().equalsId(tile.getOwner())) {
			buildColonyEnterColonyName();
			return;
		}
		
		int landPrice = -1;
		if (unit.getOwner().hasContacted(tile.getOwner())) {
			landPrice = unit.getTile().getLandPriceForPlayer(unit.getOwner());
		}
		if (landPrice == 0) {
			buildColonyEnterColonyName();
			return;
		}
		
		final QuestionDialog.OptionAction<Unit> buildColonyEnterColonyNameAction = new QuestionDialog.OptionAction<Unit>() {
			@Override
			public void executeAction(Unit claimedUnit) {
				buildColonyEnterColonyName();
			}
		};
    	
		QuestionDialog questionDialog = new IndianLandDemandQuestionsDialog(landPrice, unit, tile, buildColonyEnterColonyNameAction);
    	mapHudStage.showDialog(questionDialog);
	}
	
	public void buildColonyEnterColonyName() {
		Unit unit = guiGameModel.getActiveUnit();
		String colonyName = SettlementFactory.generateSettlmentName(unit.getOwner());
		
		ColonyNameDialog cnd = new ColonyNameDialog(this, mapHudStage.getWidth() * 0.5f, colonyName);
		mapHudStage.showDialog(cnd);
	}
	
	public void buildColony(String colonyName) {
		Unit unit = guiGameModel.getActiveUnit();
		Tile tile = unit.getTile();
		
		ColonyFactory colonyFactory = new ColonyFactory(guiGameModel.game, pathFinder);
		colonyFactory.buildColony(unit, tile, colonyName);
		
		nextActiveUnit();
		resetMapModel();
	}
	
	public void showTilesOwners() {
		mapActor.showTileOwners();
	}
	
	public void hideTilesOwners() {
		mapActor.hideTileOwners();
	}
	
	public void resetUnexploredBorders(MoveExploredTiles exploredTiles) {
		mapActor.resetUnexploredBorders(exploredTiles);
	}
	
	public void showDialog(ModalDialog<?> dialog) {
		mapHudStage.showDialog(dialog);
	}

	public void showDialog(QuestionDialog dialog) {
		mapHudStage.showDialog(dialog);
	}
	
    public void setMapActor(MapActor mapActor) {
        this.mapActor = mapActor;
    }
    
	public void setApplicationScreenManager(ApplicationScreenManager screenManager) {
		this.screenManager = screenManager;
	}
	
	public void setMapHudStage(HudStage hudStage) {
		this.mapHudStage = hudStage;
	}
	
}
