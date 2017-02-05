package net.sf.freecol.common.model.x;

import java.io.IOException;

import com.badlogic.gdx.utils.XmlWriter;

import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.TileType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class XTileType extends ObjectWithId {

	public XTileType(String id) {
		super(id);
	}

	
	public static class Xml extends XmlNodeParser<XTileType> {
		public Xml() {
//            addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
//            addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
//            addNode(Production.class, new ObjectFromNodeSetter<TileType, Production>() {
//				@Override
//				public void set(TileType target, Production entity) {
//					target.productionInfo.addProduction(entity);
//				}
//			});
//            addNodeForMapIdEntities("allowedResourceTypes", TileTypeAllowedResource.class);
//            addNode(GenerationValues.class, "generationValues");
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttribute("id");
			boolean isForest = attr.getBooleanAttribute("is-forest");
			
			TileType tileType = new TileType(id, isForest);
//			tileType.basicMoveCost = attr.getIntAttribute("basic-move-cost");
//			tileType.basicWorkTurns = attr.getIntAttribute("basic-work-turns");
//			tileType.elevation = attr.getBooleanAttribute("is-elevation", false);
//			tileType.canSettle = attr.getBooleanAttribute("can-settle", !tileType.isWater());
			nodeObject = tileType; 
		}

		@Override
		public void startWriteAttr(XTileType node, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(node);
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}
		
		public static String tagName() {
		    return "tile-type";
		}
	}
	
}
