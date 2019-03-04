package net.sf.freecol.common.model;

import promitech.colonization.ui.resources.StringTemplate;

public class Turn {

    public static enum Season { YEAR, SPRING, AUTUMN }
	
    /**
     * The first years of the "ages" of the game, which are only used
     * for weighting {@link FoundingFather}s.
     */
    private static final int[] AGES = {
        1492, 1600, 1700
    };

    /** The year in which the game starts. */
    private static int startingYear = 1492;
    
    /** The first year in which there are two seasons. */
    private static int seasonYear = 1600;
    
    /**
     * The number of ages.
     * Used by FoundingFather for age-dependent weights.
     */
    public static final int NUMBER_OF_AGES = AGES.length;
    
    private int turn = 1;
    
    public Turn(int turn) {
    	this.turn = turn;
    }
    
    public Season getSeason() {
        int year = turn - 1 + startingYear;
        return (year < seasonYear) ? Season.YEAR
            : (year % 2 == 0) ? Season.SPRING
            : Season.AUTUMN;
    }
    
    public int getYear() {
        int year = turn - 1 + startingYear;
        return (year < seasonYear) ? year : seasonYear + (year - seasonYear)/2;
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
		return turn;
	}

	public StringTemplate getTurnDateLabel() {
		return StringTemplate.template("year." + getSeason()).addAmount("%year%", getYear());
	}

	public void increaseTurnNumber() {
		turn += 1;
	}
	
}
