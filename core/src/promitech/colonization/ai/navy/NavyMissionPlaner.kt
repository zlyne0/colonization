package promitech.colonization.ai

import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.ExplorerMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.TransportUnitMission
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMissionPlaner
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerMission
import net.sf.freecol.common.model.ai.missions.pioneer.RequestGoodsMission
import net.sf.freecol.common.model.ai.missions.scout.ScoutMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.Companion.hasNotTransportUnitMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.Companion.isFromEurope
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.Companion.isFromTileLocation
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.Companion.isTransportHasParentType
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerRequestPlaner
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.util.Predicate
import net.sf.freecol.common.util.PredicateUtil.and
import promitech.colonization.ai.navy.PlanNavyUnitContext
import promitech.colonization.ai.navy.scenario1
import promitech.colonization.ai.navy.scenarioPriorityTransportFromTile
import promitech.colonization.ai.navy.scenarioTransportFromEurope
import promitech.colonization.ai.purchase.PurchasePlaner

class NavyMissionPlaner(
    private val purchasePlaner: PurchasePlaner,
    private val colonyWorkerRequestPlaner: ColonyWorkerRequestPlaner,
    private val transportGoodsToSellMissionPlaner: TransportGoodsToSellMissionPlaner
) {

    private var activeUnits = 0

    fun plan(player: Player, playerMissionsContainer: PlayerMissionsContainer) {
        val vacantNavyUnits = findVacantNavyUnits(player, playerMissionsContainer)

        while (vacantNavyUnits.isNotEmpty()) {
            val navyUnit = vacantNavyUnits.removeFirst()
            val context = PlanNavyUnitContext(navyUnit, playerMissionsContainer)

            if ((activeUnits - vacantNavyUnits.size + 1) % 2 == 0) {
                scenario1(context)
            } else {
                scenarioPriorityTransportFromTile(context)
            }
        }
    }

    private fun findVacantNavyUnits(player: Player, playerMissionsContainer: PlayerMissionsContainer): MutableList<Unit> {
        val navyUnits = mutableListOf<Unit>()
        for (unit in player.units) {
            if (unit.isNaval && !unit.isDamaged) {
                activeUnits++
                if (!playerMissionsContainer.isUnitBlockedForMission(unit)) {
                    navyUnits.add(unit)
                }
            }
        }
        navyUnits.sortWith(Units.FREE_CARGO_SPACE_COMPARATOR)
        return navyUnits
    }

    fun plan(navyUnit: Unit, playerMissionsContainer: PlayerMissionsContainer) {
        val context = PlanNavyUnitContext(navyUnit, playerMissionsContainer)
        scenario1(context)
    }

    fun transportFromEurope(navyUnit: Unit, playerMissionsContainer: PlayerMissionsContainer) {
        val context = PlanNavyUnitContext(navyUnit, playerMissionsContainer)
        scenarioTransportFromEurope(context)
    }

    internal fun avoidPurchasesAndCollectGold() = purchasePlaner.avoidPurchasesAndCollectGold

    internal fun buyUnitsToNavyCapacity(context: PlanNavyUnitContext) {
        colonyWorkerRequestPlaner.buyUnitsToNavyCapacity(context.playerMissionContainer.player, context.playerMissionContainer, context.unit)
    }

    internal fun transportGoodsToSell(context: PlanNavyUnitContext): MissionPlanStatus {
        return transportGoodsToSellMissionPlaner.plan(context.unit)
    }

    internal fun planSellGoodsToBuyUnit(context: PlanNavyUnitContext): MissionPlanStatus {
        return transportGoodsToSellMissionPlaner.planSellGoodsToBuyUnit(context.unit)
    }


    internal fun createTransportFromScoutMission(context: PlanNavyUnitContext): MissionPlanStatus {
        return createTransportMissionFromTransportRequest(
            context,
            and(hasNotTransportUnitMission, isFromEurope, isTransportHasParentType(context.playerMissionContainer, ScoutMission::class.java))
        )
    }

    internal fun createTransportFromPioneerMission(context: PlanNavyUnitContext): MissionPlanStatus {
        return createTransportMissionFromTransportRequest(
            context,
            and(hasNotTransportUnitMission, isFromEurope, isTransportHasParentType(context.playerMissionContainer, PioneerMission::class.java))
        )
    }

    internal fun createTransportFromOtherTransportRequest(context: PlanNavyUnitContext): MissionPlanStatus {
        return createTransportMissionFromTransportRequest(
            context,
            and(hasNotTransportUnitMission, isFromEurope)
        )
    }

    internal fun transportPioneerFromTileToTile(context: PlanNavyUnitContext): MissionPlanStatus {
        return createTransportMissionFromTransportRequest(
            context,
            and(hasNotTransportUnitMission, isFromTileLocation, isTransportHasParentType(context.playerMissionContainer, PioneerMission::class.java))
        )
    }

    internal fun createTransportMissionFromTransportRequestFromSourceTileLocation(context: PlanNavyUnitContext): MissionPlanStatus {
        return createTransportMissionFromTransportRequest(
            context,
            and(hasNotTransportUnitMission, isFromTileLocation)
        )
    }

    internal fun createTransportMissionFromAtShipLocation(context: PlanNavyUnitContext): MissionPlanStatus {
        // scenario from beggining of game ship with colonist without transport mission, create colonyWorkerMission
        // and then TransportMission
        return createTransportMissionFromTransportRequest(
            context,
            and(hasNotTransportUnitMission, TransportUnitRequestMission.isAtShipLocation(context.unit))
        )
    }

    internal fun prepareExploreMissions(context: PlanNavyUnitContext): MissionPlanStatus {
        val explorerMission = ExplorerMission(context.unit)
        context.playerMissionContainer.addMission(explorerMission)
        return MissionPlanStatus.MISSION_CREATED
    }

    internal fun transportMissionFromBoughtRequestGoodsMission(context: PlanNavyUnitContext) {
        val transportRequestMissions = context.playerMissionContainer.findMissions(
            RequestGoodsMission::class.java,
            RequestGoodsMission.isBoughtPredicate
        )

        for (transportRequestMission in transportRequestMissions) {
            if (context.unit.hasSpaceForAdditionalCargo(transportRequestMission.goodsCollection)) {
                context.forTransportMission { transportMission ->
                    if (transportMission.isNotLoaded(transportRequestMission)) {
                        transportMission.addCargoDest(context.unit.owner, transportRequestMission)
                    }
                }
            }
        }
        if (context.tum != null) {
            context.playerMissionContainer.addMissionWhenNotAdded(context.tum)
        }
    }

    private fun createTransportMissionFromTransportRequest(
        context: PlanNavyUnitContext,
        transportUnitRequestMissionPredicate: Predicate<TransportUnitRequestMission>
    ): MissionPlanStatus {
        val transportRequestMissions = context.playerMissionContainer.findMissions(
            TransportUnitRequestMission::class.java,
            transportUnitRequestMissionPredicate
        )

        for (transportRequestMission in transportRequestMissions) {
            val unitToTransport = transportRequestMission.unit
            if (canEmbarkUnit(context.unit, context.tum, unitToTransport) || isAlreadyEmbarked(context.unit, unitToTransport)) {
                context.forTransportMission { transportMission ->
                    transportMission.addUnitDest(transportRequestMission)
                    transportRequestMission.transportUnitMissionId = transportMission.id
                }
            }
        }
        if (context.tum != null) {
            context.playerMissionContainer.addMissionWhenNotAdded(context.tum)
        }
        return context.missionPlanStatus()
    }

    private fun isAlreadyEmbarked(navyUnit: Unit, unit: Unit): Boolean {
        return navyUnit.unitContainer.isContainUnit(unit)
    }

    private fun canEmbarkUnit(navyUnit: Unit, mission: TransportUnitMission?, unit: Unit): Boolean {
        if (mission == null) {
            return navyUnit.hasSpaceForAdditionalUnit(unit.unitType)
        }
        return mission.canEmbarkUnit(unit)
    }

}