package net.sf.freecol.common.model.map;

import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
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
	
	public static class Xml extends XmlNodeParser {

		@Override
		public void startElement(XmlNodeAttributes attr) {
			Region r = new Region(
				attr.getStrAttribute("nameKey"), 
				attr.getEnumAttribute(RegionType.class, "type")
			);
			r.prediscovered = attr.getBooleanAttribute("prediscovered", false);
			r.claimable = attr.getBooleanAttribute("claimable", false);
			r.discoverable = attr.getBooleanAttribute("discoverable", false);
			r.scoreValue = attr.getIntAttribute("scoreValue", 0);
			nodeObject = r;
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
