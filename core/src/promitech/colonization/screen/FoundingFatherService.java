package promitech.colonization.screen;

import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Turn;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.RecruitFoundingFatherNotification;
import net.sf.freecol.common.model.specification.FoundingFather;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.FoundingFather.FoundingFatherType;
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
		generateRandomFoundingFathers(player, turn);
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
