package promitech.colonization.actors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import net.sf.freecol.common.model.Turn;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.GameResources;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class PlayerGoldTaxYearLabel extends Label {
	private Player player;
	private Turn turn;
	
	private int gold = -1;
	private int tax = 0;
	private int turnNumber = 0;
	
	public PlayerGoldTaxYearLabel() {
		super("", GameResources.instance.getUiSkin());
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (gold != player.getGold() || tax != player.getTax() || turnNumber != turn.getNumber()) {
			String str = 
					Messages.msg("goldTitle") + ": " + player.getGold() + ", " + 
					Messages.msg("tax") + ": " + player.getTax() + "%, " +
					Messages.message(StringTemplate.template("year." + turn.getSeason()).addAmount("%year%", turn.getYear())); 
			setText(str);
			this.gold = player.getGold();
			this.tax = player.getTax();
			this.turnNumber = turn.getNumber();
		}
		super.draw(batch, parentAlpha);
	}
	
	public void init(Player player, Turn turn) {
		this.player = player;
		this.turn = turn;
	}
}