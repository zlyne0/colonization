package promitech.colonization.savegame;

import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;

import promitech.colonization.Direction;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.ui.resources.Messages;

public class Savegame1600BaseClass {

	protected Game game;
	protected Player dutch;
	protected Player spain;
	protected Colony nieuwAmsterdam;
	protected Colony fortOranje;
	protected Colony fortNassau;
	protected Colony fortMaurits;

	@BeforeAll
    public static void beforeClass() throws Exception {
        Gdx.files = new LwjglFiles();
		Locale.setDefault(Locale.US);
		Messages.instance().load();
	}

    @BeforeEach
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	dutch = game.players.getById("player:1");
        spain = game.players.getById("player:133");
        
        nieuwAmsterdam = game.map.getTile(24, 78).getSettlement().asColony();
		fortOranje = game.map.getTile(25, 75).getSettlement().asColony();
		fortNassau = game.map.getTile(20, 79).getSettlement().asColony();
		fortMaurits = game.map.getTile(21, 72).getSettlement().asColony();

	}

	protected UnitType unitType(String unitTypeId) {
		return Specification.instance.unitTypes.getById(unitTypeId);
	}

	protected GoodsType goodsType(String goodsTypeId) {
		return Specification.instance.goodsTypes.getById(goodsTypeId);
	}

	protected Tile tileFrom(Settlement settlement, Direction direction) {
		return game.map.getTile(settlement.tile, direction);
	}

}
