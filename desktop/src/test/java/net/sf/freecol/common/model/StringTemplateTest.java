package net.sf.freecol.common.model;

import static org.junit.Assert.*;

import java.util.Locale;

import net.sf.freecol.common.model.specification.Goods;

import org.junit.BeforeClass;
import org.junit.Test;

import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

public class StringTemplateTest {

    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
        Locale.setDefault(Locale.US);
        Messages.instance().load();
    }
	
    @Test
	public void unitEquipmentLabel() throws Exception {
		// given
    	Unit unit = new Unit("unit:1");
    	unit.roleCount = 3;
    	Goods goods = new Goods("model.goods.tools", 20);
    	
    	StringTemplate extra;
    	
    	extra = StringTemplate.label("");
    	extra.addStringTemplate(StringTemplate.template("model.goods.goodsAmount")
				.addName("%goods%", goods)
				.addAmount("%amount%", goods.getAmount() *  unit.roleCount)
		);
    	
    	// when
    	String message = Messages.message(extra);
    	
    	// then
    	assertEquals("60 Tools", message);
	}
	
    @Test
	public void buildableNeedsGoods() throws Exception {
		// given
    	int amount = 200;
    	String goodsTypeId = "model.goods.tools";
    	String colonyName = "New Amsterdam";
    	String buildingId = "model.building.schoolhouse";
    	
    	StringTemplate st = StringTemplate.template("model.colony.buildableNeedsGoods")
			.addName("%goodsType%", goodsTypeId)
			.addAmount("%amount%", amount)
			.add("%colony%", colonyName)
			.addName("%buildable%", buildingId);
    	// when
    	String str = Messages.message(st);

    	// then
		assertEquals("200 Tools are missing to build Schoolhouse in New Amsterdam, Your Excellency.", str);
	}
    
}
