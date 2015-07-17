package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import promitech.colonization.savegame.XmlNodeParser;

public class Unit {

	private Player owner;
    private UnitType unitType;
    private UnitRole unitRole;
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
    
    public String resourceImageKey() {
    	// TODO: there is problem with two units
    	if (!owner.nationType.isEuropean()) {
    		if (UnitType.FREE_COLONIST.equals(unitType.getId())) {
    			return unitType.getId() + unitRole.getRoleSuffix() + ".native.image";
    		}
    	}
    	// model.unit.freeColonist.soldier.native.image
    	// model.unit.freeColonist.native.image
    	return unitType.getId() + unitRole.getRoleSuffix() + ".image"; 
//    	return unitType.getId() + unitRole.getRoleSuffix()
//	        + ((owner.nationType.isEuropean()) ? "" : ".native")
//	        + ".image";
    }

	public Player getOwner() {
		return owner;
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
            String unitRoleStr = getStrAttribute(attributes, "role");
            
            UnitType unitType = specification.unitTypes.getById(unitTypeStr);
            Unit unit = new Unit();
            unit.unitRole = specification.unitRoles.getById(unitRoleStr);
            unit.unitType = unitType;
            
            if (containerUnit == null) {
                containerUnit = unit;
                Tile.Xml tileXmlParser = getParentXmlParser();
                tileXmlParser.tile.addUnit(unit);
            } else {
                secoundLevel = true;
                containerUnit.addUnit(unit);
            }
            
            String ownerStr = getStrAttribute(attributes, "owner");
            unit.owner = game.players.getById(ownerStr);
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
