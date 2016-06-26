package promitech.colonization.actors.europe;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.HighSeas;
import promitech.colonization.actors.UnitActor;
import promitech.colonization.actors.UnitsPanel;

public class HighSeasUnitsPanel extends Table {

	private final UnitsPanel ingoingUnits;
	private final UnitsPanel outgoingUnits;
	
	public HighSeasUnitsPanel() {
		ingoingUnits = new UnitsPanel();
		outgoingUnits = new UnitsPanel();
		
		add(ingoingUnits).row();
		add(outgoingUnits);
	}

	public void initUnits(HighSeas highSeas) {
		ingoingUnits.clear();
		outgoingUnits.clear();
	
		for (Unit unit : highSeas.getUnits().entities()) {
			if (unit.isDestinationEurope()) {
				ingoingUnits.addUnit(new UnitActor(unit, null));
			}
			if (unit.isDestinationTile()) {
				outgoingUnits.addUnit(new UnitActor(unit, null));
			}
		}
	}
	
}
