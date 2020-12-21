package promitech.colonization.screen.debug;

import net.sf.freecol.common.model.ai.MapTileDebugInfo;
import net.sf.freecol.common.model.map.path.Path;
import promitech.colonization.screen.map.MapActor;
import promitech.colonization.screen.map.hud.GUIGameModel;

public class TileDebugView implements MapTileDebugInfo {

    private final MapActor mapActor;
    private final GUIGameModel gameModel;
    private String[][] debugTileStrTab;
    
    public TileDebugView(MapActor mapActor, GUIGameModel gameModel) {
        this.gameModel = gameModel;
        this.mapActor = mapActor;
    }

    public boolean isDebug() {
        return true;
    }

    @Override
    public void str(int x, int y, String str) {
        initTileStrTab();
        debugTileStrTab[y][x] = str;
    }

    public String[][] getDebugTileStrTab() {
        initTileStrTab();
        return debugTileStrTab;
    }
    
    private void initTileStrTab() {
        if (debugTileStrTab == null) {
            debugTileStrTab = new String[gameModel.game.map.height][gameModel.game.map.width];
            mapActor.showTileDebugStrings(debugTileStrTab);
        }
    }

    public void showPath(Path path) {
        mapActor.mapDrawModel().unitPath = path;
    }
    
}
