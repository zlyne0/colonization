package promitech.colonization.screen.ui;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.ui.QuestionDialog;
import promitech.colonization.ui.resources.StringTemplate;

public class IndianLandDemandQuestionsDialog extends QuestionDialog {

	public IndianLandDemandQuestionsDialog(
			int landPrice, 
			final Unit claimedUnit, 
			final Tile claimedTile, 
			final QuestionDialog.OptionAction<Unit> actionAfterDemand) 
	{
		QuestionDialog.OptionAction<Unit> takeLandAction = new QuestionDialog.OptionAction<Unit>() {
			@Override
			public void executeAction(Unit claimedUnit) {
				claimedTile.demandTileByPlayer(claimedUnit.getOwner());
				
				actionAfterDemand.executeAction(claimedUnit);
			}
		};
		QuestionDialog.OptionAction<Unit> payForLandAction = new QuestionDialog.OptionAction<Unit>() {
			@Override
			public void executeAction(Unit claimedUnit) {
				if (claimedTile.buyTileByPlayer(claimedUnit.getOwner())) {
					actionAfterDemand.executeAction(claimedUnit);
				}
			}
		};
		
		if (claimedUnit.getOwner().hasContacted(claimedTile.getOwner())) {
			addQuestion(StringTemplate.template("indianLand.text")
				.addStringTemplate("%player%", claimedTile.getOwner().getNationName())
			);
			if (landPrice > 0) {
				StringTemplate landPriceStrTemp = StringTemplate.template("indianLand.pay").addAmount("%amount%", landPrice);
				addAnswer(landPriceStrTemp, payForLandAction, claimedUnit);
			}
		} else {
			addQuestion(StringTemplate.template("indianLand.unknown"));
		}
		
		addAnswer("indianLand.take", takeLandAction, claimedUnit);
		addOnlyCloseAnswer("indianLand.cancel");
	}
	
}
