package promitech.colonization.actors.europe;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.actors.UnitsPanel;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class HighSeasUnitsPanel extends Table {
	private final UnitsPanel ingoingUnits;
	private final UnitsPanel outgoingUnits;
	
	public HighSeasUnitsPanel() {
		ingoingUnits = new UnitsPanel();
		outgoingUnits = new UnitsPanel();
		
		defaults().fillX().expandX();
		add(ingoingUnits).row();
		add(outgoingUnits);
	}

	public void initUnits(Player player) {
		StringTemplate st = StringTemplate.template("goingTo")
	        .add("%type%", "ship")
	        .addStringTemplate("%location%", StringTemplate.key(player.nation().getId() + ".europe"));		
		ingoingUnits.withLabel(Messages.message(st));
		
		st = StringTemplate.template("goingTo")
		        .add("%type%", "ship")
		        .add("%location%", player.getNewLandName());		
		outgoingUnits.withLabel(Messages.message(st));
		
		
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
