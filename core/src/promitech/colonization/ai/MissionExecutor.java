package promitech.colonization.ai;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.ExplorerMission;
import net.sf.freecol.common.model.ai.missions.FoundColonyMission;
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
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.map.path.TransportPathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.orders.combat.CombatService;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.hud.GUIGameController;

public class MissionExecutor {

    private final Map<Class<? extends AbstractMission>, MissionHandler<? extends AbstractMission>> missionHandlerMapping = 
    		new HashMap<Class<? extends AbstractMission>, MissionHandler<? extends AbstractMission>>();

	private final Game game;
	private final TransportPathFinder transportPathFinder;
	
	
	private final ExplorerMissionHandler explorerMissionHandler;
	private final WanderMissionHandler wanderMissionHandler;
	private final FoundColonyMissionHandler foundColonyMissionHandler;
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
        foundColonyMissionHandler = new FoundColonyMissionHandler(pathFinder, game);
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
    		game, pathFinder, moveService
		);
        
        missionHandlerMapping.put(FoundColonyMission.class, foundColonyMissionHandler);
        missionHandlerMapping.put(RellocationMission.class, rellocationMissionHandler);
        missionHandlerMapping.put(WanderMission.class, wanderMissionHandler);
        missionHandlerMapping.put(ExplorerMission.class, explorerMissionHandler);
		missionHandlerMapping.put(IndianBringGiftMission.class, indianBringGiftMission);
		missionHandlerMapping.put(DemandTributeMission.class, demandTributeMissionHandler);
		missionHandlerMapping.put(TransportGoodsToSellMission.class, transportGoodsToSellMissionHandler);
		missionHandlerMapping.put(TransportUnitMission.class, transportUnitMissionHandler);
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
    	MissionHandlerLogger.logger.debug("executeMission[%s]", am.getId());
    	
        MissionHandler missionHandler = missionHandlerMapping.get(am.getClass());
		missionHandler.handle(missionsContainer, am);
		if (am.isDone()) {
			missionsContainer.unblockUnitsFromMission(am);
		}
    }
}
