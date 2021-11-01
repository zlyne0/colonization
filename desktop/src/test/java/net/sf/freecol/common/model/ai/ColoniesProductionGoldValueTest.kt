package net.sf.freecol.common.model.ai.missions.goodsToSell

import net.sf.freecol.common.model.Specification
import promitech.colonization.savegame.Savegame1600BaseClass
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

public class ColoniesProductionGoldValueTest : Savegame1600BaseClass() {

	@Test
	fun canGoToColoniesTakeGoodsAndSellInEurope() {
		// given
		var sut = ColoniesProductionValue(dutch)
		
		// when
		val gold = sut.goldValue()
		
		// then
		assertThat(gold).isEqualTo(283)
	}

}