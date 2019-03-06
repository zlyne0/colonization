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

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.SettlementType;
import net.sf.freecol.common.model.Specification;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

/**
 * Represents the type of one of the nations present in the game.
 */
public abstract class NationType extends ObjectWithFeatures {

	public static final String TRADE = "model.nationType.trade";
	public static final String COOPERATION = "model.nationType.cooperation";
	public static final String IMMIGRATION = "model.nationType.immigration";
	public static final String CONQUEST = "model.nationType.conquest";
	
    private static final ObjectFromNodeSetter<NationType, SettlementType> SETTLEMENT_TYPE_NODE_SETTER = new ObjectFromNodeSetter<NationType, SettlementType>() {
        @Override
        public void set(NationType target, SettlementType entity) {
        	target.settlementTypes.add(entity);
        }
		@Override
		public void generateXml(NationType source, ChildObject2XmlCustomeHandler<SettlementType> xmlGenerator) throws IOException {
			xmlGenerator.generateXmlFromCollection(source.settlementTypes.entities());
		}
    };
	
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
    
    public boolean isIndian() {
    	return false;
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

	public String regularSettlementTypePluralMsgKey() {
		return getSettlementRegularType().getId() + ".plural";
	}
	
	public SettlementType getSettlementRegularType() {
		for (SettlementType type : settlementTypes.entities()) {
			if (!type.isCapital()) {
				return type;
			}
		}
		throw new IllegalStateException("can not find regular settlement type for nation type: " + getId());
	}
	
	public SettlementType getSettlementCapitalType() {
		for (SettlementType type : settlementTypes.entities()) {
			if (type.isCapital()) {
				return type;
			}
		}
		throw new IllegalStateException("can not find capital settlement type fro nation type: " + getId());
	}
	
	public static class Xml {
		
		private static final String ATTR_EXTENDS = "extends";
		private static final String ATTR_AGGRESSION = "aggression";
		private static final String ATTR_NUMBER_OF_SETTLEMENTS = "number-of-settlements";

		public static void abstractAddNodes(XmlNodeParser<? extends NationType> nodeParser) {
			nodeParser.addNode(SettlementType.class, SETTLEMENT_TYPE_NODE_SETTER);

			nodeParser.addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
			nodeParser.addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
		}
		
		public static void abstractStartElement(XmlNodeAttributes attr, NationType nt) {
            nt.settlementNumber = attr.getEnumAttribute(SettlementNumber.class, ATTR_NUMBER_OF_SETTLEMENTS, SettlementNumber.AVERAGE);
            nt.aggressionLevel = attr.getEnumAttribute(AggressionLevel.class, ATTR_AGGRESSION);
            nt.parentNationTypeId = attr.getStrAttribute(ATTR_EXTENDS);
            if (nt.parentNationTypeId != null) {
            	NationType parent = Specification.instance.nationTypes.getById(nt.parentNationTypeId);
            	nt.settlementTypes.addAll(parent.settlementTypes);
            	nt.addFeaturesAndOverwriteExisted(parent);
            }
		}
		
		public static void abstractStartWriteAttr(NationType nt, XmlNodeAttributesWriter attr) throws IOException {
			// do not save extends attribute because is difficulty to distinguish object features from parent and child 
			attr.set(ATTR_NUMBER_OF_SETTLEMENTS, nt.settlementNumber);
            attr.set(ATTR_AGGRESSION, nt.aggressionLevel);
		}
	}

}
