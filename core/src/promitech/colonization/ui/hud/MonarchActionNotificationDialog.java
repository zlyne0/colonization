package promitech.colonization.ui.hud;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Nation;
import net.sf.freecol.common.model.player.Monarch.MonarchAction;
import net.sf.freecol.common.model.player.MonarchActionNotification;
import net.sf.freecol.common.model.player.MonarchLogic;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.GUIGameController;
import promitech.colonization.Randomizer;
import promitech.colonization.ui.QuestionDialog;
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
				System.out.println("answer yes " + payload);
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
	

}
