package promitech.colonization.ai;

import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.ExplorerMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.TransportUnitMission;
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMission;
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMissionHandler;
import net.sf.freecol.common.model.ai.missions.indian.DemandTributeMission;
import net.sf.freecol.common.model.ai.missions.indian.DemandTributeMissionHandler;
import net.sf.freecol.common.model.ai.missions.indian.IndianBringGiftMission;
import net.sf.freecol.common.model.ai.missions.indian.IndianBringGiftMissionHandler;
import net.sf.freecol.common.model.ai.missions.indian.WanderMission;
import net.sf.freecol.common.model.ai.missions.indian.WanderMissionHandler;
import net.sf.freecol.common.model.ai.missions.scout.ScoutMission;
import net.sf.freecol.common.model.ai.missions.scout.ScoutMissionHandler;
import net.sf.freecol.common.model.ai.missions.scout.ScoutMissionPlaner;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerMission;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerMissionHandler;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.orders.combat.CombatService;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.hud.GUIGameController;

import static promitech.colonization.ai.MissionHandlerLogger.*;

public class MissionExecutor implements Disposable {

    private final Map<Class<? extends AbstractMission>, MissionHandler<? extends AbstractMission>> missionHandlerMapping = 
    		new HashMap<Class<? extends AbstractMission>, MissionHandler<? extends AbstractMission>>();

	private final Game game;

	private final ExplorerMissionHandler explorerMissionHandler;
	private final WanderMissionHandler wanderMissionHandler;

	public MissionExecutor(
		Game game, 
		MoveService moveService, 
		CombatService combatService, 
		GUIGameController guiGameController,
		PathFinder pathFinder,
        PathFinder pathFinder2
	) {
		this.game = game;

		explorerMissionHandler = new ExplorerMissionHandler(game, pathFinder, moveService);
		wanderMissionHandler = new WanderMissionHandler(game, moveService);

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
    		this, game, pathFinder, moveService, pathFinder2
		);
        ColonyWorkerMissionHandler colonyWorkerMissionHandler = new ColonyWorkerMissionHandler(
    		game, pathFinder, moveService
		);
        ScoutMissionHandler scoutMissionHandler = new ScoutMissionHandler(
            game, new ScoutMissionPlaner(game, pathFinder, pathFinder2), moveService
        );

        missionHandlerMapping.put(WanderMission.class, wanderMissionHandler);
        missionHandlerMapping.put(ExplorerMission.class, explorerMissionHandler);
		missionHandlerMapping.put(IndianBringGiftMission.class, indianBringGiftMission);
		missionHandlerMapping.put(DemandTributeMission.class, demandTributeMissionHandler);
		missionHandlerMapping.put(TransportGoodsToSellMission.class, transportGoodsToSellMissionHandler);
		missionHandlerMapping.put(TransportUnitMission.class, transportUnitMissionHandler);
		missionHandlerMapping.put(ColonyWorkerMission.class, colonyWorkerMissionHandler);
        missionHandlerMapping.put(ScoutMission.class, scoutMissionHandler);
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

        MissionHandler missionHandler = findMissionHandler(am);
		missionHandler.handle(missionsContainer, am);
		if (am.isDone()) {
			missionsContainer.unblockUnitsFromMission(am);
		}
    }

    public <T extends AbstractMission> MissionHandler<T> findMissionHandler(T am) {
        MissionHandler<T> missionHandler = (MissionHandler<T>)missionHandlerMapping.get(am.getClass());
        if (missionHandler == null) {
            throw new IllegalStateException("can not find missionHandler for mission type " + am.getClass());
        }
        return missionHandler;
    }

    @Override
    public void dispose() {
        for (MissionHandler<? extends AbstractMission> missionHandler : missionHandlerMapping.values()) {
            if (missionHandler instanceof Disposable) {
                ((Disposable) missionHandler).dispose();
            }
        }
    }
}
