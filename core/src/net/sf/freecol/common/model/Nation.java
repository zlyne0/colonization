package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.NationType;
import promitech.colonization.GameResources;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

import com.badlogic.gdx.graphics.Color;

public class Nation extends ObjectWithId {
    
    public static final String UNKNOWN_NATION_ID = "model.nation.unknownEnemy";
    
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
    
    public void updateReferences(Specification specification) {
        if (refId != null) {
            royalNation = specification.nations.getById(refId);
        }
        for (Nation n : specification.nations.entities()) {
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
			NationType type = game.specification.nationTypes.getById(nationTypeStr);
			
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
