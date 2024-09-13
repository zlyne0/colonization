package promitech.colonization.ai.military

import com.badlogic.gdx.utils.ObjectFloatMap
import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Turn
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitId
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.foreachMission
import net.sf.freecol.common.model.ai.missions.military.DefenceMission
import net.sf.freecol.common.model.forEachTile
import net.sf.freecol.common.model.map.InfluenceRangeMapBuilder
import net.sf.freecol.common.model.map.PowerInfluenceMap
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.player.Stance
import net.sf.freecol.common.model.player.Tension
import net.sf.freecol.common.util.whenNotNull
import promitech.colonization.orders.combat.DefencePower.calculatePaperDefencePower
import promitech.colonization.orders.combat.OffencePower
import promitech.map.FloatFloatArray
import promitech.map.IntIntArray
import promitech.map.forEachCords

class ThreatModel(val game: Game, var player: Player) {

    val ALLOW_ONLY_LAND_TILES = { tile: Tile -> tile.type.isLand }

    private val influenceRangeMapBuilder = InfluenceRangeMapBuilder()
    internal val threatInfluenceMap: PowerInfluenceMap
    internal val warRange: IntIntArray
    internal val singleTileDefence: FloatFloatArray
    internal val powerBalance: FloatFloatArray

    private val powerProjectionRange = 5
    private val offencePowerCache = ObjectFloatMap<String>()
    var createTurn: Turn
        private set

    init {
        createTurn = game.turn

        threatInfluenceMap = PowerInfluenceMap(game.map, ALLOW_ONLY_LAND_TILES, influenceRangeMapBuilder)
        singleTileDefence = FloatFloatArray(game.map.width, game.map.height, FLOAT_UNKNOWN_VALUE)
        powerBalance = FloatFloatArray(game.map.width, game.map.height, FLOAT_UNKNOWN_VALUE)
        warRange = IntIntArray(game.map.width, game.map.height, Int.MAX_VALUE)

        calculateThreatInfluenceMap()
        calculateTileDefenceMap()
        calculatePowerBalanceMap(singleTileDefence, threatInfluenceMap)
        generateWarRange()
    }

    fun recalculate(p: Player) {
        this.player = p

        threatInfluenceMap.reset()
        singleTileDefence.resetToUnknown()
        powerBalance.resetToUnknown()
        warRange.resetToUnknown()

        calculateThreatInfluenceMap()
        calculateTileDefenceMap()
        calculatePowerBalanceMap(singleTileDefence, threatInfluenceMap)
        generateWarRange()
    }

    internal fun calculateColonyDefencePriority(): List<ColonyThreat> {
        val colonyThreats = ArrayList<ColonyThreat>(player.settlements.size())
        for (settlement in player.settlements) {
            val colony = settlement.asColony()
            val colonyTile = colony.tile

            colonyThreats.add(ColonyThreat(
                colony,
                ColonyThreatWeights(
                    threatPower = powerBalance.get(colonyTile.x, colonyTile.y),
                    threatProjection = threatInfluenceMap.powerProjectionRange(settlement.tile),
                    colonyWealth = scoreColoniesToDefend(colony)
                ),
                isInWarRange(colonyTile),
                singleTileDefence.get(colonyTile.x, colonyTile.y)
            ))
        }
        colonyThreats.sortWith(ColonyThreat.REQUIRE_MORE_DEFENCE_COMPARATOR)
        return colonyThreats
    }

    fun isDefencePowerBelowThreatPower(tile: Tile): Boolean {
        return singleTileDefence.get(tile.x, tile.y) < threatInfluenceMap.powerProjectionRange(tile)
    }

    fun isInWarRange(tile: Tile): Boolean {
        return !warRange.isUnknownValue(tile.x, tile.y);
    }

    private fun calculateThreatInfluenceMap() {
        for (gamePlayer in game.players) {
            if (player.notEqualsId(gamePlayer) && player.getStance(gamePlayer) != Stance.UNCONTACTED) {
                for (unit in gamePlayer.units) {
                    if (unit.isAtTileLocation) {
                        val power = offencePower(unit)
                        if (power > 0) {
                            threatInfluenceMap.addSourceLayer(unit.tile, powerProjectionRange, power)
                        }
                    }
                }
                if (gamePlayer.isIndian) {
                    for (settlement in gamePlayer.settlements) {
                        val indianSettlement = settlement.asIndianSettlement()
                        if (gamePlayer.getStance(player) == Stance.WAR || indianSettlement.getTension(player).isWorst(Tension.Level.CONTENT)) {
                            threatInfluenceMap.addSourceLayer(settlement.tile, powerProjectionRange, 1f)
                        }
                    }
                }
            }
        }
    }

    private fun generateWarRange() {
        influenceRangeMapBuilder.init(game.map, ALLOW_ONLY_LAND_TILES)

        for (gamePlayer in game.players) {
            if (player.notEqualsId(gamePlayer) && player.getStance(gamePlayer) == Stance.WAR) {
                for (unit in gamePlayer.units) {
                    val tile = unit.tileLocationOrNull
                    if (tile != null) {
                        influenceRangeMapBuilder.addRangeSource(tile)
                    }
                }
                for (settlement in gamePlayer.settlements) {
                    influenceRangeMapBuilder.addRangeSource(settlement.tile)
                }
            }
        }

        influenceRangeMapBuilder.generateRange(warRange, powerProjectionRange)
    }

    private fun calculateTileDefenceMap() {
        val missionsContainer: PlayerMissionsContainer = game.aiContainer.missionContainer(player)
        val defenceMissionUnits = mutableSetOf<UnitId>()
        missionsContainer.foreachMission(DefenceMission::class.java) { defenceMission ->
            player.units.getByIdOrNull(defenceMission.unitId).whenNotNull { defenceMissionUnit ->
                defenceMissionUnits.add(defenceMissionUnit.id)
                val defTile = defenceMission.tile
                singleTileDefence.addValue(
                    defTile.x, defTile.y,
                    calculatePaperDefencePower(defenceMissionUnit.unitType, defenceMissionUnit.unitRole)
                )
            }
        }

        game.map.forEachTile { tile ->
            val power = calculatePaperDefencePower(player, tile, defenceMissionUnits)
            if (power > 0) {
                singleTileDefence.addValue(tile.x, tile.y, power)
            }
        }
    }

    private fun calculatePowerBalanceMap(playerDefenceMap: FloatFloatArray, threatMap: PowerInfluenceMap): FloatFloatArray {
        var threadValue: Float
        var playerPowerValue: Float
        var powerBalanceValue: Float
        powerBalance.forEachCords { x, y ->
            powerBalanceValue = powerBalance.unknownValue
            threadValue = threatMap.powerSum(x, y)
            if (!threatMap.isUnknownValue(threadValue)) {
                powerBalanceValue = threadValue
            }

            playerPowerValue = playerDefenceMap.get(x, y)
            if (playerPowerValue != playerDefenceMap.unknownValue) {
                if (powerBalanceValue == powerBalance.unknownValue) {
                    powerBalanceValue = 0f
                }
                powerBalanceValue -= playerPowerValue
            }
            powerBalance.set(x, y, powerBalanceValue)
        }
        return powerBalance
    }

    private fun offencePower(unit: Unit): Float {
        val key = unit.unitType.id + unit.unitRole.id
        if (offencePowerCache.containsKey(key)) {
            return offencePowerCache.get(key, 0f)
        }
        val offencePower = OffencePower.calculatePaperOffencePower(unit.unitType, unit.unitRole)
        offencePowerCache.put(key, offencePower)
        return offencePower
    }

    private fun scoreColoniesToDefend(colony: Colony): Int {
        var score = 0
        for (unit in colony.units.entities()) {
            score += unit.unitType.scoreValue * 250
        }
        for (building in colony.buildings) {
            if (!building.buildingType.isAutomaticBuild) {
                score += player.market().buildingGoodsPrice(building.buildingType)
            }
        }
        return score
    }

    companion object {
        val FLOAT_UNKNOWN_VALUE = PowerInfluenceMap.FLOAT_UNKNOWN_VALUE
        val UNKNOWN_VALUE = PowerInfluenceMap.INT_UNKNOWN_VALUE
    }
}