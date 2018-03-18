package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.specification.Scope;
import net.sf.freecol.common.model.specification.ScopeAppliable;
import promitech.colonization.Randomizer;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class SettlementPlunderRange {

    protected int probability = 0;
    protected int minimum = 0;
    protected int maximum = 0;
    protected int factor = 0;
    private final List<Scope> scopes = new ArrayList<Scope>();

    public List<Scope> getScopes() {
        return scopes;
    }

	public boolean canApplyTo(ScopeAppliable appliable) {
		for (Scope s : scopes) {
			if (s.isAppliesTo(appliable)) {
				return true;
			}
		}
		return false;
	}
    
	public int randomValue() {
		if (probability >= 100 || probability > 0 && Randomizer.instance().isHappen(probability)) {
			int r = Randomizer.instance().randomInt(minimum, maximum);
			return r * factor;
		}
		return 0;
	}
	
    public static class Xml extends XmlNodeParser<SettlementPlunderRange> {

        private static final String ATTR_FACTOR = "factor";
        private static final String ATTR_MAXIMUM = "maximum";
        private static final String ATTR_MINIMUM = "minimum";
        private static final String ATTR_PROBABILITY = "probability";

        public Xml() {
            addNode(Scope.class, new ObjectFromNodeSetter<SettlementPlunderRange, Scope>() {
                @Override
                public void set(SettlementPlunderRange target, Scope entity) {
                    target.scopes.add(entity);
                }
                
                @Override
                public void generateXml(SettlementPlunderRange source, ChildObject2XmlCustomeHandler<Scope> xmlGenerator) throws IOException {
                    xmlGenerator.generateXmlFromCollection(source.scopes);
                }
            });            
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            SettlementPlunderRange plunderRange = new SettlementPlunderRange();
            plunderRange.probability = attr.getIntAttribute(ATTR_PROBABILITY, 0);
            plunderRange.minimum = attr.getIntAttribute(ATTR_MINIMUM, 0);
            plunderRange.maximum = attr.getIntAttribute(ATTR_MAXIMUM, 0);
            plunderRange.factor = attr.getIntAttribute(ATTR_FACTOR, 0);
            nodeObject = plunderRange;
        }

        @Override
        public void startWriteAttr(SettlementPlunderRange plunderRange, XmlNodeAttributesWriter attr) throws IOException {
            attr.set(ATTR_PROBABILITY, plunderRange.probability, 0);
            attr.set(ATTR_MINIMUM, plunderRange.minimum, 0);
            attr.set(ATTR_MAXIMUM, plunderRange.maximum, 0);
            attr.set(ATTR_FACTOR, plunderRange.factor, 0);
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "plunder";
        }
    }

	@Override
	public String toString() {
		return "SettlementPlunderRange [probability=" + probability + ", minimum=" + minimum + ", maximum=" + maximum
				+ ", factor=" + factor + "]";
	}
}
