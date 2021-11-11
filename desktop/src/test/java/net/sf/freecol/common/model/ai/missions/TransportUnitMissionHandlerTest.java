package net.sf.freecol.common.model.ai.missions;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileAssert;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitAssert;
import net.sf.freecol.common.model.UnitFactory;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.savegame.AbstractMissionAssert;

class TransportUnitMissionHandlerTest extends MissionHandlerBaseTestClass {
	
    Tile sourceTile;
    Tile disembarkTile;
    Tile fortOrangeTile;
    Unit galleon;
    Unit u1;
    Unit u2;
	
    @Test
	void canMoveFromOneTileToAnother() throws Exception {
		// given
        game.aiContainer.missionContainer(this.dutch).clearAllMissions();

    	Tile tileSource = game.map.getTile(22, 83);
    	Tile tileDest = game.map.getTile(29, 73);

    	Unit galleon = UnitFactory.create(UnitType.GALLEON, dutch, tileSource);
    	
		Path path = di.pathFinder.findToTile(
			game.map,
			galleon.getTile(),
			tileDest,
			galleon,
			PathFinder.includeUnexploredAndExcludeNavyThreatTiles
		);
		MoveContext moveContext = new MoveContext(galleon, path);

		// when
		di.moveService.aiConfirmedMovePath(moveContext);
		newTurnAndExecuteMission(dutch);
		di.moveService.aiConfirmedMovePath(moveContext);

		// then
		TileAssert.assertThat(galleon.getTile()).isEqualsCords(tileDest);
	}
    
    @Test
    void canTranportUnitsFromEuropeToNewWorld() {
        // given
        createTransportUnitMissionFromEurope();

        // when
        // move to europe and back to new world for colonists
        newTurnAndExecuteMission(dutch, 8);

        // move to destination
        newTurnAndExecuteMission(dutch, 2);

        // then
        UnitAssert.assertThat(u1)
        	.isNotAtLocation(galleon)
        	.isNotAtLocation(dutch.getEurope())
        	.isAtLocation(fortOrangeTile);
        UnitAssert.assertThat(u2)
	    	.isNotAtLocation(galleon)
	    	.isNotAtLocation(dutch.getEurope())
	    	.isAtLocation(disembarkTile);
        UnitAssert.assertThat(galleon)
	        .hasNoUnits()
	        .isAtLocation(fortOrangeTile)
        ;
    }

    @Test
	public void canNotEnterToColony() throws Exception {
        // given
        createTransportUnitMissionFromEurope();
		fortOrangeTile.getSettlement().setOwner(spain);
        
        // when
        // move to europe and back to new world for colonists
        newTurnAndExecuteMission(dutch, 8);
        // move to destination
        newTurnAndExecuteMission(dutch, 2);

		// then
        UnitAssert.assertThat(u1)
	    	.isAtLocation(galleon)
	    	.isNotAtLocation(dutch.getEurope())
	    	.isNotAtLocation(fortOrangeTile);
	    UnitAssert.assertThat(u2)
	    	.isNotAtLocation(galleon)
	    	.isNotAtLocation(dutch.getEurope())
	    	.isAtLocation(disembarkTile);
	    UnitAssert.assertThat(galleon)
	        .hasUnit(u1)
	        .isNextToLocation(game.map, fortOrangeTile)
        ;
	}
    
    @Test
	public void canNotDisembarkToOccupiedTile() throws Exception {
        // given
        createTransportUnitMissionFromEurope();
        UnitFactory.create(UnitType.FREE_COLONIST, spain, disembarkTile);
        
        // when
        // move to europe and back to new world for colonists
        newTurnAndExecuteMission(dutch, 8);
        // move to destination
        newTurnAndExecuteMission(dutch, 2);

		// then
        UnitAssert.assertThat(u1)
	    	.isAtLocation(galleon)
	    	.isNotAtLocation(dutch.getEurope())
	    	.isNotAtLocation(disembarkTile);
	    UnitAssert.assertThat(u2)
	    	.isAtLocation(galleon)
	    	.isNotAtLocation(dutch.getEurope())
	    	.isNotAtLocation(disembarkTile);
	    UnitAssert.assertThat(galleon)
	        .hasUnit(u1)
	        .hasUnit(u2)
			.isNextToLocation(game.map, disembarkTile)
        ;
	}

	TransportUnitMission createTransportUnitMissionFromEurope() {
		game.aiContainer.missionContainer(this.dutch).clearAllMissions();

        sourceTile = game.map.getTile(26, 79);
        disembarkTile = game.map.getTile(27, 76);
        fortOrangeTile = game.map.getTile(25, 75);

        galleon = UnitFactory.create(UnitType.GALLEON, dutch, sourceTile);
        u1 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, dutch.getEurope());
        u2 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, dutch.getEurope());

        TransportUnitMission transportMission = new TransportUnitMission(galleon)
    		.addUnitDest(u2, disembarkTile)
            .addUnitDest(u1, fortOrangeTile);

        game.aiContainer.missionContainer(this.dutch).addMission(transportMission);
        
        return transportMission;
	}

	@Test
	void shouldDisembarkNextToInLandDestination() {
		// given
		game.aiContainer.missionContainer(this.dutch).clearAllMissions();

		Tile sourceTile = game.map.getTile(28, 82);
		Tile destInLand = game.map.getTile(26, 72);
		galleon = UnitFactory.create(UnitType.GALLEON, dutch, sourceTile);
		u1 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, galleon);
		TransportUnitMission transportMission = new TransportUnitMission(galleon)
			.addUnitDest(u1, destInLand);
		game.aiContainer.missionContainer(this.dutch).addMission(transportMission);

		// when
		newTurnAndExecuteMission(dutch);

		// then
		Tile transferLocation = game.map.getTile(27, 72);
		UnitAssert.assertThat(u1)
			.isNotAtLocation(galleon)
			.isAtLocation(transferLocation);

		assertThat(transportMission.destTiles()).isEmpty();

		AbstractMissionAssert.assertThat(transportMission)
			.isDone();
	}
}
