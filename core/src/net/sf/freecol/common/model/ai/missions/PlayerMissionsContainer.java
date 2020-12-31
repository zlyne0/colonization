package net.sf.freecol.common.model.ai.missions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.SAXException;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.MapIdEntitiesReadOnly;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMission;
import net.sf.freecol.common.model.ai.missions.indian.DemandTributeMission;
import net.sf.freecol.common.model.ai.missions.indian.IndianBringGiftMission;
import net.sf.freecol.common.model.ai.missions.indian.WanderMission;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerMission;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.util.Predicate;

import static promitech.colonization.ai.MissionHandlerLogger.*;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

/**
 * Wdech, wydech
 * Jesz, srasz, śpisz.
 * Bierzesz, co Ci dają i niczego nie dajesz w zamian.  
 */
public class PlayerMissionsContainer extends ObjectWithId {

	private final MapIdEntities<AbstractMission> missions = MapIdEntities.linkedMapIdEntities();
	private final Player player;
	private final UnitMissionsMapping unitMissionsMapping = new UnitMissionsMapping();
	
	public PlayerMissionsContainer(Player player) {
		super(player.getId());
		this.player = player;
	}

	public void addMission(AbstractMission m) {
		logger.debug("player[%s] add mission[%s]", player.getId(), m.getId());
		missions.add(m);
		blockUnitsForMission(m);
	}

	public void clearAllMissions() {
		missions.clear();
	}

	public void clearDoneMissions() {
		List<AbstractMission> l = new ArrayList<AbstractMission>(missions.entities());
		for (AbstractMission am : l) {
			if (am.isDone() && !am.hasDependMissions()) {
				logger.debug("player[%s] clear done mission[%s]", player.getId(), am.getId());
				missions.removeId(am);
				am.unblockUnits(unitMissionsMapping);
			}
		}
	}

	public <T extends AbstractMission> boolean hasMission(Class<? extends AbstractMission> clazz, Predicate<T> predicate) {
		for (AbstractMission abstractMission : missions) {
			if (abstractMission.is(clazz) && predicate.test((T)abstractMission)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasMissionType(Class<? extends AbstractMission> clazz) {
		for (AbstractMission am : missions.entities()) {
			if (am.getClass() == clazz) {
				return true;
			}
			if (am.hasDependMissionsType(clazz)) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractMission> T firstMissionByType(Class<T> clazz) {
		for (AbstractMission abstractMission : missions.entities()) {
			if (abstractMission.is(clazz)) {
				return (T)abstractMission;
			}
		}
		throw new IllegalArgumentException("can not find mission by type " + clazz);
	}
	
	public MapIdEntitiesReadOnly<AbstractMission> getMissions() {
		return missions;
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractMission> T getMission(String id) {
		return (T)missions.getById(id);
	}
	
	public void blockUnitForMission(Unit unit, AbstractMission mission) {
		unitMissionsMapping.blockUnit(unit, mission);
	}
	
	private void blockUnitsForMission(AbstractMission mission) {
		mission.blockUnits(unitMissionsMapping);
		
		if (mission.hasDependMissions()) {
			List<AbstractMission> dm = new ArrayList<AbstractMission>();
			dm.addAll(mission.dependMissions);
			
			while (!dm.isEmpty()) {
				AbstractMission first = dm.remove(0);
				first.blockUnits(unitMissionsMapping);
				dm.addAll(first.dependMissions);
			}
		}
	}

	public void unblockUnitFromMission(Unit unit, AbstractMission mission) {
		if (unit == null) {
			return;
		}
		unitMissionsMapping.unblockUnitFromMission(unit, mission);
	}
	
	public void unblockUnitsFromMission(AbstractMission mission) {
		mission.unblockUnits(unitMissionsMapping);
	}

	public <T extends AbstractMission> List<T> findMissions(Class<T> clazz, Unit unit) {
		List<T> filteredMissions = null;
		for (AbstractMission abstractMission : unitMissionsMapping.getUnitMission(unit)) {
			if (abstractMission.is(clazz)) {
				if (filteredMissions == null) {
					filteredMissions = new LinkedList<T>();
				}
				filteredMissions.add((T)abstractMission);
			}
		}
		if (filteredMissions == null) {
			filteredMissions = Collections.emptyList();
		}
		return filteredMissions;
	}
	
	public boolean isUnitBlockedForMission(Unit unit) {
		return unitMissionsMapping.isUnitInMission(unit.getId());
	}

	public boolean isUnitBlockedForMission(String unitId) {
		return unitMissionsMapping.isUnitInMission(unitId);
	}
	
	public void interruptMission(Unit unit) {
		for (AbstractMission mission : unitMissionsMapping.getUnitMission(unit)) {
			mission.setDone();
			unitMissionsMapping.unblockUnitFromMission(unit, mission);
		}
	}

	public Player getPlayer() {
		return player;
	}
	
    public static class Xml extends XmlNodeParser<PlayerMissionsContainer> {
        private static final String ATTR_PLAYER = "player";

        private static Player player;
        public static Unit getPlayerUnit(String unitId) {
            return player.units.getByIdOrNull(unitId);
        }
        
        public static Player getPlayer() {
        	return player;
        }
        
        public static IndianSettlement getPlayerIndianSettlement(String settlementId) {
        	Settlement settlement = player.settlements.getByIdOrNull(settlementId);
        	if (settlement == null) {
        		return null;
        	}
        	return settlement.asIndianSettlement();
        }

		public static Colony getPlayerColony(String settlementId) {
			Settlement settlement = player.settlements.getByIdOrNull(settlementId);
			if (settlement == null) {
				return null;
			}
			return settlement.asColony();
		}

        public Xml() {
            addNodeForMapIdEntities("missions", WanderMission.class);
            addNodeForMapIdEntities("missions", TransportUnitMission.class);
            addNodeForMapIdEntities("missions", RellocationMission.class);
            addNodeForMapIdEntities("missions", ExplorerMission.class);
            addNodeForMapIdEntities("missions", IndianBringGiftMission.class);
            addNodeForMapIdEntities("missions", DemandTributeMission.class);
            addNodeForMapIdEntities("missions", TransportGoodsToSellMission.class);
			addNodeForMapIdEntities("missions", ColonyWorkerMission.class);
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            Player player = game.players.getById(attr.getStrAttributeNotNull(ATTR_PLAYER));
            PlayerMissionsContainer pmc = new PlayerMissionsContainer(player);
            nodeObject = pmc;
            
            PlayerMissionsContainer.Xml.player = player;
        }

        @Override
        public void startWriteAttr(PlayerMissionsContainer node, XmlNodeAttributesWriter attr) throws IOException {
            attr.set(ATTR_PLAYER, node.player);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (getTagName().equals(qName)) {
                PlayerMissionsContainer.Xml.player = null;
                markMissionUnitsAsBlocked(nodeObject);
            }
        }
        
        private void markMissionUnitsAsBlocked(PlayerMissionsContainer missionContainer) {
        	for (AbstractMission mission : missionContainer.missions) {
				missionContainer.blockUnitsForMission(mission);
			}
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "playerMissions";
        }

    }
}
