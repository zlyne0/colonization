package net.sf.freecol.common.model.ai.missions

import net.sf.freecol.common.model.ColonyFactory
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.TileAssert
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitAssert
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.map.path.PathFinder
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import promitech.colonization.orders.move.MoveContext
import promitech.colonization.savegame.AbstractMissionAssert

class TransportUnitMissionHandlerTest : MissionHandlerBaseTestClass() {

    @BeforeEach
    override fun setup() {
        super.setup()
        game.aiContainer.missionContainer(dutch).clearAllMissions()
    }

    @Test
    fun canMoveFromOneTileToAnother() {
        // given
        val tileSource = game.map.getTile(22, 83)
        val tileDest = game.map.getTile(29, 73)
        val galleon = UnitFactory.create(UnitType.GALLEON, dutch, tileSource)
        val path = di.pathFinder.findToTile(
            game.map,
            galleon.tile,
            tileDest,
            galleon,
            PathFinder.includeUnexploredAndExcludeNavyThreatTiles
        )
        val moveContext = MoveContext(galleon, path)

        // when
        di.moveService.aiConfirmedMovePath(moveContext)
        newTurnAndExecuteMission(dutch)
        di.moveService.aiConfirmedMovePath(moveContext)

        // then
        TileAssert.assertThat(galleon.tile).isEqualsCords(tileDest)
    }

    @Nested
    inner class Disembark {

        @Test
        fun shouldDisembarkNextToInLandDestination() {
            // given
            val sourceTile = game.map.getTile(28, 82)
            val destInLand = game.map.getTile(26, 72)
            val galleon = UnitFactory.create(UnitType.GALLEON, dutch, sourceTile)
            val colonist = UnitFactory.create(UnitType.FREE_COLONIST, dutch, galleon)
            val transportMission = TransportUnitMission(galleon)
                .addUnitDest(colonist, destInLand)
            game.aiContainer.missionContainer(dutch).addMission(transportMission)

            // when
            newTurnAndExecuteMission(dutch)

            // then
            val transferLocation = game.map.getTile(27, 72)
            UnitAssert.assertThat(colonist)
                .isNotAtLocation(galleon)
                .isAtLocation(transferLocation)
            Assertions.assertThat(transportMission.destTiles()).isEmpty()
            AbstractMissionAssert.assertThat(transportMission)
                .isDone
        }

        @Test
        fun shouldDisembarkNextToOccupiedByNative() {
            // given
            val sourceTile = game.map.getTile(28, 82)
            val destInLand = game.map.getTile(27, 73)
            val galleon = UnitFactory.create(UnitType.GALLEON, dutch, sourceTile)
            val colonist = UnitFactory.create(UnitType.FREE_COLONIST, dutch, galleon)
            val transportMission = TransportUnitMission(galleon)
                .addUnitDest(colonist, destInLand)
            game.aiContainer.missionContainer(dutch).addMission(transportMission)

            // when
            newTurnAndExecuteMission(dutch)

            // then
            val transferLocation = game.map.getTile(27, 72)
            UnitAssert.assertThat(colonist)
                .isNotAtLocation(galleon)
                .isAtLocation(transferLocation)
            Assertions.assertThat(transportMission.destTiles()).isEmpty()
            AbstractMissionAssert.assertThat(transportMission)
                .isDone
        }

        @Test
        fun shouldDisembarkNextToLostCity() {
            // given
            val sourceTile = game.map.getTile(28, 82)
            val destInLand = game.map.getTile(27, 75)
            destInLand.addLostCityRumors()

            val galleon = UnitFactory.create(UnitType.GALLEON, dutch, sourceTile)
            val colonist = UnitFactory.create(UnitType.FREE_COLONIST, dutch, galleon)
            val transportMission = TransportUnitMission(galleon)
                .addUnitDest(colonist, destInLand)
            game.aiContainer.missionContainer(dutch).addMission(transportMission)

            // when
            newTurnAndExecuteMission(dutch)

            // then
            val transferLocation = game.map.getTile(27, 74)
            UnitAssert.assertThat(colonist)
                .isNotAtLocation(galleon)
                .isAtLocation(transferLocation)
            Assertions.assertThat(transportMission.destTiles()).isEmpty()
            AbstractMissionAssert.assertThat(transportMission)
                .isDone
        }

        @Test
        fun shouldDisembarkNextToNativeSettlement() {
            // given
            val sourceTile = game.map.getTile(28, 82)
            val destInLand = game.map.getTile(25, 71) // native settlement

            val galleon = UnitFactory.create(UnitType.GALLEON, dutch, sourceTile)
            val colonist = UnitFactory.create(UnitType.FREE_COLONIST, dutch, galleon)
            val transportMission = TransportUnitMission(galleon)
                .addUnitDest(colonist, destInLand)
            game.aiContainer.missionContainer(dutch).addMission(transportMission)

            // when
            newTurnAndExecuteMission(dutch, 2)

            // then
            val transferLocation = game.map.getTile(26, 70)
            UnitAssert.assertThat(colonist)
                .isNotAtLocation(galleon)
                .isAtLocation(transferLocation)
            Assertions.assertThat(transportMission.destTiles()).isEmpty()
            AbstractMissionAssert.assertThat(transportMission)
                .isDone
        }

        @Test
        fun shouldDisembarkDirectlyToColony() {
            // given
            val sourceTile = game.map.getTile(28, 82)
            val dest = nieuwAmsterdam.tile

            val galleon = UnitFactory.create(UnitType.GALLEON, dutch, sourceTile)
            val colonist = UnitFactory.create(UnitType.FREE_COLONIST, dutch, galleon)
            val transportMission = TransportUnitMission(galleon)
                .addUnitDest(colonist, dest)
            game.aiContainer.missionContainer(dutch).addMission(transportMission)

            // when
            newTurnAndExecuteMission(dutch)

            // then
            UnitAssert.assertThat(colonist)
                .isNotAtLocation(galleon)
                .isAtLocation(dest)
            UnitAssert.assertThat(galleon)
                .isAtLocation(dest)
            Assertions.assertThat(transportMission.destTiles()).isEmpty()
            AbstractMissionAssert.assertThat(transportMission)
                .isDone
        }
    }

    @Nested
    inner class Embark {

        @Test
        fun shouldEmbarkUnitWhenUnitIsInland() {
            // given
            val destInlandTile = game.map.getTile(26, 72)
            val sourceInlandTile = game.map.getTile(25, 88)
            createIsland(sourceInlandTile, game)
            val scout = UnitFactory.create(UnitType.FREE_COLONIST, UnitRole.SCOUT, dutch, sourceInlandTile)
            val caravel = UnitFactory.create(UnitType.CARAVEL, dutch, game.map.getTile(29, 78))

            // create transport mission
            val transportUnitMission = TransportUnitMission(caravel)
            transportUnitMission.addUnitDest(scout, destInlandTile, true)
            game.aiContainer.missionContainer(dutch).addMission(transportUnitMission)

            // when
            newTurnAndExecuteMission(dutch, 2)

            // then
            UnitAssert.assertThat(scout).isAtLocation(caravel)
            UnitAssert.assertThat(caravel).hasUnit(scout)
        }

        @Test
        fun shouldEmbarkUnitWhenUnitIsOnSeaSide() {
            // given
            val destInlandTile = game.map.getTile(26, 72)
            val sourceInlandTile = game.map.getTile(25, 88)
            createIsland(sourceInlandTile, game)
            val seaSide = game.map.getTile(25, 87)
            val scout = UnitFactory.create(UnitType.FREE_COLONIST, UnitRole.SCOUT, dutch, seaSide)
            val caravel = UnitFactory.create(UnitType.CARAVEL, dutch, game.map.getTile(29, 78))

            // create transport mission
            val transportUnitMission = TransportUnitMission(caravel)
            transportUnitMission.addUnitDest(scout, destInlandTile, true)
            game.aiContainer.missionContainer(dutch).addMission(transportUnitMission)

            // when
            newTurnAndExecuteMission(dutch, 2)

            // then
            UnitAssert.assertThat(scout).isAtLocation(caravel)
            UnitAssert.assertThat(caravel).hasUnit(scout)
        }

        @Test
        fun shouldEmbarkUnitWhenUnitsOnSeaSideInColony() {
            // given
            val destInlandTile = game.map.getTile(26, 72)
            val sourceInlandTile = game.map.getTile(25, 88)
            createIsland(sourceInlandTile, game)
            val seaSide = game.map.getTile(25, 87)
            val scout = UnitFactory.create(UnitType.FREE_COLONIST, UnitRole.SCOUT, dutch, seaSide)
            val caravel = UnitFactory.create(UnitType.CARAVEL, dutch, game.map.getTile(29, 78))

            // create transport mission
            val transportUnitMission = TransportUnitMission(caravel)
            transportUnitMission.addUnitDest(scout, destInlandTile, true)
            game.aiContainer.missionContainer(dutch).addMission(transportUnitMission)

            val colonyFactory = ColonyFactory(game, di.pathFinder)
            colonyFactory.buildColonyByAI(UnitFactory.create(UnitType.FREE_COLONIST, dutch, seaSide), seaSide)

            // when
            newTurnAndExecuteMission(dutch, 2)

            // then
            UnitAssert.assertThat(scout).isAtLocation(caravel)
            UnitAssert.assertThat(caravel).hasUnit(scout)
        }

        fun createIsland(sourceTile: Tile, game: Game) {
            val plains = Specification.instance.tileTypes.getById("model.tile.plains")
            sourceTile.changeTileType(plains)
            for (neighbourTile in game.map.neighbourTiles(sourceTile)) {
                neighbourTile.tile.changeTileType(plains)
            }
        }

    }

    @Nested
    inner class TransportFromEurope {
        var sourceTile: Tile? = null
        var disembarkTile: Tile? = null
        var fortOrangeTile: Tile? = null
        var galleon: Unit? = null
        var u1: Unit? = null
        var u2: Unit? = null

        @Test
        fun canTranportUnitsFromEuropeToNewWorld() {
            // given
            createTransportUnitMissionFromEurope()

            // when
            // move to europe and back to new world for colonists
            newTurnAndExecuteMission(dutch, 8)

            // move to destination
            newTurnAndExecuteMission(dutch, 2)

            // then
            UnitAssert.assertThat(u1)
                .isNotAtLocation(galleon)
                .isNotAtLocation(dutch.getEurope())
                .isAtLocation(fortOrangeTile)
            UnitAssert.assertThat(u2)
                .isNotAtLocation(galleon)
                .isNotAtLocation(dutch.getEurope())
                .isAtLocation(disembarkTile)
            UnitAssert.assertThat(galleon)
                .hasNoUnits()
                .isAtLocation(fortOrangeTile)
        }

        @Test
        fun canNotEnterToColony() {
            // given
            createTransportUnitMissionFromEurope()
            fortOrangeTile!!.settlement.owner = spain

            // when
            // move to europe and back to new world for colonists
            newTurnAndExecuteMission(dutch, 8)
            // move to destination
            newTurnAndExecuteMission(dutch, 2)

            // then
            UnitAssert.assertThat(u1)
                .isAtLocation(galleon)
                .isNotAtLocation(dutch.europe)
                .isNotAtLocation(fortOrangeTile)
            UnitAssert.assertThat(u2)
                .isNotAtLocation(galleon)
                .isNotAtLocation(dutch.europe)
                .isAtLocation(disembarkTile)
            UnitAssert.assertThat(galleon)
                .hasUnit(u1)
                .isNextToLocation(fortOrangeTile)
        }

        @Test
        fun canNotDisembarkToOccupiedTile() {
            // given
            createTransportUnitMissionFromEurope()
            UnitFactory.create(UnitType.FREE_COLONIST, spain, disembarkTile)

            // when
            // move to europe and back to new world for colonists
            newTurnAndExecuteMission(dutch, 8)
            // move to destination
            newTurnAndExecuteMission(dutch, 2)

            // then
            UnitAssert.assertThat(u1)
                .isAtLocation(fortOrangeTile)
                .isNotAtLocation(dutch.europe)
                .isNotAtLocation(disembarkTile)
            UnitAssert.assertThat(u2)
                .isNextToLocation(disembarkTile)
                .isNotAtLocation(dutch.europe)
                .isNotAtLocation(disembarkTile)
            UnitAssert.assertThat(galleon)
                .hasNoUnits()
                .isAtLocation(fortOrangeTile)
        }

        fun createTransportUnitMissionFromEurope(): TransportUnitMission? {
            sourceTile = game.map.getTile(26, 79)
            disembarkTile = game.map.getTile(27, 76)
            fortOrangeTile = game.map.getTile(25, 75)
            galleon = UnitFactory.create(UnitType.GALLEON, dutch, sourceTile)
            u1 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, dutch.getEurope())
            u2 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, dutch.getEurope())
            val transportMission = TransportUnitMission(galleon)
                .addUnitDest(u2, disembarkTile)
                .addUnitDest(u1, fortOrangeTile)
            game.aiContainer.missionContainer(dutch).addMission(transportMission)
            return transportMission
        }

    }

}