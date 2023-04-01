package promitech.colonization.ai.navy

import promitech.colonization.ai.MissionPlanStatus
import promitech.colonization.ai.NavyMissionPlaner

internal fun NavyMissionPlaner.scenario1(context: PlanNavyUnitContext) {
    if (context.unit.isAtEuropeLocation) {
        scenarioTransportFromEurope(context)
            .whenNoMission { scenario1TransportNotDependLocation(context) }
    }
    if (context.unit.isAtTileLocation) {
        transportPioneerFromTileToTile(context)
            .whenNoMission { scenario1TransportNotDependLocation(context) }
            .whenNoMission { scenarioTransportFromEurope(context) }
            .whenNoMission { prepareExploreMissions(context) }
    }
}

internal fun NavyMissionPlaner.scenarioPriorityTransportFromTile(context: PlanNavyUnitContext) {
    if (context.unit.isAtEuropeLocation) {
        scenarioTransportFromEurope(context)
            .whenNoMission { scenario1TransportNotDependLocation(context) }
    }
    if (context.unit.isAtTileLocation) {
        transportPioneerFromTileToTile(context)
            .whenNoMission { createTransportMissionFromTransportRequestFromSourceTileLocation(context) }
            .whenNoMission { createTransportMissionFromAtShipLocation(context) }
            .whenNoMission { transportGoodsToSell(context) }
            .whenNoMission { planSellGoodsToBuyUnit(context) }
            .whenNoMission { scenarioTransportFromEurope(context) }
            .whenNoMission { prepareExploreMissions(context) }
    }
}

internal fun NavyMissionPlaner.scenarioTransportFromEurope(context: PlanNavyUnitContext): MissionPlanStatus {
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
