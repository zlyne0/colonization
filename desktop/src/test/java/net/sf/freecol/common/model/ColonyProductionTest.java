package net.sf.freecol.common.model;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.BuildableType;
import promitech.colonization.NewTurnContext;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.ui.resources.Messages;

public class ColonyProductionTest {

    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
        Messages.instance().load();
    }

    Game game;
    Colony colony;
    Player player;
    
    @Before
    public void setup() throws IOException, ParserConfigurationException, SAXException {
        SaveGameParser saveGameParser = new SaveGameParser("maps/savegame_1600_for_jtests.xml");
        game = saveGameParser.parse();
        player = game.players.getById("player:1");
        colony = (Colony)player.settlements.getById("colony:6528");
    }
    
    @Test
	public void calcuateSonsOfLiberty() throws Exception {
    	// given
        // when
        colony.calculateSonsOfLiberty();
        colony.calculateImmigration();        
        // then
        assertEquals(147, colony.liberty);
        assertEquals(12, colony.sonsOfLiberty());
        assertEquals(113, colony.immigration);
	}
    
    @Test
    public void canCalculateProductionForColony() throws Exception {
        // given
        player.foundingFathers.add(Specification.instance.foundingFathers.getById("model.foundingFather.henryHudson"));
        
        // when
        colony.updateColonyFeatures();
        colony.updateProductionBonus();
        ProductionSummary ps = colony.productionSummary();

        // then
        System.out.println("productionSummary = " + ps);
        
        assertEquals(3, ps.getQuantity("model.goods.cotton"));
        assertEquals(-2, ps.getQuantity("model.goods.lumber")); 
        assertEquals(2, ps.getQuantity("model.goods.horses")); 
        assertEquals(-9, ps.getQuantity("model.goods.furs")); 
        assertEquals(12, ps.getQuantity("model.goods.hammers")); 
        assertEquals(1, ps.getQuantity("model.goods.crosses")); 
        assertEquals(5, ps.getQuantity("model.goods.bells")); 
        assertEquals(0, ps.getQuantity("model.goods.fish")); 
        assertEquals(9, ps.getQuantity("model.goods.coats")); 
        assertEquals(2, ps.getQuantity("model.goods.food")); 
        assertEquals(0, ps.getQuantity("model.goods.grain"));
        
        Building furTrading = colony.buildings.getById("building:6545");
        ProductionConsumption furTradingProdCons = colony.productionSummary(furTrading);
        System.out.println("furTradingProdCons = " + furTradingProdCons);
        
        assertEquals(-9, furTradingProdCons.baseConsumption.getQuantity("model.goods.furs"));
        assertEquals(0, furTradingProdCons.baseProduction.getQuantity("model.goods.furs"));
        assertEquals(9, furTradingProdCons.baseProduction.getQuantity("model.goods.coats"));

        assertEquals(-9, furTradingProdCons.realConsumption.getQuantity("model.goods.furs"));
        assertEquals(9, furTradingProdCons.realProduction.getQuantity("model.goods.coats"));
    }

    @Test
    public void canCalculateProductionForColonyForUpgradedFurTraderHouse() throws Exception {
        // given
        player.foundingFathers.add(Specification.instance.foundingFathers.getById("model.foundingFather.henryHudson"));
        
        Building ft = colony.buildings.getById("building:6545");
        ft.upgrade(Specification.instance.buildingTypes.getById("model.building.furTradingPost"));
        
        // when
        colony.updateColonyFeatures();
        colony.updateProductionBonus();
        ProductionSummary ps = colony.productionSummary();

        // then
        System.out.println("productionSummary = " + ps);
        
        assertEquals(3, ps.getQuantity("model.goods.cotton"));
        assertEquals(-2, ps.getQuantity("model.goods.lumber")); 
        assertEquals(2, ps.getQuantity("model.goods.horses")); 
        assertEquals(-18, ps.getQuantity("model.goods.furs")); 
        assertEquals(12, ps.getQuantity("model.goods.hammers")); 
        assertEquals(1, ps.getQuantity("model.goods.crosses")); 
        assertEquals(5, ps.getQuantity("model.goods.bells")); 
        assertEquals(0, ps.getQuantity("model.goods.fish")); 
        assertEquals(18, ps.getQuantity("model.goods.coats")); 
        assertEquals(2, ps.getQuantity("model.goods.food")); 
        assertEquals(0, ps.getQuantity("model.goods.grain"));
        
        Building furTrading = colony.buildings.getById("building:6545");
        ProductionConsumption furTradingProdCons = colony.productionSummary(furTrading);
        System.out.println("furTradingProdCons = " + furTradingProdCons);
        
        assertEquals(-18, furTradingProdCons.baseConsumption.getQuantity("model.goods.furs"));
        
        assertEquals(0, furTradingProdCons.baseProduction.getQuantity("model.goods.furs"));
        assertEquals(18, furTradingProdCons.baseProduction.getQuantity("model.goods.coats"));

        assertEquals(-18, furTradingProdCons.realConsumption.getQuantity("model.goods.furs"));
        assertEquals(18, furTradingProdCons.realProduction.getQuantity("model.goods.coats"));
    }

    @Test
    public void canCalculateProductionForColonyForUpgradedFurFactory() throws Exception {
        // given
        player.foundingFathers.add(Specification.instance.foundingFathers.getById("model.foundingFather.henryHudson"));
        
        Building ft = colony.buildings.getById("building:6545");
        ft.upgrade(Specification.instance.buildingTypes.getById("model.building.furFactory"));
        
        // when
        colony.updateColonyFeatures();
        colony.updateProductionBonus();
        ProductionSummary ps = colony.productionSummary();

        // then
        System.out.println("productionSummary = " + ps);
        
        assertEquals(3, ps.getQuantity("model.goods.cotton"));
        assertEquals(-2, ps.getQuantity("model.goods.lumber")); 
        assertEquals(2, ps.getQuantity("model.goods.horses")); 
        assertEquals(-27, ps.getQuantity("model.goods.furs")); 
        assertEquals(12, ps.getQuantity("model.goods.hammers")); 
        assertEquals(1, ps.getQuantity("model.goods.crosses")); 
        assertEquals(5, ps.getQuantity("model.goods.bells")); 
        assertEquals(0, ps.getQuantity("model.goods.fish")); 
        assertEquals(27, ps.getQuantity("model.goods.coats")); 
        assertEquals(2, ps.getQuantity("model.goods.food")); 
        assertEquals(0, ps.getQuantity("model.goods.grain"));
        
        Building furTrading = colony.buildings.getById("building:6545");
        ProductionConsumption furTradingProdCons = colony.productionSummary(furTrading);
        System.out.println("furTradingProdCons = " + furTradingProdCons);
        
        assertEquals(-27, furTradingProdCons.baseConsumption.getQuantity("model.goods.furs"));
        assertEquals(0, furTradingProdCons.baseProduction.getQuantity("model.goods.furs"));
        assertEquals(27, furTradingProdCons.baseProduction.getQuantity("model.goods.coats"));

        assertEquals(-27, furTradingProdCons.realConsumption.getQuantity("model.goods.furs"));
        assertEquals(27, furTradingProdCons.realProduction.getQuantity("model.goods.coats"));
    }
    
    @Test
    public void fursProductionWithoutHenryHudson() throws Exception {
        // given

        // move statesment from townHall to tile 
        Unit unit = player.units.getById("unit:7076");
        Building townHall = colony.findBuildingByType("model.building.townHall");
        townHall.workers.removeId(unit);
        ColonyTile fursColonyTile = colony.colonyTiles.getById("tile:3352");
        
        colony.addWorkerToTerrain(fursColonyTile, unit);
        
        fursColonyTile.productionInfo.clear();
        List<GoodMaxProductionLocation> potentialTerrainProductions = colony.determinePotentialTerrainProductions(unit);
        for (GoodMaxProductionLocation goodProd : potentialTerrainProductions) {
            System.out.println("potential goodProd " + goodProd);
            if (goodProd.getGoodsType().equalsId("model.goods.furs")) {
                fursColonyTile.productionInfo.addProduction(goodProd.tileTypeInitProduction);
            }
        }
        
        // when
        colony.updateModelOnWorkerAllocationOrGoodsTransfer();
        colony.updateColonyFeatures();
        colony.updateProductionBonus();
        ProductionSummary ps = colony.productionSummary();

        // then
        System.out.println("productionSummary = " + ps);
        
        assertEquals(3, ps.getQuantity("model.goods.cotton"));
        assertEquals(-2, ps.getQuantity("model.goods.lumber")); 
        assertEquals(2, ps.getQuantity("model.goods.horses")); 
        assertEquals(-4, ps.getQuantity("model.goods.furs")); 
        assertEquals(12, ps.getQuantity("model.goods.hammers")); 
        assertEquals(1, ps.getQuantity("model.goods.crosses")); 
        assertEquals(-3, ps.getQuantity("model.goods.bells")); 
        assertEquals(0, ps.getQuantity("model.goods.fish")); 
        assertEquals(9, ps.getQuantity("model.goods.coats")); 
        assertEquals(2, ps.getQuantity("model.goods.food")); 
        assertEquals(0, ps.getQuantity("model.goods.grain"));
        
        Building furTrading = colony.buildings.getById("building:6545");
        ProductionConsumption furTradingProdCons = colony.productionSummary(furTrading);
        System.out.println("furTradingProdCons = " + furTradingProdCons);
        
        assertEquals(-9, furTradingProdCons.baseConsumption.getQuantity("model.goods.furs"));
        assertEquals(0, furTradingProdCons.baseProduction.getQuantity("model.goods.furs"));
        assertEquals(9, furTradingProdCons.baseProduction.getQuantity("model.goods.coats"));

        assertEquals(-9, furTradingProdCons.realConsumption.getQuantity("model.goods.furs"));
        assertEquals(9, furTradingProdCons.realProduction.getQuantity("model.goods.coats"));
    }
    
    @Test
    public void fursProductionWithHenryHudson() throws Exception {
        // given
        player.foundingFathers.add(Specification.instance.foundingFathers.getById("model.foundingFather.henryHudson"));

        // move statesment from townHall to tile 
        Unit unit = player.units.getById("unit:7076");
        Building townHall = colony.findBuildingByType("model.building.townHall");
        townHall.workers.removeId(unit);
        ColonyTile fursColonyTile = colony.colonyTiles.getById("tile:3352");
        
        colony.addWorkerToTerrain(fursColonyTile, unit);
        
        fursColonyTile.productionInfo.clear();
        List<GoodMaxProductionLocation> potentialTerrainProductions = colony.determinePotentialTerrainProductions(unit);
        for (GoodMaxProductionLocation goodProd : potentialTerrainProductions) {
            System.out.println("potential goodProd " + goodProd);
            if (goodProd.getGoodsType().equalsId("model.goods.furs")) {
                fursColonyTile.productionInfo.addProduction(goodProd.tileTypeInitProduction);
            }
        }
        
        // when
        colony.updateModelOnWorkerAllocationOrGoodsTransfer();
        colony.updateColonyFeatures();
        colony.updateProductionBonus();
        ProductionSummary ps = colony.productionSummary();

        // then
        System.out.println("productionSummary = " + ps);
        
        assertEquals(3, ps.getQuantity("model.goods.cotton"));
        assertEquals(-2, ps.getQuantity("model.goods.lumber")); 
        assertEquals(2, ps.getQuantity("model.goods.horses")); 
        assertEquals(-1, ps.getQuantity("model.goods.furs")); 
        assertEquals(12, ps.getQuantity("model.goods.hammers")); 
        assertEquals(1, ps.getQuantity("model.goods.crosses")); 
        assertEquals(-3, ps.getQuantity("model.goods.bells")); 
        assertEquals(0, ps.getQuantity("model.goods.fish")); 
        assertEquals(9, ps.getQuantity("model.goods.coats")); 
        assertEquals(2, ps.getQuantity("model.goods.food")); 
        assertEquals(0, ps.getQuantity("model.goods.grain"));
        
        Building furTrading = colony.buildings.getById("building:6545");
        ProductionConsumption furTradingProdCons = colony.productionSummary(furTrading);
        System.out.println("furTradingProdCons = " + furTradingProdCons);

        assertEquals(-9, furTradingProdCons.baseConsumption.getQuantity("model.goods.furs"));
        
        assertEquals(0, furTradingProdCons.baseProduction.getQuantity("model.goods.furs"));
        assertEquals(9, furTradingProdCons.baseProduction.getQuantity("model.goods.coats"));

        assertEquals(-9, furTradingProdCons.realConsumption.getQuantity("model.goods.furs"));
        assertEquals(9, furTradingProdCons.realProduction.getQuantity("model.goods.coats"));
    }
    
    @Test
    public void canBuildDocks() throws Exception {
        // given
        Colony fortOrange = (Colony)player.settlements.getById("colony:6554");
        fortOrange.updateColonyFeatures();
        
        fortOrange.goodsContainer.increaseGoodsQuantity("model.goods.hammers", 52);
        //fortOrange.buildBuildings(game, new NewTurnContext());
        
        BuildableType dock = Specification.instance.buildingTypes.getById("model.building.docks");
        
        // when
        boolean hasAbilitiesRequiredFrom = fortOrange.colonyUpdatableFeatures.hasAbilitiesRequiredFrom(dock);
        
        // then
        assertEquals(true, hasAbilitiesRequiredFrom);
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
        colony.goodsContainer.increaseGoodsQuantity("model.goods.furs", 5);
        
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
