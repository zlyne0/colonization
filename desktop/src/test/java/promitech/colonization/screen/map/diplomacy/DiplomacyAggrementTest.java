package promitech.colonization.screen.map.diplomacy;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.PlayerAssert;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.ui.resources.Messages;

public class DiplomacyAggrementTest {

	private Game game;
    private Player spanish;
    private Player dutch;
    private DiplomacyAggrement sut;
	
    @BeforeClass
    public static void beforeClass() throws Exception {
        Gdx.files = new LwjglFiles();
        Locale.setDefault(Locale.US);
        Messages.instance().load();
    }

    @Before
    public void setup() throws IOException, ParserConfigurationException, SAXException {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	spanish = game.players.getById("player:133"); 
    	dutch = game.players.getById("player:1");
    	
    	sut = new DiplomacyAggrement(game, dutch, spanish);
    }
	
    @Test 
	public void canAcceptTradeGoldAggrement() throws Exception {
		// given
		List<TradeItem> offers = new ArrayList<>();
		List<TradeItem> demands = new ArrayList<>();
		demands.add(new GoldTradeItem(500, TradeType.Demand));
		
		spanish.addGold(10000);
		int initialSpanishGold = spanish.getGold(); 
		int initialDutchGold = dutch.getGold(); 
		
		// when
		sut.acceptTrade(offers, demands);

		// then
		PlayerAssert.assertThat(spanish).hasGold(initialSpanishGold - 500);
		PlayerAssert.assertThat(dutch).hasGold(initialDutchGold + 500);
	}

    @Test 
	public void canAcceptTradeColonyAggrement() throws Exception {
		// given
    	Colony santoDomingo = (Colony)spanish.settlements.getById("colony:6730");

    	MapIdEntities<Unit> santoDomingoUnits = new MapIdEntities<>(santoDomingo.getUnits());
    	
		List<TradeItem> offers = new ArrayList<>();
		List<TradeItem> demands = new ArrayList<>();
		demands.add(new ColonyTradeItem(TradeType.Demand, santoDomingo));
    	
		// when
		sut.acceptTrade(offers, demands);

		// then
		PlayerAssert.assertThat(spanish)
			.hasNotColony(santoDomingo)
			.notContainsUnits(santoDomingoUnits);
		PlayerAssert.assertThat(dutch)
			.hasColony(santoDomingo)
			.containsUnits(santoDomingoUnits);
	}
    
}
