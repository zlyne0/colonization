package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.Goods;
import net.sf.freecol.common.util.StringUtils;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class UnitRole extends ObjectWithFeatures {
	public static final String DEFAULT_ROLE_ID = "model.role.default";	
	
    public static String getRoleSuffix(String roleId) {
        return StringUtils.lastPart(roleId, ".");
    }
	
    public static boolean isDefaultRoleId(String roleId) {
        return DEFAULT_ROLE_ID.equals(roleId);
    }
	
	private final String roleSuffix;
	protected String expertUnitTypeId;
	protected MapIdEntities<Goods> requiredGoods = new MapIdEntities<Goods>();
	
	public UnitRole(String id) {
		super(id);
		
		if (isDefaultRoleId(id)) {
			this.roleSuffix = ""; 
		} else {
			this.roleSuffix = "." + StringUtils.lastPart(id, ".");
		}
	}
	
    public boolean isDefaultRole() {
        return isDefaultRoleId(getId());
    }
	
	public String getRoleSuffix() {
		return roleSuffix;
	}
	
    public boolean isOffensive() {
        return hasModifier(Modifier.OFFENCE);
    }
	
	public static class Xml extends XmlNodeParser {
		public Xml() {
            addNodeForMapIdEntities("modifiers", Modifier.class);
            addNodeForMapIdEntities("abilities", Ability.class);
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
			String idStr = attr.getStrAttribute("id");
			UnitRole ur = new UnitRole(idStr);
			ur.expertUnitTypeId = attr.getStrAttribute("expertUnit");
			nodeObject = ur;
		}

		@Override
		public void startReadChildren(XmlNodeAttributes attr) {
			if (attr.isQNameEquals("required-goods")) {
				Goods goods = new Goods(attr.getStrAttribute("id"), attr.getIntAttribute("value"));
				((UnitRole)nodeObject).requiredGoods.add(goods);
			}
		}
		
		@Override
		public String getTagName() {
		    return tagName();
		}
		
        public static String tagName() {
            return "role";
        }
	}
}
