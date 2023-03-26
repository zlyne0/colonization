package promitech.colonization.ai.navy

import promitech.colonization.ai.MissionPlanStatus
import promitech.colonization.ai.NavyMissionPlaner

internal fun NavyMissionPlaner.scenario1(context: PlanNavyUnitContext) {
    if (context.unit.isAtEuropeLocation) {
        scenario1TransportFromEurope(context)
            .whenNoMission { scenario1TransportNotDependLocation(context) }
    }
    if (context.unit.isAtTileLocation) {
        transportPioneerFromTileToTile(context)
            .whenNoMission { scenario1TransportNotDependLocation(context) }
            .whenNoMission { scenario1TransportFromEurope(context) }
            .whenNoMission { prepareExploreMissions(context) }
    }
}

internal fun NavyMissionPlaner.scenario1TransportFromEurope(context: PlanNavyUnitContext): MissionPlanStatus {
    transportMissionFromBoughtRequestGoodsMission(context)
    createTransportFromScoutMission(context)
    createTransportFromPioneerMission(context)
    createTransportFromOtherTransportRequest(context)
    if (!avoidPurchasesAndCollectGold()) {
        buyUnitsToNavyCapacity(context)
        createTransportFromOtherTransportRequest(context)
    }
    return context.missionPlanStatus()
}

internal fun NavyMissionPlaner.scenario1TransportNotDependLocation(context: PlanNavyUnitContext): MissionPlanStatus {
    return createTransportMissionFromAtShipLocation(context)
        .whenNoMission { transportGoodsToSell(context) }
        .whenNoMission { createTransportMissionFromTransportRequestFromSourceTileLocation(context) }
        .whenNoMission { planSellGoodsToBuyUnit(context) }
}
