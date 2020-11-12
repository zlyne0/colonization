package net.sf.freecol.common.model.ai;

import java.util.List;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.ai.ObjectsListScore;
import promitech.colonization.ai.ObjectsListScore.ObjectScore;

public class ColonyWorkerRequestPlaner {

	public void plan(Player player) {
		List<GoodsType> goodsTypeToScoreByPrice = Specification.instance.goodsTypeToScoreByPrice;
		
		ObjectsListScore<ColonyWorkerReq> colonyWorkerReqScores = new ObjectsListScore<ColonyWorkerReq>(player.settlements.size());
		for (Settlement settlement : player.settlements) {
			ColonyWorkerReq colonyWorkerReq = new ColonyWorkerReq(settlement.asColony(), goodsTypeToScoreByPrice);
			ObjectsListScore<UnitType> score = colonyWorkerReq.simulate();
			colonyWorkerReqScores.add(colonyWorkerReq, score.scoreSum());
		}
		colonyWorkerReqScores.sortDescending();
		
		for (ObjectScore<ColonyWorkerReq> colonyWorkerReq : colonyWorkerReqScores) {
			System.out.println(colonyWorkerReq.getScore() + " " + colonyWorkerReq.getObj().getColony().getName());
			ObjectsListScore<UnitType> workersReq = colonyWorkerReq.getObj().simulate();
			for (ObjectScore<UnitType> objectScore : workersReq) {
				System.out.println("  " + objectScore.getScore() + " " + objectScore.getObj());
			}
		}
	}
	
	private Colony colonyWorkerDestination(Unit unit, ObjectsListScore<ColonyWorkerReq> colonyWorkerReqScores) {
		Colony colony = null;
		for (ObjectScore<ColonyWorkerReq> objectScore : colonyWorkerReqScores) {
			ColonyWorkerReq colonyWorkerReqScore = objectScore.getObj();
			colony = colonyWorkerReqScore.getColony();
			if (!hasDeliverWorkerMission(colonyWorkerReqScore)) {
				break;
			}
		}
		return colony;
	}

	private boolean hasDeliverWorkerMission(ColonyWorkerReq colonyWorkerReqScore) {
		colonyWorkerReqScore.simulate().size();
		// TODO: check deliver worker to colony for work, 
		// it is possible to have mission but no more then colonyWorkerReqScore.simulate().size()  
		return false;
	}
}
