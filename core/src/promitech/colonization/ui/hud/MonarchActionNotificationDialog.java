package promitech.colonization.ui.hud;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Nation;
import net.sf.freecol.common.model.UnitLabel;
import net.sf.freecol.common.model.player.ArmyForceAbstractUnit;
import net.sf.freecol.common.model.player.Monarch.MonarchAction;
import net.sf.freecol.common.model.player.MonarchActionNotification;
import net.sf.freecol.common.model.player.MonarchLogic;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.GUIGameController;
import promitech.colonization.Randomizer;
import promitech.colonization.ui.QuestionDialog;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class MonarchActionNotificationDialog extends QuestionDialog {

	public MonarchActionNotificationDialog(final GUIGameController guiGameController, final MonarchActionNotification ntfhy) {
		Game game = guiGameController.getGame();
		final Player player = game.playingPlayer;
		
		switch (ntfhy.getAction()) {
	    	case RAISE_TAX_ACT:
	    	case RAISE_TAX_WAR:
	    		generateRiseTaxContent(ntfhy, game, player);
	    		
				break;
	    	case LOWER_TAX_WAR:
	    	case LOWER_TAX_OTHER:
	    		generateLowerTaxContent(ntfhy, game, player);
				break;
	    	case WAIVE_TAX:
	    		generateWaiveTaxContent(ntfhy, game, player);
	    		break;
	    	case ADD_TO_REF:
	    	case DECLARE_PEACE:
	    	case DECLARE_WAR:
	    	case SUPPORT_LAND:
	    	case SUPPORT_SEA:
	    	case DISPLEASURE:
	    		genereteContentFromNotificationMsgBody(ntfhy);
	    		break;
	    	case MONARCH_MERCENARIES:
	    		generateMercenariesContent(ntfhy, game, player);
	    		break;
			default:
				throw new IllegalStateException("can not recognize monarch action " + ntfhy.getAction());
		}
		
	}

	private void generateRiseTaxContent(final MonarchActionNotification ntfhy, Game game, final Player player) {
		StringTemplate template = StringTemplate.template("model.monarch.action." + ntfhy.getAction())
			.addName("%goods%", ntfhy.getGoodsType())
			.addAmount("%amount%", ntfhy.getTax());
		
		if (ntfhy.getAction() == MonarchAction.RAISE_TAX_WAR) {
			template.addKey("%nation%", Nation.getRandomNonPlayerNationNameKey(game));
		} else if (ntfhy.getAction() == MonarchAction.RAISE_TAX_ACT) {
			template.addAmount("%number%", Randomizer.getInstance().randomInt(6));
			template.add("%newWorld%", player.getNewLandName());
		}
		
		addQuestion(template);

		OptionAction<MonarchActionNotification> optionActionYes = new OptionAction<MonarchActionNotification>() {
			@Override
			public void executeAction(MonarchActionNotification payload) {
				MonarchLogic.acceptRiseTax(player, payload);
			}

		};
		addAnswer("model.monarch.action." + ntfhy.getAction() + ".yes", optionActionYes, ntfhy);
		
		OptionAction<MonarchActionNotification> optionActionNo = new OptionAction<MonarchActionNotification>() {
			@Override
			public void executeAction(MonarchActionNotification payload) {
				MonarchLogic.refuseRiseTax(player, payload);
			}
		};
		addAnswer("model.monarch.action." + ntfhy.getAction() + ".no", optionActionNo, ntfhy);
	}

	private void generateLowerTaxContent(MonarchActionNotification ntfhy, Game game, final Player player) {
		StringTemplate template = StringTemplate.template("model.monarch.action." + ntfhy.getAction())
                .addAmount("%difference%", player.getTax() - ntfhy.getTax())
                .addAmount("%newTax%", ntfhy.getTax());
		
        if (ntfhy.getAction() == MonarchAction.LOWER_TAX_WAR) {
            template = template.add("%nation%", Nation.getRandomNonPlayerNationNameKey(game));
        } else {
            template = template.addAmount("%number%", Randomizer.getInstance().randomInt(5));
        }
        addQuestion(template);
        
        OptionAction<MonarchActionNotification> confirmAnswer = new OptionAction<MonarchActionNotification>() {
			@Override
			public void executeAction(MonarchActionNotification payload) {
				MonarchLogic.lowerRax(player, payload);
			}
		};
		addAnswer("model.monarch.action." + ntfhy.getAction() + ".no", confirmAnswer, ntfhy);
	}

	private void generateWaiveTaxContent(MonarchActionNotification ntfhy, Game game, Player player) {
		StringTemplate st = StringTemplate.template("model.monarch.action." + ntfhy.getAction());
		addQuestion(st);
		addOnlyCloseAnswer("model.monarch.action." + ntfhy.getAction() + ".no");
	}

	private void genereteContentFromNotificationMsgBody(MonarchActionNotification ntfhy) {
		addQuestion(ntfhy.getMsgBody());
		addOnlyCloseAnswer("model.monarch.action." + ntfhy.getAction() + ".no");
	}

	private void generateMercenariesContent(MonarchActionNotification ntfhy, Game game, final Player player) {
		String st = ""; 
		for (ArmyForceAbstractUnit af : ntfhy.getMercenaries()) {
			if (!st.isEmpty()) {
				st += ", ";
			}
			st += Messages.message(UnitLabel.getLabelWithAmount(af.getUnitType(), af.getUnitRole(), af.getAmount()));
		}
		
		StringTemplate temp = StringTemplate.template("model.monarch.action." + ntfhy.getAction())
                .add("%mercenaries%", st)
                .addAmount("%gold%", ntfhy.getPrice());
		addQuestion(temp);
		
		OptionAction<MonarchActionNotification> optionActionYes = new OptionAction<MonarchActionNotification>() {
			@Override
			public void executeAction(MonarchActionNotification payload) {
				MonarchLogic.buyMercenaries(player, payload);
			}
		};
		addAnswer("model.monarch.action." + ntfhy.getAction() + ".yes", optionActionYes, ntfhy);

		addOnlyCloseAnswer("model.monarch.action." + ntfhy.getAction() + ".no");
	}
	
}
