package promitech.colonization.savegame;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Collection;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.XmlWriter;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.SavedGame;

public class SaveGameCreator {

	class ChildObjectXmlGenerator implements ObjectFromNodeSetter.ChildObject2XmlCustomeHandler<Object>, Poolable {
		XmlNodeParser childXmlParser;
		XmlTagMetaData tagMetaData;
		
		@Override
		public void generateXml(Object obj) throws IOException {
			generateXmlFromObj(obj, tagMetaData, childXmlParser);
		}
		@Override
		public void generateXmlFromCollection(Collection<Object> objs) throws IOException {
			for (Object obj : objs) {
				generateXmlFromObj(obj, tagMetaData, childXmlParser);
			}
		}

		@Override
		public void reset() {
			this.childXmlParser = null;
			this.tagMetaData = null;
		}

	}
	
	private final Pool<ChildObjectXmlGenerator> customeXmlGeneratorPool = new Pool<SaveGameCreator.ChildObjectXmlGenerator>() {
		@Override
		protected ChildObjectXmlGenerator newObject() {
			return new ChildObjectXmlGenerator();
		}
	};
	
	private XmlWriter xml;
	private final XmlNodeAttributesWriter attrWriter;
	
	
	public SaveGameCreator(Writer writer) {
		xml = new XmlWriter(writer);
		attrWriter = new XmlNodeAttributesWriter(xml);
	}

	public void generateXmlFrom(SavedGame savedGame) throws IOException {
		XmlNodeParser.game = savedGame.game;
		generateXmlFromObj(savedGame, null, null);
		XmlNodeParser.game = null;
		
		customeXmlGeneratorPool.clear();
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
		xmlParser.startWriteAttr(obj, attrWriter);
		
		saveChildren(obj, xmlParser);
		
		xml.pop();
	}
	
	protected void saveChildren(Object objWithChildren, XmlNodeParser xmlParser) throws IOException {
		for (XmlTagMetaData tagMetaData : (Collection<XmlTagMetaData>)xmlParser.childrenNodeParsers()) {
			if (tagMetaData instanceof XmlTagMapIdEntitiesMetaData) {
				saveMapEntityObj(objWithChildren, (XmlTagMapIdEntitiesMetaData)tagMetaData);
			} else {
				if (tagMetaData.targetFieldName == null && tagMetaData.setter != null) {
					XmlNodeParser childXmlParser = tagMetaData.createXmlParser();
					ChildObjectXmlGenerator childObjectXmlGenerator = customeXmlGeneratorPool.obtain();
					childObjectXmlGenerator.childXmlParser = childXmlParser;
					childObjectXmlGenerator.tagMetaData = tagMetaData;
					tagMetaData.setter.generateXml(objWithChildren, childObjectXmlGenerator);
					customeXmlGeneratorPool.free(childObjectXmlGenerator);
				} else {
					Object childObj = getValueByFieldName(objWithChildren, tagMetaData.targetFieldName);
					if (childObj != null) {
						generateXmlFromObj(childObj, tagMetaData, null);
					}
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
					if (entity.getClass().equals(entityMetaData.entityClass)) {
						generateXmlFromObj(entity, entityMetaData, entityXmlParser);
					}
				}
				xml.pop();
			} else {
				XmlNodeParser<?> mapEntityXmlParser = mapEntityMetaData.createEntityXmlParser();
				for (Identifiable entity : entityMap.entities()) {
					if (entity.getClass().equals(mapEntityMetaData.entityClass)) {
						generateXmlFromObj(entity, mapEntityMetaData, mapEntityXmlParser);
					}
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
