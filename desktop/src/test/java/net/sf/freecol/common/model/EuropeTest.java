package net.sf.freecol.common.model;

import net.sf.freecol.common.model.player.Player;

import org.junit.jupiter.api.Test;

import promitech.colonization.savegame.Savegame1600BaseClass;

import static org.assertj.core.api.Assertions.assertThat;

class EuropeTest extends Savegame1600BaseClass {

    @Test
	void canNotBuyBecauseNoBudget() throws Exception {
		// given
    	UnitType freeColonist = Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST);
    	int budget = 100;
    	
		// when
		boolean canBuyUnit = dutch.getEurope().canAiBuyUnit(freeColonist, budget);

		// then
		assertThat(canBuyUnit).isFalse();
	}

    @Test
	void canBuy() throws Exception {
		// given
    	UnitType freeColonist = Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST);
    	int budget = 1000;
    	
		// when
		boolean canBuyUnit = dutch.getEurope().canAiBuyUnit(freeColonist, budget);

		// then
		assertThat(canBuyUnit).isTrue();
	}
    
    @Test
    void canNotBuyUnitBecauseOnlyNewWorldTrainable() throws Exception {
    	// given
    	UnitType tabaccoPlanter = Specification.instance.unitTypes.getById(UnitType.MASTER_TOBACCO_PLANTER);
    	int budget = 10000;
    	
    	// when
    	boolean canBuyUnit = dutch.getEurope().canAiBuyUnit(tabaccoPlanter, budget);
    	
    	// then
    	assertThat(canBuyUnit).isFalse();
    }
    
    @Test
	void canRecruitImmigrant() throws Exception {
		// given
    	int gold = dutch.getGold();
    	int immigrantPrice = dutch.getEurope().getRecruitImmigrantPrice();
    	
    	UnitType freeColonist = Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST);
    	
		// when
		Unit unit = dutch.getEurope().buyUnitByAI(freeColonist);

		// then
		UnitAssert.assertThat(unit)
			.isUnitType(freeColonist)
			.isAtLocation(dutch.getEurope());
		assertThat(dutch.getGold()).isEqualTo(gold - immigrantPrice);
	}
    
    @Test
	void canTrain() throws Exception {
		// given
    	int gold = dutch.getGold();
    	UnitType furTrader = Specification.instance.unitTypes.getById(UnitType.MASTER_FUR_TRADER);
    	
		// when
		Unit unit = dutch.getEurope().buyUnitByAI(furTrader);

		// then
		UnitAssert.assertThat(unit)
			.isUnitType(furTrader)
			.isAtLocation(dutch.getEurope());
		
		assertThat(dutch.getGold()).isEqualTo(gold - furTrader.getPrice());
	}

	@Test
	void shouldIncreasePriceAfterBuyImmigrant() {
		// given
		Player player = spain;
		player.addGold(10000);

		int priceBeforePurchase = player.getEurope().getRecruitImmigrantPrice();

		// when
		player.getEurope().buyImmigrant(unitType(UnitType.FREE_COLONIST), 100);
		player.getEurope().buyImmigrant(unitType(UnitType.FREE_COLONIST), 100);
		player.getEurope().buyImmigrant(unitType(UnitType.FREE_COLONIST), 100);
		player.getEurope().buyImmigrant(unitType(UnitType.FREE_COLONIST), 100);

		// then
		int priceAfterPurchase = player.getEurope().getRecruitImmigrantPrice();
		assertThat(priceBeforePurchase).isEqualTo(188);
		assertThat(priceAfterPurchase).isEqualTo(320);
	}
}
