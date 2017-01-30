package promitech.colonization.savegame;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.badlogic.gdx.utils.XmlWriter;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.x.XGame;
import net.sf.freecol.common.model.x.XMap;
import net.sf.freecol.common.model.x.XSavedGame;
import net.sf.freecol.common.model.x.XSpecification;

public class SaveGameTest {

	XmlWriter xml;
	StringWriter strWriter;

	@Before
	public void setup() {
		strWriter = new StringWriter();
		xml = new XmlWriter(strWriter);
	}
	
	@Test
	public void canCreateSaveGameXml() throws Exception {
		// given
		XSavedGame savedGame = new XSavedGame();
		savedGame.game = new XGame();
		savedGame.game.setMap(new XMap());
		savedGame.game.setSpecification(new XSpecification());
		
		
		// when
		saveGame(savedGame, null, null);
		
		// then
		System.out.println("xml = \n" + strWriter);
	}
	
	public void saveGame(Object obj, XmlTagMetaData metaData, XmlNodeParser xmlParser) throws IOException {
		if (metaData == null || !metaData.entityClass.equals(obj.getClass())) {
			metaData = new XmlTagMetaData((Class<? extends Identifiable>)obj.getClass(), null);
			System.out.println("metaData = " + metaData);
			xmlParser = metaData.createXmlParser();
		}
		if (xmlParser == null) {
			xmlParser = metaData.createXmlParser();
		}
		xml.element(metaData.tagName);
		xmlParser.startWriteAttr(xml);
		
		saveChildren(obj, xmlParser);
		
		xml.pop();
	}
	
	public void saveChildren(Object obj, XmlNodeParser xmlParser) throws IOException {
		System.out.println("children.size " + xmlParser.nodeMetaData.size() );
		for (Entry<String, XmlTagMetaData> entry : xmlParser.nodeMetaData.entrySet()) {
			System.out.println("" + entry.getKey() + ", " + entry.getValue());
			
			Object childObj = getValueByFieldName(obj, entry.getValue().targetFieldName);
			if (childObj != null) {
				System.out.println("childObj = " + childObj);
				saveGame(childObj, entry.getValue(), null);
			}
		}
	}

	private Object getValueByFieldName(Object obj, String targetFieldName) {
		try {
            Field field = obj.getClass().getDeclaredField(targetFieldName);
            field.setAccessible(true);
            return field.get(obj);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
}
