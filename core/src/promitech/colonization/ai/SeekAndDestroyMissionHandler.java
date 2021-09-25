package promitech.colonization.ai;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.UnitMoveType;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.SeekAndDestroyMission;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import promitech.colonization.SpiralIterator;
import promitech.colonization.orders.combat.CombatService;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;

public class SeekAndDestroyMissionHandler implements MissionHandler<SeekAndDestroyMission> {

    private final MoveService moveService;
    private final CombatService combatService;
    private final Game game;
    private final PathFinder pathFinder;
    private final UnitMoveType unitMoveType = new UnitMoveType();
    
    public SeekAndDestroyMissionHandler(Game game, MoveService moveService, CombatService combatService, PathFinder pathFinder) {
        this.moveService = moveService;
        this.combatService = combatService;
        this.game = game;
        this.pathFinder = pathFinder;
    }
    
    @Override
    public void handle(PlayerMissionsContainer playerMissionsContainer, SeekAndDestroyMission mission) {
    	if (mission.unit == null || mission.unit.isDisposed() || mission.unit.isDamaged()) {
    		mission.setDone();
    		return;
    	}
        Tile enemyTile = enemyTile(mission);
        System.out.println("enemy tile : " + enemyTile);
        if (enemyTile == null) {
            return;
        }
        
        Path pathToEnemy = pathFinder.findToTile(
            game.map, 
            mission.unit.getTileLocationOrNull(), enemyTile, 
            mission.unit,
            PathFinder.includeUnexploredAndNavyThreatTiles
        );

        System.out.println("path to enemy = " + pathToEnemy);
        
        MoveContext moveContext = new MoveContext(mission.unit, pathToEnemy);
        moveContext.initNextPathStep();
        while (moveContext.canHandleMove()) {
            if (moveContext.isMoveType(MoveType.ATTACK_UNIT) 
            		|| moveContext.isMoveType(MoveType.ATTACK_SETTLEMENT)
    		) {
                System.out.println("attack");
                combatService.doCombat(moveContext);
                break;
            }
            moveService.aiConfirmedMoveProcessor(moveContext);
            moveContext.initNextPathStep();
        }
    }
    
    private Tile enemyTile(SeekAndDestroyMission mission) {
        unitMoveType.init(mission.unit);

        Map map = game.map;
        Tile unitTileLocation = mission.unit.getTileLocationOrNull();
        
        int radius = mission.unit.lineOfSight();
        SpiralIterator spiralIterator = new SpiralIterator(map.width, map.height);
        spiralIterator.reset(unitTileLocation.x, unitTileLocation.y, true, radius);
        
        while (spiralIterator.hasNext()) {
            Tile destTile = map.getSafeTile(spiralIterator.getX(), spiralIterator.getY());
            MoveType moveType = unitMoveType.calculateMoveType(unitTileLocation, destTile);
            if (moveType == MoveType.ATTACK_UNIT || moveType == MoveType.ATTACK_SETTLEMENT) {
                return destTile;
            }
            spiralIterator.next();
        }
        return null;
    }
}
