package net.sf.freecol.common.model;

public class Turn {

    /**
     * The first years of the "ages" of the game, which are only used
     * for weighting {@link FoundingFather}s.
     */
    private static final int[] AGES = {
        1492, 1600, 1700
    };

    /**
     * The number of ages.
     * Used by FoundingFather for age-dependent weights.
     */
    public static final int NUMBER_OF_AGES = AGES.length;
    
    
}
