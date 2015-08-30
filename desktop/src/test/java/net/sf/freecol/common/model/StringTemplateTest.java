package net.sf.freecol.common.model;

import static org.junit.Assert.*;
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
	
}
