package net.sf.freecol.common.model.ai.missions.workerrequest;

import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.utils.ObjectIntMap;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.ai.ObjectsListScore;
import promitech.colonization.ai.ObjectsListScore.ObjectScore;
import promitech.map.isometric.NeighbourIterableTile;

public class ColonyWorkerRequestPlaner {

	private final Player player;
	private final List<GoodsType> goodsTypeToScoreByPrice;
	
	public ColonyWorkerRequestPlaner(Player player) {
		this.player = player;
		this.goodsTypeToScoreByPrice = Specification.instance.goodsTypeToScoreByPrice;
	}
	
	public void plan() {
		workerForColony();
		workerForCreateColony();
	}
	
	private void workerForCreateColony() {
		
	}
	
	private void workerForColony() {
		ObjectsListScore<ColonyWorkerReqScore> colonyWorkerReqScores = new ObjectsListScore<ColonyWorkerReqScore>(player.settlements.size());
		for (Settlement settlement : player.settlements) {
			ColonyWorkerReqScore colonyWorkerReq = new ColonyWorkerReqScore(settlement.asColony(), goodsTypeToScoreByPrice);
			ObjectsListScore<UnitType> score = colonyWorkerReq.simulate();
			colonyWorkerReqScores.add(colonyWorkerReq, score.scoreSum());
		}
		colonyWorkerReqScores.sortDescending();
		
		for (ObjectScore<ColonyWorkerReqScore> colonyWorkerReq : colonyWorkerReqScores) {
			System.out.println(colonyWorkerReq.getScore() + " " + colonyWorkerReq.getObj().getColony().getName());
			ObjectsListScore<UnitType> workersReq = colonyWorkerReq.getObj().simulate();
			for (ObjectScore<UnitType> objectScore : workersReq) {
				System.out.println("  " + objectScore.getScore() + " " + objectScore.getObj());
			}
		}
	}
	
	private Colony colonyWorkerDestination(Unit unit, ObjectsListScore<ColonyWorkerReqScore> colonyWorkerReqScores) {
		Colony colony = null;
		for (ObjectScore<ColonyWorkerReqScore> objectScore : colonyWorkerReqScores) {
			ColonyWorkerReqScore colonyWorkerReqScore = objectScore.getObj();
			colony = colonyWorkerReqScore.getColony();
			if (!hasDeliverWorkerMission(colonyWorkerReqScore)) {
				break;
			}
		}
		return colony;
	}

	private boolean hasDeliverWorkerMission(ColonyWorkerReqScore colonyWorkerReqScore) {
		colonyWorkerReqScore.simulate().size();
		// TODO: check deliver worker to colony for work, 
		// it is possible to have mission but no more then colonyWorkerReqScore.simulate().size()  
		return false;
	}
	
	
	
}
