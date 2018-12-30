package net.sf.freecol.common.model;

import java.io.IOException;

import net.sf.freecol.common.model.specification.RandomRange;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class SettlementTypeGift extends RandomRange {
    public static class Xml extends XmlNodeParser<SettlementTypeGift> {

        public Xml() {
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
        	SettlementTypeGift giftRange = new SettlementTypeGift();
        	RandomRange.Xml.startElement(giftRange, attr);
            nodeObject = giftRange;
        }

        @Override
        public void startWriteAttr(SettlementTypeGift giftRange, XmlNodeAttributesWriter attr) throws IOException {
        	RandomRange.Xml.startWriteAttr(giftRange, attr);
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "gifts";
        }
    }
}
