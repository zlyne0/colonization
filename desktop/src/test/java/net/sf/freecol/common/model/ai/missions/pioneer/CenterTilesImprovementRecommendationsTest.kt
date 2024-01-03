package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.ai.missions.pioneer.CenterTilesImprovementRecommendations.BenefitResult
import net.sf.freecol.common.model.specification.GoodsType.COTTON
import net.sf.freecol.common.model.specification.GoodsType.FURS
import net.sf.freecol.common.model.specification.GoodsType.SUGAR
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import promitech.colonization.savegame.Savegame1600BaseClass

class CenterTilesImprovementRecommendationsTest : Savegame1600BaseClass() {

    @Test
    fun `should determine transformation benefit`() {
        assertThat(CenterTilesImprovementRecommendations(FURS to 4).recommend(FURS, SUGAR))
            .isEqualTo(BenefitResult.OK)

        assertThat(CenterTilesImprovementRecommendations(SUGAR to 1, FURS to 3).recommend(FURS, SUGAR))
            .isEqualTo(BenefitResult.OK)

        assertThat(CenterTilesImprovementRecommendations(SUGAR to 2, FURS to 2).recommend(FURS, SUGAR))
            .isEqualTo(BenefitResult.NO)

        assertThat(CenterTilesImprovementRecommendations(SUGAR to 3, FURS to 1).recommend(FURS, SUGAR))
            .isEqualTo(BenefitResult.NO)

        assertThat(CenterTilesImprovementRecommendations(SUGAR to 2, FURS to 2).recommend(FURS, COTTON))
            .isEqualTo(BenefitResult.OK)

        assertThat(CenterTilesImprovementRecommendations(SUGAR to 1, COTTON to 1, FURS to 2).recommend(FURS, SUGAR))
            .isEqualTo(BenefitResult.CONDITIONAL)
    }

    @Test
    fun `can change only on last colony`() {
        // given
        val recommendations = CenterTilesImprovementRecommendations(dutch)
        //recommendations.print()

        // when
        assertThat(recommendations.recommend(fortMaurits, FURS, SUGAR))
            .isEqualTo(BenefitResult.OK)
        assertThat(recommendations.recommend(fortNassau, FURS, SUGAR))
            .describedAs("can change only last founded settlement")
            .isEqualTo(BenefitResult.NO)
    }

}