package promitech.colonization.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Turn;
import net.sf.freecol.common.model.player.FoundingFather;
import net.sf.freecol.common.model.player.FoundingFather.FoundingFatherType;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.RecruitFoundingFatherNotification;
import net.sf.freecol.common.model.specification.NationType;
import net.sf.freecol.common.model.specification.RandomChoice;
import promitech.colonization.Randomizer;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class FoundingFatherService {

	public void checkFoundingFathers(Game game, Player player) {
		if (player.foundingFathers.canRecruitFoundingFather()) {
			System.out.println("FoundingFathers[" + player.getId() + "].generate");
			if (player.isAi()) {
				chooseFoundingFatherForAI(player, game.getTurn());
			} else {
				player.eventsNotifications.addMessageNotification(new RecruitFoundingFatherNotification());
			}
		} else {
			FoundingFather foundingFathers = player.foundingFathers.checkAddNewFoundingFathers();
			if (foundingFathers != null) {
				System.out.println("FoundingFathers[" + player.getId() + "].add " + foundingFathers);
				
				if (player.isHuman()) {
					StringTemplate st = StringTemplate.template("model.player.foundingFatherJoinedCongress")
						.addKey("%foundingFather%", Messages.nameKey(foundingFathers))
						.addKey("%description%", Messages.descriptionKey(foundingFathers));
					player.eventsNotifications.addMessageNotification(st);
				}
				handleNewFoundingFather(game, player, foundingFathers);
			}
		}
	}
	
	private void chooseFoundingFatherForAI(Player player, Turn turn) {
		Map<FoundingFatherType, FoundingFather> ffs = player.foundingFathers.generateRandomFoundingFathers(turn);
		
		// for ai always PETER_STUYVESANT
		FoundingFather ff = ffs.get(FoundingFatherType.TRADE);
		if (ff != null && ff.equalsId(FoundingFather.PETER_STUYVESANT)) {
			player.foundingFathers.setCurrentFoundingFather(ff);
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
		
		RandomChoice<FoundingFather> one = Randomizer.instance().randomOne(ffToChoose);
		player.foundingFathers.setCurrentFoundingFather(ff);
		
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
	
	private void handleNewFoundingFather(Game game, Player player, FoundingFather foundingFathers) {
		player.addFoundingFathers(game, foundingFathers);
	}
	
}
