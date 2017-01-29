package promitech.colonization;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IdGenerator;
import net.sf.freecol.common.model.Nation;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.UnitIterator;
import net.sf.freecol.common.model.map.Path;
import net.sf.freecol.common.model.map.PathFinder;
import net.sf.freecol.common.model.map.generator.MapGenerator;
import net.sf.freecol.common.model.player.MarketSnapshoot;
import net.sf.freecol.common.model.player.Notification;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.GameOptions;
import promitech.colonization.actors.cheat.CheatConsole;
import promitech.colonization.actors.colony.ColonyApplicationScreen;
import promitech.colonization.actors.europe.EuropeApplicationScreen;
import promitech.colonization.actors.map.ColonyNameDialog;
import promitech.colonization.actors.map.MapActor;
import promitech.colonization.actors.map.MapDrawModel;
import promitech.colonization.ai.AILogic;
import promitech.colonization.ai.AIMoveDrawer;
import promitech.colonization.ai.NavyExplorer;
import promitech.colonization.gamelogic.MoveContext;
import promitech.colonization.gamelogic.MoveType;
import promitech.colonization.math.Point;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.ui.QuestionDialog;
import promitech.colonization.ui.hud.HudStage;
import promitech.colonization.ui.resources.StringTemplate;

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
	private HudStage mapHudStage;
	private ApplicationScreenManager screenManager;
	
	private boolean blockUserInteraction = false;
	private PathFinder finder = new PathFinder();
	private final EndOfUnitDislocationAnimationAction endOfUnitDislocationAnimation = new EndOfUnitDislocationAnimationAction();
	private final LinkedList<MoveContext> movesToAnimate = new LinkedList<MoveContext>();
	private Unit disembarkCarrier;
	
	private AIMoveDrawer aiMoveDrawer = new AIMoveDrawer(this);
	
	public GUIGameController() {
	}
	
	public void initGameFromSavegame() throws IOException, ParserConfigurationException, SAXException {
        SaveGameParser saveGameParser = new SaveGameParser("maps/savegame_1600.xml");
        game = saveGameParser.parse();
        game.playingPlayer = game.players.getById("player:1");
        game.playingPlayer.eventsNotifications.setAddNotificationListener(guiGameModel);
        System.out.println("game = " + game);

		postCreateGame();
	}
	
	public void initNewGame() throws IOException, ParserConfigurationException, SAXException {
		SaveGameParser.loadDefaultSpecification();
		Specification.instance.updateOptionsFromDifficultyLevel("model.difficulty.medium");
		
		Game.idGenerator = new IdGenerator(0);
		game = new Game();
		game.activeUnitId = null;
		
		game.playingPlayer = Player.newStartingPlayer(Game.idGenerator, Specification.instance.nations.getById("model.nation.french"));
		game.players.add(game.playingPlayer);
		
		for (Nation nation : Specification.instance.nations.entities()) {
			if (nation.nationType.isEuropean()) {
				if (!nation.nationType.isREF() && game.playingPlayer.nation().notEqualsId(nation)) {
					System.out.println("create european player: " + nation +  " " + nation.nationType);
					game.players.add(Player.newStartingPlayer(Game.idGenerator, nation));
				}
			} else {
				System.out.println("create native player: " + nation + " " + nation.nationType);
				game.players.add(Player.newStartingPlayer(Game.idGenerator, nation));
			}
		}
		game.map = new MapGenerator().generate(game.players);

		postCreateGame();
	}
	
	private void postCreateGame() {
		guiGameModel.unitIterator = new UnitIterator(game.playingPlayer, new Unit.ActivePredicate());
		guiGameModel.player = game.playingPlayer;
		gameLogic = new GameLogic(game);
		
		for (Player player : game.players.entities()) {
			player.fogOfWar.resetFogOfWar(player);
		}
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
		    	showColonyScreen(tile);
		    }
		}
	}
	
	public void showColonyScreen(Tile tile) {
		ColonyApplicationScreen colonyApplicationScreen = screenManager.getApplicationScreen(ApplicationScreenType.COLONY);
		colonyApplicationScreen.initColony(tile.getSettlement().getColony(), tile);
		screenManager.setScreen(ApplicationScreenType.COLONY);
	}
	
	public void clickOnTile(Point p) {
	    clickOnTileDebugInfo(p);
	    
		if (blockUserInteraction) {
			return;
		}
		Tile clickedTile = game.map.getTile(p.x, p.y);
		if (clickedTile == null) {
			return;
		}
		
		MapDrawModel mapDrawModel = mapActor.mapDrawModel();
		
		if (guiGameModel.isCreateGotoPathMode()) {
			generateGotoPath(clickedTile);
			return;
		}
		
		if (guiGameModel.isViewMode()) {
			mapDrawModel.selectedTile = clickedTile;
			mapDrawModel.setSelectedUnit(null);
		} else {
			mapDrawModel.selectedTile = null;
			if (!clickedTile.hasSettlement()) {
				Unit newSelectedUnit = clickedTile.getUnits().first();
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
			System.out.println("moveContext.pressDirectionKey = " + moveContext);
			
			if (moveContext.isRequireUserInteraction()) {
				switch (moveContext.moveType) {
				case DISEMBARK:
					if (moveContext.unit.getUnitContainer().getUnits().size() == 1) {
						disembarkUnitToLocation(
							moveContext.unit, 
							moveContext.unit.getUnitContainer().getUnits().first(), 
							destTile
						);
					} else {
						mapHudStage.showChooseUnitsToDisembarkDialog(moveContext);
					}
					break;
				default:
					break;
				}
			} else {
				if (moveContext.canHandleMove()) {
					moveContext.handleMove();
					guiMoveInteraction(moveContext);
				}
			}
		}
	}

	private void guiMoveInteraction() {
		if (movesToAnimate.isEmpty()) {
			return;
		}
		MoveContext mc = movesToAnimate.removeFirst();
		mc.handleMove();
		guiMoveInteraction(mc);
	}
	
	private void guiMoveInteraction(MoveContext moveContext) {
		if (mapActor.isTileOnScreenEdge(moveContext.destTile)) {
			mapActor.centerCameraOnTile(moveContext.destTile);
		}
		blockUserInteraction = true;
		endOfUnitDislocationAnimation.moveContext = moveContext;
		mapActor.startUnitDislocationAnimation(moveContext, endOfUnitDislocationAnimation);
	}
	
	public void guiAIMoveInteraction(MoveContext moveContext) {
		if (mapActor.isTileOnScreenEdge(moveContext.destTile)) {
			mapActor.centerCameraOnTile(moveContext.destTile);
		}
		mapActor.startUnitDislocationAnimation(moveContext, aiMoveDrawer);
	}
	
	public boolean showAIMoveOnPlayerScreen(MoveContext moveContext) {
		return !game.playingPlayer.fogOfWar.hasFogOfWar(moveContext.sourceTile)
				|| !game.playingPlayer.fogOfWar.hasFogOfWar(moveContext.destTile);
	}
	
	private void onEndOfUnitDislocationAnimation(MoveContext moveContext) {
		if (moveContext.isMoveType(MoveType.MOVE) || moveContext.isMoveType(MoveType.MOVE_HIGH_SEAS)) {
			boolean exloredNewTiles = game.playingPlayer.revealMapAfterUnitMove(game.map, moveContext.unit);
			if (exloredNewTiles) {
				mapActor.resetUnexploredBorders();
			}
		}
		
		if (moveContext.isMoveType(MoveType.EMBARK)) {
			System.out.println("XXX onEndOfUnitDislocationAnimation.embark");
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
			System.out.println("moveContext.isMoveViaPath = " + moveContext);
			if (moveContext.canHandleMove()) {
				moveContext.handleMove();
				guiMoveInteraction(moveContext);
			} else {
				if (moveContext.isEndOfPath()) {
					if (moveContext.unit.isDestinationEurope() && moveContext.unit.getTile().getType().isHighSea()) {
			            moveContext.unit.moveUnitToHighSea();
			            logicNextActiveUnit();
					} else {
	                    moveContext.unit.clearDestination();
	                    mapActor.mapDrawModel().unitPath = null;
					}
					
					if (!moveContext.unit.couldMove()) {
						logicNextActiveUnit();
						blockUserInteraction = false;
					}					
					if (moveContext.unit.isCarrier() && moveContext.destTile.hasSettlement()) {
						showColonyScreen(moveContext.destTile);
					}
				} else {
                    moveContext.unit.setState(UnitState.SKIPPED);
					logicNextActiveUnit();
				}
				blockUserInteraction = false;
			}
		} else {
			
			if (disembarkCarrier != null) {
				if (movesToAnimate.isEmpty()) {
					disembarkCarrier = null;
					blockUserInteraction = false;
				} else {
					guiMoveInteraction();
				}
			} else {
				if (!moveContext.unit.couldMove()) {
					logicNextActiveUnit();
					blockUserInteraction = false;
				} else {
				    if (moveContext.unit.getTile().getType().isHighSea()) {
				        createHighSeasQuestion(moveContext);
				    } else {
		                blockUserInteraction = false;
				    }
				}
				if (moveContext.unit.isCarrier() && moveContext.destTile.hasSettlement()) {
					showColonyScreen(moveContext.destTile);
				}
			}
			
		}
	}

	private final QuestionDialog.OptionAction<MoveContext> sailHighSeasYesAnswer = new QuestionDialog.OptionAction<MoveContext>() {
        @Override
        public void executeAction(MoveContext payload) {
            payload.unit.moveUnitToHighSea();
            logicNextActiveUnit();
            blockUserInteraction = false;
        }
    };

    private final QuestionDialog.OptionAction<MoveContext> sailHighSeasNoAnswer = new QuestionDialog.OptionAction<MoveContext>() {
        @Override
        public void executeAction(MoveContext payload) {
            blockUserInteraction = false;
        }
    };
    
    private void createHighSeasQuestion(MoveContext moveContext) {
        QuestionDialog questionDialog = new QuestionDialog();
        questionDialog.addQuestion(StringTemplate.template("highseas.text")
            .addAmount("%number%", moveContext.unit.getSailTurns())
        );
        questionDialog.addAnswer("highseas.yes", sailHighSeasYesAnswer, moveContext);
        questionDialog.addAnswer("highseas.no", sailHighSeasNoAnswer, moveContext);
        
        mapHudStage.showDialog(questionDialog);
    }
	
    public void showMapScreenAndActiveNextUnit() {
    	screenManager.setScreen(ApplicationScreenType.MAP_VIEW);
    	logicNextActiveUnit();
    }
    
	public void showEuropeScreen() {
        EuropeApplicationScreen screen = screenManager.getApplicationScreen(ApplicationScreenType.EUROPE);
        screen.init(game.playingPlayer, game);
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

    public GUIGameModel getGuiGameModel() {
        return guiGameModel;
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
//			if (moveContext.isMoveType(MoveType.EMBARK)) {
//				guiGameModel.setActiveUnit(null);
//				mapActor.mapDrawModel().setSelectedUnit(null);
//			}
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

		game.playingPlayer.endTurn();
		
		MarketSnapshoot marketSnapshoot = new MarketSnapshoot(game.playingPlayer.market());
		
		AILogic aiLogic = new AILogic(game, gameLogic, aiMoveDrawer);
		
		List<Player> players = game.players.allToProcessedOrder(game.playingPlayer);
		for (Player player : players) {			
			endOfTurnPhaseListener.nextAIturn(player);
			System.out.println("new turn for player " + player);
			
			aiLogic.aiNewTurn(player);
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		gameLogic.comparePrices(game.playingPlayer, marketSnapshoot);
		
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

	public Notification getFirstNotification() {
		Notification firstNotification = game.playingPlayer.eventsNotifications.firstNotification();
		guiGameModel.runListeners();
		return firstNotification;
	}

	public void disembarkUnitToLocation(Unit carrier, Unit unitToDisembark, Tile destTile) {
		disembarkCarrier = carrier;
		
		MoveContext mc = new MoveContext(carrier.getTileLocationOrNull(), destTile, unitToDisembark);
		mc.handleMove();
		guiMoveInteraction(mc);
	}
	
	public void disembarkUnitsToLocation(Unit carrier, Collection<Unit> unitsToDisembark, Tile destTile) {
		disembarkCarrier = carrier;
		
		for (Unit u : unitsToDisembark) {
			MoveContext mc = new MoveContext(carrier.getTileLocationOrNull(), destTile, u);
			System.out.println("try disembark " + mc.toString());
			if (mc.canHandleMove()) {
				movesToAnimate.add(mc);
			}
		}
		guiMoveInteraction();
	}

	public void setMapHudStage(HudStage hudStage) {
		this.mapHudStage = hudStage;
	}

	public void resetMapModel() {
		mapActor.resetMapModel();
	}

	public void showCheatConsoleDialog() {
		CheatConsole cheatConsole = new CheatConsole(this);
		cheatConsole.setSelectedTile(mapActor.mapDrawModel().selectedTile);
		mapHudStage.showDialog(cheatConsole);
	}

	public void buildColony() {
		Unit unit = guiGameModel.getActiveUnit();
		Tile tile = unit.getTile();
	
		if (!tile.getType().canSettle()) {
			System.out.println("can not settle on tile type " + tile.getType());
			return;
		}
		if (game.map.isOnMapEdge(tile)) {
			System.out.println("can not settle on map edge");
			return;
		}
		if (game.map.hasColonyInRange(tile, 1)) {
			System.out.println("another colony in one tile range");
			return;
		}
		if (!unitCanBuildColony(unit)) {
			System.out.println("unit can not build colony");
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
    	
		QuestionDialog questionDialog = createIndianLandDemandQuestions(landPrice, unit, tile, buildColonyEnterColonyNameAction);
    	mapHudStage.showDialog(questionDialog);
	}

	public QuestionDialog createIndianLandDemandQuestions(int landPrice, final Unit claimedUnit,
			final Tile claimedTile, final QuestionDialog.OptionAction<Unit> actionAfterDemand) {
		QuestionDialog.OptionAction<Unit> takeLandAction = new QuestionDialog.OptionAction<Unit>() {
			@Override
			public void executeAction(Unit claimedUnit) {
				claimedTile.demandTileByPlayer(claimedUnit.getOwner());
				
				actionAfterDemand.executeAction(claimedUnit);
			}
		};
		QuestionDialog.OptionAction<Unit> payForLandAction = new QuestionDialog.OptionAction<Unit>() {
			@Override
			public void executeAction(Unit claimedUnit) {
				if (claimedTile.buyTileByPlayer(claimedUnit.getOwner())) {
					actionAfterDemand.executeAction(claimedUnit);
				}
			}
		};
		
		QuestionDialog questionDialog = new QuestionDialog();
		if (claimedUnit.getOwner().hasContacted(claimedTile.getOwner())) {
			questionDialog.addQuestion(StringTemplate.template("indianLand.text")
				.addStringTemplate("%player%", claimedTile.getOwner().getNationName())
			);
			
			if (landPrice > 0) {
				StringTemplate landPriceStrTemp = StringTemplate.template("indianLand.pay").addAmount("%amount%", landPrice);
				questionDialog.addAnswer(landPriceStrTemp, payForLandAction, claimedUnit);
			}
		} else {
			questionDialog.addQuestion(StringTemplate.template("indianLand.unknown"));
		}
		questionDialog.addAnswer("indianLand.take", takeLandAction, claimedUnit);
		questionDialog.addOnlyCloseAnswer("indianLand.cancel");
		return questionDialog;
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
		
		Settlement.buildColony(game.map, unit, tile, colonyName);
		changeActiveUnit(null);
		resetMapModel();
	}
	
	private boolean unitCanBuildColony(Unit unit) {
		return unit.hasMovesPoints() 
				&& unit.unitType.canBuildColony() 
				&& (!unit.getOwner().isRebel() || Specification.options.getBoolean(GameOptions.FOUND_COLONY_DURING_REBELLION));
	}

	public void showTilesOwners() {
		mapActor.showTileOwners();
	}
	
	public void hideTilesOwners() {
		mapActor.hideTileOwners();
	}

	public void theBestMove() {
		final Unit unit = guiGameModel.getActiveUnit();
		if (unit == null) {
			System.out.println("no unit selected");
			return;
		}
		System.out.println("the best move");

		
        final PathFinder pathFinder = new PathFinder();
        pathFinder.generateRangeMap(game.map, unit.getTile(), unit);
        
        NavyExplorer navyExplorer = new NavyExplorer(game.map);
        navyExplorer.generateExploreDestination(pathFinder, unit.getOwner());
        
        if (navyExplorer.isFoundExploreDestination()) {
            if (navyExplorer.isExploreDestinationInOneTurn()) {
                Direction direction = navyExplorer.getExploreDestinationAsDirection();
				System.out.println("exploration destination " + direction);
				pressDirectionKey(direction);
            } else {
                System.out.println("exploration path " + navyExplorer.getExploreDestinationAsPath());
            }
        } else {
            // maybe is everything explored or blocked in some how
            System.out.println("can not find tile to explore");
        }
        
        final String tileStrings[][] = new String[game.map.height][game.map.width];
        navyExplorer.toStringsBorderValues(tileStrings);
        mapActor.showTileDebugStrings(tileStrings);
		
	}
	
}
