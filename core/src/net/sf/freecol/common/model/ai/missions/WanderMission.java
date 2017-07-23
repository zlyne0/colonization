package net.sf.freecol.common.model.ai.missions;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.UnitMissionsMapping;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.Direction;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class WanderMission extends AbstractMission {

	public final Unit unit;
	public Direction previewDirection;
	
	public WanderMission(Unit unit) {
		super(Game.idGenerator.nextId(WanderMission.class));
		this.unit = unit;
	}

	@Override
	public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.blockUnit(unit, this);
	}

	@Override
	public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.unblockUnitFromMission(unit, this);
	}
	
	public String toString() {
		return "WanderMission unit: " + unit + ", previewDirection: " + previewDirection;
	}
	
	public static class Xml extends XmlNodeParser<Player> {

		@Override
		public void startElement(XmlNodeAttributes attr) {
			// TODO: details load save as xml
		}

		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "wanderMission";
		}
		
	}
	
}
