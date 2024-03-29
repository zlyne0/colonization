package net.sf.freecol.common.model.ai.missions

import net.sf.freecol.common.model.ColonyFactory
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.SettlementAssert
import net.sf.freecol.common.model.SettlementFactory
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.TileAssert
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitAssert.assertThat
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainerAssert.assertThat
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.specification.GoodsType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import promitech.colonization.orders.move.MoveContext
import promitech.colonization.savegame.AbstractMissionAssert.assertThat

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
        TileAssert.assertThat(galleon.tile).isEquals(tileDest)
    }

    @Nested
    inner class DisembarkAndUnloadCargo {

        @Test
        fun disembarkToTheClosestColonyAndNextUnloadCargo() {
            // given
            val player = dutch
            val sourceTile = game.map.getTile(29, 71)
            val ship = UnitFactory.create(UnitType.CARAVEL, player, sourceTile)
            ship.goodsContainer.increaseGoodsQuantity(GoodsType.SILVER, 100)
            val freeColonist = UnitFactory.create(UnitType.FREE_COLONIST, player, ship)

            clearAllMissions(player)
            val transportMission = TransportUnitMission(ship)
            transportMission.addUnitDest(freeColonist, fortOranje.tile)
            transportMission.addCargoDest(nieuwAmsterdam.tile, goodsType(GoodsType.SILVER), 100)
            game.aiContainer.missionContainer(dutch).addMission(transportMission)

            // when
            newTurnAndExecuteMission(dutch, 2)
            // disembark
            assertThat(freeColonist).isAtLocation(fortOranje.tile)
            assertThat(ship).isAtLocation(fortOranje.tile).hasNoUnits()
            assertThat(transportMission.unitsDest).isEmpty()

            newTurnAndExecuteMission(dutch, 1)
            // unload cargo

            // then
            assertThat(transportMission).isDone
            SettlementAssert.assertThat(nieuwAmsterdam).hasGoods(GoodsType.SILVER, 100)
            assertThat(ship).hasNoGoods()
        }

        @Test
        fun disembarkAndUnloadInTheSameDestination() {
            // given
            val player = dutch
            val sourceTile = game.map.getTile(29, 71)
            val ship = UnitFactory.create(UnitType.CARAVEL, player, sourceTile)
            ship.goodsContainer.increaseGoodsQuantity(GoodsType.SILVER, 100)
            val freeColonist = UnitFactory.create(UnitType.FREE_COLONIST, player, ship)

            clearAllMissions(player)
            val transportMission = TransportUnitMission(ship)
            transportMission.addUnitDest(freeColonist, fortOranje.tile)
            transportMission.addCargoDest(fortOranje.tile, goodsType(GoodsType.SILVER), 100)
            game.aiContainer.missionContainer(dutch).addMission(transportMission)

            // when
            newTurnAndExecuteMission(dutch, 2)
            // disembark and unload cargo

            // then
            assertThat(transportMission.unitsDest).isEmpty()
            assertThat(transportMission).isDone
            SettlementAssert.assertThat(fortOranje).hasGoods(GoodsType.SILVER, 100)
            assertThat(ship).isAtLocation(fortOranje.tile).hasNoGoods().hasNoUnits()
            assertThat(freeColonist).isAtLocation(fortOranje.tile)
        }
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
            assertThat(colonist)
                .isNotAtLocation(galleon)
                .isAtLocation(transferLocation)
            assertThat(transportMission.destTiles()).isEmpty()
            assertThat(transportMission)
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
            assertThat(colonist)
                .isNotAtLocation(galleon)
                .isAtLocation(transferLocation)
            assertThat(transportMission.destTiles()).isEmpty()
            assertThat(transportMission)
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
            assertThat(colonist)
                .isNotAtLocation(galleon)
                .isAtLocation(transferLocation)
            assertThat(transportMission.destTiles()).isEmpty()
            assertThat(transportMission)
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
            assertThat(colonist)
                .isNotAtLocation(galleon)
                .isAtLocation(transferLocation)
            assertThat(transportMission.destTiles()).isEmpty()
            assertThat(transportMission)
                .isDone
        }

        @Test
        fun shouldDisembarkNextToNativeSettlement2() {
            // given
            val sourceTile = game.map.getTile(28, 82)
            val destInLand = game.map.getTile(25, 86) // native settlement

            val settlementFactory = SettlementFactory(game.map)
            val nativePlayer = game.players.getById("player:22")
            settlementFactory.create(nativePlayer, destInLand, nativePlayer.nationType().getSettlementRegularType())

            val galleon = UnitFactory.create(UnitType.GALLEON, dutch, sourceTile)
            //val colonist = UnitFactory.create(UnitType.SCOUT, UnitRole.SCOUT, dutch, galleon)
            val colonist = UnitFactory.create(UnitType.FREE_COLONIST, dutch, galleon)
            val transportMission = TransportUnitMission(galleon)
                .addUnitDest(colonist, destInLand)
            game.aiContainer.missionContainer(dutch).addMission(transportMission)

            // when
            newTurnAndExecuteMission(dutch, 2)

            // then
            val transferLocation = game.map.getTile(24, 87)
            assertThat(colonist)
                .isNotAtLocation(galleon)
                .isAtLocation(transferLocation)
            assertThat(transportMission.destTiles()).isEmpty()
            assertThat(transportMission).isDone
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
            assertThat(colonist)
                .isNotAtLocation(galleon)
                .isAtLocation(dest)
            assertThat(galleon)
                .isAtLocation(dest)
            assertThat(transportMission.destTiles()).isEmpty()
            assertThat(transportMission)
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
            assertThat(scout).isAtLocation(caravel)
            assertThat(caravel).hasUnit(scout)
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
            assertThat(scout).isAtLocation(caravel)
            assertThat(caravel).hasUnit(scout)
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
            assertThat(scout).isAtLocation(caravel)
            assertThat(caravel).hasUnit(scout)
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
        lateinit var sourceTile: Tile
        lateinit var disembarkTile: Tile
        lateinit var fortOrangeTile: Tile
        lateinit var galleon: Unit
        lateinit var u1: Unit
        lateinit var u2: Unit

        @BeforeEach
        fun beforeEach() {
            sourceTile = game.map.getTile(26, 79)
            disembarkTile = game.map.getTile(27, 76)
            fortOrangeTile = game.map.getTile(25, 75)
            galleon = UnitFactory.create(UnitType.GALLEON, dutch, sourceTile)
            u1 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, dutch.getEurope())
            u2 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, dutch.getEurope())
        }

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
            verifyUnitsAtDestination()
        }

        @Test
        fun `should end transport request missions after disembark`() {
            // given
            val missionContainer = game.aiContainer.missionContainer(dutch)

            val request1 = TransportUnitRequestMission(game.turn, u1, fortOrangeTile)
            val request2 = TransportUnitRequestMission(game.turn, u2, disembarkTile)

            val transportMission = TransportUnitMission(galleon)
                .addUnitDest(request2)
                .addUnitDest(request1)
            missionContainer.addMission(transportMission)
            missionContainer.addMission(request1)
            missionContainer.addMission(request2)

            // when
            // move to europe and back to new world for colonists
            newTurnAndExecuteMission(dutch, 8)

            // move to destination
            newTurnAndExecuteMission(dutch, 2)

            // then
            verifyUnitsAtDestination()

            assertThat(missionContainer)
                .doesNotHaveMission(request1).isDone(request1)
                .doesNotHaveMission(request2).isDone(request2)
        }

        @Test
        fun canNotEnterToColony() {
            // given
            createTransportUnitMissionFromEurope()
            fortOrangeTile.settlement.owner = spain

            // when
            // move to europe and back to new world for colonists
            newTurnAndExecuteMission(dutch, 8)
            // move to destination
            newTurnAndExecuteMission(dutch, 2)

            // then
            assertThat(u1)
                .isAtLocation(galleon)
                .isNotAtLocation(dutch.europe)
                .isNotAtLocation(fortOrangeTile)
            assertThat(u2)
                .isNotAtLocation(galleon)
                .isNotAtLocation(dutch.europe)
                .isAtLocation(disembarkTile)
            assertThat(galleon)
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
            assertThat(u1)
                .isAtLocation(fortOrangeTile)
                .isNotAtLocation(dutch.europe)
                .isNotAtLocation(disembarkTile)
            assertThat(u2)
                .isNextToLocation(disembarkTile)
                .isNotAtLocation(dutch.europe)
                .isNotAtLocation(disembarkTile)
            assertThat(galleon)
                .hasNoUnits()
                .isAtLocation(fortOrangeTile)
        }

        fun createTransportUnitMissionFromEurope(): TransportUnitMission {
            val transportMission = TransportUnitMission(galleon)
                .addUnitDest(u2, disembarkTile)
                .addUnitDest(u1, fortOrangeTile)
            game.aiContainer.missionContainer(dutch).addMission(transportMission)
            return transportMission
        }

        fun verifyUnitsAtDestination() {
            assertThat(u1)
                .isNotAtLocation(galleon)
                .isNotAtLocation(dutch.getEurope())
                .isAtLocation(fortOrangeTile)
            assertThat(u2)
                .isNotAtLocation(galleon)
                .isNotAtLocation(dutch.getEurope())
                .isAtLocation(disembarkTile)
            assertThat(galleon)
                .hasNoUnits()
                .isAtLocation(fortOrangeTile)
        }
    }

    @Test
    fun `should handle unhandled carried units destinations`() {
        // given
        val missionContainer = game.aiContainer.missionContainer(dutch)
        val twoTurnsAwayFromFortOrange = game.map.getTile(29, 89)

        val galleon = UnitFactory.create(UnitType.GALLEON, dutch, twoTurnsAwayFromFortOrange)
        val u1 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, galleon)
        val u2 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, galleon)

        val request1 = TransportUnitRequestMission(game.turn, u1, fortOranje.tile)
        val request2 = TransportUnitRequestMission(game.turn, u2, fortOranje.tile)
        missionContainer.addMission(request1)
        missionContainer.addMission(request2)

        val transportMission = TransportUnitMission(galleon)
        missionContainer.addMission(transportMission)

        //printMissions(dutch)

        assertThat(transportMission.isCarriedUnitTransportDestinationSet(u1)).isFalse()
        assertThat(transportMission.isCarriedUnitTransportDestinationSet(u2)).isFalse()
        assertThat(request1.transportUnitMissionId).isNull()
        assertThat(request2.transportUnitMissionId).isNull()

        // when
        newTurnAndExecuteMission(dutch, 1)

        // then
        //printMissions(dutch)
        assertThat(transportMission.isCarriedUnitTransportDestinationSet(u1)).isTrue()
        assertThat(transportMission.isCarriedUnitTransportDestinationSet(u2)).isTrue()
        assertThat(request1.transportUnitMissionId).isEqualTo(transportMission.id)
        assertThat(request2.transportUnitMissionId).isEqualTo(transportMission.id)
    }

}