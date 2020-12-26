package net.sf.freecol.common.model.ai.missions.workerrequest;

import java.io.IOException;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;

public class ColonyWorkerMission extends AbstractMission {

	private Tile tile;
	private Unit unit;

	public ColonyWorkerMission(Tile tile, Unit unit) {
		super(Game.idGenerator.nextId(ColonyWorkerMission.class));
		this.tile = tile;
		this.unit = unit;
	}
	
	private ColonyWorkerMission(String id) {
		super(id);
	}

	@Override
	public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.blockUnit(unit, this);
	}

	@Override
	public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.unblockUnitFromMission(unit, this);
	}

	public Tile getTile() {
		return tile;
	}

	public Unit getUnit() {
		return unit;
	}

    public static class Xml extends AbstractMission.Xml<ColonyWorkerMission> {

        private static final String ATTR_UNIT = "unit";
        private static final String ATTR_TILE = "tile";

        @Override
        public void startElement(XmlNodeAttributes attr) {
            ColonyWorkerMission m = new ColonyWorkerMission(attr.getId());
            m.tile = game.map.getSafeTile(attr.getPoint(ATTR_TILE));
            m.unit = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_UNIT));
            nodeObject = m;
        }

        @Override
        public void startWriteAttr(ColonyWorkerMission mission, XmlNodeAttributesWriter attr) throws IOException {
            attr.setId(mission);
            attr.setPoint(ATTR_TILE, mission.tile.x, mission.tile.y);
            attr.set(ATTR_UNIT, mission.unit);
        }

        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "colonyWorkerMission";
        }
    }

}
