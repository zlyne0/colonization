package promitech.colonization.orders.combat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.freecol.common.model.player.Stance;
import net.sf.freecol.common.model.specification.Modifier;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

class CombatMsg {
	public static final DecimalFormat MODIFIER_FORMAT = new DecimalFormat("0.00");

	private final CombatSides combatSides;

	CombatMsg(Combat combat) {
		combatSides = combat.combatSides;
	}
	
	public List<String[]> createDefenceModifiersMessages() {
		return createModifiersMessages(combatSides.defenceModifiers.getModifiers());
	}
	
	public List<String[]> createOffenceModifiersMessages() {
		return createModifiersMessages(combatSides.offenceModifers.getModifiers());
	}
	
	private List<String[]> createModifiersMessages(Map<String, List<Modifier>> modifiers) {
		List<String[]> msgs = new ArrayList<String[]>();
		for (Entry<String, List<Modifier>> modEntry : modifiers.entrySet()) {
			for (Modifier modifier : modEntry.getValue()) {
				String sourceId = modifier.getSourceId();
				if (modifier.getSourceId() == null) {
					sourceId = combatSides.combatModifiersNames.get(modifier.getId());
				}
				msgs.add(new String[] {
					Messages.msgName(sourceId),
					createBonusModifierMessage(modifier)
				});
			}
		}
		return msgs;
	}
	
	private String createBonusModifierMessage(Modifier mod) {
		String msg = null;
		if (mod.getValue() >= 0) {
			msg = "+" + MODIFIER_FORMAT.format(mod.getValue());
		} else {
			msg = MODIFIER_FORMAT.format(mod.getValue());
		}
		msg += mod.createTypeMessage();
		return msg;
	}

	StringTemplate createAttackConfirmationMessageTemplate() {
		Stance stance = combatSides.stance();
		String msgStr = null;
		switch (stance) {
	        case WAR:
	        	throw new IllegalStateException("Player at war, no confirmation needed");
	        case CEASE_FIRE:
	        	msgStr = "model.diplomacy.attack.ceaseFire";
	        	break;
	        case ALLIANCE:
	        	msgStr = "model.diplomacy.attack.alliance";
	        	break;
	        case UNCONTACTED: 
	    	case PEACE: 
			default:
				msgStr = "model.diplomacy.attack.peace";
				break;
		}
		
		return StringTemplate.template(msgStr)
			.addStringTemplate("%nation%", combatSides.defender.getOwner().getNationName());
	}
	
}
