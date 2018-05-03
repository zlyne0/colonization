package net.sf.freecol.common.model;

import java.io.IOException;

import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class IndianSettlementMissionary {

    protected Unit unit;

    public boolean isMissionaryExpert() {
    	return unit.isExpert();
    }
    
    public static class Xml extends XmlNodeParser<IndianSettlementMissionary> {

        public Xml() {
            addNode(Unit.class, new ObjectFromNodeSetter<IndianSettlementMissionary, Unit>() {
                @Override
                public void set(IndianSettlementMissionary target, Unit entity) {
                    target.unit = entity;
                }

                @Override
                public void generateXml(IndianSettlementMissionary source, ChildObject2XmlCustomeHandler<Unit> xmlGenerator) throws IOException {
                    xmlGenerator.generateXml(source.unit);
                }
            });
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            nodeObject = new IndianSettlementMissionary();
        }

        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "missionary";
        }
    }
}
