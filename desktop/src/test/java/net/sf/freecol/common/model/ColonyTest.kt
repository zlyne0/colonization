package net.sf.freecol.common.model

import net.sf.freecol.common.model.Colony.NoBuildReason
import net.sf.freecol.common.model.specification.GoodsType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import promitech.colonization.savegame.Savegame1600BaseClass

class ColonyTest : Savegame1600BaseClass() {

    @Test
    fun shouldDisallowBuildUnit() {
        // given
        val freeColonist = Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST)

        // when
        val noBuildReason = nieuwAmsterdam.getNoBuildReason(freeColonist)

        // then
        assertThat(noBuildReason).isEqualTo(NoBuildReason.MISSING_BUILD_ABILITY)
    }

    @Test
    fun canCalculateTurnsToCompleteBuilding() {
        // given
        val colony = nieuwAmsterdam
        colony.goodsContainer.increaseGoodsQuantity(GoodsType.HAMMERS, 100)
        colony.goodsContainer.decreaseToZero(GoodsType.TOOLS)
        val runDistilleryType = Specification.instance.buildingTypes.getById("model.building.rumDistillery")

        // when
        val buildProgress = BuildProgress.calculateBuildProgress(colony, runDistilleryType)

        // then
        assertThat(buildProgress.turnsToComplete).isEqualTo(BuildProgress.LACK_OF_RESOURCES)
        assertThat(buildProgress.isCompleted).isFalse
        assertThat(buildProgress.noComponentsInProduction()).isTrue
    }

    @Test
    fun should_calculate_finished_building() {
        // given
        val colony = nieuwAmsterdam
        colony.goodsContainer.increaseGoodsQuantity(GoodsType.HAMMERS, 100)
        colony.goodsContainer.increaseGoodsQuantity(GoodsType.TOOLS, 20)
        val runDistilleryType = Specification.instance.buildingTypes.getById("model.building.rumDistillery")

        // when
        val buildProgress = BuildProgress.calculateBuildProgress(colony, runDistilleryType)

        // then
        assertThat(buildProgress.turnsToComplete).isEqualTo(0)
        assertThat(buildProgress.isCompleted).isTrue
        assertThat(buildProgress.noComponentsInProduction()).isFalse
        assertThat(buildProgress.isProgressStopedBecauseOfLackOfComponents).isFalse
    }


}