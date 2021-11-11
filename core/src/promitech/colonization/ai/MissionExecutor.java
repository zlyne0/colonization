package promitech.colonization.ai;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.ExplorerMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.RellocationMission;
import net.sf.freecol.common.model.ai.missions.TransportUnitMission;
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMission;
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMissionHandler;
import net.sf.freecol.common.model.ai.missions.indian.DemandTributeMission;
import net.sf.freecol.common.model.ai.missions.indian.DemandTributeMissionHandler;
import net.sf.freecol.common.model.ai.missions.indian.IndianBringGiftMission;
import net.sf.freecol.common.model.ai.missions.indian.IndianBringGiftMissionHandler;
import net.sf.freecol.common.model.ai.missions.indian.WanderMission;
import net.sf.freecol.common.model.ai.missions.indian.WanderMissionHandler;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerMission;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerMissionHandler;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.map.path.TransportPathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.orders.combat.CombatService;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.hud.GUIGameController;

import static promitech.colonization.ai.MissionHandlerLogger.*;

public class MissionExecutor {

    private final Map<Class<? extends AbstractMission>, MissionHandler<? extends AbstractMission>> missionHandlerMapping = 
    		new HashMap<Class<? extends AbstractMission>, MissionHandler<? extends AbstractMission>>();

	private final Game game;
	private final TransportPathFinder transportPathFinder;
	
	
	private final ExplorerMissionHandler explorerMissionHandler;
	private final WanderMissionHandler wanderMissionHandler;
	private final RellocationMissionHandler rellocationMissionHandler;
    
	public MissionExecutor(
		Game game, 
		MoveService moveService, 
		CombatService combatService, 
		GUIGameController guiGameController,
		PathFinder pathFinder
	) {
		this.game = game;
		
        transportPathFinder = new TransportPathFinder(game.map);
        
		explorerMissionHandler = new ExplorerMissionHandler(game, pathFinder, moveService);
		wanderMissionHandler = new WanderMissionHandler(game, moveService);
        rellocationMissionHandler = new RellocationMissionHandler(pathFinder, transportPathFinder, game, moveService);
        
        IndianBringGiftMissionHandler indianBringGiftMission = new IndianBringGiftMissionHandler(
    		game, pathFinder, moveService, guiGameController
		);
        DemandTributeMissionHandler demandTributeMissionHandler = new DemandTributeMissionHandler(
    		game, pathFinder, moveService, combatService, guiGameController
		);
        TransportGoodsToSellMissionHandler transportGoodsToSellMissionHandler = new TransportGoodsToSellMissionHandler(
    		game, pathFinder, moveService
		);
        TransportUnitMissionHandler transportUnitMissionHandler = new TransportUnitMissionHandler(
    		game, pathFinder, moveService, new PathFinder()
		);
        ColonyWorkerMissionHandler colonyWorkerMissionHandler = new ColonyWorkerMissionHandler(
    		game, pathFinder, moveService
		);
        
        
        missionHandlerMapping.put(RellocationMission.class, rellocationMissionHandler);
        missionHandlerMapping.put(WanderMission.class, wanderMissionHandler);
        missionHandlerMapping.put(ExplorerMission.class, explorerMissionHandler);
		missionHandlerMapping.put(IndianBringGiftMission.class, indianBringGiftMission);
		missionHandlerMapping.put(DemandTributeMission.class, demandTributeMissionHandler);
		missionHandlerMapping.put(TransportGoodsToSellMission.class, transportGoodsToSellMissionHandler);
		missionHandlerMapping.put(TransportUnitMission.class, transportUnitMissionHandler);
		missionHandlerMapping.put(ColonyWorkerMission.class, colonyWorkerMissionHandler);
	}
    
	public void executeMissions(Player player) {
		PlayerMissionsContainer missionsContainer = game.aiContainer.missionContainer(player);
        for (AbstractMission am : missionsContainer.getMissions().entities()) {
        	if (am.isDone()) {
        		continue;
        	}
        	executedAllLeafs(missionsContainer, am);
        }
        missionsContainer.clearDoneMissions();
	}

	public <T extends AbstractMission> void executeMissions(PlayerMissionsContainer missionsContainer, Class<T> missionClass) {
        List<T> missions = missionsContainer.findMissions(missionClass);
        for (T mission : missions) {
            if (!mission.isDone() && !mission.hasDependMissions()) {
                executeSingleMission(missionsContainer, mission);
            }
        }
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
	    if (logger.isDebug()) {
    	    logger.debug("player[%s].executeMission[%s] %s", missionsContainer.getPlayer().getId(), am.getId(), am.toString());
        }

        MissionHandler missionHandler = missionHandlerMapping.get(am.getClass());
        if (missionHandler == null) {
        	throw new IllegalStateException("can not find missionHandler for mission type " + am.getClass());
        }
		missionHandler.handle(missionsContainer, am);
		if (am.isDone()) {
			missionsContainer.unblockUnitsFromMission(am);
		}
    }
}
