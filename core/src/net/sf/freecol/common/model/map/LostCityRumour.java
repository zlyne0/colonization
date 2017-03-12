package net.sf.freecol.common.model.map;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.RandomChoice;
import net.sf.freecol.common.model.specification.UnitTypeChange.ChangeType;
import net.sf.freecol.common.model.specification.WithProbability;
import promitech.colonization.Randomizer;

public class LostCityRumour {
	
	public static final String CIBOLA_CITY_NAME_KEY_PREFIX = "lostCityRumour.cityName.";
	
    public static enum RumourType {
        NO_SUCH_RUMOUR(true),
        BURIAL_GROUND(true),
        EXPEDITION_VANISHES(false),
        NOTHING(true),
        LEARN(true),
        TRIBAL_CHIEF(true),
        COLONIST(true),
        MOUNDS(true),
        RUINS(true),
        CIBOLA(true),
        FOUNTAIN_OF_YOUTH(true);

    	public final boolean moveUnit;
        
        private RumourType(boolean moveUnit) {
        	this.moveUnit = moveUnit;
        }
    }
	
    public RumourType type(Game game, Unit unit, Tile tile) {
        int percentBad = Specification.options.getIntValue(GameOptions.BAD_RUMOUR);
        int percentGood = Specification.options.getIntValue(GameOptions.GOOD_RUMOUR);
    	
        if (unit.getOwner().getFeatures().hasAbility(Ability.RUMOURS_ALWAYS_POSITIVE)) {
            percentBad = 0;
            percentGood = 100;
        } else {
        	percentBad = (int)unit.unitType.applyModifier(Modifier.EXPLORE_LOST_CITY_RUMOUR, percentBad);
        	percentGood = (int)unit.unitType.applyModifier(Modifier.EXPLORE_LOST_CITY_RUMOUR, percentGood);
        }
        int percentNeutral = Math.max(0, 100 - percentBad - percentGood);
        

        List<WithProbability<RumourType>> l = new ArrayList<WithProbability<RumourType>>(15);
        
        boolean allowLearn = unit.unitType.canBeUpgraded(ChangeType.LOST_CITY);
        if (allowLearn) {
        	l.add(new RandomChoice<RumourType>(RumourType.LEARN, 30 * percentGood));
        	l.add(new RandomChoice<RumourType>(RumourType.TRIBAL_CHIEF, 30 * percentGood));
        	l.add(new RandomChoice<RumourType>(RumourType.COLONIST, 20 * percentGood));        	
        } else {
        	l.add(new RandomChoice<RumourType>(RumourType.TRIBAL_CHIEF, 50 * percentGood));
        	l.add(new RandomChoice<RumourType>(RumourType.COLONIST, 30 * percentGood));
        }
        if (unit.getOwner().isColonial()) {
        	l.add(new RandomChoice<RumourType>(RumourType.FOUNTAIN_OF_YOUTH, 2 * percentGood));
        }
        l.add(new RandomChoice<RumourType>(RumourType.RUINS, 6 * percentGood));
        if (game.getCitiesOfCibola().size() > 0) {
        	l.add(new RandomChoice<RumourType>(RumourType.CIBOLA, 4 * percentGood));
        }
        
        if (tile.getOwner() != null && tile.getOwner().isIndian()) {
            l.add(new RandomChoice<RumourType>(RumourType.BURIAL_GROUND, 25 * percentBad));
            l.add(new RandomChoice<RumourType>(RumourType.EXPEDITION_VANISHES, 75 * percentBad));
        } else {
            l.add(new RandomChoice<RumourType>(RumourType.EXPEDITION_VANISHES, 100 * percentBad));
        }
        l.add(new RandomChoice<RumourType>(RumourType.NOTHING, 100 * percentNeutral));

        WithProbability<RumourType> randomOne = Randomizer.instance().randomOne(l);
        return randomOne.probabilityObject();
    }
    
    
}
