package net.sf.freecol.common.model;

import org.assertj.core.api.AbstractAssert;

import net.sf.freecol.common.model.player.Player;

public class PlayerAssert extends AbstractAssert<PlayerAssert, Player> {

	public PlayerAssert(Player actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static PlayerAssert assertThat(Player player) {
		return new PlayerAssert(player, PlayerAssert.class);
	}

	public PlayerAssert notContainsUnit(Unit notContainedUnit) {
	    if (actual.units.containsId(notContainedUnit)) {
	        failWithMessage("expected player <%s> does not contain unit <%s>", actual.getId(), notContainedUnit.getId());
	    }
	    return this;
	}
	
	public PlayerAssert notContainsUnits(MapIdEntities<Unit> notContainedUnits) {
		for (Unit excluded : notContainedUnits.entities()) {
			if (actual.units.containsId(excluded)) {
				failWithMessage("expected player <%s> does not contain unit <%s>", actual.getId(), excluded.getId());
			}
		}
		return this;
	}

	public PlayerAssert containsUnits(MapIdEntities<Unit> units) {
		for (Unit unit : units.entities()) {
			if (!actual.units.containsId(unit)) {
				failWithMessage("expected player <%s> contain unit <%s>", actual.getId(), unit.getId());
			}
		}
		return this;
	}
    
}
