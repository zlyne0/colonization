package net.sf.freecol.common.model.ai.missions;

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
import net.sf.freecol.common.model.ai.missions.military.DefenceMission;
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerMission;
import net.sf.freecol.common.model.ai.missions.pioneer.ReplaceColonyWorkerMission;
import net.sf.freecol.common.model.ai.missions.pioneer.RequestGoodsMission;
import net.sf.freecol.common.model.ai.missions.pioneer.TakeRoleEquipmentMission;
import net.sf.freecol.common.model.ai.missions.scout.ScoutMission;
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerMission;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.util.Predicate;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

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

	public void addMissionWhenNotAdded(AbstractMission m) {
		if (!missions.containsId(m)) {
			addMission(m);
		}
	}

	public void addMission(AbstractMission m) {
		if (logger.isDebug()) {
			logger.debug("player[%s] add mission[%s] %s", player.getId(), m.getId(), m.toString());
		}
		missions.add(m);
		blockUnitsForMission(m);
	}

	public void addMission(AbstractMission parentMission, AbstractMission childMission) {
		if (logger.isDebug()) {
			logger.debug("player[%s].mission[%s] add child mission[%s] %s", player.getId(), parentMission.getId(), childMission.getId(), childMission.toString());
		}

		if (parentMission.dependMissions.isEmpty()) {
			parentMission.dependMissions = new HashSet<String>();
		}
		parentMission.dependMissions.add(childMission.getId());

		childMission.parentMissionId = parentMission.getId();

		missions.add(childMission);
		blockUnitsForMission(childMission);
	}

	/**
	 * @return List of {@link AbstractMission} that is not done and leaf on mission tree
	 */
	public List<AbstractMission> findMissionToExecute() {
		List<AbstractMission> toExecute = new ArrayList<AbstractMission>(missions.size());
		for (AbstractMission mission : missions) {
			if (mission.isDone()) {
				continue;
			}
			if (mission.dependMissions.isEmpty() || isAllDependMissionDone(mission)) {
				toExecute.add(mission);
			}
		}
		return toExecute;
	}

	public AbstractMission findParentToExecute(AbstractMission mission) {
		if (mission.parentMissionId == null) {
			return null;
		}
		AbstractMission parentMission = missions.getByIdOrNull(mission.parentMissionId);
		if (parentMission == null) {
			return null;
		}
		if (isAllDependMissionDone(parentMission)) {
			return parentMission;
		}
		return null;
	}

	public <T extends AbstractMission> T findParentMission(AbstractMission mission, Class<T> parentTypeMission) {
		if (mission.parentMissionId == null) {
			return null;
		}
		AbstractMission parentMission = missions.getByIdOrNull(mission.parentMissionId);
		if (parentMission != null && parentMission.is(parentTypeMission)) {
			return (T)parentMission;
		}
		return null;
	}

	public boolean isAllDependMissionDone(AbstractMission mission) {
		for (String missionId : mission.dependMissions) {
			AbstractMission childMission = missions.getByIdOrNull(missionId);
			if (childMission != null && !childMission.isDone()) {
				return false;
			}
		}
		return true;
	}

	public void clearAllMissions() {
		missions.clear();
		unitMissionsMapping.unblockAll();
	}

	public void clearDoneMissions() {
		List<AbstractMission> list = new ArrayList<AbstractMission>(missions.entities());
		for (AbstractMission am : list) {
			if (am.isDone()) {
				logger.debug("player[%s] clear done mission[%s]", player.getId(), am.getId());
				missions.removeId(am);
				am.unblockUnits(unitMissionsMapping);
				if (am.parentMissionId != null) {
					AbstractMission parentMission = missions.getByIdOrNull(am.parentMissionId);
					if (parentMission != null) {
						parentMission.dependMissions.remove(am.getId());
					}
				}
			}
		}
	}

	public <T extends AbstractMission> List<T> findMissions(Class<T> clazz, Predicate<T> predicate) {
		List<T> result = null;
		for (AbstractMission abstractMission : missions) {
			if (abstractMission.is(clazz) && predicate.test((T)abstractMission)) {
				if (result == null) {
					result = new ArrayList<T>();
				}
				result.add((T)abstractMission);
			}
		}
		if (result == null) {
			return Collections.emptyList();
		}
		return result;
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
	
	private void blockUnitsForMission(AbstractMission mission) {
		mission.blockUnits(unitMissionsMapping);
	}

	public void blockUnitForMission(Unit unit, AbstractMission mission) {
		if (unit == null) {
			return;
		}
		unitMissionsMapping.blockUnit(unit, mission);
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

	/**
	 * Find missions for unit
	 */
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

	protected UnitMissionsMapping getUnitMissionsMapping() {
		return unitMissionsMapping;
	}

	public <T extends AbstractMission> List<T> findMissions(Class<T> clazz) {
		List<T> result = null;
		
		for (AbstractMission abstractMission : missions) {
			if (abstractMission.is(clazz)) {
				if (result == null) {
					result = new ArrayList<T>();
				}
				result.add((T)abstractMission);
			}
		}
		if (result == null) {
			return Collections.emptyList();
		}
		return result;
	}

	public <T extends AbstractMission> boolean isMissionTypeExists(Class<T> clazz) {
		for (AbstractMission abstractMission : missions) {
			if (abstractMission.is(clazz)) {
				return true;
			}
		}
		return false;
	}

	public boolean isUnitBlockedForMission(Unit unit) {
		return unitMissionsMapping.isUnitInMission(unit.getId());
	}

	public boolean isUnitBlockedForMission(String unitId) {
		return unitMissionsMapping.isUnitInMission(unitId);
	}
	
	public MapIdEntitiesReadOnly<AbstractMission> getMissions() {
		return missions;
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractMission> T getMission(String id) {
		return (T)missions.getById(id);
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractMission> T findMission(String id) {
		return (T)missions.getByIdOrNull(id);
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
            addNodeForMapIdEntities("missions", ExplorerMission.class);
            addNodeForMapIdEntities("missions", IndianBringGiftMission.class);
            addNodeForMapIdEntities("missions", DemandTributeMission.class);
            addNodeForMapIdEntities("missions", TransportGoodsToSellMission.class);
			addNodeForMapIdEntities("missions", ColonyWorkerMission.class);
			addNodeForMapIdEntities("missions", ScoutMission.class);
			addNodeForMapIdEntities("missions", PioneerMission.class);
			addNodeForMapIdEntities("missions", RequestGoodsMission.class);
			addNodeForMapIdEntities("missions", TransportUnitRequestMission.class);
			addNodeForMapIdEntities("missions", ReplaceColonyWorkerMission.class);
			addNodeForMapIdEntities("missions", TakeRoleEquipmentMission.class);
			addNodeForMapIdEntities("missions", DefenceMission.class);
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
				createTreeStructure(nodeObject);
            }
        }
        
        private void markMissionUnitsAsBlocked(PlayerMissionsContainer missionContainer) {
        	for (AbstractMission mission : missionContainer.missions) {
				missionContainer.blockUnitsForMission(mission);
			}
        }

        private void createTreeStructure(PlayerMissionsContainer missionContainer) {
			for (AbstractMission mission : missionContainer.missions) {
				if (mission.parentMissionId != null) {
					AbstractMission parentMission = missionContainer.missions.getByIdOrNull(mission.parentMissionId);
					if (parentMission != null) {
						if (parentMission.dependMissions.isEmpty()) {
							parentMission.dependMissions = new HashSet<String>();
						}
						parentMission.dependMissions.add(mission.getId());
					}
				}
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
