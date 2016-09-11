package net.sf.freecol.common.model;

import java.util.Set;

import com.badlogic.gdx.graphics.Color;

import net.sf.freecol.common.model.specification.NationType;
import promitech.colonization.GameResources;
import promitech.colonization.Randomizer;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;
import promitech.colonization.ui.resources.Messages;

public class Nation extends ObjectWithId {
    
    public static final String UNKNOWN_NATION_ID = "model.nation.unknownEnemy";
    
    
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
		int start = Randomizer.getInstance().randomInt(EUROPEAN_NATIONS.length);
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
	
	public Color getColor() {
		return color;
	}
	
    public Nation getRoyalNation() {
        return royalNation;
    }

    public Nation getRebelNation() {
        return rebelNation;
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
    
    public final boolean isUnknownEnemy() {
        return UNKNOWN_NATION_ID.equals(getId());
    }
    
	public static class Xml extends XmlNodeParser {
		
		@Override
        public void startElement(XmlNodeAttributes attr) {
			String nationTypeStr = attr.getStrAttribute("nation-type");
			String id = attr.getStrAttribute("id");
			NationType type = Specification.instance.nationTypes.getById(nationTypeStr);
			
			String colorStrVal = attr.getStrAttribute("color");
			
            Nation nation = new Nation(id, type);
			nation.color = GameResources.colorFromValue(colorStrVal);
			nation.refId = attr.getStrAttribute("ref");
			
			nodeObject = nation;
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
