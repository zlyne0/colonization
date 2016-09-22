package net.sf.freecol.common.model.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.WithProbability;
import promitech.colonization.Randomizer;
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
        DISPLEASURE;
        
        public String msgKey() {
            return "model.monarch.action." + this;
        }
        
        public String yesMsgKey() {
            return msgKey() + ".yes";
        }
        
        public String noMsgKey() {
            return msgKey() + ".no";
        }
    }
    
    /** The minimum price for a monarch offer of mercenaries. */
    public static final int MERCENARIES_MINIMUM_PRICE = 200;

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
    
    public ArmyForce expeditionaryForce;
    public ArmyForce interventionForce;
    public ArmyForce mercenaryForce;
    
    public Monarch(String id) {
        super(id);
    }

    public List<WithProbability<MonarchAction>> getActionChoices(Game game) {
        int dx = 1 + Specification.options.getIntValue(GameOptions.MONARCH_MEDDLING);
        int turn = game.getTurn().getNumber();
        int grace = (6 - dx) * 10; // 10-50

        // Nothing happens during the first few turns, if there are no
        // colonies, or after the revolution begins.
        if (turn < grace
            || player.settlements.isEmpty()
            || !player.isColonial()) {
            return Collections.emptyList();
        }
        List<WithProbability<MonarchAction>> choices = new ArrayList<WithProbability<MonarchAction>>(MonarchAction.values().length);

        // The more time has passed, the less likely the monarch will
        // do nothing.
        addIfValid(game, choices, MonarchAction.NO_ACTION, Math.max(200 - turn, 100));
        addIfValid(game, choices, MonarchAction.RAISE_TAX_ACT, 5 + dx);
        addIfValid(game, choices, MonarchAction.RAISE_TAX_WAR, 5 + dx);
        addIfValid(game, choices, MonarchAction.LOWER_TAX_WAR, 5 - dx);
        addIfValid(game, choices, MonarchAction.LOWER_TAX_OTHER, 5 - dx);
        addIfValid(game, choices, MonarchAction.ADD_TO_REF, 10 + dx);
        addIfValid(game, choices, MonarchAction.DECLARE_PEACE, 6 - dx);
        addIfValid(game, choices, MonarchAction.DECLARE_WAR, 5 + dx);
        if (player.hasGold(MERCENARIES_MINIMUM_PRICE)) {
            addIfValid(game, choices, MonarchAction.MONARCH_MERCENARIES, 6-dx);
        } else if (dx < 3) {
            addIfValid(game, choices, MonarchAction.SUPPORT_LAND, 3 - dx);
        }
        addIfValid(game, choices, MonarchAction.SUPPORT_SEA, 6 - dx);
        addIfValid(game, choices, MonarchAction.HESSIAN_MERCENARIES, 6-dx);
        return choices;
    }
    
	private void addIfValid(Game game, List<WithProbability<MonarchAction>> choices, MonarchAction action, int weight) {
		if (actionIsValid(game, action)) {
			choices.add(new MonarchActionProbability(weight, action));
		}
	}
    
    private boolean actionIsValid(Game game, MonarchAction action) {
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
        case ADD_TO_REF:
//          return !(navalREFUnitTypes.isEmpty() || landREFUnitTypes.isEmpty());
        	return true;
        case DECLARE_PEACE:
            return hasPotentialFriends(game);
        case DECLARE_WAR:
            return hasPotentialEnemies(game);
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

    private boolean hasPotentialFriends(Game game) {
    	for (Player p : game.players.entities()) {
    		if (isPotentialFriend(p)) {
    			return true;
    		}
    	}
    	return false;
    }
    
	public List<Player> collectPotentialFriends(Game game) {
		List<Player> friends = new ArrayList<Player>();
		for (Player p : game.players.entities()) {
			if (isPotentialFriend(p)) {
				friends.add(p);
			}
		}
		return friends;
	}
	
	private boolean isPotentialFriend(Player p) {
		return !p.isRoyal() && p.isLiveEuropeanPlayer() && p.notEqualsId(player) && player.atWarOrCeaseFire(p);
	}
    
    private boolean hasPotentialEnemies(Game game) {
    	if (player.getFeatures().hasAbility(Ability.IGNORE_EUROPEAN_WARS)) {
    		return false;
    	}
    	for (Player p : game.players.entities()) {
    		if (isPotentialEnemy(p)) {
    			return true;
    		}
    	}
    	return false;
    }

	private boolean isPotentialEnemy(Player p) {
		return !p.isRoyal() 
				&& p.isLiveEuropeanPlayer() 
				&& p.notEqualsId(player) 
				&& player.atPeaceOrCeaseFire(p) 
				&& !p.getFeatures().hasAbility(Ability.IGNORE_EUROPEAN_WARS);
	}
    
	public List<Player> collectPotentialEnemies(Game game) {
		List<Player> friends = new ArrayList<Player>();
		for (Player p : game.players.entities()) {
			if (isPotentialEnemy(p)) {
				friends.add(p);
			}
		}
		return friends;
	}
    
    private int getMaximumTaxInGame() {
        return Specification.options.getIntValue(GameOptions.MAXIMUM_TAX);
    }

    public int potentialTaxRaiseValue(Game game) {
        int taxAdjustment = Specification.options.getIntValue(GameOptions.TAX_ADJUSTMENT);
        int turn = game.getTurn().getNumber();
        int oldTax = player.getTax();
        int adjust = Math.max(1, (6 - taxAdjustment) * 10); // 20-60
        adjust = 1 + Randomizer.instance().randomInt(5 + turn/adjust);
        return Math.min(oldTax + adjust, getMaximumTaxInGame());
    }
    
    public int generateLowerTaxValue() {
        int taxAdjustment = Specification.options.getIntValue(GameOptions.TAX_ADJUSTMENT);
        int oldTax = player.getTax();
        int adjust = Math.max(1, 10 - taxAdjustment); // 5-10
        adjust = 1 + Randomizer.instance().randomInt(adjust);
        return Math.max(oldTax - adjust, Monarch.MINIMUM_TAX_RATE);
    }
    
    public ArmyForceAbstractUnit chooseForAddRoyalExpedition() {
    	boolean needNaval = expeditionaryForce.capacity() < expeditionaryForce.spaceRequired() + 15;
        List<UnitType> types = (needNaval) ? Specification.instance.getRoyalNavyUnitTypes() : Specification.instance.getRoyalLandUnitTypes();
        if (types.isEmpty()) {
        	return null;
        }
        UnitType unitType = Randomizer.instance().randomMember(types);

        UnitRole role;
        int number = 0;
        if (needNaval || !unitType.hasAbility(Ability.CAN_BE_EQUIPPED)) {
        	role = Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID);
        	number = 1;
        } else {
        	if (Randomizer.instance().randomInt(3) == 0) {
        		role = Specification.instance.unitRoles.getById(UnitRole.CAVALRY_ROLE_ID);
        	} else {
        		role = Specification.instance.unitRoles.getById(UnitRole.INFANTRY_ROLE_ID);
        	}
        	number = Randomizer.instance().randomInt(1, 4);
        }
        return new ArmyForceAbstractUnit(unitType, role, number);
    }

	public List<ArmyForceAbstractUnit> chooseForSupport(final MonarchAction action) {
		List<ArmyForceAbstractUnit> support = new ArrayList<ArmyForceAbstractUnit>();
		
		Specification spec = Specification.instance;
		Randomizer rand = Randomizer.instance();
		
		if (action == MonarchAction.SUPPORT_SEA) {
			support.add(new ArmyForceAbstractUnit(rand.randomMember(spec.navalTypes), spec.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID), 1));
			supportSea = true;
			return support;
		}
		
		int difficulty = Specification.options.getIntValue(GameOptions.MONARCH_SUPPORT);
        switch (difficulty) {
        case 4:
			support.add(new ArmyForceAbstractUnit(rand.randomMember(spec.bombardTypes), spec.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID), 1));
			support.add(new ArmyForceAbstractUnit(rand.randomMember(spec.landTypes), spec.unitRoles.getById(UnitRole.DRAGOON), 2));
            break;
        case 3:
        	support.add(new ArmyForceAbstractUnit(rand.randomMember(spec.landTypes), spec.unitRoles.getById(UnitRole.DRAGOON), 2));
			support.add(new ArmyForceAbstractUnit(rand.randomMember(spec.landTypes), spec.unitRoles.getById(UnitRole.SOLDIER), 1));
            break;
        case 2:
        	support.add(new ArmyForceAbstractUnit(rand.randomMember(spec.landTypes), spec.unitRoles.getById(UnitRole.DRAGOON), 2));
            break;
        case 1:
        	support.add(new ArmyForceAbstractUnit(rand.randomMember(spec.landTypes), spec.unitRoles.getById(UnitRole.DRAGOON), 1));
			support.add(new ArmyForceAbstractUnit(rand.randomMember(spec.landTypes), spec.unitRoles.getById(UnitRole.SOLDIER), 1));
            break;
        case 0:
			support.add(new ArmyForceAbstractUnit(rand.randomMember(spec.landTypes), spec.unitRoles.getById(UnitRole.SOLDIER), 1));
            break;
        default:
            break;
        }
		return support;
	}

	public int chooseMercenaries(final List<ArmyForceAbstractUnit> mercenaries) {
		final int mercPrice = Specification.options.getIntValue(GameOptions.MERCENARY_PRICE);
		
		
		List<UnitRole> unitsRoles = new ArrayList<UnitRole>(2);
		unitsRoles.add(Specification.instance.unitRoles.getById(UnitRole.DRAGOON));
		unitsRoles.add(Specification.instance.unitRoles.getById(UnitRole.SOLDIER));
		
		List<UnitType> mercTypes = new ArrayList<UnitType>(Specification.instance.mercenaryTypes);
		
		int price = 0;
		int unitsRoleCount = Randomizer.instance().randomInt(2, 4);
		while (unitsRoleCount > 0) {
			UnitType unitType = Randomizer.instance().randomMember(mercTypes);
			if (unitType == null) {
				break;
			}
			mercTypes.remove(unitType);
			
			UnitRole unitRole;
			if (unitType.hasAbility(Ability.CAN_BE_EQUIPPED)) {
				unitRole = Randomizer.instance().randomMember(unitsRoles);
			} else {
				unitRole = Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID);
			}
			int unitsCount = Randomizer.instance().randomInt(1, Math.min(unitsRoleCount, 2) + 1);
			unitsRoleCount -= unitsCount;

			ArmyForceAbstractUnit af = new ArmyForceAbstractUnit(unitType, unitRole, 0);
			int oneUnitPrice = player.getEurope().getUnitPrice(unitType, unitRole, 1) * mercPrice / 100;
			for (int i=0; i<unitsCount; i++) {
				if (player.hasGold(price + oneUnitPrice)) {
					price += oneUnitPrice;
					af.increaseAmount();
				}
			}
			if (af.getAmount() > 0) {
				mercenaries.add(af);
			}
		}
		
		System.out.println("Mercenaries: ");
		for (ArmyForceAbstractUnit af : mercenaries) {
			System.out.println("  " + af);
		}		
		return price;
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

	public void setDispleasure(boolean displeasure) {
		this.displeasure = displeasure;
	}

    public void setPlayer(Player player) {
        this.player = player;
    }

	public ArmyForce getExpeditionaryForce() {
		return expeditionaryForce;
	}

	public ArmyForce getInterventionForce() {
		return interventionForce;
	}

	public ArmyForce getMercenaryForce() {
		return mercenaryForce;
	}

    public static class Xml extends XmlNodeParser {

        public Xml() {
            addNode("expeditionaryForce", ArmyForce.class, "expeditionaryForce");
            addNode("interventionForce", ArmyForce.class, "interventionForce");
            addNode("mercenaryForce", ArmyForce.class, "mercenaryForce");
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            Monarch monarch = new Monarch(attr.getStrAttribute("id"));
            
            monarch.nameKey = attr.getStrAttribute("nameKey");
            monarch.supportSea = attr.getBooleanAttribute("supportSea", false);
            monarch.displeasure = attr.getBooleanAttribute("displeasure", false);
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
