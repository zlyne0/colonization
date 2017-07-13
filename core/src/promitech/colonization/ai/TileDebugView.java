package promitech.colonization.ai;

import promitech.colonization.GUIGameModel;
import promitech.colonization.actors.map.MapActor;

public class TileDebugView {

    private final MapActor mapActor;
    private final GUIGameModel gameModel;
    private String[][] debugPathRange;
    
    public TileDebugView(MapActor mapActor, GUIGameModel gameModel) {
        this.gameModel = gameModel;
        this.mapActor = mapActor;
    }

    public boolean isDebug() {
        return false;
    }

    public void debug(int x, int y, String str) {
        if (debugPathRange == null) {
            debugPathRange = new String[gameModel.game.map.height][gameModel.game.map.width];
            mapActor.showTileDebugStrings(debugPathRange);
        }
        debugPathRange[y][x] = str;
    }
    
}
