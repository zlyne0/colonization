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
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;

import net.sf.freecol.common.model.colonyproduction.ColonyProduction;
import net.sf.freecol.common.model.colonyproduction.DefaultColonySettingProvider;
import net.sf.freecol.common.model.colonyproduction.MaxGoodsProductionLocation;
import net.sf.freecol.common.model.player.FoundingFather;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.BuildableType;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.savegame.Savegame1600BaseClass;
import promitech.colonization.ui.resources.Messages;

public class ColonyProductionTest extends Savegame1600BaseClass {

    FoundingFather henryHudson;
    FoundingFather thomasPaine;
    
    @BeforeEach
    public void setup2() throws IOException, ParserConfigurationException, SAXException {
        henryHudson = Specification.instance.foundingFathers.getById("model.foundingFather.henryHudson");
        thomasPaine = Specification.instance.foundingFathers.getById("model.foundingFather.thomasPaine");
    }
    
    @Test
	public void calcuateSonsOfLiberty() throws Exception {
    	// given
        // when
        nieuwAmsterdam.calculateSonsOfLiberty();
        // then
        assertEquals(147, nieuwAmsterdam.colonyLiberty.liberty);
        assertEquals(12, nieuwAmsterdam.sonsOfLiberty());
	}
    
    @Test
    public void canCalculateProductionForColony() throws Exception {
        // given
        dutch.addFoundingFathers(game, henryHudson);
        
        // when
        nieuwAmsterdam.updateColonyFeatures();
        nieuwAmsterdam.updateProductionBonus();
        ProductionSummary ps = nieuwAmsterdam.productionSummary();

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
        
        Building furTrading = nieuwAmsterdam.buildings.getById("building:6545");
        ProductionConsumption furTradingProdCons = nieuwAmsterdam.productionSummary(furTrading);
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
        nieuwAmsterdam.colonyLiberty.setLibertyForOneProductionBonus(nieuwAmsterdam.getColonyUnitsCount());
        
        // when
        nieuwAmsterdam.updateColonyFeatures();
        nieuwAmsterdam.calculateSonsOfLiberty();
        nieuwAmsterdam.updateProductionBonus();
        ProductionSummary ps = nieuwAmsterdam.productionSummary();

        // then
        assertThat(nieuwAmsterdam.productionBonus().asInt()).isEqualTo(1);
        
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
        
        Building furTrading = nieuwAmsterdam.buildings.getById("building:6545");
        ProductionConsumption furTradingProdCons = nieuwAmsterdam.productionSummary(furTrading);
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
        
        Building ft = nieuwAmsterdam.buildings.getById("building:6545");
        ft.upgrade(Specification.instance.buildingTypes.getById("model.building.furTradingPost"));
        
        // when
        nieuwAmsterdam.updateColonyFeatures();
        nieuwAmsterdam.updateProductionBonus();
        ProductionSummary ps = nieuwAmsterdam.productionSummary();

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
        
        Building furTrading = nieuwAmsterdam.buildings.getById("building:6545");
        ProductionConsumption furTradingProdCons = nieuwAmsterdam.productionSummary(furTrading);
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
        
        Building ft = nieuwAmsterdam.buildings.getById("building:6545");
        ft.upgrade(Specification.instance.buildingTypes.getById("model.building.furFactory"));
        
        // when
        nieuwAmsterdam.updateColonyFeatures();
        nieuwAmsterdam.updateProductionBonus();
        ProductionSummary ps = nieuwAmsterdam.productionSummary();

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
        
        Building furTrading = nieuwAmsterdam.buildings.getById("building:6545");
        ProductionConsumption furTradingProdCons = nieuwAmsterdam.productionSummary(furTrading);
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
        ColonyTile fursColonyTile = nieuwAmsterdam.colonyTiles.getById("tile:3352");
        
        nieuwAmsterdam.addWorkerToTerrain(fursColonyTile, unit, Specification.instance.goodsTypes.getById("model.goods.furs"));
        
        // when
        nieuwAmsterdam.updateModelOnWorkerAllocationOrGoodsTransfer();
        nieuwAmsterdam.updateColonyFeatures();
        nieuwAmsterdam.updateProductionBonus();
        ProductionSummary ps = nieuwAmsterdam.productionSummary();

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
        
        Building furTrading = nieuwAmsterdam.buildings.getById("building:6545");
        ProductionConsumption furTradingProdCons = nieuwAmsterdam.productionSummary(furTrading);
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
        ColonyTile fursColonyTile = nieuwAmsterdam.colonyTiles.getById("tile:3352");
        
        nieuwAmsterdam.addWorkerToTerrain(fursColonyTile, unit, Specification.instance.goodsTypes.getById("model.goods.furs"));
        
        // when
        nieuwAmsterdam.updateModelOnWorkerAllocationOrGoodsTransfer();
        nieuwAmsterdam.updateColonyFeatures();
        nieuwAmsterdam.updateProductionBonus();
        ProductionSummary ps = nieuwAmsterdam.productionSummary();

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
        
        Building furTrading = nieuwAmsterdam.buildings.getById("building:6545");
        ProductionConsumption furTradingProdCons = nieuwAmsterdam.productionSummary(furTrading);
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
        
        fortOrange.goodsContainer.increaseGoodsQuantity(GoodsType.HAMMERS, 52);
        //fortOrange.buildBuildings(game, new NewTurnContext());
        
        BuildableType dock = Specification.instance.buildingTypes.getById(BuildingType.DOCKS);
        
        // when
        boolean hasAbilitiesRequiredFrom = fortOrange.getColonyUpdatableFeatures().hasAbilitiesRequiredFrom(dock);
        
        // then
        assertEquals(true, hasAbilitiesRequiredFrom);
    }
    
    @Test
	public void canRiseCrossesProductionWithWilliamPenn() throws Exception {
		// given
    	FoundingFather williamPenn = Specification.instance.foundingFathers.getById("model.foundingFather.williamPenn");
    	dutch.addFoundingFathers(game, williamPenn);
    	
    	BuildingType churchType = Specification.instance.buildingTypes.getById("model.building.church");
        Building church = nieuwAmsterdam.addBuilding(churchType);
        nieuwAmsterdam.addWorkerToBuilding(church, nieuwAmsterdam.getUnits().getById("unit:7076"));
        nieuwAmsterdam.addWorkerToBuilding(church, nieuwAmsterdam.getUnits().getById("unit:6940"));
        
        // when
        ProductionSummary ps = nieuwAmsterdam.productionSummary();
        
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
        ProductionSummary ps = nieuwAmsterdam.productionSummary();

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
		ProductionSummary ps = nieuwAmsterdam.productionSummary();

		// then
		assertThat(ps.getQuantity(GoodsType.BELLS)).isEqualTo(5);
	}
    
	@Test
	public void canDeterminePotentialProduction() throws Exception {
		// given
        UnitType statesman = Specification.instance.unitTypes.getById("model.unit.elderStatesman");

        dutch.addFoundingFathers(game, henryHudson);
		nieuwAmsterdam.updateColonyFeatures();
		
		// when
		List<MaxGoodsProductionLocation> locations = nieuwAmsterdam.productionSimulation().determinePotentialMaxGoodsProduction(statesman, false);

		// then
		for (MaxGoodsProductionLocation l : locations) {
			System.out.println(l);
			if (l.getGoodsType().equalsId("model.goods.furs")) {
				assertThat(l.getProduction()).isEqualTo(8);
				assertThat(l.getColonyTile().getId()).isEqualTo("tile:3352");
			}
		}
	}
}
