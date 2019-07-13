package promitech.colonization.screen.map.hud;

import net.sf.freecol.common.model.player.Player;

interface EndOfTurnPhaseListener {

	void nextAIturn(Player player);

	void endOfAIturns();
	
}
