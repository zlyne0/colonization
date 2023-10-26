package promitech.colonization.ai.military

import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.emptyMapTileDebugInfo
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.player.Stance
import net.sf.freecol.common.model.specification.BuildingType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import promitech.colonization.ai.military.ColonyDefencePurchaseAssert.Companion.assertThat
import promitech.colonization.ai.military.DefencePrice.DefencePurchase
import promitech.colonization.ai.military.DefencePrice.generateRequests
import promitech.colonization.ai.military.DefencePurchasePlaner.ColonyDefencePurchase
import promitech.colonization.ai.military.DefencePurchasePlaner.Companion.calculateExpectedDefencePower
import promitech.colonization.orders.combat.DefencePower
import promitech.colonization.savegame.Savegame1600BaseClass

class DefencePurchasePlanerTest : Savegame1600BaseClass() {

    @Test
    fun `should generate request`() {
        // given
        val actualDefenceStrength = ColonyDefenceSimulation(DefencePower.ColonyDefenceSnapshot(3f))

        // when
        val defencePurchases = generateRequests(nieuwAmsterdam)

        // then
        for (defencePurchase in defencePurchases) {
            println(defencePurchase)
        }
        with((defencePurchases[0] as DefencePurchase.ColonyBuildingPurchase)) {
            assertThat(buildingType.id).isEqualTo(BuildingType.STOCKADE)
            assertThat(price).isEqualTo(1280)
            assertThat(powerAfterPurchase(actualDefenceStrength)).isEqualTo(6f)
        }
        with((defencePurchases[1] as DefencePurchase.UnitPurchase)) {
            assertThat(unitType.id).isEqualTo(UnitType.ARTILLERY)
            assertThat(price).isEqualTo(500)
            assertThat(powerAfterPurchase(actualDefenceStrength)).isEqualTo(8f)
        }
        with((defencePurchases[2] as DefencePurchase.UnitPurchase)) {
            assertThat(unitType.id).isEqualTo(UnitType.FREE_COLONIST)
            assertThat(unitRole.id).isEqualTo(UnitRole.SOLDIER)
            assertThat(price).isEqualTo(750)
            assertThat(powerAfterPurchase(actualDefenceStrength)).isEqualTo(5.0f)
        }
    }

    @Test
    fun `should calculate colony defence power depends colony wealth`() {

        data class Param(val colonyWealth: Int, val expectedDefencePower: Float)

        val testParams = listOf(
            Param(1000, 0f),
            Param(2000, 2.0f),
            Param(3000, 3.0f),
            Param(5000, 5.0f),
            Param(6000, 7.0f),
            Param(7000, 8.0f),
            Param(8000, 9.0f),
            Param(9000, 10.0f),
            Param(10000, 10.0f),
            Param(11000, 10.0f),
            Param(12000, 11.0f),
            Param(14000, 12.0f),
            Param(16000, 13.0f),
        )

        testParams.forEach { param ->
            // given
            // when
            val calculatedUnitCount = calculateExpectedDefencePower(param.colonyWealth)

            // then
            assertThat(calculatedUnitCount)
                .describedAs("for colonyWealth ${param.colonyWealth}")
                .isEqualTo(param.expectedDefencePower)
        }
    }

    @Test
    fun `should generate purchase orders when war`() {
        // given
        warWithAll(dutch)

        val threatModel = ThreatModel(game, dutch)
        val defencePurchasePlaner = DefencePurchasePlaner(dutch, threatModel)

        // when
        val defencePurchases: List<ColonyDefencePurchase> = defencePurchasePlaner.generateOrders()

        // then
        printDefenceOrders(threatModel, defencePurchases)

        assertThat(defencePurchases).hasSize(3)
        assertThat(defencePurchases.get(0))
            .isColony(fortNassau)
            .isUnitType(UnitType.ARTILLERY)
        assertThat(defencePurchases.get(1))
            .isColony(fortNassau)
            .isBuilding(BuildingType.STOCKADE)
        assertThat(defencePurchases.get(2))
            .isColony(fortNassau)
            .isUnitType(UnitType.ARTILLERY)
    }

    @Test
    fun `should generate purchase orders on peace`() {
        // given
        val threatModel = ThreatModel(game, dutch)
        val defencePurchasePlaner = DefencePurchasePlaner(dutch, threatModel)

        // when
        val defencePurchases: List<ColonyDefencePurchase> = defencePurchasePlaner.generateOrders()

        // then
        printDefenceOrders(threatModel, defencePurchases)

        assertThat(defencePurchases).hasSize(3)
        assertThat(defencePurchases.get(0))
            .isColony(fortNassau)
            .isUnitType(UnitType.ARTILLERY)
        assertThat(defencePurchases.get(1))
            .isColony(fortNassau)
            .isBuilding(BuildingType.STOCKADE)
        assertThat(defencePurchases.get(2))
            .isColony(nieuwAmsterdam)
            .isBuilding(BuildingType.STOCKADE)
    }

    fun printDefenceOrders(
        threatModel: ThreatModel,
        defencePurchases: List<ColonyDefencePurchase>
    ) {
        threatModel.printColonyDefencePriority(emptyMapTileDebugInfo)
        println("defencePurchases.size = ${defencePurchases.size}")
        defencePurchases.forEach { purchase ->
            val strWar = if (purchase.colonyThreat.war) "at war" else "no war"
            println("${purchase.colony.name} [$strWar]" +
                    ", threatPower = " + purchase.colonyThreat.colonyThreatWeights.threatPower.toString() +
                    ", purchase = " + purchase.purchase
            )
        }
    }

    fun warWithAll(player: Player) {
        for (otherPlayer in game.players) {
            if (otherPlayer.notEqualsId(player)) {
                player.changeStance(otherPlayer, Stance.WAR)
            }
        }
    }
}