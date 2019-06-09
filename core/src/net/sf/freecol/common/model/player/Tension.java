/**
 *  Copyright (C) 2002-2015   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.model.player;

import java.util.Locale;


/**
 * A measure of the tension between nations.
 */
public class Tension {

    // Minimum possible tension value.
    public static final int TENSION_MIN = 0;
    // Maximum possible tension value.
    public static final int TENSION_MAX = Level.HATEFUL.limit + 100;
    // Hysteresis value
    public static final int DELTA = 10;
    // Unit destroyed, etc
    public static final int TENSION_ADD_MINOR = 100;
    // Unit destroyed in a Settlement, etc
    public static final int TENSION_ADD_NORMAL = 200;
    // Unit destroyed in a capital, etc
    public static final int TENSION_ADD_MAJOR = 300;
    // Grab land without paying
    public static final int TENSION_ADD_LAND_TAKEN = 200;
    // Unit destroyed
    public static final int TENSION_ADD_UNIT_DESTROYED = 400;
    // Settlement attacked
    public static final int TENSION_ADD_SETTLEMENT_ATTACKED = 500;
    // Capital attacked
    public static final int TENSION_ADD_CAPITAL_ATTACKED = 600;
    // War inciter
    public static final int TENSION_ADD_WAR_INCITER = 250;
    // Alarm added when a new missionary is added. 
    public static final int ALARM_NEW_MISSIONARY = -100;

    // Tension modifiers
    public static final int CONTACT_MODIFIER = 0;
    public static final int ALLIANCE_MODIFIER = -500;
    public static final int DROP_ALLIANCE_MODIFIER = 200;
    public static final int PEACE_TREATY_MODIFIER = -250;
    public static final int CEASE_FIRE_MODIFIER = -250;
    public static final int WAR_MODIFIER = Level.HATEFUL.limit;
    public static final int RESUME_WAR_MODIFIER = 750; // War from cease fire

    /** Tension level to set when surrendering. */
    public static final int SURRENDERED = (Level.CONTENT.limit + Level.HAPPY.limit) / 2;
    
    /** 
     * Constants for describing alarm levels.
     */
    public static enum Level { 
        HAPPY(100),
        CONTENT(600), 
        DISPLEASED(700),
        ANGRY(800), 
        HATEFUL(1000);

        private final int limit;

        Level(int limit) {
            this.limit = limit;
        }

        public int getLimit() {
            return limit;
        }
        
        public boolean worst(Level level) {
        	if (level == null) {
        		return false;
        	}
        	return this.limit > level.limit;
        }
    }
    
    private int value;


    /**
     * Create the default tension.
     */
    public Tension() {
        setValue(Level.HAPPY.getLimit());
    }

    /**
     * Create tension at a specified level.
     *
     * @param newTension The level of tension.
     */
    public Tension(int newTension) {
        setValue(newTension);
    }

    /**
     * Get the current tension value.
     *
     * @return The value of this <code>Tension</code>.
     */
    public final int getValue() {
        return this.value;
    }

    /**
     * Sets the current tension value.
     *
     * @param newValue The new value of the tension.
     */
    public final void setValue(int newValue) {
        if (newValue < TENSION_MIN) {
            value = TENSION_MIN;
        } else if (newValue > TENSION_MAX) {
            value = TENSION_MAX;
        } else {
            value = newValue;
        }
    }

    /**
     * Get the current tension level.
     *
     * @return The current level.
     */
    public final Level getLevel() {
        for (Level level : Level.values()) {
            if (value <= level.getLimit()) {
            	return level;
            }
        }
        return Level.HATEFUL;
    }

    /**
     * Modify the tension by the given amount.
     *
     * @param newTension The amount to modify tension by.
     */
    public final void modify(int newTension) {
        setValue(value + newTension);
    }

    /**
     * Gets a message id for this tension.
     *
     * @return A message id string.
     */
    public String getKey() {
        return "tension." + getLevel().toString().toLowerCase(Locale.US);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getLevel().toString();
    }

    public void surrende() {
        setValue(SURRENDERED);
    }

}
