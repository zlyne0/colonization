package net.sf.freecol.common.model;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.ui.resources.Messages;

public class ColonyProductionTest {

    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
        Messages.instance().load();
    }

    @Test
	public void calcuateSonsOfLiberty() throws Exception {
    	// given
        SaveGameParser saveGameParser = new SaveGameParser("maps/savegame_1600_for_jtests.xml");
        Game game = saveGameParser.parse();
        Player player = game.players.getById("player:1");
        Colony colony = (Colony)player.settlements.getById("colony:6528");
        // when
        colony.calculateSonsOfLiberty();
        // then
        
	}
    
    @Test
    public void testName() throws Exception {
        // given
        SaveGameParser saveGameParser = new SaveGameParser("maps/savegame_1600_for_jtests.xml");
        Game game = saveGameParser.parse();

        Player player = game.players.getById("player:1");
        
        Colony colony = (Colony)player.settlements.getById("colony:6528");
        System.out.println("warehouse: " + colony.goodsContainer.cloneGoods());
        
        int allFood = colony.goodsContainer.goodsAmount("model.goods.food");
        colony.goodsContainer.increaseGoodsQuantity("model.goods.food", -allFood);
        //colony.goodsContainer.increaseGoodsQuantity("model.goods.food", 20);
        
        int allFurs = colony.goodsContainer.goodsAmount("model.goods.furs");
        colony.goodsContainer.increaseGoodsQuantity("model.goods.furs", -allFurs);
        //colony.goodsContainer.increaseGoodsQuantity("model.goods.furs", 15);
        
        int allCoast = colony.goodsContainer.goodsAmount("model.goods.coats");
        colony.goodsContainer.increaseGoodsQuantity("model.goods.coats", -allCoast);
        
        System.out.println("### warehouse after modyfication ###");
        System.out.println("" + colony.goodsContainer.cloneGoods());
        System.out.println("### building:6545 ###");
        Building furTrading = colony.buildings.getByIdOrNull("building:6545");
        ProductionConsumption productionSummary = colony.productionSummary(furTrading);
        System.out.println("productionSummary = " + productionSummary);
        colony.notificationsAboutLackOfResources();

        // TODO: dokonczenie sprawdania warunkow, sprawdzanie notification events
        
        // when

        // then

    }
}
