package net.sf.freecol.common.model;

import net.sf.freecol.common.model.colonyproduction.GoodsCollection;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.RequiredGoods;
import net.sf.freecol.common.util.StringUtils;

import java.io.IOException;
import java.util.Comparator;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class UnitRole extends ObjectWithFeatures {
	
	public static final Comparator<UnitRole> OFFENCE_POWER_COMPARATOR = new Comparator<UnitRole>() {
		@Override
		public int compare(UnitRole o1, UnitRole o2) {
			float sum1 = Modifier.sumValues(o1.getModifiers(Modifier.OFFENCE));
			float sum2 = Modifier.sumValues(o2.getModifiers(Modifier.OFFENCE));
			return Float.compare(sum2, sum1);
		}
	};	
	
	public static final String DEFAULT_ROLE_ID = "model.role.default";
	public static final String CAVALRY_ROLE_ID = "model.role.cavalry";
	public static final String INFANTRY_ROLE_ID = "model.role.infantry";

	public static final String SOLDIER = "model.role.soldier";
	public static final String DRAGOON = "model.role.dragoon";
	public static final String SCOUT = "model.role.scout";
	public static final String PIONEER = "model.role.pioneer";

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
	public final MapIdEntities<UnitRoleChange> roleChanges = MapIdEntities.linkedMapIdEntities();
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
    
    public boolean hasMoreOffensivePower(UnitRole ur) {
    	return OFFENCE_POWER_COMPARATOR.compare(ur, this) > 0;
    }
	
	public boolean isDefensive() {
		return hasModifier(Modifier.DEFENCE);
	}
    
	boolean noDowngradeRole() {
		return downgradeRoleId == null;
	}
	
    String getDowngradeRoleId() {
        return downgradeRoleId;
    }
	
	public boolean isCompatibleWith(UnitRole role) {
		if (role == null) {
			return false;
		}
		return this.getId().equals(role.getId()) 
				|| role.getId().equals(this.downgradeRoleId) 
				|| this.getId().equals(role.downgradeRoleId);
	}

	public int getMaximumCount() {
		return maximumCount;
	}

	boolean canChangeRole(UnitRole fromRole, UnitRole toRole) {
		for (UnitRoleChange urc : roleChanges.entities()) {
			if (urc.match(fromRole, toRole)) {
				return true;
			}
		}
		return false;
	}

	public GoodsCollection sumOfRequiredGoods() {
		GoodsCollection gc = new GoodsCollection();
		for (RequiredGoods requiredGood : requiredGoods) {
			gc.add(requiredGood.goodsType, requiredGood.amount * maximumCount);
		}
		return gc;
	}

	public boolean isContainerHasRequiredGoods(GoodsContainer goodsContainer) {
		return isContainerHasRequiredGoods(goodsContainer, maximumCount);
	}

	public boolean isContainerHasRequiredGoods(GoodsContainer goodsContainer, int roleCount) {
		for (RequiredGoods requiredGood : requiredGoods) {
			if (!goodsContainer.hasGoodsQuantity(requiredGood.goodsType, requiredGood.amount * roleCount)) {
				return false;
			}
		}
		return true;
	}

	public int maximumAvailableRequiredGoods(final Unit unit, GoodsContainer goodsContainer, ProductionSummary required) {
		int maxRoleCount = DEFAULT_UNIT_ROLE_COUNT;
		for (RequiredGoods g : requiredGoods.entities()) {
			int marg = 0;
			int containerGoodsAmount = goodsContainer.goodsAmount(g.getId());
			for (int i = 1; i <= maximumCount; i++) {
				if (containerGoodsAmount >= i * g.amount) {
					marg = i * g.amount;
					maxRoleCount = Math.max(maxRoleCount, i);
				}
			}
			if (marg > 0) {
				required.addGoods(g.getId(), marg);
			}
		}

		for (RequiredGoods g : unit.unitRole.requiredGoods.entities()) {
			required.addGoods(g.getId(), -g.amount * unit.getRoleCount());
		}
		return maxRoleCount;
	}

	public GoodsCollection requiredGoodsForRoleCount(int roleCount) {
		if (requiredGoods.isEmpty()) {
			return GoodsCollection.emptyReadOnly;
		}
		GoodsCollection required = new GoodsCollection();
		for (RequiredGoods g : requiredGoods.entities()) {
			required.add(g.goodsType, g.amount * roleCount);
		}
		return required;
	}

	public ProductionSummary requiredGoodsToChangeRole(UnitRole newRole) {
		ProductionSummary required = new ProductionSummary();
		for (RequiredGoods g : newRole.requiredGoods) {
			required.addGoods(g.getId(), g.amount * newRole.maximumCount);
		}
		for (RequiredGoods g : requiredGoods) {
			required.addGoods(g.getId(), -g.amount * maximumCount);
		}
		return required;
	}

	public static ProductionSummary requiredGoodsToChangeRole(Unit unit, UnitRole newRole) {
		ProductionSummary required = new ProductionSummary();

		for (RequiredGoods g : newRole.requiredGoods) {
			required.addGoods(g.getId(), g.amount * newRole.maximumCount);
		}
		for (RequiredGoods g : unit.unitRole.requiredGoods) {
			required.addGoods(g.getId(), -g.amount * unit.roleCount);
		}
		return required;
	}

	public static int countRequiredGoodsToChangeRole(GoodsType goodsType, Unit unit, UnitRole newRole) {
		int requiredAmount = 0;
		for (RequiredGoods g : newRole.requiredGoods.entities()) {
			if (g.goodsType.equalsId(goodsType)) {
				requiredAmount += g.amount * newRole.getMaximumCount();
			}
		}
		for (RequiredGoods g : unit.unitRole.requiredGoods.entities()) {
			if (g.goodsType.equalsId(goodsType)) {
				requiredAmount -= g.amount * unit.roleCount;
			}
		}
		if (requiredAmount < 0) {
			requiredAmount = 0;
		}
		return requiredAmount;
	}

	public static class Xml extends XmlNodeParser<UnitRole> {
		private static final String ATTR_MAXIMUM_COUNT = "maximumCount";
		private static final String ATTR_DOWNGRADE = "downgrade";
		private static final String ATTR_EXPERT_UNIT = "expertUnit";

		public Xml() {
			ObjectWithFeatures.Xml.abstractAddNodes(this);
			addNodeForMapIdEntities("requiredGoods", RequiredGoods.class);
			addNodeForMapIdEntities("roleChanges", UnitRoleChange.class);
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
