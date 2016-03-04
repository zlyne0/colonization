package promitech.colonization.desktop.measure;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.Path;
import net.sf.freecol.common.model.map.PathFinder;
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
    Unit privateer;
    
    public void before() throws Exception {
        Gdx.files = new LwjglFiles();
        SaveGameParser saveGameParser = new SaveGameParser("maps/savegame_1600_for_jtests.xml");
        game = saveGameParser.parse();

        // init map
        startTile = game.map.getTile(26, 77);
        endTile = game.map.getTile(28, 72);

        Tile privateerTile = game.map.getTile(12, 79);
        privateer = privateerTile.units.getById("unit:6900");
        privateerTile.units.removeId(privateer);
        startTile.units.add(privateer);

        Player fortressOwner = game.players.getById("player:112");
        Colony fortressColony = new Colony("colony:-1");
        fortressColony.setOwner(fortressOwner);

        Building fortressBuilding = new Building("building:-1");
        fortressBuilding.buildingType = Specification.instance.buildingTypes.getById("model.building.fortress");
        fortressColony.buildings.add(fortressBuilding);
        fortressColony.updateColonyFeatures();

        Tile fortressTile = game.map.getTile(27, 75);
        fortressTile.setSettlement(fortressColony);
    }

    @Override
    public void run() {
        path = sut.findToTile(game.map, startTile, endTile, privateer);
    }
    
    public static void main(String args[]) throws Exception {
        MeasureNavyPath mObject = new MeasureNavyPath();
        mObject.before();
        
        Measurement m = new Measurement(10);
        MeasurementResult result = m.measure(mObject);
        
        System.out.println("result " + result);
    }
}
