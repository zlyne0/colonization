package net.sf.freecol.common.model.ai.missions.workerrequest;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping;
import net.sf.freecol.common.model.specification.GoodsType;

import java.io.IOException;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;

public class ColonyWorkerMission extends AbstractMission {

	private Tile tile;
	private String unitId;
	private GoodsType goodsType;
	private int moveNoAccessCounter = 0;

	public ColonyWorkerMission(Tile tile, Unit unit, GoodsType goodsType) {
		super(Game.idGenerator.nextId(ColonyWorkerMission.class));
		this.tile = tile;
		this.unitId = unit.getId();
		this.goodsType = goodsType;
	}
	
	private ColonyWorkerMission(String id) {
		super(id);
	}

	@Override
	public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.blockUnit(unitId, this);
	}

	@Override
	public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.unblockUnitFromMission(unitId, this);
	}

	public boolean isUnitAtDestination(Unit unit) {
		Tile ut = unit.getTileLocationOrNull();
		return ut != null && tile != null && ut.equalsCoordinates(tile);
	}

	public void increaseMoveNoAccessCounter() {
		moveNoAccessCounter++;
	}

	public boolean isMoveNoAccessCounterAboveRange(int range) {
		return moveNoAccessCounter > range;
	}

	public Tile getTile() {
		return tile;
	}

	public String getUnitId() {
		return unitId;
	}

	public GoodsType getGoodsType() {
		return goodsType;
	}

	@Override
	public String toString() {
		String str = "dest: [" + tile.toStringCords() + "]";
		if (tile.hasSettlement()) {
			str += " " + tile.getSettlement().getName();
		}
		str += ", unit[" + unitId + "]";
		str += ", goodsType: " + goodsType.toSmallIdStr();
		return str;
	}

	public static class Xml extends AbstractMission.Xml<ColonyWorkerMission> {

        private static final String ATTR_UNIT = "unit";
        private static final String ATTR_TILE = "tile";
		private static final String ATTR_GOODS_TYPE = "goods-type";
		private static final String ATTR_MOVE_NO_ACCESS_COUNTER = "moveNoAccessCounter";

		@Override
        public void startElement(XmlNodeAttributes attr) {
            ColonyWorkerMission m = new ColonyWorkerMission(attr.getId());
            m.tile = game.map.getSafeTile(attr.getPoint(ATTR_TILE));
            m.unitId = attr.getStrAttribute(ATTR_UNIT);
			m.goodsType = attr.getEntity(ATTR_GOODS_TYPE, Specification.instance.goodsTypes);
			m.moveNoAccessCounter = attr.getIntAttribute(ATTR_MOVE_NO_ACCESS_COUNTER, 0);
            nodeObject = m;
			super.startElement(attr);
        }

        @Override
        public void startWriteAttr(ColonyWorkerMission mission, XmlNodeAttributesWriter attr) throws IOException {
			super.startWriteAttr(mission, attr);
            attr.setId(mission);
            attr.setPoint(ATTR_TILE, mission.tile.x, mission.tile.y);
            attr.set(ATTR_UNIT, mission.unitId);
            attr.set(ATTR_GOODS_TYPE, mission.goodsType);
            attr.set(ATTR_MOVE_NO_ACCESS_COUNTER, mission.moveNoAccessCounter, 0);
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
