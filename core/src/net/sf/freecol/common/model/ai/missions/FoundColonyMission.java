package net.sf.freecol.common.model.ai.missions;

import java.io.IOException;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class FoundColonyMission extends AbstractMission {
	public Tile destTile;
	public final Unit unit;

    public FoundColonyMission(String id, Tile destTile, Unit unit) {
        super(id);
        this.destTile = destTile;
        this.unit = unit;
    }
	
	public FoundColonyMission(Tile destTile, Unit unit) {
	    this(Game.idGenerator.nextId(FoundColonyMission.class), destTile, unit);
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
	
	public static class Xml extends XmlNodeParser<FoundColonyMission> {

		private static final String ATTR_UNIT = "unit";
        private static final String ATTR_DEST = "dest";

        @Override
		public void startElement(XmlNodeAttributes attr) {
		    FoundColonyMission m = new FoundColonyMission(
		        attr.getId(), 
		        game.map.getSafeTile(attr.getPoint(ATTR_DEST)), 
		        PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_UNIT))
	        );
		    nodeObject = m;
		}

		@Override
		public void startWriteAttr(FoundColonyMission node, XmlNodeAttributesWriter attr) throws IOException {
		    attr.setId(node);
		    attr.setPoint(ATTR_DEST, node.destTile.x, node.destTile.y);
		    attr.set(ATTR_UNIT, node.unit);
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