package net.sf.freecol.common.model;

import java.util.List;

import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import promitech.colonization.Randomizer;

public class UnitFactory {

	public static Unit createTreasureTrain(Player owner, UnitLocation location, int gold) {
		List<UnitType> carryTreasureUnitTypes = Specification.instance.getUnitTypesWithAbility(Ability.CARRY_TREASURE);
		UnitType carryTreasureUnitType = Randomizer.instance().randomMember(carryTreasureUnitTypes);
		Unit unit = new Unit(
			Game.idGenerator.nextId(Unit.class), 
			carryTreasureUnitType, 
			Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID), 
			owner
		);
		unit.setTreasureAmount(gold);
		unit.setState(UnitState.ACTIVE);
		unit.reduceMovesLeftToZero();
		unit.changeUnitLocation(location);
		
		owner.units.add(unit);
		return unit;
	}

}
