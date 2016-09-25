package net.sf.freecol.common.model;

import static net.sf.freecol.common.model.UnitRole.*;
import net.sf.freecol.common.model.specification.Goods;

public class UnitRoleLogic {

	public static int maximumAvailableRequiredGoods(final Unit unit, UnitRole newRole, GoodsContainer goodsContainer, ProductionSummary required) {
		int maxRoleCount = DEFAULT_UNIT_ROLE_COUNT;
		for (Goods g : newRole.requiredGoods.entities()) {
			int marg = 0;
			int containerGoodsAmount = goodsContainer.goodsAmount(g.getId());
			for (int i=1; i<=newRole.getMaximumCount(); i++) {
				if (containerGoodsAmount >= i * g.getAmount()) {
					marg = i * g.getAmount();
					maxRoleCount = Math.max(maxRoleCount, i);
				}
			}
			if (marg > 0) {
				required.addGoods(g.getId(), marg);
			}
		}
		
		for (Goods g : unit.unitRole.requiredGoods.entities()) {
			required.addGoods(g.getId(), -g.getAmount() * unit.getRoleCount());
		}
		return maxRoleCount;
	}
	
	public static ProductionSummary minimumRequiredGoods(UnitRole actualRole, UnitRole newRole) {
        ProductionSummary required = new ProductionSummary();
        for (Goods g : newRole.requiredGoods.entities()) {
        	required.addGoods(g.getId(), g.getAmount() * DEFAULT_UNIT_ROLE_COUNT);
		}
		for (Goods g : actualRole.requiredGoods.entities()) {
			required.addGoods(g.getId(), -g.getAmount() * DEFAULT_UNIT_ROLE_COUNT);
		}
        return required;
	}
	
	public static ProductionSummary requiredGoodsToChangeRole(Unit unit, UnitRole newRole) {
        ProductionSummary required = new ProductionSummary();
        
        for (Goods g : newRole.requiredGoods.entities()) {
        	required.addGoods(g.getId(), g.getAmount() * newRole.getMaximumCount());
		}
		for (Goods g : unit.unitRole.requiredGoods.entities()) {
			required.addGoods(g.getId(), -g.getAmount() * unit.roleCount);
		}
	    return required;
	}
	
	
}
