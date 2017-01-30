package net.sf.freecol.common.model.x;

import org.xml.sax.SAXException;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.map.Region;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class XMap implements Identifiable {

	@Override
	public String getId() {
		throw new IllegalStateException("no id for object");
	}

	public static class Xml extends XmlNodeParser {
		
		public Xml() {
//			addNode(Tile.class, new ObjectFromNodeSetter<Map,Tile>() {
//                @Override
//                public void set(Map target, Tile entity) {
//                    Tile tile = (Tile)entity;
//                    target.createTile(tile.x, tile.y, tile);
//                }
//            });
//			addNodeForMapIdEntities("regions", Region.class);
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "map";
		}
		
	}
	
	
}
