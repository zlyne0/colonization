package promitech.colonization.ai;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitFactory;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.SaveGameParser;

class SettlementWarehouseScoreGoodsTest {

	Game game;
	Player dutch;
	List<GoodsType> goodsTypeToScore;
	
    @BeforeAll
    public static void beforeClass() throws Exception {
        Gdx.files = new LwjglFiles();
    }

    @BeforeEach
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	dutch = game.players.getById("player:1");
    	
    	Specification spec = Specification.instance;
    	goodsTypeToScore = new ArrayList<>();
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.sugar"));
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.tobacco"));
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.cotton"));
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.furs"));
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.ore"));
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.silver"));
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.rum"));
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.cigars"));
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.cloth"));
    	goodsTypeToScore.add(spec.goodsTypes.getById("model.goods.coats"));
    }

    @Test
	public void canDetermineSettlementGoodsScore() throws Exception {
		// given
    	Unit wagonTrain = UnitFactory.create(
			Specification.instance.unitTypes.getById(UnitType.WAGON_TRAIN), 
			dutch, 
			dutch.getEurope()
		);
    	SettlementWarehouseScoreGoods sut = new SettlementWarehouseScoreGoods(dutch, wagonTrain);
    	
		// when
    	ObjectsListScore<Settlement> settlementsScore = sut.score(goodsTypeToScore);
    	
		// then
    	ObjectsListScoreAssert.assertThat(settlementsScore)
    		.hasSize(4)
    		.hasScore(0, 1264, dutch.settlements.getById("colony:6528"))
    		.hasScore(1, 650, dutch.settlements.getById("colony:6554"))
    		.hasScore(2, 378, dutch.settlements.getById("colony:6788"))
    		.hasScore(3, 344, dutch.settlements.getById("colony:6993"))
		;
	}

    @Test
	public void canDetermineSettlementGoodsScoreForGalen() throws Exception {
		// given
    	Unit galleon = UnitFactory.create(
			Specification.instance.unitTypes.getById(UnitType.GALLEON), 
			dutch, 
			dutch.getEurope()
		);
    	SettlementWarehouseScoreGoods sut = new SettlementWarehouseScoreGoods(dutch, galleon);
    	
		// when
    	ObjectsListScore<Settlement> settlementsScore = sut.score(goodsTypeToScore);
    	
		// then
    	ObjectsListScoreAssert.assertThat(settlementsScore)
    		.hasSize(4)
    		.hasScore(0, 2186, dutch.settlements.getById("colony:6528"))
    		.hasScore(1, 1130, dutch.settlements.getById("colony:6554"))
    		.hasScore(2, 378, dutch.settlements.getById("colony:6788"))
    		.hasScore(3, 344, dutch.settlements.getById("colony:6993"))
		;
	}
    
    
}
