package net.sf.freecol.common.model.map;

import java.io.IOException;

import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class Region extends ObjectWithId {

	public static final String ARCTIC = "model.region.arctic";
	public static final String ANTARCTIC = "model.region.antarctic";
	
	public static enum RegionType {
        OCEAN,
        COAST,
        LAKE,
        RIVER,
        LAND,
        MOUNTAIN,
        DESERT;
	}	
	
	private RegionType type;
	
    /**
     * Whether this Region is claimable.  Ocean Regions and non-leaf
     * Regions should not be claimable.
     */
    protected boolean claimable = false;

    /**
     * Whether this Region is discoverable.  The Eastern Ocean regions
     * should not be discoverable.  In general, non-leaf regions should
     * not be discoverable.  The Pacific Ocean is an exception however,
     * unless players start there.
     */
    protected boolean discoverable = false;


    /** Whether the Region is already discovered when the game starts. */
    protected boolean prediscovered = false;

    /**
     * How much discovering this Region contributes to your score.
     * This should be zero unless the Region is discoverable.
     */
    protected int scoreValue = 0;
	
	public Region(String id, RegionType type) {
		super(id);
		this.type = type;
	}
	
	public void setPrediscovered(boolean prediscovered) {
		this.prediscovered = prediscovered;
	}
	
	public static class Xml extends XmlNodeParser<Region> {

		private static final String ATTR_SCORE_VALUE = "scoreValue";
		private static final String ATTR_DISCOVERABLE = "discoverable";
		private static final String ATTR_CLAIMABLE = "claimable";
		private static final String ATTR_PREDISCOVERED = "prediscovered";
		private static final String ATTR_TYPE = "type";
		private static final String ATTR_NAME_KEY = "nameKey";

		@Override
		public void startElement(XmlNodeAttributes attr) {
			Region r = new Region(
				attr.getStrAttribute(ATTR_NAME_KEY), 
				attr.getEnumAttribute(RegionType.class, ATTR_TYPE)
			);
			r.prediscovered = attr.getBooleanAttribute(ATTR_PREDISCOVERED, false);
			r.claimable = attr.getBooleanAttribute(ATTR_CLAIMABLE, false);
			r.discoverable = attr.getBooleanAttribute(ATTR_DISCOVERABLE, false);
			r.scoreValue = attr.getIntAttribute(ATTR_SCORE_VALUE, 0);
			nodeObject = r;
		}

		@Override
		public void startWriteAttr(Region r, XmlNodeAttributesWriter attr) throws IOException {
			attr.set(ATTR_NAME_KEY, r.getId());
			attr.set(ATTR_TYPE, r.type);
			
			attr.set(ATTR_PREDISCOVERED, r.prediscovered);
			attr.set(ATTR_CLAIMABLE, r.claimable);
			attr.set(ATTR_DISCOVERABLE, r.discoverable);
			attr.set(ATTR_SCORE_VALUE, r.scoreValue);
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "region";
		}
		
	}
}
