package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import promitech.colonization.savegame.XmlNodeParser;

public class Unit {

    private UnitType unitType;
    
    private List<Unit> containedUnits;
    
    public String toString() {
        String st = "unitType = " + unitType;
        if (containedUnits != null) {
            st += ", contains[";
            for (Unit u : containedUnits) {
                st += u.unitType + ", "; 
            }
            st += "]";
        }
        return st;
    }

    public void addUnit(Unit containerUnit) {
        if (containedUnits == null) {
            containedUnits = new ArrayList<Unit>();
        }
        containedUnits.add(containerUnit);
    }
    
    public static class Xml extends XmlNodeParser {
        
        private boolean secoundLevel = false;
        private Unit containerUnit = null;
        
        public Xml(XmlNodeParser parent) {
            super(parent);
        }

        @Override
        public void startElement(String qName, Attributes attributes) {
            String unitTypeStr = getStrAttribute(attributes, "unitType");
            
            UnitType unitType = specification.unitTypes.getById(unitTypeStr);
            Unit unit = new Unit();
            unit.unitType = unitType;
            
            if (containerUnit == null) {
                containerUnit = unit;
            } else {
                secoundLevel = true;
                containerUnit.addUnit(unit);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (secoundLevel) {
                secoundLevel = false;
            } else {
                containerUnit = null;
            }
        }
        
        @Override
        public String getTagName() {
            return "unit";
        }
    }

}
