package promitech.colonization.actors.europe;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.actors.OutsideUnitsPanel;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class HighSeasUnitsPanel extends Table {
	private final OutsideUnitsPanel	ingoingUnits;
	private final OutsideUnitsPanel	outgoingUnits;
	
	public HighSeasUnitsPanel() {
		ingoingUnits = new OutsideUnitsPanel();
		outgoingUnits = new OutsideUnitsPanel();
		
		defaults().fillX().expandX();
		add(ingoingUnits).row();
		add(outgoingUnits);
	}

	public void initUnits(Player player) {
		StringTemplate st = StringTemplate.template("goingTo")
	        .add("%type%", "ship")
	        .addStringTemplate("%location%", StringTemplate.key(player.nation().getId() + ".europe"));		
		ingoingUnits.setLabelStr(Messages.message(st));
		
		st = StringTemplate.template("goingTo")
		        .add("%type%", "ship")
		        .add("%location%", player.getNewLandName());		
		outgoingUnits.setLabelStr(Messages.message(st));
		
		
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
