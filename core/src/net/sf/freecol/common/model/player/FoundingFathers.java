package net.sf.freecol.common.model.player;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Turn;
import net.sf.freecol.common.model.player.FoundingFather.FoundingFatherType;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.RandomChoice;
import net.sf.freecol.common.util.MapList;
import promitech.colonization.Randomizer;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class FoundingFathers {

    private final MapIdEntities<FoundingFather> foundingFathers = new MapIdEntities<FoundingFather>();
    private FoundingFather currentFoundingFather;
    private int liberty = 0;
    private Player player;
    
    private FoundingFathers() {
    }
    
    public FoundingFathers(Player player) {
    	this.player = player;
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
    
	public int remainingFoundingFatherCost() {
		int base = Specification.options.getIntValue(GameOptions.FOUNDING_FATHER_FACTOR);
		int count = foundingFathers.size();
		return (count == 0) ? base : 2 * (count + 1) * base + 1;
	}

	public FoundingFather checkAddNewFoundingFathers() {
		FoundingFather newFoundingFather = null;
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
			newFoundingFather = currentFoundingFather;
			currentFoundingFather = null;
		}
		return newFoundingFather;
	}

	public void modifyLiberty(int libertyAmount) {
		this.liberty = Math.max(0, this.liberty + libertyAmount);
	}

	protected void add(Game game, FoundingFather father) {
		System.out.println("FoundingFathers[" + player.getId() + "].add " + father.getId());
		foundingFathers.add(father);
		
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
						player.revealMapSeeColony(game.map, settlement.getColony());
					}
				}
			}
		}
	}
	
	public boolean canRecruitFoundingFather() {
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

	public void setPlayer(Player player) {
		this.player = player;
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
