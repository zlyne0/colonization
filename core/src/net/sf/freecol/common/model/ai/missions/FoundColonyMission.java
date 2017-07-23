package net.sf.freecol.common.model.ai.missions;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.UnitMissionsMapping;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class FoundColonyMission extends AbstractMission {
	public Tile destTile;
	public final Unit unit;
	
	public FoundColonyMission(Tile destTile, Unit unit) {
		super(Game.idGenerator.nextId(FoundColonyMission.class));
        this.destTile = destTile;
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
	
	public boolean isUnitInDestination() {
		Tile ut = unit.getTileLocationOrNull();
		return ut != null && destTile != null && ut.equalsCoordinates(destTile.x, destTile.y);
	}
	
	public void toStringDebugTileTab(String[][] tilesTab) {
	    tilesTab[destTile.y][destTile.x] = "build colony dest";
	}
	
	public String toString() {
		return "FoundColonyMission[destTile: " + destTile.toStringCords() + ", unit: " + unit + "]";
	}
	
	public static class Xml extends XmlNodeParser<Player> {

		@Override
		public void startElement(XmlNodeAttributes attr) {
		}

		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "foundColonyMission";
		}
	}
}