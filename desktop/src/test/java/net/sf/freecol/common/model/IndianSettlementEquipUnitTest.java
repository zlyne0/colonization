package net.sf.freecol.common.model;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.ui.resources.Messages;
import static net.sf.freecol.common.model.IndianSettlementAssert.*;

class IndianSettlementEquipUnitTest {

	private static final String MOUNTED_BRAVE_ROLE = "model.role.mountedBrave";
	private static final String ARMED_BRAVE_ROLE = "model.role.armedBrave";
	
	Game game;
	Player inca;
	IndianSettlement indianSettlement;
	
    @BeforeAll
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
        Messages.instance().load();
    }
    
    @BeforeEach
    public void setup() throws IOException, ParserConfigurationException, SAXException {
        game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
        
    	inca = game.players.getById("player:154");
    	indianSettlement = inca.settlements.getById("indianSettlement:6339").asIndianSettlement();
    }
	
    @Test
	public void doNotEquipMilitaryRolesWhenNoEquipment() throws Exception {
		// given
    	setAmount(GoodsType.MUSKETS, 0);
    	setAmount(GoodsType.HORSES, 0);
    	
    	assertThat(indianSettlement)
			.hasOffensiveRolesNumber(0);
    	
		// when
    	indianSettlement.equipMilitaryRoles();

		// then
    	assertThat(indianSettlement)
			.hasOffensiveRolesNumber(0);
	}	

    @Test
	public void canUpgradeRole() throws Exception {
		// given
    	setAmount(GoodsType.MUSKETS, 25);
    	setAmount(GoodsType.HORSES, 25);
    	
    	assertThat(indianSettlement)
    		.hasOffensiveRolesNumber(0);
    	
		// when
    	indianSettlement.equipMilitaryRoles();

		// then
    	assertThat(indianSettlement)
    		.hasOffensiveRolesNumber(1)
    		.hasUnitsWithRole("model.role.nativeDragoon", 1);
    	SettlementAssert.assertThat(indianSettlement)
    		.hasGoods(GoodsType.MUSKETS, 0)
    		.hasGoods(GoodsType.HORSES, 0);
	}
    
    // do not downgrade armedBrave to mountedBrave
	@Test
	public void doNotDowngradeEquipment() throws Exception {
		// given
    	setAmount(GoodsType.MUSKETS, 0);
    	setAmount(GoodsType.HORSES, 25);

    	leaveOneUnit();
    	
    	indianSettlement.getUnits().first()
    		.changeRole(Specification.instance.unitRoles.getById(ARMED_BRAVE_ROLE));

    	assertThat(indianSettlement)
			.hasOffensiveRolesNumber(1)
			.hasUnitsWithRole(ARMED_BRAVE_ROLE, 1);
    	
		// when
		indianSettlement.equipMilitaryRoles();
		
		// then
    	assertThat(indianSettlement)
			.hasOffensiveRolesNumber(1)
			.hasUnitsWithRole(ARMED_BRAVE_ROLE, 1);
    	SettlementAssert.assertThat(indianSettlement)
			.hasGoods(GoodsType.MUSKETS, 0)
			.hasGoods(GoodsType.HORSES, 25);
	}

	// can upgrade mountedBrave to armedBrave
    @Test
	public void canUpgradeRoleAddEquipmentToStore() throws Exception {
		// given
    	setAmount(GoodsType.MUSKETS, 25);
    	setAmount(GoodsType.HORSES, 0);

    	leaveOneUnit();
    	
    	indianSettlement.getUnits().first()
    		.changeRole(Specification.instance.unitRoles.getById(MOUNTED_BRAVE_ROLE));

    	assertThat(indianSettlement)
			.hasOffensiveRolesNumber(1)
			.hasUnitsWithRole(MOUNTED_BRAVE_ROLE, 1);
    	
		// when
		indianSettlement.equipMilitaryRoles();

		// then
    	assertThat(indianSettlement)
			.hasOffensiveRolesNumber(1)
			.hasUnitsWithRole(ARMED_BRAVE_ROLE, 1);
    	SettlementAssert.assertThat(indianSettlement)
			.hasGoods(GoodsType.MUSKETS, 0)
			.hasGoods(GoodsType.HORSES, 25);
	}

    void setAmount(String goodsTypeId, int amount) {
    	indianSettlement.getGoodsContainer().decreaseToZero(goodsTypeId);
    	indianSettlement.getGoodsContainer().increaseGoodsQuantity(goodsTypeId, amount);
    }
    
	private void leaveOneUnit() {
		List<Unit> units = new ArrayList<>(indianSettlement.getUnits().entities());
    	for (Unit u : units) {
    		indianSettlement.removeUnit(u);
    	}
    	indianSettlement.addUnit(units.get(0));
	}

}
