package net.sf.freecol.common.model.specification;

import java.io.IOException;

import promitech.colonization.Randomizer;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;

public class RandomRange {

    protected int probability = 0;
    protected int minimum = 0;
    protected int maximum = 0;
    protected int factor = 0;

	public int randomValue() {
		if (probability >= 100 || probability > 0 && Randomizer.instance().isHappen(probability)) {
			int r = Randomizer.instance().randomInt(minimum, maximum);
			return r * factor;
		}
		return 0;
	}
	
	public static class Xml {
        private static final String ATTR_FACTOR = "factor";
        private static final String ATTR_MAXIMUM = "maximum";
        private static final String ATTR_MINIMUM = "minimum";
        private static final String ATTR_PROBABILITY = "probability";
        
        public static void startElement(RandomRange range, XmlNodeAttributes attr) {
        	range.probability = attr.getIntAttribute(Xml.ATTR_PROBABILITY, 0);
        	range.minimum = attr.getIntAttribute(Xml.ATTR_MINIMUM, 0);
        	range.maximum = attr.getIntAttribute(Xml.ATTR_MAXIMUM, 0);
        	range.factor = attr.getIntAttribute(Xml.ATTR_FACTOR, 0);
        }
        
        public static void startWriteAttr(RandomRange range, XmlNodeAttributesWriter attr) throws IOException {
        	attr.set(Xml.ATTR_PROBABILITY, range.probability, 0);
        	attr.set(Xml.ATTR_MINIMUM, range.minimum, 0);
        	attr.set(Xml.ATTR_MAXIMUM, range.maximum, 0);
        	attr.set(Xml.ATTR_FACTOR, range.factor, 0);
        }
	}

	@Override
	public String toString() {
		return "RandomRange [probability=" + probability + ", minimum=" + minimum + ", maximum=" + maximum + ", factor="
				+ factor + "]";
	}
	
}
