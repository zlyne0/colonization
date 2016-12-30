/**
 *  Copyright (C) 2002-2015   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.model.specification;

import java.util.Collections;
import java.util.Set;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.SettlementType;
import net.sf.freecol.common.model.Specification;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

/**
 * Represents the type of one of the nations present in the game.
 */
public abstract class NationType extends ObjectWithFeatures {

	public static enum SettlementNumber { LOW, AVERAGE, HIGH }
    public static enum AggressionLevel { LOW, AVERAGE, HIGH }

    protected SettlementNumber settlementNumber = SettlementNumber.AVERAGE;
    protected AggressionLevel aggressionLevel = AggressionLevel.AVERAGE;
    
    protected boolean european = false;
    public final MapIdEntities<SettlementType> settlementTypes = new MapIdEntities<SettlementType>();
	protected boolean abstractType = false;
	protected String parentNationTypeId;

    public NationType(String id) {
		super(id);
	}

    public boolean isEuropean() {
    	return european;
    }
    
    public String toString() {
    	return "nationType: " + getId();
    }

	public SettlementNumber getSettlementNumber() {
		return settlementNumber;
	}
    
    public abstract boolean isREF();
    
	public Set<String> getRegionNames() {
		return Collections.emptySet();
	}

	public SettlementType getSettlementRegularType() {
		for (SettlementType type : settlementTypes.entities()) {
			if (!type.isCapital()) {
				return type;
			}
		}
		return null;
	}
	
	public SettlementType getSettlementCapitalType() {
		for (SettlementType type : settlementTypes.entities()) {
			if (type.isCapital()) {
				return type;
			}
		}
		return null;
	}
	
	public static class Xml {
		
		public static void abstractAddNodes(XmlNodeParser nodeParser) {
			nodeParser.addNodeForMapIdEntities("settlementTypes", SettlementType.class);
		}
		
		public static void abstractStartElement(XmlNodeAttributes attr, NationType nt) {
            nt.settlementNumber = attr.getEnumAttribute(SettlementNumber.class, "number-of-settlements", SettlementNumber.AVERAGE);
            nt.aggressionLevel = attr.getEnumAttribute(AggressionLevel.class, "aggression");
            nt.parentNationTypeId = attr.getStrAttribute("extends");
            if (nt.parentNationTypeId != null) {
            	NationType parent = Specification.instance.nationTypes.getById(nt.parentNationTypeId);
            	nt.settlementTypes.addAll(parent.settlementTypes);
            	nt.addFeaturesAndOverwriteExisted(parent);
            }
		}
	}

}
