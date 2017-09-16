package net.sf.freecol.common.model;

import java.io.IOException;

import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.RequiredGoods;
import net.sf.freecol.common.util.StringUtils;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class UnitRole extends ObjectWithFeatures {
	public static final String DEFAULT_ROLE_ID = "model.role.default";
	public static final String CAVALRY_ROLE_ID = "model.role.cavalry";
	public static final String INFANTRY_ROLE_ID = "model.role.infantry";
	
	public static final String SOLDIER = "model.role.soldier";
	public static final String DRAGOON = "model.role.dragoon";
	
	public static final int DEFAULT_UNIT_ROLE_COUNT = 1;
	
    public static String getRoleSuffix(String roleId) {
        return StringUtils.lastPart(roleId, ".");
    }
	
    public static boolean isDefaultRoleId(String roleId) {
        return DEFAULT_ROLE_ID.equals(roleId);
    }
	
	private final String roleSuffix;
	protected String expertUnitTypeId;
	public final MapIdEntities<RequiredGoods> requiredGoods = MapIdEntities.linkedMapIdEntities();
	private String downgradeRoleId;
	private int maximumCount = DEFAULT_UNIT_ROLE_COUNT;
	
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
	
	public boolean isDefensive() {
		return hasModifier(Modifier.DEFENCE);
	}
    
	public boolean isCompatibleWith(UnitRole role) {
		if (role == null) {
			return false;
		}
		return this.getId().equals(role.getId()) 
				|| role.getId().equals(this.downgradeRoleId) 
				|| this.getId().equals(role.downgradeRoleId);
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
    
	public static class Xml extends XmlNodeParser<UnitRole> {
		private static final String ATTR_MAXIMUM_COUNT = "maximumCount";
		private static final String ATTR_DOWNGRADE = "downgrade";
		private static final String ATTR_EXPERT_UNIT = "expertUnit";

		public Xml() {
			ObjectWithFeatures.Xml.abstractAddNodes(this);
			addNodeForMapIdEntities("requiredGoods", RequiredGoods.class);
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
			UnitRole ur = new UnitRole(attr.getStrAttribute(ATTR_ID));
			ur.expertUnitTypeId = attr.getStrAttribute(ATTR_EXPERT_UNIT);
			ur.downgradeRoleId = attr.getStrAttribute(ATTR_DOWNGRADE);
			ur.maximumCount = attr.getIntAttribute(ATTR_MAXIMUM_COUNT, DEFAULT_UNIT_ROLE_COUNT);
			nodeObject = ur;
		}

		@Override
		public void startWriteAttr(UnitRole ur, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(ur);
			attr.set(ATTR_EXPERT_UNIT, ur.expertUnitTypeId);
			attr.set(ATTR_DOWNGRADE, ur.downgradeRoleId);
			attr.set(ATTR_MAXIMUM_COUNT, ur.maximumCount);
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
