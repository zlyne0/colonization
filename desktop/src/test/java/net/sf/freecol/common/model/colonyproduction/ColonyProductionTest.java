package net.sf.freecol.common.model.colonyproduction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.GoodMaxProductionLocation;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.ProductionSummaryAssert;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.ai.missions.Savegame1600BaseClass;
import net.sf.freecol.common.model.specification.GoodsType;

class ColonyProductionTest extends Savegame1600BaseClass {

	@Test
	public void backwardCompatibilityForColonyProduction() throws Exception {
		for (Settlement settlement : dutch.settlements) {
			// given
			Colony colony = settlement.asColony();
			
			// when
			ProductionSummary oldColonyProduction = colony.productionSummary();
			ColonyProduction newColonyProduction = new ColonyProduction(new DefaultColonySettingProvider(colony));
			
			// then
			assertThat(oldColonyProduction)
				.as("incorrect colony production for colony %s", colony.getName())
				.isEqualTo(newColonyProduction.globalProductionConsumption());
		}
	}

	@Test
	public void backwardCompatibilityForColonyProductionWithColonyBonus() throws Exception {
		for (Settlement settlement : dutch.settlements) {
			// given
			Colony colony = settlement.asColony();
	        colony.colonyLiberty.setLibertyForProductionBonus(colony.getColonyUnitsCount());
	        colony.updateColonyFeatures();
	        colony.calculateSonsOfLiberty();
	        
			// when
			ProductionSummary oldColonyProduction = colony.productionSummary();
			ColonyProduction newColonyProduction = new ColonyProduction(new DefaultColonySettingProvider(colony));
			
			// then
			assertThat(oldColonyProduction)
				.as("incorrect colony production for colony %s", colony.getName())
				.isEqualTo(newColonyProduction.globalProductionConsumption());
		}
	}
	
	@Test
	public void colonyProductionConsumption() throws Exception {
		// given
		ColonyProduction colonyProduction = new ColonyProduction(new DefaultColonySettingProvider(nieuwAmsterdam));

		// when
		ProductionSummary production = colonyProduction.globalProductionConsumption();
		
		// then
		System.out.println(production);

		ProductionSummaryAssert.assertThat(production)
			.has("model.goods.cotton", 3) 
			.has("model.goods.furs", -9) 
			.has("model.goods.hammers", 12) 
			.has("model.goods.crosses", 1) 
			.has("model.goods.fish", 0) 
			.has("model.goods.coats", 9) 
			.has("model.goods.food", 2) 
			.has("model.goods.grain", 0) 
			.has("model.goods.bells", 5) 
			.has("model.goods.horses", 2) 
			.has("model.goods.lumber", -2) 
		;
	}

	@Test
	public void colonyProductionConsumptionWithMinusProductionBonus() throws Exception {
		// given
		ColonySimulationSettingProvider colonyProvider = new ColonySimulationSettingProvider(nieuwAmsterdam);
		colonyProvider.withConsumeWarehouseResources();
		colonyProvider.addWorkerToColony(
			Specification.instance.unitTypes.getById(UnitType.MASTER_TOBACCONIST),
			Specification.instance.buildingTypes.getById("model.building.tobacconistHouse")
		);

		ColonyProduction colonyProduction = new ColonyProduction(colonyProvider);

		// when
		ProductionSummary production = colonyProduction.globalProductionConsumption();

		// then
		System.out.println(production);

		ProductionSummaryAssert.assertThat(production)
				.has("model.goods.cotton", 3)
				.has("model.goods.furs", -7)
				.has("model.goods.hammers", 11)
				.has("model.goods.crosses", 0)
				.has("model.goods.fish", 0)
				.has("model.goods.coats", 7)
				.has("model.goods.food", 1)
				.has("model.goods.grain", 0)
				.has("model.goods.bells", 2)
				.has("model.goods.horses", 1)
				.has("model.goods.lumber", -1)
				.has("model.goods.cigars", 5)
		;
	}


	@Test
	public void backwardCompatibilityForMaxPotentialProduction() throws Exception {
		UnitType unitType = Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST);
		boolean ignoreIndianOwner = false;
		Collection<GoodsType> goodsTypes = Specification.instance.goodsTypeToScoreByPrice.entities();
		
		for (Settlement settlement : dutch.settlements) {
			// given
			Colony colony = settlement.asColony();
	        
			ColonyProduction newColonyProduction = new ColonyProduction(new DefaultColonySettingProvider(colony));
			// when
			List<GoodMaxProductionLocation> potentialProductions = colony.determinePotentialMaxGoodsProduction(
				goodsTypes, unitType, ignoreIndianOwner
			);
			List<MaxGoodsProductionLocation> newPotentialProductions = newColonyProduction.simulation().determinePotentialMaxGoodsProduction(
				goodsTypes, unitType, ignoreIndianOwner 
			);
			// then
			theSameProductionList(potentialProductions, newPotentialProductions);
		}
	}
	
	@Test
	public void backwardCompatibilityForMaxPotentialProductionWithProductionBonus() throws Exception {
		UnitType unitType = Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST);
		boolean ignoreIndianOwner = false;
		Collection<GoodsType> goodsTypes = Specification.instance.goodsTypeToScoreByPrice.entities();
		
		for (Settlement settlement : dutch.settlements) {
			// given
			Colony colony = settlement.asColony();
	        colony.colonyLiberty.setLibertyForProductionBonus(colony.getColonyUnitsCount());
	        colony.updateColonyFeatures();
	        colony.calculateSonsOfLiberty();
	        
			ColonyProduction newColonyProduction = new ColonyProduction(new DefaultColonySettingProvider(colony));
			// when
			List<GoodMaxProductionLocation> potentialProductions = colony.determinePotentialMaxGoodsProduction(
				goodsTypes, unitType, ignoreIndianOwner
			);
			List<MaxGoodsProductionLocation> newPotentialProductions = newColonyProduction.simulation().determinePotentialMaxGoodsProduction(
				goodsTypes, unitType, ignoreIndianOwner 
			);
			// then
			theSameProductionList(potentialProductions, newPotentialProductions);
		}
	}

	void theSameProductionList(
		List<GoodMaxProductionLocation> potentialProductions, 
		List<MaxGoodsProductionLocation> newPotentialProductions
	) {
		assertThat(potentialProductions).hasSize(newPotentialProductions.size());
		
		for (int i = 0; i < potentialProductions.size(); i++) {
			GoodMaxProductionLocation oldOne = potentialProductions.get(i);
			MaxGoodsProductionLocation newOne = newPotentialProductions.get(i);
			
			assertThat(oldOne.getGoodsType()).isEqualTo(newOne.goodsType);
			assertThat(oldOne.getProduction()).isEqualTo(newOne.production);
			
			if (oldOne.getBuilding() != null) {
				assertThat(oldOne.getBuilding().buildingType.getId()).isEqualTo(newOne.buildingType.getId());
			}
			if (oldOne.getColonyTile() != null) {
				assertThat(oldOne.getColonyTile().tile.getId()).isEqualTo(newOne.colonyTile.getId());
			}
		}
	}
	
	@Test
	public void canDeterminePotentialMaxGoodsProduction() throws Exception {
		UnitType unitType = Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST);
		boolean ignoreIndianOwner = false;
		Collection<GoodsType> goodsTypes = Specification.instance.goodsTypeToScoreByPrice.entities();
		
		// given
		ColonyProduction newColonyProduction = new ColonyProduction(new DefaultColonySettingProvider(nieuwAmsterdam));
		// when
		List<MaxGoodsProductionLocation> potentialProductions = newColonyProduction.simulation().determinePotentialMaxGoodsProduction(
			goodsTypes, unitType, ignoreIndianOwner 
		);
		// then
		
//		for (MaxGoodsProductionLocation maxGoodsProductionLocation : potentialProductions) {
//			System.out.println(maxGoodsProductionLocation);
//		}
		
		verifyProdLoc(potentialProductions.get(0), "model.goods.furs", 5, "tile:3352");
		verifyProdLoc(potentialProductions.get(1), "model.goods.ore", 3, "tile:3391");
		verifyProdLoc(potentialProductions.get(2), "model.goods.cotton", 3, "tile:3431");
		verifyProdLoc(potentialProductions.get(3), "model.goods.sugar", 2, "tile:3391");
		verifyProdLoc(potentialProductions.get(4), "model.goods.cloth", 3, "model.building.weaverHouse");
		verifyProdLoc(potentialProductions.get(5), "model.goods.silver", 1, "tile:3391");
		verifyProdLoc(potentialProductions.get(6), "model.goods.coats", 3, "model.building.furTraderHouse");
		verifyProdLoc(potentialProductions.get(7), "model.goods.cigars", 3, "model.building.tobacconistHouse");
	}
	
	void verifyProdLoc(MaxGoodsProductionLocation prodLoc, String goodsTypeId, int quantity, String locationId) {
		assertThat(prodLoc.goodsType.getId()).isEqualTo(goodsTypeId);
		assertThat(prodLoc.production).isEqualTo(quantity);
		if (prodLoc.colonyTile != null) {
			assertThat(prodLoc.colonyTile.getId()).isEqualTo(locationId);
		}
		if (prodLoc.buildingType != null) {
			assertThat(prodLoc.buildingType.getId()).isEqualTo(locationId);
		}
	}
}
