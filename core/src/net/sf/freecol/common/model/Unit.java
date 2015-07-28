package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import promitech.colonization.savegame.XmlNodeParser;

public class Unit implements Identifiable {

	private String id;
	private Player owner;
    private UnitType unitType;
    private UnitRole unitRole;
    private List<Unit> containedUnits;
    public Tile tile;

	@Override
	public String getId() {
		return id;
	}
    
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
    	if (!owner.nationType.isEuropean()) {
    		if (UnitType.FREE_COLONIST.equals(unitType.getId())) {
    			return unitType.getId() + unitRole.getRoleSuffix() + ".native.image";
    		}
    	}
    	return unitType.getId() + unitRole.getRoleSuffix() + ".image"; 
    }

	public Player getOwner() {
		return owner;
	}

	public int lineOfSight() {
		return unitType.lineOfSight();
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
            unit.id = getStrAttribute(attributes, "id");
            unit.unitRole = specification.unitRoles.getById(unitRoleStr);
            unit.unitType = unitType;
            
            Tile.Xml tileXmlParser = getParentXmlParser();
            unit.tile = tileXmlParser.tile;
            if (containerUnit == null) {
                containerUnit = unit;
                tileXmlParser.tile.units.add(unit);
            } else {
                secoundLevel = true;
                containerUnit.addUnit(unit);
            }
            
            String ownerStr = getStrAttribute(attributes, "owner");
            Player owner = game.players.getById(ownerStr);
            unit.owner = owner;
            owner.units.add(unit);
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
