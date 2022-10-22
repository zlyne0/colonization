package net.sf.freecol.common.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.BuildingType;
import promitech.colonization.savegame.SaveGameParser;

public class ColonyAddFreeBuildingTest {

	Game game;
	Player dutch;
	BuildingType stockadeType;
	Colony colony;
	
    @BeforeAll
    public static void beforeClass() {
        Gdx.files = new Lwjgl3Files();
    }
	
    @BeforeEach
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	dutch = game.players.getById("player:1");
		stockadeType = Specification.instance.buildingTypes.getById("model.building.stockade");

		Settlement fortMaurits = dutch.settlements.getById("colony:6993");
		colony = fortMaurits.asColony();
		
		ColonyAssert.assertThat(colony)
			.hasNotBuilding(stockadeType.getId());
    }

	@Test
	public void canAddStockadeFreeBuilding() throws Exception {
		// given
		addUnitToColonyTownHall(colony);
		
		// when
		colony.ifPossibleAddFreeBuilding(stockadeType);

		// then
		ColonyAssert.assertThat(colony)
			.hasBuilding(stockadeType.getId());
	}

	@Test
	public void shouldNotAddStockadeFreeBuildingBecauseOf2Colonists() throws Exception {
		// given
		
		// when
		colony.ifPossibleAddFreeBuilding(stockadeType);

		// then
		ColonyAssert.assertThat(colony)
			.hasNotBuilding(stockadeType.getId());
	}
	
	@Test
	public void shouldNotAddStockadeFreeBuildingBecauseOfAlreadyBuilt() throws Exception {
		// given
		addUnitToColonyTownHall(colony);
		addUnitToColonyTownHall(colony);
		
		colony.buildings.add(new Building(Game.idGenerator, stockadeType));
		colony.updateColonyFeatures();
		
		// when
		colony.ifPossibleAddFreeBuilding(stockadeType);

		// then
		ColonyAssert.assertThat(colony)
			.hasBuilding(stockadeType.getId());
	}
	
	@Test
	public void canAddStockadeFreeBuildingAndRemoveFromBuildingQueue() throws Exception {
		// given
		addUnitToColonyTownHall(colony);
		addUnitToColonyTownHall(colony);
		
		colony.addItemToBuildingQueue(new ColonyBuildingQueueItem(
			Specification.instance.buildingTypes.getById("model.building.warehouse"))
		);
		colony.addItemToBuildingQueue(new ColonyBuildingQueueItem(stockadeType));
		colony.addItemToBuildingQueue(new ColonyBuildingQueueItem(
			Specification.instance.buildingTypes.getById("model.building.printingPress"))
		);
		
		// when
		colony.ifPossibleAddFreeBuilding(stockadeType);

		// then
		ColonyAssert.assertThat(colony)
			.hasBuilding(stockadeType.getId());
		assertThat(colony.buildingQueue)
			.extracting(ColonyBuildingQueueItem::getId)
			.doesNotContain(stockadeType.getId());
	}
	
	@Test
	public void canAddStockadeFreeBuildingAndRemoveFromBeginingOfBuildingQueue() throws Exception {
		// given
		addUnitToColonyTownHall(colony);
		addUnitToColonyTownHall(colony);
		
		colony.addItemToBuildingQueue(new ColonyBuildingQueueItem(stockadeType));
		colony.addItemToBuildingQueue(new ColonyBuildingQueueItem(
			Specification.instance.buildingTypes.getById("model.building.warehouse"))
		);
		colony.addItemToBuildingQueue(new ColonyBuildingQueueItem(
			Specification.instance.buildingTypes.getById("model.building.printingPress"))
		);
		
		// when
		colony.ifPossibleAddFreeBuilding(stockadeType);

		// then
		ColonyAssert.assertThat(colony)
			.hasBuilding(stockadeType.getId());
		assertThat(colony.buildingQueue)
			.extracting(ColonyBuildingQueueItem::getId)
			.doesNotContain(stockadeType.getId());
	}
	
	@Test
	public void shouldNotAddStockadeFreeBuildingBecauseOfAlreadyHasFort() throws Exception {
		// given
		addUnitToColonyTownHall(colony);
		addUnitToColonyTownHall(colony);
		
		colony.buildings.add(new Building(Game.idGenerator, Specification.instance.buildingTypes.getById("model.building.fortress")));
		colony.updateColonyFeatures();
		
		// when
		colony.ifPossibleAddFreeBuilding(stockadeType);

		// then
		ColonyAssert.assertThat(colony)
			.hasNotBuilding(stockadeType.getId());
	}
	
	private void addUnitToColonyTownHall(Colony colony) {
		Building townHall = colony.findBuildingByType(BuildingType.TOWN_HALL);
		Unit colonist = UnitFactory.create(UnitType.FREE_COLONIST, dutch, colony.tile);
		colonist.changeUnitLocation(townHall);
		colony.updateColonyPopulation();
	}
	
}
