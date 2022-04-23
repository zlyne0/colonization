package net.sf.freecol.common.model.ai.missions

import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainerAssert.assertThat
import net.sf.freecol.common.model.player.Player
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import promitech.colonization.savegame.Savegame1600BaseClass

class PlayerMissionsContainerTest {

    private val a = TestingMission("A")
    private val b = TestingMission("B")
    private val c = TestingMission("C")
    private val d = TestingMission("D")
    private val e = TestingMission("E")
    private val f = TestingMission("F")
    private val g = TestingMission("G")
    val player = Player("id")
    private val playerMissionsContainer = PlayerMissionsContainer(player)

    @Test
    fun `should find mission to execute`() {
        // given
        createMissionGraph()

        // when
        val missionToExecute = playerMissionsContainer.findMissionToExecute()

        // then
        assertThat(missionToExecute)
            .hasSize(4)
            .contains(g, d, e, f)
    }

    @Test
    fun `should find mission to execute 2`() {
        // given
        createMissionGraph()
        d.setDone()
        e.setDone()

        // when
        val missionToExecute = playerMissionsContainer.findMissionToExecute()

        // then
        assertThat(missionToExecute)
            .hasSize(3)
            .contains(c, g, f)
    }

    @Test
    fun `should find mission to execute 3`() {
        // given
        createMissionGraph()
        e.setDone()

        // when
        val missionToExecute = playerMissionsContainer.findMissionToExecute()

        // then
        assertThat(missionToExecute)
            .hasSize(3)
            .contains(g, d, f)
    }

    @Test
    fun `should find mission to execute 4`() {
        // given
        createMissionGraph()
        c.setDone()
        d.setDone()
        e.setDone()

        // when
        val missionToExecute = playerMissionsContainer.findMissionToExecute()

        // then
        assertThat(missionToExecute)
            .hasSize(2)
            .contains(g, f)
    }

    @Test
    fun `should find mission to execute 5`() {
        // given
        createMissionGraph()
        e.setDone()
        f.setDone()
        g.setDone()

        // when
        val missionToExecute = playerMissionsContainer.findMissionToExecute()

        // then
        assertThat(missionToExecute)
            .hasSize(1)
            .contains(d)
    }

    @Test
    fun `should clear done missions`() {
        // given
        createMissionGraph()

        c.setDone()
        d.setDone()
        e.setDone()

        // when
        playerMissionsContainer.clearDoneMissions()
        val missionToExecute = playerMissionsContainer.findMissionToExecute()

        // then
        assertThat(playerMissionsContainer)
            .doesNotHaveMission(c)
            .doesNotHaveMission(d)
            .doesNotHaveMission(e)
        assertThat(missionToExecute)
            .hasSize(2)
            .contains(g, f)
    }

    @Test
    fun `should clear done missions for part of leaf`() {
        // given
        createMissionGraph()
        e.setDone()
        f.setDone()
        g.setDone()

        // when
        playerMissionsContainer.clearDoneMissions()
        val missionToExecute = playerMissionsContainer.findMissionToExecute()

        // then
        assertThat(playerMissionsContainer)
            .doesNotHaveMission(e)
            .doesNotHaveMission(f)
            .doesNotHaveMission(g)
        assertThat(missionToExecute)
            .hasSize(1)
            .contains(d)
    }

    @Test
    fun `can determine that mission has child`() {
        // given
        val missionB = MissionB("missionB")

        val player = Player("id")
        val playerMissionsContainer = PlayerMissionsContainer(player)
        playerMissionsContainer.addMission(a)
        playerMissionsContainer.addMission(a, missionB)

        // when
        val hasMissionType = playerMissionsContainer.hasMission(MissionB::class.java)

        // then
        assertThat(hasMissionType).isTrue()
    }

    @Test
    fun `should find deep depend mission from B`() {
        // given
        createMissionGraph()

        // when
        val missionToExecute = playerMissionsContainer.findDeepDependMissions(b)

        // then
        assertThat(missionToExecute)
            .hasSize(5)
            .contains(c, d, e, f, g)
    }

    @Test
    fun `should find deep depend mission from E`() {
        // given
        createMissionGraph()

        // when
        val missionToExecute = playerMissionsContainer.findDeepDependMissions(e)

        // then
        assertThat(missionToExecute)
            .isEmpty()
    }

    @Test
    fun `should not find parent mission to execute due to no parent`() {
        // given
        createMissionGraph()

        // when
        val missionToExecute = playerMissionsContainer.findParentToExecute(a)

        // then
        assertThat(missionToExecute).isNull()
    }

    @Test
    fun `should not find parent mission to execute due to all depend mission are not done`() {
        // given
        createMissionGraph()

        // when
        val missionToExecute = playerMissionsContainer.findParentToExecute(d)

        // then
        assertThat(missionToExecute).isNull()
    }

    @Test
    fun `should find parent mission to execute when all depend mission are done`() {
        // given
        createMissionGraph()
        d.setDone()
        e.setDone()

        // when
        val missionToExecute = playerMissionsContainer.findParentToExecute(d)

        // then
        assertThat(missionToExecute).isEqualTo(c)
    }

    /*

         .--> G
         |
    A -> B -> C -> D
         |    + -> E
         |
         `--> F

     */
    private fun createMissionGraph() {
        playerMissionsContainer.addMission(a)
        playerMissionsContainer.addMission(a, b)
        playerMissionsContainer.addMission(b, g)
        playerMissionsContainer.addMission(b, f)
        playerMissionsContainer.addMission(b, c)
        playerMissionsContainer.addMission(c, d)
        playerMissionsContainer.addMission(c, e)
    }

}