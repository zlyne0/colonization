package net.sf.freecol.common.model;

import org.assertj.core.api.AbstractAssert;

import net.sf.freecol.common.model.player.Player;

public class IndianSettlementAssert extends AbstractAssert<IndianSettlementAssert, IndianSettlement> {

	public IndianSettlementAssert(IndianSettlement actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static IndianSettlementAssert assertThat(IndianSettlement settlement) {
		return new IndianSettlementAssert(settlement, IndianSettlementAssert.class);
	}

	public IndianSettlementAssert hasNoMissionary(Player player) {
		if (actual.hasMissionary(player)) {
			failWithMessage("expected indian settlement <%s> has no missionary", actual.getId());
		}
		return this;
	}

    public IndianSettlementAssert notOwnedBy(Player player) {
    	if (player.settlements.containsId(actual)) {
    		failWithMessage("expected indian settlement <%s> not owned by player <%s>", 
				actual.getId(), player.getId()
			);
    	}
    	return this;
    }
}
