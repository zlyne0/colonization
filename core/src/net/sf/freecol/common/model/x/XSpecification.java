package net.sf.freecol.common.model.x;

import org.xml.sax.SAXException;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.Nation;
import net.sf.freecol.common.model.ResourceType;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileType;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.EuropeanNationType;
import net.sf.freecol.common.model.specification.FoundingFather;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.IndianNationType;
import net.sf.freecol.common.model.specification.options.OptionGroup;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class XSpecification implements Identifiable {

	@Override
	public String getId() {
		throw new IllegalStateException("no id for object");
	}
	
	public static class Xml extends XmlNodeParser {
		public Xml() {
//			addNodeForMapIdEntities("tile-types", "tileTypes", TileType.class);
//            addNodeForMapIdEntities("resource-types", "resourceTypes", ResourceType.class);
//            addNodeForMapIdEntities("tileimprovement-types", "tileImprovementTypes", TileImprovementType.class);
//            addNodeForMapIdEntities("unit-types", "unitTypes", UnitType.class);
//            addNodeForMapIdEntities("roles", "unitRoles", UnitRole.class);
//            addNodeForMapIdEntities("european-nation-types", "nationTypes", EuropeanNationType.class);
//            addNodeForMapIdEntities("indian-nation-types", "nationTypes", IndianNationType.class);
//            addNodeForMapIdEntities("nations", "nations", Nation.class);
//            addNodeForMapIdEntities("goods-types", "goodsTypes", GoodsType.class);
//            addNodeForMapIdEntities("building-types", "buildingTypes", BuildingType.class);
//            addNodeForMapIdEntities("founding-fathers", "foundingFathers", FoundingFather.class);
//            addNodeForMapIdEntities("options", "optionGroupEntities", OptionGroup.class);
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
		    Specification specification = Specification.instance;
//		    specification.difficultyLevel = attr.getStrAttribute("difficultyLevel");
//		    specification.clear();
		    nodeObject = specification;
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "freecol-specification";
		}
	}	
	
}
