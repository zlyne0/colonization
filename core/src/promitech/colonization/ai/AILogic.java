package promitech.colonization.ai;

import java.util.HashSet;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ai.AbstractMission;
import net.sf.freecol.common.model.ai.PlayerMissionsContainer;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.map.path.TransportPathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.GameLogic;
import promitech.colonization.MoveLogic;

public class AILogic {

	private final Game game;
	private final GameLogic gameLogic;
	private final PathFinder pathFinder = new PathFinder();
	private final TransportPathFinder transportPathFinder;

	private final ExplorerMissionHandler explorerMissionHandler;
	private final WanderMissionHandler wanderMissionHandler;
	private final FoundColonyMissionHandler foundColonyMissionHandler;
	private final RellocationMissionHandler rellocationMissionHandler;
	
	private final NativeMissionPlaner nativeMissionPlaner = new NativeMissionPlaner();
	private final EuropeanMissionPlaner europeanMissionPlaner;
	
	public AILogic(Game game, GameLogic gameLogic, MoveLogic moveLogic) {
		this.game = game;
		this.gameLogic = gameLogic;
		
        transportPathFinder = new TransportPathFinder(game.map);
		
		explorerMissionHandler = new ExplorerMissionHandler(game, pathFinder, moveLogic);
		wanderMissionHandler = new WanderMissionHandler(game, moveLogic);
        foundColonyMissionHandler = new FoundColonyMissionHandler(pathFinder, game);
        rellocationMissionHandler = new RellocationMissionHandler(pathFinder, transportPathFinder, game, moveLogic);
		
        europeanMissionPlaner = new EuropeanMissionPlaner(foundColonyMissionHandler);
        
	}
	
	public void aiNewTurn(Player player) {
		gameLogic.newTurn(player);
		
//		if (player.isIndian()) {
//			PlayerMissionsContainer playerMissionContainer = game.aiContainer.getMissionContainer(player);
//			nativeMissionPlaner.prepareIndianWanderMissions(player, playerMissionContainer);
//			
//			executeMissions(playerMissionContainer);
//		}
		if (player.isLiveEuropeanPlayer()) {
			PlayerMissionsContainer playerMissionContainer = game.aiContainer.getMissionContainer(player);
			
			europeanMissionPlaner.prepareMissions(player, playerMissionContainer);

			executeMissions(playerMissionContainer);
		}
		
        System.out.println("end of newTurn");
	}
	
	private void executeMissions(PlayerMissionsContainer missionsContainer) {
        for (AbstractMission am : missionsContainer.getMissions().entities()) {
        	if (am.isDone()) {
        		continue;
        	}
        	executedAllLeafs(am);
        }
        missionsContainer.clearDoneMissions();
	}

    private void executedAllLeafs(AbstractMission am) {
        if (!am.hasDependMissions()) {
            executeSingleMission(am);
            return;
        }

        HashSet<AbstractMission> executedMissions = new HashSet<AbstractMission>();
        HashSet<AbstractMission> leafMissionToExecute = new HashSet<AbstractMission>();

        leafMissionToExecute.addAll(am.getLeafMissionToExecute());
        while (!leafMissionToExecute.isEmpty()) {
            boolean foundDoneMission = false;
            for (AbstractMission abs : leafMissionToExecute) {
                if (!executedMissions.contains(abs)) {
                    executeSingleMission(abs);
                    executedMissions.add(abs);
                    if (abs.isDone()) {
                        foundDoneMission = true;
                    }
                }
            }
            leafMissionToExecute.clear();
            if (foundDoneMission) {
                leafMissionToExecute.addAll(am.getLeafMissionToExecute());

                for (AbstractMission executedMission : executedMissions) {
                    leafMissionToExecute.remove(executedMission);
                }
            }
        }
    }
	
    private void executeSingleMission(AbstractMission am) {
        System.out.println("execute mission: " + am);
        
        if (am instanceof FoundColonyMission) {
            FoundColonyMission foundColonyMission = (FoundColonyMission)am;
            foundColonyMissionHandler.handle(foundColonyMission);
        }
        if (am instanceof RellocationMission) {
            RellocationMission rellocationMission = (RellocationMission)am;
            rellocationMissionHandler.handle(rellocationMission);
        }
        if (am instanceof WanderMission) {
        	wanderMissionHandler.executeMission((WanderMission)am);
        }
        // TODO: explore mission handler
    }
	
	
}
