package promitech.colonization.ai;

import com.badlogic.gdx.utils.Disposable;

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
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerMission;
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerMissionHandler;
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerMissionPlaner;
import net.sf.freecol.common.model.ai.missions.pioneer.RequestGoodsMission;
import net.sf.freecol.common.model.ai.missions.pioneer.RequestGoodsMissionHandler;
import net.sf.freecol.common.model.ai.missions.scout.ScoutMission;
import net.sf.freecol.common.model.ai.missions.scout.ScoutMissionHandler;
import net.sf.freecol.common.model.ai.missions.scout.ScoutMissionPlaner;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerMission;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerMissionHandler;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import promitech.colonization.orders.combat.CombatService;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.hud.GUIGameController;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

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
        PioneerMissionHandler pioneerMissionHandler = new PioneerMissionHandler(
            game, new PioneerMissionPlaner(game, pathFinder), moveService, pathFinder
        );

        missionHandlerMapping.put(WanderMission.class, wanderMissionHandler);
        missionHandlerMapping.put(ExplorerMission.class, explorerMissionHandler);
		missionHandlerMapping.put(IndianBringGiftMission.class, indianBringGiftMission);
		missionHandlerMapping.put(DemandTributeMission.class, demandTributeMissionHandler);
		missionHandlerMapping.put(TransportGoodsToSellMission.class, transportGoodsToSellMissionHandler);
		missionHandlerMapping.put(TransportUnitMission.class, transportUnitMissionHandler);
		missionHandlerMapping.put(ColonyWorkerMission.class, colonyWorkerMissionHandler);
        missionHandlerMapping.put(ScoutMission.class, scoutMissionHandler);
        missionHandlerMapping.put(PioneerMission.class, pioneerMissionHandler);
        missionHandlerMapping.put(RequestGoodsMission.class, new RequestGoodsMissionHandler());
	}
    
	public void executeMissions(Player player) {
		PlayerMissionsContainer missionsContainer = game.aiContainer.missionContainer(player);

        List<AbstractMission> missionToExecute = missionsContainer.findMissionToExecute();

        while (!missionToExecute.isEmpty()) {
            AbstractMission mission = missionToExecute.remove(missionToExecute.size() - 1);
            executeSingleMission(missionsContainer, mission);

            if (mission.isDone()) {
                AbstractMission parentMission = missionsContainer.findParentToExecute(mission);
                if (parentMission != null) {
                    missionToExecute.add(parentMission);
                }
            }
        }
        missionsContainer.clearDoneMissions();
	}

	public <T extends AbstractMission> void executeMissions(PlayerMissionsContainer missionsContainer, Class<T> missionClass) {
        List<T> missions = missionsContainer.findMissions(missionClass);
        for (T mission : missions) {
            if (!mission.isDone() && missionsContainer.isAllDependMissionDone(mission)) {
                executeSingleMission(missionsContainer, mission);
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

    public Collection<MissionHandler<? extends AbstractMission>> allMissionHandlers() {
	    return missionHandlerMapping.values();
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
