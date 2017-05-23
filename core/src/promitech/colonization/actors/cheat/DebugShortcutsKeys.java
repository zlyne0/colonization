package promitech.colonization.actors.cheat;

import com.badlogic.gdx.Input;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.PathFinder;
import promitech.colonization.DI;
import promitech.colonization.Direction;
import promitech.colonization.GUIGameModel;
import promitech.colonization.MoveController;
import promitech.colonization.actors.map.MapActor;
import promitech.colonization.ai.BuildColony;
import promitech.colonization.ai.NavyExplorer;
import promitech.colonization.ui.hud.HudStage;

public class DebugShortcutsKeys {

    private DI di;
    private MapActor mapActor;
    private HudStage hudStage;
    
    public DebugShortcutsKeys(HudStage hudStage, DI di, MapActor mapActor) {
        this.di = di;
        this.hudStage = hudStage;
        this.mapActor = mapActor;
    }

    public boolean canHandleKey(int keycode) {
        switch (keycode) {
            case Input.Keys.NUM_1:
            case Input.Keys.NUM_2:
            case Input.Keys.GRAVE:
                return true;
            default:
                return false;
        }
    }
    
    public void handleKey(int keycode) {
        switch (keycode) {
            case Input.Keys.GRAVE:
                showCheatConsoleDialog();
                break;
            case Input.Keys.NUM_1:
                theBestPlaceToBuildColony();
                break;
            default:
                break;
        }
    }
    
    public void theBestPlaceToBuildColony() {
        GUIGameModel guiGameModel = di.guiGameModel;
        
        BuildColony buildColony = new BuildColony(guiGameModel.game.map);
        buildColony.generateWeights(guiGameModel.game.playingPlayer);
        final String tileStrings[][] = new String[guiGameModel.game.map.height][guiGameModel.game.map.width];
        buildColony.toStringValues(tileStrings);
        mapActor.showTileDebugStrings(tileStrings);
    }
    
    public void theBestMove() {
        GUIGameModel guiGameModel = di.guiGameModel;
        MoveController moveController = di.moveController;
        
        final Unit unit = guiGameModel.getActiveUnit();
        if (unit == null) {
            System.out.println("no unit selected");
            return;
        }
        System.out.println("the best move");

        
        final PathFinder pathFinder = new PathFinder();
        pathFinder.generateRangeMap(guiGameModel.game.map, unit.getTile(), unit);
        
        NavyExplorer navyExplorer = new NavyExplorer(guiGameModel.game.map);
        navyExplorer.generateExploreDestination(pathFinder, unit.getOwner());
        
        if (navyExplorer.isFoundExploreDestination()) {
            if (navyExplorer.isExploreDestinationInOneTurn()) {
                Direction direction = navyExplorer.getExploreDestinationAsDirection();
                System.out.println("exploration destination " + direction);
                moveController.pressDirectionKey(direction);
            } else {
                System.out.println("exploration path " + navyExplorer.getExploreDestinationAsPath());
            }
        } else {
            // maybe is everything explored or blocked in some how
            System.out.println("can not find tile to explore");
        }
        
        final String tileStrings[][] = new String[guiGameModel.game.map.height][guiGameModel.game.map.width];
        navyExplorer.toStringsBorderValues(tileStrings);
        mapActor.showTileDebugStrings(tileStrings);
    }

    public void showCheatConsoleDialog() {
        DebugConsole debugConsole = new DebugConsole(di.guiGameController, di.guiGameModel);
        debugConsole.setSelectedTile(mapActor.mapDrawModel().selectedTile);
        hudStage.showDialog(debugConsole);
    }
    
}
