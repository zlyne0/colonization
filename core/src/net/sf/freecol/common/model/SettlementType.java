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

package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;


/**
 * The various types of settlements in the game.
 */
public class SettlementType extends ObjectWithFeatures {

    /** Whether this SettlementType is a capital. */
    private boolean capital = false;

    /** How many tiles this SettlementType can see. */
    private int visibleRadius = 2;

    /** How many tiles this SettlementType can claim. */
    private int claimableRadius = 1;

    /**
     * The extra radius beyond the claimableRadius where wandering
     * units may claim as yet unclaimed tiles.
     */
    private int extraClaimableRadius = 2;

    /** How far units from this SettlementType may roam. */
    private int wanderingRadius = 4;

    /** The minimum number of units for this SettlementType. */
    private int minimumSize = 3;

    /** The maximum number of units for this SettlementType. */
    private int maximumSize = 10;

    /** The minimum number of tiles to grow this SettlementType. */
    private int minimumGrowth = 1;

    /** The maximum number of tiles to grown this SettlementType. */
    private int maximumGrowth = 10;

    /**
     * The general trade bonus, roughly proportional to the settlement
     * size and general sophistication.
     */
    private int tradeBonus = 1;

    /** The threshold at which a new convert occurs. */
    private int convertThreshold = 100;

    private List<SettlementPlunderRange> plunderRanges = new ArrayList<SettlementPlunderRange>();
    
    public SettlementType(String id) {
		super(id);
	}

    /**
     * Is this a capital settlement type?
     *
     * @return True if this is a capital.
     */
    public final boolean isCapital() {
        return capital;
    }

    /**
     * Get the minimum size of this settlement type.
     *
     * @return The minimum settlement size.
     */
    public final int getMinimumSize() {
        return minimumSize;
    }

    /**
     * Get the maximum size of this settlement type.
     *
     * @return The maximum settlement size.
     */
    public final int getMaximumSize() {
        return maximumSize;
    }

    /**
     * Get the visible radius of this settlement type.
     *
     * @return The visible radius.
     */
    public final int getVisibleRadius() {
        return visibleRadius;
    }

    /**
     * Get the claimable radius of this settlement type.
     *
     * @return The claimable radius.
     */
    public final int getClaimableRadius() {
        return claimableRadius;
    }

    /**
     * Get the extra claimable radius.
     *
     * @return The extra claimable radius.
     */
    public final int getExtraClaimableRadius() {
        return extraClaimableRadius;
    }

    /**
     * Get the wandering radius for this settlement type.
     *
     * @return The wandering radius.
     */
    public final int getWanderingRadius() {
        return wanderingRadius;
    }

    /**
     * Get the minimum growth value.
     *
     * @return The minimum number of tiles to try to grow this
     *     settlement type by.
     */
    public final int getMinimumGrowth() {
        return minimumGrowth;
    }

    /**
     * Get the maximum growth value.
     *
     * @return The maximum number of tiles to try to grow this
     *     settlement type by.
     */
    public final int getMaximumGrowth() {
        return maximumGrowth;
    }

    /**
     * Gets the trade bonus.
     *
     * @return The general bonus to trade.
     */
    public final int getTradeBonus() {
        return tradeBonus;
    }

    /**
     * Gets the convert threshold for this settlement.
     *
     * @return The convert threshold.
     */
    public int getConvertThreshold() {
        return convertThreshold;
    }

    public List<SettlementPlunderRange> getPlunderRanges() {
        return plunderRanges;
    }

	public int plunderGold(Unit attacker) {
		SettlementPlunderRange range = plunderGoldRange(attacker);
		if (range != null) {
			return range.randomValue();
		}
		return 0;
	}
    
	public SettlementPlunderRange plunderGoldRange(Unit attacker) {
		for (SettlementPlunderRange range : plunderRanges) {
			if (range.canApplyTo(attacker)) {
				return range;
			}
		}
		return null;
	}
	
    public static class Xml extends XmlNodeParser<SettlementType> {

    	private static final String ATTR_CONVERT_THRESHOLD = "convertThreshold";
		private static final String ATTR_TRADE_BONUS = "tradeBonus";
		private static final String ATTR_MAXIMUM_GROWTH = "maximumGrowth";
		private static final String ATTR_MINIMUM_GROWTH = "minimumGrowth";
		private static final String ATTR_WANDERING_RADIUS = "wanderingRadius";
		private static final String ATTR_EXTRA_CLAIMABLE_RADIUS = "extraClaimableRadius";
		private static final String ATTR_CLAIMABLE_RADIUS = "claimableRadius";
		private static final String ATTR_VISIBLE_RADIUS = "visibleRadius";
		private static final String ATTR_MAXIMUM_SIZE = "maximumSize";
		private static final String ATTR_MINIMUM_SIZE = "minimumSize";
		private static final String ATTR_CAPITAL = "capital";

		public Xml() {
			ObjectWithFeatures.Xml.abstractAddNodes(this);
			
			addNode(SettlementPlunderRange.class, new ObjectFromNodeSetter<SettlementType, SettlementPlunderRange>() {
                @Override
                public void set(SettlementType target, SettlementPlunderRange entity) {
                    target.plunderRanges.add(entity);
                }
                @Override
                public void generateXml(SettlementType source, ChildObject2XmlCustomeHandler<SettlementPlunderRange> xmlGenerator) throws IOException {
                    xmlGenerator.generateXmlFromCollection(source.plunderRanges);
                }
            });
    	}
    	
        @Override
        public void startElement(XmlNodeAttributes attr) {
            SettlementType settlementType = new SettlementType(attr.getStrAttribute(ATTR_ID));
            
            settlementType.capital = attr.getBooleanAttribute(ATTR_CAPITAL, false);
            settlementType.minimumSize = attr.getIntAttribute(ATTR_MINIMUM_SIZE, 6);
            settlementType.maximumSize = attr.getIntAttribute(ATTR_MAXIMUM_SIZE, 8);
            settlementType.visibleRadius = attr.getIntAttribute(ATTR_VISIBLE_RADIUS, 2); 
            settlementType.claimableRadius = attr.getIntAttribute(ATTR_CLAIMABLE_RADIUS, 1); 
            settlementType.extraClaimableRadius = attr.getIntAttribute(ATTR_EXTRA_CLAIMABLE_RADIUS, 2); 
            settlementType.wanderingRadius = attr.getIntAttribute(ATTR_WANDERING_RADIUS, 4); 
            settlementType.minimumGrowth = attr.getIntAttribute(ATTR_MINIMUM_GROWTH, 1); 
            settlementType.maximumGrowth = attr.getIntAttribute(ATTR_MAXIMUM_GROWTH, 8); 
            settlementType.tradeBonus = attr.getIntAttribute(ATTR_TRADE_BONUS, 2); 
            settlementType.convertThreshold = attr.getIntAttribute(ATTR_CONVERT_THRESHOLD, 100);
            
            nodeObject = settlementType;
        }

        @Override
        public void startWriteAttr(SettlementType settlementType, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(settlementType);

        	attr.set(ATTR_CAPITAL, settlementType.capital);
        	attr.set(ATTR_MINIMUM_SIZE, settlementType.minimumSize);
        	attr.set(ATTR_MAXIMUM_SIZE, settlementType.maximumSize);
        	attr.set(ATTR_VISIBLE_RADIUS, settlementType.visibleRadius);
        	attr.set(ATTR_CLAIMABLE_RADIUS, settlementType.claimableRadius);
        	attr.set(ATTR_EXTRA_CLAIMABLE_RADIUS, settlementType.extraClaimableRadius);
        	attr.set(ATTR_WANDERING_RADIUS, settlementType.wanderingRadius);
        	attr.set(ATTR_MINIMUM_GROWTH, settlementType.minimumGrowth);
        	attr.set(ATTR_MAXIMUM_GROWTH, settlementType.maximumGrowth);
        	attr.set(ATTR_TRADE_BONUS, settlementType.tradeBonus);
        	attr.set(ATTR_CONVERT_THRESHOLD, settlementType.convertThreshold);
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "settlement";
        }
    }
}
