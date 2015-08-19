package net.sf.freecol.common.model;

import java.util.Locale;

/** The stance one player has towards another player. */
public enum Stance {
    // Valid transitions:
    //
    //   [FROM] \  [TO]  U  A  P  C  W        Reasons
    //   ----------------------------------   a = attack
    //   UNCONTACTED  |  -  x  c  x  i    |   c = contact
    //   ALLIANCE     |  x  -  d  x  adit |   d = act of diplomacy
    //   PEACE        |  x  d  -  x  adit |   i = incitement/rebellion
    //   CEASE_FIRE   |  x  d  t  -  adit |   t = change of tension
    //   WAR          |  x  d  ds dt -    |   s = surrender
    //   ----------------------------------   x = invalid
    //
    UNCONTACTED,
    ALLIANCE,
    PEACE,
    CEASE_FIRE,
    WAR;


    // Helpers to enforce valid transitions
    private void badStance() {
        throw new IllegalStateException("Bogus stance");
    }
    private void badTransition(Stance newStance) {
        throw new IllegalStateException("Bad transition: " + this + " -> " + newStance);
    }

    /**
     * Check whether tension has changed enough to merit a stance
     * change.  Do not simply check for crossing tension
     * thresholds, add in Tension.DELTA to provide a bit of
     * hysteresis to dampen ringing.
     *
     * @param tension The <code>Tension</code> to check.
     * @return The <code>Stance</code> appropriate to the tension level.
     */
    public Stance getStanceFromTension(Tension tension) {
        int value = tension.getValue();
        switch (this) {
        case WAR: // Cease fire if tension decreases
            if (value <= Tension.Level.CONTENT.getLimit()-Tension.DELTA) {
                return Stance.CEASE_FIRE;
            }
            break;
        case CEASE_FIRE: // Peace if tension decreases
            if (value <= Tension.Level.HAPPY.getLimit()-Tension.DELTA) {
                return Stance.PEACE;
            }
            // Fall through
        case ALLIANCE: case PEACE: // War if tension increases
            if (value > Tension.Level.HATEFUL.getLimit()+Tension.DELTA) {
                return Stance.WAR;
            }
            break;
        case UNCONTACTED:
            break;
        default:
            this.badStance();
        }
        return this;
    }

    /**
     * A stance change is about to happen.  Get the appropriate tension
     * modifier.
     *
     * @param newStance The new <code>Stance</code>.
     * @return A modifier to the current tension.
     */
    public int getTensionModifier(Stance newStance)
        throws IllegalStateException {
        switch (newStance) {
        case UNCONTACTED:     badTransition(newStance);
        case ALLIANCE:
            switch (this) {
            case UNCONTACTED: badTransition(newStance);
            case ALLIANCE:    return 0;
            case PEACE:       return Tension.ALLIANCE_MODIFIER;
            case CEASE_FIRE:  return Tension.ALLIANCE_MODIFIER + Tension.PEACE_TREATY_MODIFIER;
            case WAR:         return Tension.ALLIANCE_MODIFIER + Tension.CEASE_FIRE_MODIFIER + Tension.PEACE_TREATY_MODIFIER;
            default:          this.badStance();
            }
        case PEACE:
            switch (this) {
            case UNCONTACTED: return Tension.CONTACT_MODIFIER;
            case ALLIANCE:    return Tension.DROP_ALLIANCE_MODIFIER;
            case PEACE:       return 0;
            case CEASE_FIRE:  return Tension.PEACE_TREATY_MODIFIER;
            case WAR:         return Tension.CEASE_FIRE_MODIFIER + Tension.PEACE_TREATY_MODIFIER;
            default:          this.badStance();
            }
        case CEASE_FIRE:
            switch (this) {
            case UNCONTACTED: badTransition(newStance);
            case ALLIANCE:    badTransition(newStance);
            case PEACE:       badTransition(newStance);
            case CEASE_FIRE:  return 0;
            case WAR:         return Tension.CEASE_FIRE_MODIFIER;
            default:          this.badStance();
            }
        case WAR:
            switch (this) {
            case UNCONTACTED: return Tension.WAR_MODIFIER;
            case ALLIANCE:    return Tension.WAR_MODIFIER;
            case PEACE:       return Tension.WAR_MODIFIER;
            case CEASE_FIRE:  return Tension.RESUME_WAR_MODIFIER;
            case WAR:         return 0;
            default:          this.badStance();
            }
        default:
            throw new IllegalStateException("Bogus newStance");
        }
    }

    public String getKey() {
        return toString().toLowerCase(Locale.US);
    }
}
