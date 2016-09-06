package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.GameOptions;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Monarch extends ObjectWithId {

    public static enum MonarchAction {
        NO_ACTION,
        RAISE_TAX_ACT,
        RAISE_TAX_WAR,
        FORCE_TAX,
        LOWER_TAX_WAR,
        LOWER_TAX_OTHER,
        WAIVE_TAX,
        ADD_TO_REF,
        DECLARE_PEACE,
        DECLARE_WAR,
        SUPPORT_LAND,
        SUPPORT_SEA,
        MONARCH_MERCENARIES, 
        HESSIAN_MERCENARIES,
        DISPLEASURE,
    }
    
    /** The minimum price for a monarch offer of mercenaries. */
    public static final int MONARCH_MINIMUM_PRICE = 200;

    /** The minimum price for a Hessian offer of mercenaries. */
    public static final int HESSIAN_MINIMUM_PRICE = 5000;
    
    /**
     * The minimum tax rate (given in percentage) from where it
     * can be lowered.
     */
    public static final int MINIMUM_TAX_RATE = 20;

    private Player player;
    private String nameKey;
    private boolean supportSea = false;
    private boolean displeasure = false;
    
    public Monarch(String id) {
        super(id);
    }

    public boolean actionIsValid(Game game, Player player, MonarchAction action) {
        switch (action) {
        case NO_ACTION:
            return true;
        case RAISE_TAX_ACT: 
        case RAISE_TAX_WAR:
            return player.getTax() < getMaximumTaxInGame();
        case FORCE_TAX:
            return false;
        case LOWER_TAX_WAR: 
        case LOWER_TAX_OTHER:
            return player.getTax() > MINIMUM_TAX_RATE + 10;
        case WAIVE_TAX:
            return true;
// TODO:            
//        case ADD_TO_REF:
//            return !(navalREFUnitTypes.isEmpty() || landREFUnitTypes.isEmpty());
//        case DECLARE_PEACE:
//            return !collectPotentialFriends().isEmpty();
//        case DECLARE_WAR:
//            return !collectPotentialEnemies().isEmpty();
        case SUPPORT_SEA:
            return player.isAttackedByPrivateers() && !isSupportSea() && !isDispleasure();
        case SUPPORT_LAND: 
        case MONARCH_MERCENARIES:
            return player.atWarWithAnyEuropean(game.players) && !isDispleasure();
        case HESSIAN_MERCENARIES:
            return player.hasGold(HESSIAN_MINIMUM_PRICE);
        case DISPLEASURE:
            return false;
        default:
            throw new IllegalArgumentException("Bogus monarch action: " + action);
        }
    }

    private int getMaximumTaxInGame() {
        return Specification.options.getIntValue(GameOptions.MAXIMUM_TAX);
    }

    protected String getNameKey() {
        return nameKey;
    }

    protected boolean isSupportSea() {
        return supportSea;
    }

    protected boolean isDispleasure() {
        return displeasure;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public static class Xml extends XmlNodeParser {

        @Override
        public void startElement(XmlNodeAttributes attr) {
            Monarch monarch = new Monarch(attr.getStrAttribute("id"));
            
            monarch.nameKey = attr.getStrAttribute("nameKey");
            monarch.supportSea = attr.getBooleanAttribute("supportSea", false);
            monarch.displeasure = attr.getBooleanAttribute("displeasure", false);
            // TODO: load interventionForce etc
            nodeObject = monarch;
        }

        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "monarch";
        }
    }

}
