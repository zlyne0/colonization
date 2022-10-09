package net.sf.freecol.common.model;

import promitech.colonization.ui.resources.StringTemplate;

public class Turn {

    public enum Season { YEAR, SPRING, AUTUMN }
	
    /**
     * The first years of the "ages" of the game, which are only used
     * for weighting {@link net.sf.freecol.common.model.player.FoundingFather}s.
     */
    private static final int[] AGES = {
        1492, 1600, 1700
    };

    /** The year in which the game starts. */
    private static final int STARTING_YEAR = 1492;
    
    /** The first year in which there are two seasons. */
    private static final int SEASON_YEAR = 1600;
    
    /**
     * The number of ages.
     * Used by FoundingFather for age-dependent weights.
     */
    public static final int NUMBER_OF_AGES = AGES.length;
    
    private final int number;
    
    public Turn(int turn) {
    	this.number = turn;
    }
    
    public Season getSeason() {
        int year = number - 1 + STARTING_YEAR;
        return (year < SEASON_YEAR) ? Season.YEAR
            : (year % 2 == 0) ? Season.SPRING
            : Season.AUTUMN;
    }
    
    public int getYear() {
        int year = number - 1 + STARTING_YEAR;
        return (year < SEASON_YEAR) ? year : SEASON_YEAR + (year - SEASON_YEAR)/2;
    }

    public int getAges() {
    	int year = getYear();
    	for (int i=0; i<AGES.length; i++) {
    		if (year < AGES[i]) {
    			return i;
    		}
    	}
    	return AGES.length - 1;
    }
    
	public int getNumber() {
		return number;
	}

	public StringTemplate getTurnDateLabel() {
		return StringTemplate.template("year." + getSeason()).addAmount("%year%", getYear());
	}

	public Turn increaseTurnNumber() {
        return new Turn(number + 1);
	}
	
}
