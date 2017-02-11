package net.sf.freecol.common.model.specification;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithFeatures;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public abstract class BuildableType extends ObjectWithFeatures {

    private static final ObjectFromNodeSetter<BuildableType, RequiredGoods> REQUIRED_GOODS_NODE_SETTER = new ObjectFromNodeSetter<BuildableType, RequiredGoods>() {
        @Override
        public void set(BuildableType target, RequiredGoods entity) {
        	target.requiredGoods.add(entity);
        }
        @Override
        public Collection<RequiredGoods> get(BuildableType target) {
        	return target.requiredGoods.entities();
        }
    };
	
	
    /** The required population for an ordinary buildable. */
    private static final int DEFAULT_REQUIRED_POPULATION = 1;

    /**
     * The minimum population that a Colony needs in order to build
     * this type.
     */
    protected int requiredPopulation = DEFAULT_REQUIRED_POPULATION;
	
	public final MapIdEntities<RequiredGoods> requiredGoods = new MapIdEntities<RequiredGoods>();

	public BuildableType(String id) {
		super(id);
	}
	
	public int getRequiredPopulation() {
		return requiredPopulation;
	}
	
	public List<RequiredGoods> requiredGoods() {
		return requiredGoods.sortedEntities();
	}
	
	public boolean doesNotNeedGoodsToBuild() {
		return requiredGoods().isEmpty();
	}

	public boolean isBuildingType() {
		return false;
	}

	public boolean isUnitType() {
		return false;
	}
	
	public static class Xml {
		protected static final String TAG_REQUIRED_POPULATION = "required-population";

		public static void abstractAddNodes(XmlNodeParser<? extends BuildableType> nodeParser) {
        	ObjectWithFeatures.Xml.abstractAddNodes(nodeParser);
        	nodeParser.addNode(RequiredGoods.class, REQUIRED_GOODS_NODE_SETTER);
		}
		
		public static void abstractStartElement(XmlNodeAttributes attr, BuildableType bt) {
			ObjectWithFeatures.Xml.abstractStartElement(attr, bt);
            bt.requiredPopulation = attr.getIntAttribute(TAG_REQUIRED_POPULATION, DEFAULT_REQUIRED_POPULATION);
		}
		
		public static void abstractStartWriteAttr(BuildableType bt, XmlNodeAttributesWriter attr) throws IOException {
			ObjectWithFeatures.Xml.abstractStartWriteAttr(bt, attr);
			attr.set(TAG_REQUIRED_POPULATION, bt.requiredPopulation);
		}
	}
}
