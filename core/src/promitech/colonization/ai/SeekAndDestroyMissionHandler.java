package promitech.colonization.ai;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.SeekAndDestroyMission;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import promitech.colonization.SpiralIterator;

public class SeekAndDestroyMissionHandler implements MissionHandler<SeekAndDestroyMission> {

    private final Game game;
    private final PathFinder pathFinder;
    
    public SeekAndDestroyMissionHandler(Game game) {
        this.game = game;
        this.pathFinder = new PathFinder();
    }
    
    @Override
    public void handle(PlayerMissionsContainer playerMissionsContainer, SeekAndDestroyMission mission) {
        Tile enemyTile = enemyTile(mission);
        System.out.println("enemy tile : " + enemyTile);
        if (enemyTile == null) {
            return;
        }
        
        Path pathToEnemy = pathFinder.findToTile(
            game.map, 
            mission.unit.getTileLocationOrNull(), enemyTile, 
            mission.unit, 
            false, true
        );
        System.out.println("path to enemy = " + pathToEnemy);
    }
    
    private Tile enemyTile(SeekAndDestroyMission mission) {
        Map map = game.map;
        Tile unitTileLocation = mission.unit.getTileLocationOrNull();
        
        int radius = mission.unit.lineOfSight();
        SpiralIterator spiralIterator = new SpiralIterator(map.width, map.height);
        spiralIterator.reset(unitTileLocation.x, unitTileLocation.y, true, radius);
        
        while (spiralIterator.hasNext()) {
            Tile destTile = map.getSafeTile(spiralIterator.getX(), spiralIterator.getY());
            MoveType moveType = mission.unit.getMoveType(unitTileLocation, destTile);
            if (moveType == MoveType.ATTACK_UNIT) {
                return destTile;
            }
            spiralIterator.next();
        }
        return null;
    }
}
