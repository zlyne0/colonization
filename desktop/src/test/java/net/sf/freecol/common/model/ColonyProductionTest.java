package net.sf.freecol.common.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.xml.sax.SAXException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.player.FoundingFather;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.BuildableType;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.ui.resources.Messages;

public class ColonyProductionTest {

    @BeforeAll
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
        Messages.instance().load();
    }

    Game game;
    Colony colony;
    Player dutch;
    FoundingFather henryHudson;
    FoundingFather thomasPaine;
    
    @BeforeEach
    public void setup() throws IOException, ParserConfigurationException, SAXException {
        game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
        dutch = game.players.getById("player:1");
        // Nieuw Amsterdam
        colony = (Colony)dutch.settlements.getById("colony:6528"); 
        henryHudson = Specification.instance.foundingFathers.getById("model.foundingFather.henryHudson");
        thomasPaine = Specification.instance.foundingFathers.getById("model.foundingFather.thomasPaine");
    }
    
    @Test
	public void calcuateSonsOfLiberty() throws Exception {
    	// given
        // when
        colony.calculateSonsOfLiberty();
        // then
        assertEquals(147, colony.colonyLiberty.liberty);
        assertEquals(12, colony.sonsOfLiberty());
	}
    
    @Test
    public void canCalculateProductionForColony() throws Exception {
        // given
        dutch.addFoundingFathers(game, henryHudson);
        
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
    public void canCalculateProductionForColonyWithOneProductionBonus() throws Exception {
        // given
        dutch.addFoundingFathers(game, henryHudson);
        colony.colonyLiberty.setLibertyForOneProductionBonus(colony.getColonyUnitsCount());
        
        // when
        colony.updateColonyFeatures();
        colony.calculateSonsOfLiberty();
        colony.updateProductionBonus();
        ProductionSummary ps = colony.productionSummary();

        // then
        assertThat(colony.productionBonus().asInt()).isEqualTo(1);
        
        System.out.println("productionSummary = " + ps);
        
        assertEquals(4, ps.getQuantity("model.goods.cotton"));
        assertEquals(-2, ps.getQuantity("model.goods.lumber")); 
        assertEquals(2, ps.getQuantity("model.goods.horses")); 
        assertEquals(-11, ps.getQuantity("model.goods.furs")); 
        assertEquals(13, ps.getQuantity("model.goods.hammers")); 
        assertEquals(2, ps.getQuantity("model.goods.crosses")); 
        assertEquals(6, ps.getQuantity("model.goods.bells")); 
        assertEquals(0, ps.getQuantity("model.goods.fish")); 
        assertEquals(11, ps.getQuantity("model.goods.coats")); 
        assertEquals(4, ps.getQuantity("model.goods.food")); 
        assertEquals(0, ps.getQuantity("model.goods.grain"));
        
        Building furTrading = colony.buildings.getById("building:6545");
        ProductionConsumption furTradingProdCons = colony.productionSummary(furTrading);
        System.out.println("furTradingProdCons = " + furTradingProdCons);
        
        assertEquals(-11, furTradingProdCons.baseConsumption.getQuantity("model.goods.furs"));
        assertEquals(0, furTradingProdCons.baseProduction.getQuantity("model.goods.furs"));
        assertEquals(11, furTradingProdCons.baseProduction.getQuantity("model.goods.coats"));

        assertEquals(-11, furTradingProdCons.realConsumption.getQuantity("model.goods.furs"));
        assertEquals(11, furTradingProdCons.realProduction.getQuantity("model.goods.coats"));
    }
    
    
    @Test
    public void canCalculateProductionForColonyForUpgradedFurTraderHouse() throws Exception {
        // given
        dutch.addFoundingFathers(game, henryHudson);
        
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
        dutch.addFoundingFathers(game, henryHudson);
        
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
        Unit unit = dutch.units.getById("unit:7076");
        unit.removeFromLocation();
        ColonyTile fursColonyTile = colony.colonyTiles.getById("tile:3352");
        
        colony.addWorkerToTerrain(fursColonyTile, unit, Specification.instance.goodsTypes.getById("model.goods.furs"));
        
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
        dutch.addFoundingFathers(game, henryHudson);

        // move statesment from townHall to tile 
        Unit unit = dutch.units.getById("unit:7076");
        unit.removeFromLocation();
        ColonyTile fursColonyTile = colony.colonyTiles.getById("tile:3352");
        
        colony.addWorkerToTerrain(fursColonyTile, unit, Specification.instance.goodsTypes.getById("model.goods.furs"));
        
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
        Colony fortOrange = (Colony)dutch.settlements.getById("colony:6554");
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
	public void canRiseCrossesProductionWithWilliamPenn() throws Exception {
		// given
    	FoundingFather williamPenn = Specification.instance.foundingFathers.getById("model.foundingFather.williamPenn");
    	dutch.addFoundingFathers(game, williamPenn);
    	
    	BuildingType churchType = Specification.instance.buildingTypes.getById("model.building.church");
        Building church = colony.addBuilding(churchType);
        colony.addWorkerToBuilding(church, colony.getUnits().getById("unit:7076"));
        colony.addWorkerToBuilding(church, colony.getUnits().getById("unit:6940"));
        
        // when
        ProductionSummary ps = colony.productionSummary();
        
        // then
        assertThat(ps.getQuantity("model.goods.crosses")).isEqualTo(9);
	}
    
	@Test
	public void thomasPaineCanRiseBellProduction() throws Exception {
		// given
        if (!dutch.foundingFathers.containsId(thomasPaine)) {
        	dutch.addFoundingFathers(game, thomasPaine);
        }
        dutch.setTax(20);
		
		// when
        ProductionSummary ps = colony.productionSummary();

		// then
		assertThat(ps.getQuantity(GoodsType.BELLS)).isEqualTo(6);
	}
	
	@Test
	public void withoutThomasPaineNoTaxRiseBellProduction() throws Exception {
		// given
		if (dutch.foundingFathers.containsId(thomasPaine)) {
			fail("should has not " + thomasPaine);
		}
		dutch.setTax(20);
		
		// when
		ProductionSummary ps = colony.productionSummary();

		// then
		assertThat(ps.getQuantity(GoodsType.BELLS)).isEqualTo(5);
	}
    
	@Test
	public void canDeterminePotentialProduction() throws Exception {
		// given
		Unit unit = dutch.units.getById("unit:7076");
		dutch.addFoundingFathers(game, henryHudson);
		colony.updateColonyFeatures();
		
		// when
		List<GoodMaxProductionLocation> locations = colony.determinePotentialMaxGoodsProduction(unit.unitType, false);

		// then
		for (GoodMaxProductionLocation l : locations) {
			System.out.println(l);
			if (l.getGoodsType().equalsId("model.goods.furs")) {
				assertThat(l.getProduction()).isEqualTo(8);
				assertThat(l.getColonyTile().getId()).isEqualTo("tile:3352");
			}
		}
		
	}
}
