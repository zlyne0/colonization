package net.sf.freecol.common.model.player;

import org.assertj.core.api.AbstractAssert;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Unit;

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

	public PlayerAssert containsUnit(Unit unit) {
		if (!actual.units.containsId(unit)) {
			failWithMessage("expected player <%s> contain unit <%s>", actual.getId(), unit.getId());
		}
		return this;
	}
	
	public PlayerAssert hasNotificationSize(int size) {
		if (actual.eventsNotifications.getNotifications().size() != size) {
			failWithMessage("expected player <%s> has <%s> notifications, but has <%s>", 
				actual.getId(), 
				size,
				actual.eventsNotifications.getNotifications().size()
			);
		}
		return this;
	}
	
	public PlayerAssert hasNotSettlement(Settlement settlement) {
	    if (actual.settlements.containsId(settlement)) {
	        failWithMessage("expected player <%s> has not settlement <%s>", actual.getId(), settlement.getId());
	    }
	    return this;
	}
	
	public PlayerAssert hasColony(Colony colony) {
		if (!actual.settlements.containsId(colony)) {
			failWithMessage("expected player <%s> has colony <%s>", actual.getId(), colony.getId());
		}
		if (actual.notEqualsId(colony.getOwner())) {
			failWithMessage("expected player <%s> has colony <%s>", actual.getId(), colony.getId());
		}
		return this;
	}
	
	public PlayerAssert containsExactlyBanMissions(String ... playerIds) {
	    org.assertj.core.api.Assertions.assertThat(actual.banMission)
	        .isNotNull()
	        .containsExactly(playerIds);
	    return this;
	}
    
	public PlayerAssert hasGold(int gold) {
		if (actual.getGold() != gold) {
			failWithMessage("expected player <%s> has gold equals <%s> but has <%s>", actual.getId(), gold, actual.getGold());
		}
		return this;
	}

	public PlayerAssert hasStance(Player player, Stance stance) {
		Stance actualStance = actual.getStance(player);
		if (actualStance != stance) {
			failWithMessage("expected player <%s> has stance <%s> with player <%s> but has <%s>", 
				actual.getId(), stance, player.getId(), actualStance
			);
		}
		return this;
	}

	public PlayerAssert hasNotFoundingFather(FoundingFather ff) {
		return hasNotFoundingFather(ff.getId());
	}
	
	public PlayerAssert hasNotFoundingFather(String ffId) {
		if (actual.foundingFathers.containsId(ffId)) {
			failWithMessage("expected player <%s> has not founding father <%s> but player has", 
				actual.getId(), ffId
			);
		}
		return this;
	}
}
