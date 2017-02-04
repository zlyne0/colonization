package promitech.colonization.savegame;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Collection;

import com.badlogic.gdx.utils.XmlWriter;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.MapIdEntities;

public class SaveGameCreator {

	private XmlWriter xml;
	private StringWriter strWriter;
	
	public SaveGameCreator() {
		strWriter = new StringWriter();
		xml = new XmlWriter(strWriter);
	}

	public void generateXmlFrom(Object obj) throws IOException {
		generateXmlFromObj(obj, null, null);
		
		System.out.println("xml = \n" + strWriter);
	}
	
	protected void generateXmlFromObj(Object obj, XmlTagMetaData metaData, XmlNodeParser xmlParser) throws IOException {
		if (metaData == null || !metaData.entityClass.equals(obj.getClass())) {
			metaData = new XmlTagMetaData((Class<? extends Identifiable>)obj.getClass());
			xmlParser = metaData.createXmlParser();
		}
		if (xmlParser == null) {
			xmlParser = metaData.createXmlParser();
		}
		xml.element(metaData.tagName);
		xmlParser.startWriteAttr(obj, xml);
		
		saveChildren(obj, xmlParser);
		
		xml.pop();
	}
	
	protected void saveChildren(Object obj, XmlNodeParser xmlParser) throws IOException {
		for (XmlTagMetaData tagMetaData : (Collection<XmlTagMetaData>)xmlParser.childrenNodeParsers()) {
			if (tagMetaData instanceof XmlTagMapIdEntitiesMetaData) {
				saveMapEntityObj(obj, (XmlTagMapIdEntitiesMetaData)tagMetaData);
			} else {
				Object childObj = getValueByFieldName(obj, tagMetaData.targetFieldName);
				if (childObj != null) {
					generateXmlFromObj(childObj, tagMetaData, null);
				}
			}
		}
	}

	protected void saveMapEntityObj(Object parentObj, final XmlTagMapIdEntitiesMetaData mapEntityMetaData) throws IOException {
		MapIdEntities<Identifiable> entityMap = (MapIdEntities<Identifiable>)getValueByFieldName(parentObj, mapEntityMetaData.fieldName);
		
		if (entityMap.size() > 0) {
			if (mapEntityMetaData.isPossesWrapperTag()) {
				xml.element(mapEntityMetaData.getTagName());
				XmlTagMetaData entityMetaData = new XmlTagMetaData(mapEntityMetaData.entityClass);
				XmlNodeParser entityXmlParser = mapEntityMetaData.createEntityXmlParser();
				for (Identifiable entity : entityMap.entities()) {
					generateXmlFromObj(entity, entityMetaData, entityXmlParser);
				}
				xml.pop();
			} else {
				XmlNodeParser mapEntityXmlParser = mapEntityMetaData.createEntityXmlParser();
				for (Identifiable entity : entityMap.entities()) {
					generateXmlFromObj(entity, mapEntityMetaData, mapEntityXmlParser);
				}
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
