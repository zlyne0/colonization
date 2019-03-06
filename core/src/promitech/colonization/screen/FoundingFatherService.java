package promitech.colonization.screen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Turn;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.RecruitFoundingFatherNotification;
import net.sf.freecol.common.model.specification.FoundingFather;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.NationType;
import net.sf.freecol.common.model.specification.FoundingFather.FoundingFatherType;
import net.sf.freecol.common.util.MapList;
import net.sf.freecol.common.model.specification.RandomChoice;
import promitech.colonization.Randomizer;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class FoundingFatherService {

	public Map<FoundingFatherType, FoundingFather> generateRandomFoundingFathers(Player player, Turn turn) {
		int ages = turn.getAges();
		
		MapList<FoundingFatherType, RandomChoice<FoundingFather>> mapList = new MapList<FoundingFatherType, RandomChoice<FoundingFather>>();
		
		for (FoundingFather foundingFather : Specification.instance.foundingFathers.entities()) {
			if (player.foundingFathers.containsId(foundingFather)) {
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

	public void checkFoundingFathers(Game game, Player player) {
		if (canRecruitFoundingFather(player)) {
			System.out.println("FoundingFathers[" + player.getId() + "].generate");
			if (player.isAi()) {
				chooseFoundingFatherForAI(player, game.getTurn());
			} else {
				player.eventsNotifications.addMessageNotification(new RecruitFoundingFatherNotification());
			}
		} else {
			FoundingFather foundingFathers = player.checkAddNewFoundingFathers();
			if (foundingFathers != null) {
				System.out.println("FoundingFathers[" + player.getId() + "].add " + foundingFathers);
				
				if (player.isHuman()) {
					StringTemplate st = StringTemplate.template("model.player.foundingFatherJoinedCongress")
						.addKey("%foundingFather%", Messages.nameKey(foundingFathers))
						.addKey("%description%", Messages.descriptionKey(foundingFathers));
					player.eventsNotifications.addMessageNotification(st);
				}
				handleNewFoundingFather(player, foundingFathers);
			}
		}
	}
	
	private void chooseFoundingFatherForAI(Player player, Turn turn) {
		Map<FoundingFatherType, FoundingFather> ffs = generateRandomFoundingFathers(player, turn);
		
		// for ai always PETER_STUYVESANT
		FoundingFather ff = ffs.get(FoundingFatherType.TRADE);
		if (ff != null && ff.equalsId(FoundingFather.PETER_STUYVESANT)) {
			player.setCurrentFoundingFather(ff);
			System.out.println("FoundingFathers[" + player.getId() + "].aiChoose " + ff);
			return;
		}

		int ages = turn.getAges();
		
		List<RandomChoice<FoundingFather>> ffToChoose = new ArrayList<RandomChoice<FoundingFather>>(ffs.size());
		for (Entry<FoundingFatherType, FoundingFather> entry : ffs.entrySet()) {
			float weight = entry.getValue().weightByAges(ages) * chooseFoundFatherWeight(player, entry.getKey());
			ffToChoose.add(new RandomChoice<FoundingFather>(entry.getValue(), (int)weight));
		}
		
		RandomChoice<FoundingFather> one = Randomizer.instance().randomOne(ffToChoose);
		player.setCurrentFoundingFather(ff);
		
		System.out.println("FoundingFathers[" + player.getId() + "].aiChoose " + one.probabilityObject());
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
	
	public void handleNewFoundingFather(Player player, FoundingFather foundingFathers) {
		player.foundingFathers.add(foundingFathers);
	}
	
	private boolean canRecruitFoundingFather(Player player) {
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
			&& player.getCurrentFoundingFather() == null 
			&& player.settlements.isNotEmpty()
			&& player.foundingFathers.size() < Specification.instance.foundingFathers.size();
	}
}
