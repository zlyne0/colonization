package promitech.colonization.screen.map.hud;

import java.util.List;

import com.badlogic.gdx.Gdx;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.player.MarketSnapshoot;
import net.sf.freecol.common.model.player.Notification;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import promitech.colonization.EndOfTurnPhaseListener;
import promitech.colonization.GameCreator;
import promitech.colonization.GameLogic;
import promitech.colonization.ai.AILogic;
import promitech.colonization.math.Point;
import promitech.colonization.orders.BuildColonyOrder;
import promitech.colonization.orders.BuildColonyOrder.OrderStatus;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveController;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.ApplicationScreenManager;
import promitech.colonization.screen.ApplicationScreenType;
import promitech.colonization.screen.colony.ColonyApplicationScreen;
import promitech.colonization.screen.europe.EuropeApplicationScreen;
import promitech.colonization.screen.map.ColonyNameDialog;
import promitech.colonization.screen.map.MapActor;
import promitech.colonization.screen.map.MapDrawModel;
import promitech.colonization.screen.ui.IndianLandDemandQuestionsDialog;
import promitech.colonization.ui.ClosableDialog;
import promitech.colonization.ui.QuestionDialog;

public class GUIGameController {
	private GUIGameModel guiGameModel;
	private MoveController moveController;
	private GameLogic gameLogic;
	private MoveService moveService;
	
	private MapActor mapActor;
	private HudStage mapHudStage;
	private ApplicationScreenManager screenManager;
	
	private boolean blockUserInteraction = false;
	
	public GUIGameController() {
	}
	
	public void inject(GUIGameModel guiGameModel, MoveController moveController, GameLogic gameLogic, MoveService moveService) {
		this.guiGameModel = guiGameModel;
		this.moveController = moveController;
		this.gameLogic = gameLogic;
		this.moveService = moveService;
	}
	
	public void quickSaveGame() {
		new GameCreator(guiGameModel)
			.quickSaveGame();
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
			}
		} else {
			mapActor.mapDrawModel().setSelectedUnit(null);
			mapActor.mapDrawModel().unitPath = null;
			guiGameModel.setActiveUnit(null);
		}
	}

	public void nextActiveUnitAsGdxPostRunnable() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                logicNextActiveUnit();
            }
        });
	}
	
	public void nextActiveUnitWhenNoMovePointsAsGdxPostRunnable(MoveContext moveContext) {
        if (!moveContext.unit.couldMove() || moveContext.isUnitKilled()) {
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
		    }
		}
	}
	
	public void showColonyScreen(final Tile tile) {
    	Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				ColonyApplicationScreen colonyApplicationScreen = screenManager.getApplicationScreen(ApplicationScreenType.COLONY);
				colonyApplicationScreen.initColony(tile.getSettlement().getColony(), tile);
				screenManager.setScreen(ApplicationScreenType.COLONY);
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

		guiGameModel.game.playingPlayer.endTurn();
		
		MarketSnapshoot marketSnapshoot = new MarketSnapshoot(guiGameModel.game.playingPlayer.market());
		
		AILogic aiLogic = new AILogic(guiGameModel.game, gameLogic, moveService);
		
		List<Player> players = guiGameModel.game.players.allToProcessedOrder(guiGameModel.game.playingPlayer);
		for (Player player : players) {			
			endOfTurnPhaseListener.nextAIturn(player);
			System.out.println("new turn for player " + player);
			
			aiLogic.aiNewTurn(player);
			
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
		
		gameLogic.comparePrices(guiGameModel.game.playingPlayer, marketSnapshoot);
		
		gameLogic.newTurn(guiGameModel.game.playingPlayer);
		if (gameLogic.getNewTurnContext().isRequireUpdateMapModel()) {
			mapActor.resetMapModel();
		}
		resetUnexploredBorders();
		
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
		TileImprovementType roadImprovement = Specification.instance.tileImprovementTypes.getById(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID);
		logicMakeImprovement(tile, unit, roadImprovement);
	}

	public void plowOrClearForestImprovement() {
		guiGameModel.throwExceptionWhenActiveUnitNotSet();
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
		String colonyName = Settlement.generateSettlmentName(unit.getOwner());
		
		ColonyNameDialog cnd = new ColonyNameDialog(this, mapHudStage.getWidth() * 0.5f, colonyName);
		mapHudStage.showDialog(cnd);
	}
	
	public void buildColony(String colonyName) {
		Unit unit = guiGameModel.getActiveUnit();
		Tile tile = unit.getTile();
		
		Settlement.buildColony(guiGameModel.game.map, unit, tile, colonyName);
		changeActiveUnit(null);
		resetMapModel();
	}
	
	public void showTilesOwners() {
		mapActor.showTileOwners();
	}
	
	public void hideTilesOwners() {
		mapActor.hideTileOwners();
	}
	
	private final Runnable resetUnexploredBordersPostRunnable = new Runnable() {
		@Override
		public void run() {
			mapActor.resetUnexploredBorders();
		}
		
		public String toString() {
			return "postRunnable.resetUnexploredBorders";
		}
	};
	
	public void resetUnexploredBorders() {
		Gdx.app.postRunnable(resetUnexploredBordersPostRunnable);
	}

	public void showDialog(ClosableDialog<?> dialog) {
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