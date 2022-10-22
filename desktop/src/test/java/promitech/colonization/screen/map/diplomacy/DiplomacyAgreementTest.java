package promitech.colonization.screen.map.diplomacy;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.xml.sax.SAXException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.PlayerAssert;
import net.sf.freecol.common.model.player.Stance;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.ui.resources.Messages;

public class DiplomacyAgreementTest {

	private Game game;
    private Player spanish;
    private Player dutch;
    private DiplomacyAgreement sut;
	
    @BeforeAll
    public static void beforeClass() throws Exception {
        Gdx.files = new Lwjgl3Files();
        Locale.setDefault(Locale.US);
        Messages.instance().load();
    }

    @BeforeEach
    public void setup() throws IOException, ParserConfigurationException, SAXException {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	spanish = game.players.getById("player:133"); 
    	dutch = game.players.getById("player:1");
    	
    	sut = new DiplomacyAgreement(game, dutch, spanish);
    }
	
    @Test 
	public void canAcceptTradeGoldAgreement() throws Exception {
		// given
    	sut.add(new GoldTradeItem(500, spanish, dutch));
		
		spanish.addGold(10000);
		int initialSpanishGold = spanish.getGold(); 
		int initialDutchGold = dutch.getGold(); 
		
		// when
		sut.acceptTrade();

		// then
		PlayerAssert.assertThat(spanish).hasGold(initialSpanishGold - 500);
		PlayerAssert.assertThat(dutch).hasGold(initialDutchGold + 500);
	}

    @Test 
	public void canAcceptTradeColonyAgreement() throws Exception {
		// given
    	Colony santoDomingo = (Colony)spanish.settlements.getById("colony:6730");
    	MapIdEntities<Unit> santoDomingoUnits = new MapIdEntities<>(santoDomingo.getUnits());
    	
		sut.add(new ColonyTradeItem(santoDomingo, spanish, dutch));
    	
		// when
		sut.acceptTrade();

		// then
		PlayerAssert.assertThat(spanish)
			.hasNotColony(santoDomingo)
			.notContainsUnits(santoDomingoUnits);
		PlayerAssert.assertThat(dutch)
			.hasColony(santoDomingo)
			.containsUnits(santoDomingoUnits);
	}
    
    @Test 
	public void canAcceptInciteDemandAgreement() throws Exception {
		// given
		Player victim = game.players.getById("player:22");
		
		PlayerAssert.assertThat(spanish).hasStance(victim, Stance.PEACE);
		sut.add(new InciteTradeItem(victim, spanish, dutch));
		
		// when
    	sut.acceptTrade();

		// then
		PlayerAssert.assertThat(spanish).hasStance(victim, Stance.WAR);
	}

    @Test 
	public void canAcceptInciteOfferAgreement() throws Exception {
		// given
		Player victim = game.players.getById("player:22");
		
		PlayerAssert.assertThat(dutch).hasStance(victim, Stance.PEACE);
		sut.add(new InciteTradeItem(victim, dutch, spanish));
		
		// when
    	sut.acceptTrade();

		// then
		PlayerAssert.assertThat(dutch).hasStance(victim, Stance.WAR);
	}
    
    @Test 
	public void canAcceptPeace() throws Exception {
		// given
		PlayerAssert.assertThat(dutch).hasStance(spanish, Stance.WAR);
		sut.add(new StanceTradeItem(Stance.PEACE, dutch, spanish));
		
		// when
    	sut.acceptTrade();
		
		// then
		PlayerAssert.assertThat(dutch).hasStance(spanish, Stance.PEACE);
		PlayerAssert.assertThat(spanish).hasStance(dutch, Stance.PEACE);
	}
    
}
