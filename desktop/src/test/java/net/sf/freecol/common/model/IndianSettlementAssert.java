package net.sf.freecol.common.model;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.AbstractAssert;

import net.sf.freecol.common.model.player.Player;

public class IndianSettlementAssert extends AbstractAssert<IndianSettlementAssert, IndianSettlement> {

	public IndianSettlementAssert(IndianSettlement actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static IndianSettlementAssert assertThat(IndianSettlement settlement) {
		return new IndianSettlementAssert(settlement, IndianSettlementAssert.class);
	}

	public static IndianSettlementAssert assertThat(Settlement settlement) {
		if (!settlement.isIndianSettlement()) {
			throw new IllegalStateException("settlement is not IndianSettlement " + settlement.getId());
		}
		return new IndianSettlementAssert(settlement.asIndianSettlement(), IndianSettlementAssert.class);
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
    
    public IndianSettlementAssert hasWantedGoods(String ... goodsId) {
    	Assertions.assertThat(actual.wantedGoods)
    		.extracting("id")
    		.containsExactly((Object[])goodsId);
    	return this;
    }

	public IndianSettlementAssert hasUnitsWithRole(String unitRoleId, int amount) {
		int count = 0;
		for (Unit unit : actual.getUnits().entities()) {
			if (unit.unitRole.equalsId(unitRoleId)) {
				count++;
			}
		}
		for (Unit unit : actual.tile.getUnits().entities()) {
			if (unit.unitRole.equalsId(unitRoleId)) {
				count++;
			}
		}
		if (count != amount) {
			failWithMessage(
				"expected indian settlement <%s> has units with role <%s> in amount <%d> but in has in amount <%d>", 
				actual.getId(),
				unitRoleId,
				amount,
				count
			);
		}
		return this;
	}

	public IndianSettlementAssert hasOffensiveRolesNumber(int amount) {
		int count = 0;
    	for (Unit unit : actual.getUnits().entities()) {
			if (unit.getUnitRole().isOffensive()) {
				count++;
			}
		}
    	for (Unit unit : actual.tile.getUnits().entities()) {
			if (unit.getUnitRole().isOffensive()) {
				count++;
			}
		}
		if (count != amount) {
			failWithMessage(
				"expected indian settlement <%s> has units with offensive role in amount <%d> but in has in amount <%d>", 
				actual.getId(),
				amount,
				count
			);
		}
		return this;
	}

	public IndianSettlementAssert isScouted() {
		if (!actual.isScouted()) {
			failWithMessage("expected indian settlement <%s> is scouted", actual.getId());
		}
		return this;
	}

	public IndianSettlementAssert isNotScouted() {
		if (actual.isScouted()) {
			failWithMessage("expected indian settlement <%s> is not scouted", actual.getId());
		}
		return this;
	}
}
