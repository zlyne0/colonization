package promitech.colonization.ai;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.SeekAndDestroyMission;
import promitech.colonization.SpiralIterator;

public class SeekAndDestroyMissionHandler implements MissionHandler<SeekAndDestroyMission> {

    private final Game game;
    
    public SeekAndDestroyMissionHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(PlayerMissionsContainer playerMissionsContainer, SeekAndDestroyMission mission) {
        Tile enemyTile = enemyTile(mission);
        System.out.println("enemy tile : " + enemyTile);
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
