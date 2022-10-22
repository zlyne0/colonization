package net.sf.freecol.common.model.ai.missions.goodsToSell;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitFactory;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.function.Predicate;

import promitech.colonization.ai.score.ObjectScoreList;
import promitech.colonization.ai.score.ScoreableObjectsListAssert;
import promitech.colonization.savegame.SaveGameParser;

import static org.assertj.core.api.Assertions.assertThat;

class SettlementWarehouseScoreGoodsTest {

	Game game;
	Player dutch;
	MapIdEntities<GoodsType> goodsTypeToScore;
	
    @BeforeAll
    public static void beforeClass() throws Exception {
        Gdx.files = new Lwjgl3Files();
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
		ObjectScoreList<Settlement> settlementsScore = sut.score(wagonTrain);
    	
		// then
    	ScoreableObjectsListAssert.assertThat(settlementsScore)
    		.hasSize(4)
    		.hasScore(0, 1264, eq(dutch.settlements.getById("colony:6528")))
    		.hasScore(1, 650, eq(dutch.settlements.getById("colony:6554")))
    		.hasScore(2, 378, eq(dutch.settlements.getById("colony:6788")))
    		.hasScore(3, 344, eq(dutch.settlements.getById("colony:6993")))
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
    	ObjectScoreList<Settlement> settlementsScore = sut.score(galleon);
    	
		// then
    	ScoreableObjectsListAssert.assertThat(settlementsScore)
    		.hasSize(4)
    		.hasScore(0, 2186, eq(dutch.settlements.getById("colony:6528")))
    		.hasScore(1, 1130, eq(dutch.settlements.getById("colony:6554")))
    		.hasScore(2, 378, eq(dutch.settlements.getById("colony:6788")))
    		.hasScore(3, 344, eq(dutch.settlements.getById("colony:6993")))
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
    	ObjectScoreList<Settlement> settlementsScore = sut.score(galleon);
    	
//		for (ObjectScore<Settlement> x : settlementsScore) {
//			System.out.println(x);
//		}    	
    	
		// then
    	ScoreableObjectsListAssert.assertThat(settlementsScore)
    		.hasSize(4)
    		.hasScore(0, eq(dutch.settlements.getById("colony:6993")))
    		.hasScore(0, eq(dutch.settlements.getById("colony:6554")))
    		.hasScore(0, eq(dutch.settlements.getById("colony:6788")))
    		.hasScore(0, eq(dutch.settlements.getById("colony:6993")))
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
    	ObjectScoreList<Settlement> settlementsScore = sut.score(galleon, galleon.getTile());

		// then

    	ScoreableObjectsListAssert.assertThat(settlementsScore)
			.hasSize(4)
			.hasScore(0, 1858, eq(dutch.settlements.getById("colony:6528")))
			.hasScore(1, 960, eq(dutch.settlements.getById("colony:6554")))
			.hasScore(2, 264, eq(dutch.settlements.getById("colony:6788")))
			.hasScore(3, -68, eq(dutch.settlements.getById("colony:6993")))
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
    	ObjectScoreList<Settlement> settlementsScore = sut.score(galleon, galleon.getTile());

//    	for (ObjectScore<Settlement> x : settlementsScore) {
//    		System.out.println(x);
//    	}
    	
		// then
    	ScoreableObjectsListAssert.assertThat(settlementsScore)
			.hasSize(4)
			.hasScore(0, 1530, eq(dutch.settlements.getById("colony:6528")))
			.hasScore(1, 791, eq(dutch.settlements.getById("colony:6554")))
			.hasScore(2, 321, eq(dutch.settlements.getById("colony:6788")))
			.hasScore(3, 137, eq(dutch.settlements.getById("colony:6993")))
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

	private Predicate<Settlement> eq(Settlement aSettlement) {
		return new Predicate<Settlement>() {
			@Override
			public boolean test(Settlement settlement) {
				return aSettlement.equalsId(settlement);
			}
		};
	}

}
