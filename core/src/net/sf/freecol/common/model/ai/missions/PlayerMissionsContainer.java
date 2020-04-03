package net.sf.freecol.common.model.ai.missions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.MapIdEntitiesReadOnly;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class PlayerMissionsContainer extends ObjectWithId {

	private final MapIdEntities<AbstractMission> missions = MapIdEntities.linkedMapIdEntities();
	private final Player player;
	private final UnitMissionsMapping unitMissionsMapping = new UnitMissionsMapping();
	
	public PlayerMissionsContainer(Player player) {
		super(player.getId());
		this.player = player;
	}

	public void addMission(AbstractMission m) {
		missions.add(m);
	}

	public void clearDoneMissions() {
		List<AbstractMission> l = new ArrayList<AbstractMission>(missions.entities());
		for (AbstractMission am : l) {
			if (am.isDone() && !am.hasDependMissions()) {
				missions.removeId(am);
				am.unblockUnits(unitMissionsMapping);
			}
		}
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
	
	public void blockUnitsForMission(AbstractMission mission) {
		mission.blockUnits(unitMissionsMapping);
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

	public boolean isUnitBlockedForMission(Unit unit) {
		return unitMissionsMapping.isUnitInMission(unit);
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
        
        public static IndianSettlement getPlayerIndianSettlement(String settlementId) {
        	Settlement settlement = player.settlements.getByIdOrNull(settlementId);
        	if (settlement == null) {
        		return null;
        	}
        	return settlement.asIndianSettlement();
        }
        
        public Xml() {
            addNodeForMapIdEntities("missions", WanderMission.class);
            addNodeForMapIdEntities("missions", TransportUnitMission.class);
            addNodeForMapIdEntities("missions", RellocationMission.class);
            addNodeForMapIdEntities("missions", FoundColonyMission.class);
            addNodeForMapIdEntities("missions", ExplorerMission.class);
            addNodeForMapIdEntities("missions", IndianBringGiftMission.class);
            addNodeForMapIdEntities("missions", DemandTributeMission.class);
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
