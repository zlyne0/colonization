package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.Goods;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.util.StringUtils;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class UnitRole extends ObjectWithFeatures {
	public static final String DEFAULT_ROLE_ID = "model.role.default";
	public static final String CAVALRY_ROLE_ID = "model.role.cavalry";
	public static final String INFANTRY_ROLE_ID = "model.role.infantry";
	
	public static final String SOLDIER = "model.role.soldier";
	public static final String DRAGOON = "model.role.dragoon";
	
    public static String getRoleSuffix(String roleId) {
        return StringUtils.lastPart(roleId, ".");
    }
	
    public static boolean isDefaultRoleId(String roleId) {
        return DEFAULT_ROLE_ID.equals(roleId);
    }
	
	private final String roleSuffix;
	protected String expertUnitTypeId;
	public final MapIdEntities<Goods> requiredGoods = new MapIdEntities<Goods>();
	private String downgradeRoleId;
	private int maximumCount = 1;
	
	public UnitRole(String id) {
		super(id);
		
		if (isDefaultRoleId(id)) {
			this.roleSuffix = ""; 
		} else {
			this.roleSuffix = getRoleSuffix(id);
		}
	}
	
    public boolean isDefaultRole() {
        return isDefaultRoleId(getId());
    }
	
	public String getRoleSuffix() {
		return roleSuffix;
	}
	
	public String getRoleSuffixWithDefault() {
	    if (isDefaultRole()) {
	        return "default";
	    }
	    return roleSuffix;
	}
	
    public boolean isOffensive() {
        return hasModifier(Modifier.OFFENCE);
    }
	
	public boolean isCompatibleWith(UnitRole role) {
		if (role == null) {
			return false;
		}
		return this.getId().equals(role.getId()) 
				|| role.getId().equals(this.downgradeRoleId) 
				|| this.getId().equals(role.downgradeRoleId);
	}
    
	public ProductionSummary requiredGoodsToChangeRoleTo(UnitRole newRole) {
        ProductionSummary required = new ProductionSummary();
        required.addGoods(newRole.requiredGoods.entities());
        required.decreaseGoods(requiredGoods.entities());
	    return required;
	}
	
    public boolean isAvailableTo(UnitType unitType, ObjectWithFeatures place) {
        if (requiredAbilities != null) {
            for (Ability aa : requiredAbilities.entities()) {
                boolean found = unitType.hasAbility(aa.getId());
                if (!found) {
                	found = place.hasAbility(aa.getId());
                }
                if (aa.isValueNotEquals(found)) {
                    return false;
                }
            }
        }
        return true;
    }

	public int getMaximumCount() {
		return maximumCount;
	}
    
	public static class Xml extends XmlNodeParser {
		public Xml() {
            addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
            addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
            addNode("required-ability", Ability.class, "requiredAbilities");
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
			String idStr = attr.getStrAttribute("id");
			UnitRole ur = new UnitRole(idStr);
			ur.expertUnitTypeId = attr.getStrAttribute("expertUnit");
			ur.downgradeRoleId = attr.getStrAttribute("downgrade");
			ur.maximumCount = attr.getIntAttribute("maximumCount", 1);
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
