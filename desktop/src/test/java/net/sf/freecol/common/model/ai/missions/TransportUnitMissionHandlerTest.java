package net.sf.freecol.common.model.ai.missions;

import org.junit.jupiter.api.Test;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileAssert;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitAssert;
import net.sf.freecol.common.model.UnitFactory;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import promitech.colonization.orders.move.MoveContext;

class TransportUnitMissionHandlerTest extends MissionHandlerBaseTestClass {
	
    Tile sourceTile;
    Tile disembarkTile;
    Tile fortOrangeTile;
    Unit galleon;
    Unit u1;
    Unit u2;
	
    @Test
	void canGoFromOneTileToAnother() throws Exception {
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
		MoveContext moveContext = new MoveContext(path);

		// when
		di.moveService.aiConfirmedMovePath(moveContext);
		newTurnAndExecuteMission(dutch);
		di.moveService.aiConfirmedMovePath(moveContext);

		// then
		TileAssert.assertThat(galleon.getTile()).isEqualsCords(tileDest);
	}
    
    @Test
    void canTranportUnits() {
        // given
        createTransportUnitMission();

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
        createTransportUnitMission();
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
        createTransportUnitMission();
        UnitFactory.create(UnitType.FREE_COLONIST, spain, disembarkTile);
        
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
	    	.isAtLocation(galleon)
	    	.isNotAtLocation(dutch.getEurope())
	    	.isNotAtLocation(disembarkTile);
	    UnitAssert.assertThat(galleon)
	        .hasUnit(u2)
	        .isAtLocation(fortOrangeTile)
        ;
	}

	TransportUnitMission createTransportUnitMission() {
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
}
