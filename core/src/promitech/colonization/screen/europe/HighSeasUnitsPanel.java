package promitech.colonization.screen.europe;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;

import promitech.colonization.screen.ui.UnitsPanel;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class HighSeasUnitsPanel extends Table {
	private final UnitsPanel ingoingUnits;
	private final UnitsPanel outgoingUnits;

	public HighSeasUnitsPanel() {
		ingoingUnits = new UnitsPanel().withReadonlyCargoSummaryPanel();
		outgoingUnits = new UnitsPanel().withReadonlyCargoSummaryPanel();

		this.defaults().fillX().expandX();
		this.add(ingoingUnits).row();
		this.add(outgoingUnits);
	}

	public void initUnits(Player player) {
		StringTemplate st = StringTemplate.template("goingTo")
	        .add("%type%", "ship")
	        .addStringTemplate("%location%", StringTemplate.key(player.nation().getId() + ".europe"));
		
		ingoingUnits.changeLabel(Messages.message(st));
		
		String landName = player.getNewLandName();
		if (landName == null) {
			landName = Messages.msg("NewWorld");
		}
		st = StringTemplate.template("goingTo")
		        .add("%type%", "ship")
		        .add("%location%", landName);		
		outgoingUnits.changeLabel(Messages.message(st));
		
		
		ingoingUnits.clearUnits();
		outgoingUnits.clearUnits();
	
		for (Unit unit : player.getHighSeas().getUnits().entities()) {
			if (unit.isDestinationEurope()) {
				ingoingUnits.addUnit(unit);
			}
			if (unit.isDestinationTile()) {
				outgoingUnits.addUnit(unit);
			}
		}
	}
}