package promitech.colonization.screen.europe;

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.GameResources;
import promitech.colonization.screen.ui.UnitsPanel;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class HighSeasUnitsPanel extends ScrollPane {
	private final UnitsPanel ingoingUnits;
	private final UnitsPanel outgoingUnits;
	
	private final Table content = new Table();
	
	public HighSeasUnitsPanel() {
		super(null, GameResources.instance.getUiSkin());
		super.setWidget(content);

        setForceScroll(false, false);
        setFadeScrollBars(false);
        setOverscroll(true, true);
        setScrollBarPositions(true, true);
        setScrollingDisabled(false, false);
		
		
		ingoingUnits = new UnitsPanel();
		outgoingUnits = new UnitsPanel();

		content.defaults().fillX().expandX();
		content.add(ingoingUnits).row();
		content.add(outgoingUnits);
	}

	public void initUnits(Player player) {
		StringTemplate st = StringTemplate.template("goingTo")
	        .add("%type%", "ship")
	        .addStringTemplate("%location%", StringTemplate.key(player.nation().getId() + ".europe"));		
		ingoingUnits.withLabel(Messages.message(st));
		
		String landName = player.getNewLandName();
		if (landName == null) {
			landName = Messages.msg("NewWorld");
		}
		st = StringTemplate.template("goingTo")
		        .add("%type%", "ship")
		        .add("%location%", landName);		
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
