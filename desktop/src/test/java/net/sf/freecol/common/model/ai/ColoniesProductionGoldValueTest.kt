package net.sf.freecol.common.model.ai.missions.goodsToSell

import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.ai.ColoniesProductionGoldValue
import net.sf.freecol.common.model.ai.missions.Savegame1600BaseClass
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

public class ColoniesProductionGoldValueTest : Savegame1600BaseClass() {

	@Test
	fun canGoToColoniesTakeGoodsAndSellInEurope() {
		// given
		var sut = ColoniesProductionGoldValue(dutch, Specification.instance.goodsTypeToScoreByPrice)
		
		// when
		val gold = sut.goldValue()
		
		// then
		assertThat(gold).isEqualTo(283)
	}

}