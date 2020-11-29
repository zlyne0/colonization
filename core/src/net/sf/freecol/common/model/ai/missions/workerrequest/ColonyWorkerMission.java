package net.sf.freecol.common.model.ai.missions.workerrequest;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping;
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMission;

import java.io.IOException;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;

public class ColonyWorkerMission extends AbstractMission {

	private Colony colony;
	private Unit unit;

	public ColonyWorkerMission(Colony colony, Unit unit) {
		super(Game.idGenerator.nextId(ColonyWorkerMission.class));
		this.colony = colony;
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

	public Colony getColony() {
		return colony;
	}

	public Unit getUnit() {
		return unit;
	}

    public static class Xml extends AbstractMission.Xml<ColonyWorkerMission> {

        private static final String ATTR_UNIT = "unit";
        private static final String ATTR_COLONY = "colony";

        @Override
        public void startElement(XmlNodeAttributes attr) {
            ColonyWorkerMission m = new ColonyWorkerMission(attr.getId());
            m.colony = PlayerMissionsContainer.Xml.getPlayerColony(attr.getStrAttribute(ATTR_COLONY));
            m.unit = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_UNIT));
            nodeObject = m;
        }

        @Override
        public void startWriteAttr(ColonyWorkerMission mission, XmlNodeAttributesWriter attr) throws IOException {
            attr.setId(mission);
            attr.set(ATTR_COLONY, mission.colony);
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
