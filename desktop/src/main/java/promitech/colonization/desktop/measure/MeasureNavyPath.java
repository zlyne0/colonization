package promitech.colonization.desktop.measure;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.SaveGameParser;
import promitech.measure.MeasureTask;
import promitech.measure.Measurement;
import promitech.measure.MeasurementResult;

public class MeasureNavyPath implements MeasureTask {
    
    Game game;
    PathFinder sut = new PathFinder();
    Path path;
    
    Tile startTile;
    Tile endTile;
    Tile startTile2;
    Tile endTile2;
    Unit moveUnit;
    
    public void before() throws Exception {
        Gdx.files = new Lwjgl3Files();
        game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");

        // init map
        startTile = game.map.getTile(12, 79);
        moveUnit = startTile.getUnits().getById("unit:6900");

        startTile2 = game.map.getTile(26, 77);
        endTile2 = game.map.getTile(28, 72);

        {
            // create fortress colony 
            Player fortressOwner = game.players.getById("player:112");
            Colony fortressColony = new Colony(
        		Game.idGenerator,
        		fortressOwner.nationType().getSettlementRegularType()
    		);
            fortressColony.setOwner(fortressOwner);
            fortressColony.addBuilding(Specification.instance.buildingTypes.getById("model.building.fortress"));
            fortressColony.updateColonyFeatures();
    
            Tile fortressTile = game.map.getTile(27, 75);
            fortressTile.setSettlement(fortressColony);
        }
    }

    @Override
    public void run() {
        path = sut.findToEurope(game.map, startTile, moveUnit, PathFinder.excludeUnexploredTiles);
        path = sut.findToTile(game.map, startTile2, endTile2, moveUnit, PathFinder.excludeUnexploredTiles);
    }
    
    public static void main(String args[]) throws Exception {
        /**
         * Should be run with -Xint -Djava.compiler=NONE jvm params.
         */
        MeasureNavyPath mObject = new MeasureNavyPath();
        mObject.before();
        
        Measurement m = new Measurement(100);
        MeasurementResult result = m.measure(mObject);
        
        System.out.println("result " + result);
        result.xxx();
    }
}
