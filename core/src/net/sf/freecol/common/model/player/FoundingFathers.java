package net.sf.freecol.common.model.player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Turn;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitFactory;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.FoundingFather.FoundingFatherType;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.NationType;
import net.sf.freecol.common.model.specification.RandomChoice;
import net.sf.freecol.common.util.MapList;
import promitech.colonization.Randomizer;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class FoundingFathers {

    private final MapIdEntities<FoundingFather> foundingFathers = MapIdEntities.linkedMapIdEntities();
    private FoundingFather currentFoundingFather;
    private int liberty = 0;
    private Player player;
    
    private FoundingFathers() {
    }
    
    FoundingFathers(Player player) {
    	this.player = player;
    }
    
    public MapList<FoundingFatherType, FoundingFather> foundingFathersByType() {
    	MapList<FoundingFatherType, FoundingFather> ffs = new MapList<FoundingFather.FoundingFatherType, FoundingFather>();
    	for (FoundingFather ff : foundingFathers.entities()) {
    		ffs.add(ff.getType(), ff);
    	}
    	return ffs;
    }
    
	public Map<FoundingFatherType, FoundingFather> generateRandomFoundingFathers(Turn turn) {
		int ages = turn.getAges();
		
		MapList<FoundingFatherType, RandomChoice<FoundingFather>> mapList = new MapList<FoundingFatherType, RandomChoice<FoundingFather>>();
		
		for (FoundingFather foundingFather : Specification.instance.foundingFathers.entities()) {
			if (foundingFathers.containsId(foundingFather)) {
				continue;
			}
			if (!foundingFather.isAvailableTo(player))  {
				continue;
			}

			mapList.add(
				foundingFather.getType(), 
				new RandomChoice<FoundingFather>(foundingFather, foundingFather.weightByAges(ages))
			);
		}
		
		Map<FoundingFatherType, FoundingFather> ff = new LinkedHashMap<FoundingFatherType, FoundingFather>();
		for (FoundingFatherType fft : FoundingFatherType.values()) {
			if (mapList.isNotEmpty(fft)) {
				RandomChoice<FoundingFather> one = Randomizer.instance().randomOne(mapList.get(fft));
				ff.put(fft, one.probabilityObject());
			}
		}
		return ff;
	}
    
	public void fullFillLibertyForFoundingFather() {
		this.liberty = remainingFoundingFatherCost();
	}
	
	private int remainingFoundingFatherCost() {
		int base = Specification.options.getIntValue(GameOptions.FOUNDING_FATHER_FACTOR);
		int count = foundingFathers.size();
		return (count == 0) ? base : 2 * (count + 1) * base + 1;
	}

	private FoundingFather checkAddNewFoundingFathers() {
		int fatherCost = remainingFoundingFatherCost();
		int remainingCost = fatherCost - liberty;
		
		System.out.println("FoundingFathers[" + player.getId() + "].check"  
			+ " liberty/fatherCost " + liberty + "/" + fatherCost
			+ ", currentFoundingFather: " + currentFoundingFather);
		
		if (remainingCost <= 0) {
			boolean overflow = Specification.options.getBoolean(GameOptions.SAVE_PRODUCTION_OVERFLOW);
			if (overflow) {
				liberty = -remainingCost;
			} else {
				liberty = 0;
			}
			FoundingFather newFoundingFather = currentFoundingFather;
			currentFoundingFather = null;
			return newFoundingFather;
		}
		return null;
	}

	protected void modifyLiberty(int libertyAmount) {
		this.liberty = Math.max(0, this.liberty + libertyAmount);
	}

	protected void add(Game game, FoundingFather father) {
		System.out.println("FoundingFathers[" + player.getId() + "].add " + father.getId());
		foundingFathers.add(father);
		player.getFeatures().addFeatures(father);
		
		handleEvents(game, father);
		
		player.recalculateBellBonus();
		
		for (Settlement settlement : player.settlements.entities()) {
			if (settlement.isColony()) {
				Colony colony = settlement.asColony();
				colony.updateColonyFeatures();
				colony.updateModelOnWorkerAllocationOrGoodsTransfer();
			}
		}
	}

	private void handleEvents(Game game, FoundingFather father) {
		// handle founding fathers events
		for (FoundingFatherEvent ffEvent : father.events.entities()) {
			if (ffEvent.equalsId("model.event.boycottsLifted")) {
				player.market().repayAllArrears();
			}
			
			if (ffEvent.equalsId("model.event.seeAllColonies")) {
				for (Player p : game.players.entities()) {
					if (p.equalsId(this.player) || p.isNotLiveEuropeanPlayer()) {
						continue;
					}
					
					for (Settlement settlement : p.settlements.entities()) {
						player.revealMapSeeColony(game.map, settlement.asColony(), game.getTurn());
					}
				}
			}
			
			if (ffEvent.equalsId("model.event.freeBuilding")) {
				BuildingType buildingType = Specification.instance.buildingTypes.getById(ffEvent.getValue());
				for (Settlement settlement : player.settlements.entities()) {
					Colony colony = (Colony) settlement;
					colony.ifPossibleAddFreeBuilding(buildingType);
				}
			}
			
			if (ffEvent.equalsId("model.event.resetNativeAlarm")) {
				for (Player p : game.players.entities()) {
					if (p.isLiveIndianPlayer() && player.hasContacted(p)) {
						p.resetTension(player);
						for (Settlement indianSettlements : p.settlements.entities()) {
							indianSettlements.asIndianSettlement().setTension(player, Tension.TENSION_MIN);
						}
						p.changeStance(player, Stance.PEACE);
					}
				}
			}
			
			if (ffEvent.equalsId("model.event.resetBannedMissions")) {
				for (Player p : game.players.entities()) {
					if (p.isLiveIndianPlayer() && player.hasContacted(p)) {
						p.removeMissionBan(player);
					}
				}
			}
			
			if (ffEvent.equalsId("model.event.newRecruits")) {
				if (player.isLiveEuropeanPlayer()) {
					player.getEurope().replaceNotRecruitableUnits();
				}
			}
		}
		
		if (player.getEurope() != null ) {
			for (String unitTypeId : father.getUnitTypes()) {
				UnitFactory.create(unitTypeId, player, player.getEurope());
			}
		}
		
		if (father.getUpgrades() != null) {
			for (Entry<String, String> upgradeEntry : father.getUpgrades().entrySet()) {
				String fromUnitTypeId = upgradeEntry.getKey();
				UnitType toUnitType = Specification.instance.unitTypes.getById(upgradeEntry.getValue());
				for (Unit unit : player.units.entities()) {
					if (unit.unitType.equalsId(fromUnitTypeId)) {
						unit.changeUnitType(toUnitType);
					}
				}
			}
		}
	}
	
	private boolean canRecruitFoundingFather() {
		if (player.isRebel() || player.isIndependent()) {
			if (!Specification.options.getBoolean(GameOptions.CONTINUE_FOUNDING_FATHER_RECRUITMENT)) {
				return false;
			}
		} else {
			if (!player.isColonial()) {
				return false;
			}
		}
		
		return player.canHaveFoundingFathers() 
			&& currentFoundingFather == null 
			&& player.settlements.isNotEmpty()
			&& foundingFathers.size() < Specification.instance.foundingFathers.size();
	}

	public boolean containsId(String foundingFatherId) {
		return foundingFathers.containsId(foundingFatherId);
	}

	public boolean containsId(Identifiable identifiable) {
		return foundingFathers.containsId(identifiable);
	}
	
	public Collection<FoundingFather> entities() {
		return foundingFathers.entities();
	}

	public void removeId(String foundingFatherId) {
		foundingFathers.removeId(foundingFatherId);
	}

	public int size() {
		return foundingFathers.size();
	}

	public void setCurrentFoundingFather(FoundingFather currentFoundingFather) {
		this.currentFoundingFather = currentFoundingFather;
	}

	public FoundingFather getCurrentFoundingFather() {
		return currentFoundingFather;
	}

	protected void setPlayer(Player player) {
		this.player = player;
	}
	
	public String progressStr() {
		int bellsPerTurn = 0;
		for (Settlement settlement : player.settlements.entities()) {
			bellsPerTurn += settlement.asColony().productionSummary().getQuantity(GoodsType.BELLS);
		}
		int fatherCost = remainingFoundingFatherCost();

		if (bellsPerTurn > 0) {
			int turns = (fatherCost - liberty) / bellsPerTurn;
			return "" + liberty + "+" + bellsPerTurn +" / " + fatherCost + " (Turns: " + turns + ")";
		} else {
			return "" + liberty + "+" + bellsPerTurn +" / " + fatherCost + " (Turns: never)";
		}
	}
	
	public void checkFoundingFathers(Game game) {
		if (canRecruitFoundingFather()) {
			System.out.println("FoundingFathers[" + player.getId() + "].generate");
			if (player.isAi()) {
				chooseFoundingFatherForAI(game.getTurn());
			} else {
				player.eventsNotifications.addMessageNotification(new RecruitFoundingFatherNotification());
			}
		} else {
			FoundingFather foundingFather = checkAddNewFoundingFathers();
			if (foundingFather != null) {
				System.out.println("FoundingFathers[" + player.getId() + "].add " + foundingFather);
				
				if (player.isHuman()) {
					StringTemplate st = StringTemplate.template("model.player.foundingFatherJoinedCongress")
						.addKey("%foundingFather%", Messages.nameKey(foundingFather))
						.addKey("%description%", Messages.descriptionKey(foundingFather));
					player.eventsNotifications.addMessageNotification(st);
				}
				add(game, foundingFather);
			}
		}
	}
	
	private void chooseFoundingFatherForAI(Turn turn) {
		Map<FoundingFatherType, FoundingFather> ffs = generateRandomFoundingFathers(turn);
		
		// for ai always PETER_STUYVESANT
		FoundingFather ff = ffs.get(FoundingFatherType.TRADE);
		if (ff != null && ff.equalsId(FoundingFather.PETER_STUYVESANT)) {
			setCurrentFoundingFather(ff);
			System.out.println("FoundingFathers[" + player.getId() + "].aiChoose " + ff);
			return;
		}
		int ages = turn.getAges();
		
		List<RandomChoice<FoundingFather>> ffToChoose = new ArrayList<RandomChoice<FoundingFather>>(ffs.size());
		for (Entry<FoundingFatherType, FoundingFather> entry : ffs.entrySet()) {
			float weight = entry.getValue().weightByAges(ages) * chooseFoundFatherWeight(player, entry.getKey());
			
			if (entry.getValue().equalsId("model.foundingFather.jacobFugger") && player.market().arrearsCount() <= 4) {
				continue;
			}
			ffToChoose.add(new RandomChoice<FoundingFather>(entry.getValue(), (int)weight));
		}

		if (!ffToChoose.isEmpty()) {
            RandomChoice<FoundingFather> one = Randomizer.instance().randomOne(ffToChoose);
            currentFoundingFather = one.probabilityObject();
            System.out.println("FoundingFathers[" + player.getId() + "].aiChoose " + one.probabilityObject());
        }
	}
	
	private float chooseFoundFatherWeight(Player player, FoundingFatherType fft) {
		if (player.nationType().equalsId(NationType.TRADE)) {
			if (fft == FoundingFatherType.TRADE) {
				return 2.0f;
			}
			if (fft == FoundingFatherType.EXPLORATION) {
				return 1.7f;
			}
		}
		if (player.nationType().equalsId(NationType.CONQUEST)) {
			if (fft == FoundingFatherType.MILITARY) {
				return 2.0f;
			}
			if (fft == FoundingFatherType.RELIGIOUS) {
				return 1.7f;
			}
		}
		if (player.nationType().equalsId(NationType.COOPERATION)) {
			if (fft == FoundingFatherType.RELIGIOUS) {
				return 2.0f;
			}
			if (fft == FoundingFatherType.TRADE) {
				return 1.7f;
			}
		}
		if (player.nationType().equalsId(NationType.IMMIGRATION)) {
			if (fft == FoundingFatherType.POLITICAL) {
				return 2.0f;
			}
			if (fft == FoundingFatherType.MILITARY) {
				return 1.7f;
			}
		}
		return 1.0f;
	}
	
	public static class Xml extends XmlNodeParser<FoundingFathers> {

        private static final String ATTR_CURRENT_FATHER = "currentFather";
		private static final String ELEMENT_FOUNDING_FATHER = "foundingFather";
		private static final String ATTR_LIBERTY = "liberty";
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			FoundingFathers ffs = new FoundingFathers();
			ffs.liberty = attr.getIntAttribute(ATTR_LIBERTY, 0);
			ffs.currentFoundingFather = attr.getEntity(
	      		ATTR_CURRENT_FATHER, 
	      		Specification.instance.foundingFathers
			);
			nodeObject = ffs;
		}

		@Override
		public void startWriteAttr(FoundingFathers ffs, XmlNodeAttributesWriter attr) throws IOException {
			attr.set(ATTR_CURRENT_FATHER, ffs.currentFoundingFather);
			attr.set(ATTR_LIBERTY, ffs.liberty, 0);
			
			for (FoundingFather ff : ffs.foundingFathers.entities()) {
				attr.xml.element(ELEMENT_FOUNDING_FATHER);
				attr.setId(ff);
				attr.xml.pop();
			}
		}

		@Override
		public void startReadChildren(XmlNodeAttributes attr) {
			if (attr.isQNameEquals(ELEMENT_FOUNDING_FATHER)) {
				nodeObject.foundingFathers.add(attr.getEntityId(Specification.instance.foundingFathers));
			}
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "foundingFathers";
		}
	}
}
