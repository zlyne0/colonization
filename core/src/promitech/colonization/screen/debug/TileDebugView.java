package promitech.colonization.screen.debug;

import net.sf.freecol.common.model.Tile;
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

    public void str(Tile tile, String str) {
        str(tile.x, tile.y, str);
    }

    @Override
    public void str(int x, int y, String str) {
        initTileStrTab();
        debugTileStrTab[y][x] = str;
    }

    @Override
    public void strIfNull(int x, int y, String str) {
        initTileStrTab();
        if (debugTileStrTab[y][x] == null) {
            debugTileStrTab[y][x] = str;
        }
    }

    @Override
    public void appendStr(int x, int y, String str) {
        initTileStrTab();
        String actualStr = debugTileStrTab[y][x];
        if (actualStr == null) {
            actualStr = str;
        } else {
            actualStr += " " + str;
        }
        debugTileStrTab[y][x] = actualStr;
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
    
	public void reset() {
		if (debugTileStrTab == null) {
			return;
		}
		for (int y = 0; y < debugTileStrTab.length; y++) {
			for (int x = 0; x < debugTileStrTab[y].length; x++) {
				debugTileStrTab[y][x] = null;
			}
		}
	}
}
