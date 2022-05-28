package net.sf.freecol.common.model;

import org.assertj.core.api.AbstractAssert;

import net.sf.freecol.common.model.player.Player;

public class TileAssert extends AbstractAssert<TileAssert, Tile> {

	private TileAssert(Tile actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static TileAssert assertThat(Tile tile) {
		return new TileAssert(tile, TileAssert.class);
	}

	public TileAssert hasNotUnit(Unit unit) {
		isNotNull();
		if (actual.getUnits().containsId(unit)) {
			failWithMessage("expected no unit <%s> on tile %s, [%d, %d]", unit.getId(), 
				actual.id, actual.x, actual.y
			);
		}
		return this;
	}

	public TileAssert hasUnit(Unit unit) {
	    isNotNull();
	    if (!actual.getUnits().containsId(unit)) {
            failWithMessage("expected unit <%s> on tile %s, [%d, %d]", unit.getId(),
                actual.id, actual.x, actual.y
            );
        }
	    return this;
	}

	public TileAssert isEquals(Tile tile) {
    	return isEquals(tile.x, tile.y);
    }
	
    public TileAssert isEquals(int x, int y) {
        isNotNull();
        
        if (!actual.equalsCoordinates(x, y)) {
            failWithMessage("Expected cords [%s,%s] on tile <id: %s, cords %s, %s> ", x, y, actual.getId(), actual.x, actual.y);
        }
        return this;
    }
    
    public TileAssert hasNotSettlement() {
        if (actual.getSettlement() != null) {
            failWithMessage("expected tile id: %s, x=\"%s\", y=\"%s\" has not settlement", actual.getId(), actual.x, actual.y);
        }
        return this;
    }
	
    public TileAssert hasSettlementOwnBy(Player player) {
    	if (!actual.hasSettlement()) {
    		failWithMessage("expected tile id: %s has settlement", actual.getId());
    	}
    	if (actual.getSettlement().asColony().getOwner().notEqualsId(player)) {
    		failWithMessage("expected settlement at tile id: %s has owner %s but own by %s", 
				actual.getId(),
				player.getId(),
				actual.getSettlement().asColony().getOwner().getId()
			);
    	}
    	return this;
    }

	public TileAssert hasImprovement(TileImprovementType improvementType) {
		return hasImprovement(improvementType.getId());
	}

	public TileAssert hasImprovement(String improvementTypeId) {
		if (!actual.hasImprovementType(improvementTypeId)) {
			failWithMessage("expected tile id: %s has improvement type %s", actual.getId(), improvementTypeId);
		}
		return this;
	}
}
