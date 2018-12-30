package net.sf.freecol.common.model;

import static net.sf.freecol.common.util.StringUtils.lastPart;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import com.badlogic.gdx.graphics.Color;

import net.sf.freecol.common.model.specification.NationType;
import promitech.colonization.GameResources;
import promitech.colonization.Randomizer;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;
import promitech.colonization.ui.resources.Messages;

public class Nation extends ObjectWithId {
    
	private static final String RULER_SUFFIX = ".ruler";
	
    private static final String[] EUROPEAN_NATIONS = {
		// Original Col1 nations
		"dutch", "english", "french", "spanish",
		// FreeCol's additions
		"danish", "portuguese", "swedish", "russian",
		// other European non-player nations
		"austrian", "german", "prussian", "turkish"
    };
	
	public static String getRandomNonPlayerNationNameKey(Game game) {
		Set<String> gameNationIds = game.getEuropeanNationIds();
		int start = Randomizer.instance().randomInt(EUROPEAN_NATIONS.length);
		for (int index = 0; index < EUROPEAN_NATIONS.length; index++) {
			String nationId = "model.nation." + EUROPEAN_NATIONS[(start + index) % EUROPEAN_NATIONS.length];
			if (!gameNationIds.contains(nationId)) {
				return Messages.nameKey(nationId);
			}
		}
		// this should never happen
		return "";
	}
    
	public final NationType nationType;
	private Color color;
	private String refId;
	private Nation royalNation;
    private Nation rebelNation;

	public Nation(String id, NationType nationType) {
	    super(id);
		this.nationType = nationType;
	}
	
	public String toString() {
		return "id = " + id;
	}
	
    public String rulerNameKey() {
        return id + RULER_SUFFIX;
    }
	
	public Color getColor() {
		return color;
	}
	
    public Nation getRoyalNation() {
        return royalNation;
    }

    public Nation getRebelNation() {
        return rebelNation;
    }
    
    public String getNationNameKey() {
        return lastPart(getId(), ".").toUpperCase(Locale.US);
    }
    
    public void updateReferences() {
        if (refId != null) {
            royalNation = Specification.instance.nations.getById(refId);
        }
        for (Nation n : Specification.instance.nations.entities()) {
            if (equalsId(n.refId)) {
                rebelNation = n;
            }
        }
    }
    
	public static class Xml extends XmlNodeParser<Nation> {
		
		private static final String ATTR_REF = "ref";
		private static final String ATTR_COLOR = "color";
		private static final String ATTR_NATION_TYPE = "nation-type";

		@Override
        public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttribute(ATTR_ID);
			String nationTypeStr = attr.getStrAttribute(ATTR_NATION_TYPE);
			NationType type = Specification.instance.nationTypes.getById(nationTypeStr);
			
			String colorStrVal = attr.getStrAttribute(ATTR_COLOR);
			
            Nation nation = new Nation(id, type);
			nation.color = GameResources.colorFromValue(colorStrVal);
			nation.refId = attr.getStrAttribute(ATTR_REF);
			
			nodeObject = nation;
		}
		
		@Override
		public void startWriteAttr(Nation nation, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(nation);
			attr.set(ATTR_NATION_TYPE, nation.nationType);
			attr.set(ATTR_COLOR, GameResources.colorToStr(nation.color));
			attr.set(ATTR_REF, nation.refId);
		}
		
		@Override
		public String getTagName() {
		    return tagName();
		}
		
        public static String tagName() {
            return "nation";
        }
	}

}
