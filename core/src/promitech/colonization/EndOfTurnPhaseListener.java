package promitech.colonization;

import net.sf.freecol.common.model.player.Player;

public interface EndOfTurnPhaseListener {

	void nextAIturn(Player player);

	void endOfAIturns();
	
}
