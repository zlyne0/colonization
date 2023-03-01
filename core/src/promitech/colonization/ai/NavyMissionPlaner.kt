package promitech.colonization.ai

import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.ExplorerMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.TransportUnitMission
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.Companion.hasNotTransportUnitMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.Companion.isFromTileLocation
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission.Companion.isTransportHasParentType
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.util.Predicate
import net.sf.freecol.common.util.PredicateUtil.and

class NavyMissionPlaner {

    fun plan(player: Player, playerMissionsContainer: PlayerMissionsContainer) {
        val navyUnits = createNavyUnitsList(player, playerMissionsContainer)

        while (navyUnits.isNotEmpty()) {
            val navyUnit = navyUnits.removeFirst()
        }
    }

    fun navyTransportPioneerFromTileToTile(
        navyUnit: Unit,
        playerMissionsContainer: PlayerMissionsContainer
    ): MissionPlanStatus {
        var tum: TransportUnitMission? = null

        tum = createTransportMissionFromTransportRequest(
            tum,
            navyUnit,
            playerMissionsContainer,
            and(
                hasNotTransportUnitMission,
                isFromTileLocation,
                isTransportHasParentType(playerMissionsContainer, PioneerMission::class.java)
            )
        )
        if (tum != null) {
            return MissionPlanStatus.MISSION_CREATED
        }
        return MissionPlanStatus.NO_MISSION
    }

    fun prepareExploreMissions(navyUnit: Unit, playerMissionsContainer: PlayerMissionsContainer) {
        val explorerMission = ExplorerMission(navyUnit)
        playerMissionsContainer.addMission(explorerMission)
    }

    fun createNavyUnitsList(player: Player, playerMissionsContainer: PlayerMissionsContainer): MutableList<Unit> {
        val navyUnits = mutableListOf<Unit>()
        for (unit in player.units) {
            if (unit.isNaval && !unit.isDamaged && !playerMissionsContainer.isUnitBlockedForMission(unit)) {
                navyUnits.add(unit)
            }
        }
        navyUnits.sortWith(Units.FREE_CARGO_SPACE_COMPARATOR)
        return navyUnits
    }

    fun createTransportMissionFromTransportRequest(
        argtum: TransportUnitMission?,
        navyUnit: Unit,
        playerMissionContainer: PlayerMissionsContainer,
        transportUnitRequestMissionPredicate: Predicate<TransportUnitRequestMission>
    ): TransportUnitMission? {
        var tum: TransportUnitMission? = argtum
        val transportRequestMissions = playerMissionContainer.findMissions(
            TransportUnitRequestMission::class.java,
            transportUnitRequestMissionPredicate
        )

        for (transportRequestMission in transportRequestMissions) {
            val unitToTransport = transportRequestMission.unit
            if (canEmbarkUnit(navyUnit, tum, unitToTransport) || isAlreadyEmbarked(navyUnit, unitToTransport)) {
                if (tum == null) {
                    tum = TransportUnitMission(navyUnit)
                }
                tum.addUnitDest(transportRequestMission)
                transportRequestMission.transportUnitMissionId = tum.id
            }
        }
        if (tum != null) {
            playerMissionContainer.addMissionWhenNotAdded(tum)
        }
        return tum
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