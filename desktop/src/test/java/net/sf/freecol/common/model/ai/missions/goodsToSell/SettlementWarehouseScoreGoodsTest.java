package net.sf.freecol.common.model.ai.missions.goodsToSell;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitFactory;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.ai.ObjectsListScore;
import promitech.colonization.ai.ObjectsListScoreAssert;
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
    	
    	goodsTypeToScore = Specification.instance.goodsTypeToScoreByPrice;
    }

    @Test
	public void canDetermineSettlementGoodsScore() throws Exception {
		// given
    	Unit wagonTrain = UnitFactory.create(
			Specification.instance.unitTypes.getById(UnitType.WAGON_TRAIN), 
			dutch, 
			dutch.getEurope()
		);
    	SettlementWarehouseScoreGoods sut = new SettlementWarehouseScoreGoods(goodsTypeToScore, dutch);
    	
		// when
    	ObjectsListScore<Settlement> settlementsScore = sut.score(wagonTrain);
    	
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
	public void canDetermineSettlementGoodsScoreForGallen() throws Exception {
		// given
    	Unit galleon = UnitFactory.create(
			Specification.instance.unitTypes.getById(UnitType.GALLEON), 
			dutch, 
			dutch.getEurope()
		);
    	SettlementWarehouseScoreGoods sut = new SettlementWarehouseScoreGoods(goodsTypeToScore, dutch);
    	
		// when
    	ObjectsListScore<Settlement> settlementsScore = sut.score(galleon);
    	
		// then
    	ObjectsListScoreAssert.assertThat(settlementsScore)
    		.hasSize(4)
    		.hasScore(0, 2186, dutch.settlements.getById("colony:6528"))
    		.hasScore(1, 1130, dutch.settlements.getById("colony:6554"))
    		.hasScore(2, 378, dutch.settlements.getById("colony:6788"))
    		.hasScore(3, 344, dutch.settlements.getById("colony:6993"))
		;
	}

    @Test
	public void canDetermineSettlementGoodsScoreForFullCargoGallen() throws Exception {
		// given
    	Unit galleon = UnitFactory.create(
			Specification.instance.unitTypes.getById(UnitType.GALLEON), 
			dutch, 
			dutch.getEurope()
		);
    	galleon.getGoodsContainer().increaseGoodsQuantity(GoodsType.MUSKETS, 600);
    	SettlementWarehouseScoreGoods sut = new SettlementWarehouseScoreGoods(goodsTypeToScore, dutch);
    	
		// when
    	ObjectsListScore<Settlement> settlementsScore = sut.score(galleon);
    	
//		for (ObjectScore<Settlement> x : settlementsScore) {
//			System.out.println(x);
//		}    	
    	
		// then
    	ObjectsListScoreAssert.assertThat(settlementsScore)
    		.hasSize(4)
    		.hasScore(dutch.settlements.getById("colony:6993"), 0)
    		.hasScore(dutch.settlements.getById("colony:6554"), 0)
    		.hasScore(dutch.settlements.getById("colony:6788"), 0)
    		.hasScore(dutch.settlements.getById("colony:6993"), 0)
		;
	}
    
    @Test
	public void canCalculateScoreFromPosition_30_68() throws Exception {
		// given
    	Unit galleon = UnitFactory.create(
			Specification.instance.unitTypes.getById(UnitType.GALLEON), 
			dutch, 
			game.map.getSafeTile(30,68)
		);
    	PathFinder pathFinder = new PathFinder();
    	SettlementWarehouseScoreGoods sut = new SettlementWarehouseScoreGoods(goodsTypeToScore, dutch, game.map, pathFinder);
    	
		// when
    	ObjectsListScore<Settlement> settlementsScore = sut.score(galleon, galleon.getTile());

		// then

    	ObjectsListScoreAssert.assertThat(settlementsScore)
			.hasSize(4)
			.hasScore(0, 1858, dutch.settlements.getById("colony:6528"))
			.hasScore(1, 960, dutch.settlements.getById("colony:6554"))
			.hasScore(2, 264, dutch.settlements.getById("colony:6788"))
			.hasScore(3, -68, dutch.settlements.getById("colony:6993"))
		;
	}
    
    @Test
	public void canCalculateScoreFromPosition_15_89() throws Exception {
		// given
    	Unit galleon = UnitFactory.create(
			Specification.instance.unitTypes.getById(UnitType.GALLEON), 
			dutch, 
			game.map.getSafeTile(15, 89)
		);
    	PathFinder pathFinder = new PathFinder();
    	SettlementWarehouseScoreGoods sut = new SettlementWarehouseScoreGoods(goodsTypeToScore, dutch, game.map, pathFinder);
    	
		// when
    	ObjectsListScore<Settlement> settlementsScore = sut.score(galleon, galleon.getTile());

//    	for (ObjectScore<Settlement> x : settlementsScore) {
//    		System.out.println(x);
//    	}
    	
		// then
    	ObjectsListScoreAssert.assertThat(settlementsScore)
			.hasSize(4)
			.hasScore(0, 1530, dutch.settlements.getById("colony:6528"))
			.hasScore(1, 791, dutch.settlements.getById("colony:6554"))
			.hasScore(2, 321, dutch.settlements.getById("colony:6788"))
			.hasScore(3, 137, dutch.settlements.getById("colony:6993"))
		;
	}

    @ParameterizedTest
    @CsvSource({
    	"1000, 0, 1000", 
    	"1000, 1, 850", 
    	"1000, 2, 700", 
    	"1000, 3, 550", 
    	"1000, 4, 400", 
    	"1000, 5, 250", 
    	"1000, 6, 100", 
    	"1000, 7, -50", 
    	"1000, 8, -200", 
    	"1000, 9, -350", 
    	"1000, 99, -13850", 
    	"1000, 0x7fffffff, -14000", 
	})
	public void canCalculateScoreDependsTruns(int score, int turns, int expectedScore) throws Exception {
		// given
    	SettlementWarehouseScoreGoods sut = new SettlementWarehouseScoreGoods(goodsTypeToScore, dutch);

		// when
    	int settlementTurnsScore = sut.settlementTurnsScore(score, turns);
    	
		// then
    	assertThat(settlementTurnsScore).isEqualTo(expectedScore);
	}
    
}
