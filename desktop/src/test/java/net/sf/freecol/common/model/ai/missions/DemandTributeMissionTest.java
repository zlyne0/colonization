package net.sf.freecol.common.model.ai.missions;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.indian.DemandTributeMission;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Tension;
import net.sf.freecol.common.model.specification.Goods;

import static net.sf.freecol.common.model.specification.GoodsAssert.*;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.SaveGameParser;

class DemandTributeMissionTest {

	Game game;
	Player inca;
	
    @BeforeAll
    public static void beforeClass() throws Exception {
        Gdx.files = new Lwjgl3Files();
    }

    @BeforeEach
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	inca = game.players.getById("player:154");
    }

    @Test
	public void canSelectGoodsForContentTension() throws Exception {
		// given
    	IndianSettlement indianSettlement = game.map.getSafeTile(19, 78).getSettlement().asIndianSettlement();
    	Unit unitToDemandTribute = indianSettlement.getUnits().getById("unit:6351");
    	Colony colony = game.map.getSafeTile(20, 79).getSettlement().asColony();
    	colony.getGoodsContainer().increaseGoodsQuantity(GoodsType.FOOD, 100);
    	
    	indianSettlement.getOwner().getTension(colony.getOwner()).setValue(Tension.Level.CONTENT.getLimit());
    	DemandTributeMission mission = new DemandTributeMission(indianSettlement, unitToDemandTribute, colony);
    	
		// when
    	Goods demandGoods = mission.selectGoods();

		// then
    	assertThat(demandGoods).isEquals(GoodsType.FOOD, 90);
	}
    
    @Test
	public void canSelectGoodsForDispleasedTension() throws Exception {
		// given
    	IndianSettlement indianSettlement = game.map.getSafeTile(19, 78).getSettlement().asIndianSettlement();
    	Unit unitToDemandTribute = indianSettlement.getUnits().getById("unit:6351");
    	Colony colony = game.map.getSafeTile(20, 79).getSettlement().asColony();
    	
    	indianSettlement.getOwner().getTension(colony.getOwner()).setValue(Tension.Level.DISPLEASED.getLimit());
    	DemandTributeMission mission = new DemandTributeMission(indianSettlement, unitToDemandTribute, colony);
    	
		// when
    	Goods demandGoods = mission.selectGoods();

		// then
    	assertThat(demandGoods).isEquals("model.goods.ore", 40);
	}

    @Test
	public void canSelectGoodsForAngryTension() throws Exception {
		// given
    	IndianSettlement indianSettlement = game.map.getSafeTile(19, 78).getSettlement().asIndianSettlement();
    	Unit unitToDemandTribute = indianSettlement.getUnits().getById("unit:6351");
    	Colony colony = game.map.getSafeTile(20, 79).getSettlement().asColony();
    	
    	indianSettlement.getOwner().getTension(colony.getOwner()).setValue(Tension.Level.ANGRY.getLimit());
    	DemandTributeMission mission = new DemandTributeMission(indianSettlement, unitToDemandTribute, colony);
    	
		// when
    	Goods demandGoods = mission.selectGoods();

		// then
    	assertThat(demandGoods).isEquals(GoodsType.HORSES, 32);
	}
    
}
