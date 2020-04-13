package promitech.colonization.ai;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.DemandTributeMission;
import net.sf.freecol.common.model.ai.missions.ExplorerMission;
import net.sf.freecol.common.model.ai.missions.FoundColonyMission;
import net.sf.freecol.common.model.ai.missions.IndianBringGiftMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.RellocationMission;
import net.sf.freecol.common.model.ai.missions.WanderMission;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.map.path.TransportPathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.orders.NewTurnService;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.hud.GUIGameController;

public class AILogic {

    private final Map<Class<? extends AbstractMission>, MissionHandler<? extends AbstractMission>> missionHandlerMapping = 
    		new HashMap<Class<? extends AbstractMission>, MissionHandler<? extends AbstractMission>>();
    
	private final Game game;
	private final NewTurnService newTurnService;
	private final PathFinder pathFinder = new PathFinder();
	private final TransportPathFinder transportPathFinder;

	private final ExplorerMissionHandler explorerMissionHandler;
	private final WanderMissionHandler wanderMissionHandler;
	private final FoundColonyMissionHandler foundColonyMissionHandler;
	private final RellocationMissionHandler rellocationMissionHandler;
	
	private final NativeMissionPlaner nativeMissionPlaner;
	private final EuropeanMissionPlaner europeanMissionPlaner;
	
	public AILogic(Game game, NewTurnService newTurnService, MoveService moveService, GUIGameController guiGameController) {
		this.game = game;
		this.newTurnService = newTurnService;
		
		nativeMissionPlaner = new NativeMissionPlaner(pathFinder);
        transportPathFinder = new TransportPathFinder(game.map);
		
		explorerMissionHandler = new ExplorerMissionHandler(game, pathFinder, moveService);
		wanderMissionHandler = new WanderMissionHandler(game, moveService);
        foundColonyMissionHandler = new FoundColonyMissionHandler(pathFinder, game);
        rellocationMissionHandler = new RellocationMissionHandler(pathFinder, transportPathFinder, game, moveService);
		
        europeanMissionPlaner = new EuropeanMissionPlaner(foundColonyMissionHandler);

        IndianBringGiftMissionHandler indianBringGiftMission = new IndianBringGiftMissionHandler(
    		game, pathFinder, moveService, guiGameController
		);
        DemandTributeMissionHandler demandTributeMissionHandler = new DemandTributeMissionHandler(
    		game, pathFinder, moveService, guiGameController
		);
        
        missionHandlerMapping.put(FoundColonyMission.class, foundColonyMissionHandler);
        missionHandlerMapping.put(RellocationMission.class, rellocationMissionHandler);
        missionHandlerMapping.put(WanderMission.class, wanderMissionHandler);
        missionHandlerMapping.put(ExplorerMission.class, explorerMissionHandler);
		missionHandlerMapping.put(IndianBringGiftMission.class, indianBringGiftMission);
		missionHandlerMapping.put(DemandTributeMission.class, demandTributeMissionHandler);
	}
	
	public void aiNewTurn(Player player) {
		newTurnService.newTurn(player);
		
		if (player.isIndian()) {
			PlayerMissionsContainer playerMissionContainer = game.aiContainer.getMissionContainer(player);
			nativeMissionPlaner.prepareIndianWanderMissions(player, playerMissionContainer);
			executeMissions(playerMissionContainer);
		}
		
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
        	executedAllLeafs(missionsContainer, am);
        }
        missionsContainer.clearDoneMissions();
	}

    private void executedAllLeafs(PlayerMissionsContainer missionsContainer, AbstractMission am) {
        if (!am.hasDependMissions()) {
            executeSingleMission(missionsContainer, am);
            return;
        }

        HashSet<AbstractMission> executedMissions = new HashSet<AbstractMission>();
        HashSet<AbstractMission> leafMissionToExecute = new HashSet<AbstractMission>();

        leafMissionToExecute.addAll(am.getLeafMissionToExecute());
        while (!leafMissionToExecute.isEmpty()) {
            boolean foundDoneMission = false;
            for (AbstractMission abs : leafMissionToExecute) {
                if (!executedMissions.contains(abs)) {
                    executeSingleMission(missionsContainer, abs);
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void executeSingleMission(PlayerMissionsContainer missionsContainer, AbstractMission am) {
        //System.out.println("execute mission: " + am);
        MissionHandler missionHandler = missionHandlerMapping.get(am.getClass());
		missionHandler.handle(missionsContainer, am);
    }
	
	
}
