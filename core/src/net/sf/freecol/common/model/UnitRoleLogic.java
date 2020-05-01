package net.sf.freecol.common.model;

import static net.sf.freecol.common.model.UnitRole.DEFAULT_UNIT_ROLE_COUNT;

import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.RequiredGoods;

public class UnitRoleLogic {

	private UnitRoleLogic() {
	}
	
	public static int maximumAvailableRequiredGoods(final Unit unit, UnitRole newRole, GoodsContainer goodsContainer, ProductionSummary required) {
		int maxRoleCount = DEFAULT_UNIT_ROLE_COUNT;
		for (RequiredGoods g : newRole.requiredGoods.entities()) {
			int marg = 0;
			int containerGoodsAmount = goodsContainer.goodsAmount(g.getId());
			for (int i=1; i<=newRole.getMaximumCount(); i++) {
				if (containerGoodsAmount >= i * g.amount) {
					marg = i * g.amount;
					maxRoleCount = Math.max(maxRoleCount, i);
				}
			}
			if (marg > 0) {
				required.addGoods(g.getId(), marg);
			}
		}
		
		for (RequiredGoods g : unit.unitRole.requiredGoods.entities()) {
			required.addGoods(g.getId(), -g.amount * unit.getRoleCount());
		}
		return maxRoleCount;
	}
	
	public static ProductionSummary minimumRequiredGoods(UnitRole actualRole, UnitRole newRole) {
        ProductionSummary required = new ProductionSummary();
        for (RequiredGoods g : newRole.requiredGoods.entities()) {
        	required.addGoods(g.getId(), g.amount * DEFAULT_UNIT_ROLE_COUNT);
		}
		for (RequiredGoods g : actualRole.requiredGoods.entities()) {
			required.addGoods(g.getId(), -g.amount * DEFAULT_UNIT_ROLE_COUNT);
		}
        return required;
	}
	
	public static ProductionSummary requiredGoodsToChangeRole(Unit unit, UnitRole newRole) {
        ProductionSummary required = new ProductionSummary();
        
        for (RequiredGoods g : newRole.requiredGoods.entities()) {
        	required.addGoods(g.getId(), g.amount * newRole.getMaximumCount());
		}
		for (RequiredGoods g : unit.unitRole.requiredGoods.entities()) {
			required.addGoods(g.getId(), -g.amount * unit.roleCount);
		}
	    return required;
	}
	
	public static int countRequiredGoodsToChangeRole(GoodsType goodsType, Unit unit, UnitRole newRole) {
		int requiredAmount = 0;
        for (RequiredGoods g : newRole.requiredGoods.entities()) {
        	if (g.goodsType.equalsId(goodsType)) {
        		requiredAmount += g.amount * newRole.getMaximumCount();
        	}
		}
        for (RequiredGoods g : unit.unitRole.requiredGoods.entities()) {
        	if (g.goodsType.equalsId(goodsType)) {
        		requiredAmount -= g.amount * unit.roleCount;
        	}
        }
        if (requiredAmount < 0) {
        	requiredAmount = 0;
        }
		return requiredAmount;
	}
}
