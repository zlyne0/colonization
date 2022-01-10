package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.RequiredGoods;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class UnitLabel {

	private UnitLabel() {
	}
	
	public static String getName(Unit unit) {
		return unit.name;
	}
	
	public static String getUnitEquipment(Unit unit) {
        StringTemplate extra = null;
		
        if (unit.unitRole.isDefaultRole()) {
            if (unit.canCarryTreasure()) {
                extra = StringTemplate.template("goldAmount").addAmount("%amount%", unit.getTreasureAmount());
            } else {
                // unequipped expert has no-equipment label
                if (unit.isExpert()) {
                	String key = unit.unitType.getId() + ".noequipment";
                	if (Messages.containsKey(key)) {
                		extra = StringTemplate.key(key);
                	}
                }
            }
        } else {
            String equipmentKey = unit.unitRole.getId() + ".equipment";
            if (Messages.containsKey(equipmentKey)) {
                // Currently only used for missionary which does not
                // have equipment that directly corresponds to goods.
                extra = StringTemplate.template("model.goods.goodsAmount")
                    .addKey("%goods%", equipmentKey)
                    .addAmount("%amount%", 1);
            } else {
                // Other roles can be characterized by their goods.
            	extra = StringTemplate.label("");
            	
            	if (unit.roleCount > 0) {
            		boolean first = true;
            		for (RequiredGoods goods : unit.unitRole.requiredGoods.entities()) {
            			if (first) {
            				first = false; 
            			} else {  
            				extra.addName(" ");
            			}
            			extra.addStringTemplate(StringTemplate.template("model.goods.goodsAmount")
            					.addName("%goods%", goods)
            					.addAmount("%amount%", goods.amount *  unit.roleCount)
            			);
            		}
            	}
            }
        }
        
        if (extra != null) {
        	return Messages.message(extra);
        } else {
        	return null;
        }
	}

    public static String getMovesAsString(Unit unit) {
        StringBuilder sb = new StringBuilder(16);
        int quotient = unit.getMovesLeft() / 3;
        int remainder = unit.getMovesLeft() % 3;
        if (quotient > 0 || remainder == 0) {
        	sb.append(quotient);
        }
        if (remainder > 0) {
            sb.append("(").append(remainder).append("/3) ");
        }
        sb.append("/").append(unit.initialMoves() / 3);
        return sb.toString();
    }
    
	public static String getUnitType(Unit unit) {
		StringTemplate label = getUnitType(
				unit.unitType.getId(), 1, 
				unit.getOwner().nation().getId(), 
				unit.unitRole.id
		);
		return Messages.message(label);
	}

    public static StringTemplate getLabelWithAmount(UnitType unitType, UnitRole unitRole, int amount) {
        StringTemplate tmpl = getUnitLabel(null, unitType.getId(), amount, null, unitRole.getId(), null);
        return StringTemplate.template("abstractUnit")
			.addAmount("%number%", amount)
			.addStringTemplate("%unit%", tmpl);
    }
	
	public static StringTemplate getPlainUnitLabel(Unit unit) {
		return getUnitLabel(getName(unit), unit.unitType.getId(), 1, null, unit.unitRole.getId(), null);		
	}
	
    private static StringTemplate getUnitType(String typeId, int number, String nationId, String roleId) {
        StringTemplate type;
        String roleKey;
        String baseKey = typeId + "." + UnitRole.getRoleSuffix(roleId);
        if (Messages.containsKey(baseKey)) {
            type = StringTemplate.template(baseKey).addAmount("%number%", number);
            roleKey = null;
        } else {
            type = StringTemplate.template(Messages.nameKey(typeId)).addAmount("%number%", number);
            roleKey = (UnitRole.isDefaultRoleId(roleId)) ? null : roleId;
        }
    	
        String tempStr = "unitFormat.null.nation";
        if (roleKey != null) {
        	tempStr += ".role";
        } else {
        	tempStr += ".null";
        }
        tempStr += ".null";
        
        StringTemplate ret = StringTemplate.template(tempStr)
                .addStringTemplate("%type%", type)
                .addName("%nation%", nationId);
        if (roleKey != null) {
        	ret.addName("%role%", roleKey);
        }
        return ret;
    }
    
    /**
     * Get a label for a collection of units given a name, type,
     * number, nation, role and extra annotation.
     *
     * @param name An optional unit name.
     * @param typeId The unit type identifier.
     * @param number The number of units.
     * @param nationId An optional nation identifier.
     * @param roleId The unit role identifier.
     * @param extra An optional extra annotation.
     * @return A <code>StringTemplate</code> to describe the given unit.
     */
    public static StringTemplate getUnitLabel(String name, String typeId,
                                              int number, String nationId,
                                              String roleId,
                                              StringTemplate extra) {
        // Check for special role-specific key, which will not have a
        // %role% argument.  These exist so we can avoid mentioning
        // the role twice, e.g. "Seasoned Scout Scout".
        StringTemplate type;
        String roleKey;
        String baseKey = typeId + "." + UnitRole.getRoleSuffix(roleId);
        if (Messages.containsKey(baseKey)) {
            type = StringTemplate.template(baseKey).addAmount("%number%", number);
            roleKey = null;
        } else {
            type = StringTemplate.template(Messages.nameKey(typeId))
        			.addAmount("%number%", number);
            roleKey = (UnitRole.isDefaultRoleId(roleId)) ? null : roleId;
        }

        // This is extra brutal, but crash proof.
        StringTemplate ret;
        if (name == null) {
            if (nationId == null) {
                if (roleKey == null) {
                    if (extra == null) {
                        ret = StringTemplate.template("unitFormat.null.null.null.null")
                            .addStringTemplate("%type%", type);
                    } else {
                        ret = StringTemplate.template("unitFormat.null.null.null.equip")
                            .addStringTemplate("%type%", type)
                            .addStringTemplate("%equipment%", extra);
                    }
                } else {
                    if (extra == null) {
                        ret = StringTemplate.template("unitFormat.null.null.role.null")
                            .addStringTemplate("%type%", type)
                            .addName("%role%", roleKey);
                    } else {
                        ret = StringTemplate.template("unitFormat.null.null.role.equip")
                            .addStringTemplate("%type%", type)
                            .addName("%role%", roleKey)
                            .addStringTemplate("%equipment%", extra);
                    }
                }
            } else {
                if (roleKey == null) {
                    if (extra == null) {
                        ret = StringTemplate.template("unitFormat.null.nation.null.null")
                            .addStringTemplate("%type%", type)
                            .addName("%nation%", nationId);
                    } else {
                        ret = StringTemplate.template("unitFormat.null.nation.null.equip")
                            .addStringTemplate("%type%", type)
                            .addName("%nation%", nationId)
                            .addStringTemplate("%equipment%", extra);
                    }
                } else {
                    if (extra == null) {
                        ret = StringTemplate.template("unitFormat.null.nation.role.null")
                            .addStringTemplate("%type%", type)
                            .addName("%nation%", nationId)
                            .addName("%role%", roleKey);
                    } else {
                        ret = StringTemplate.template("unitFormat.null.nation.role.equip")
                            .addStringTemplate("%type%", type)
                            .addName("%nation%", nationId)
                            .addName("%role%", roleKey)
                            .addStringTemplate("%equipment%", extra);
                    }
                }
            }
        } else {
            if (nationId == null) {
                if (roleKey == null) {
                    if (extra == null) {
                        ret = StringTemplate.template("unitFormat.name.null.null.null")
                            .add("%name%", name)
                            .addStringTemplate("%type%", type);
                    } else {
                        ret = StringTemplate.template("unitFormat.name.null.null.equip")
                            .add("%name%", name)
                            .addStringTemplate("%type%", type)
                            .addStringTemplate("%equipment%", extra);
                    }
                } else {
                    if (extra == null) {
                        ret = StringTemplate.template("unitFormat.name.null.role.null")
                            .addName("%name%", name)
                            .addStringTemplate("%type%", type)
                            .addName("%role%", roleKey);
                    } else {
                        ret = StringTemplate.template("unitFormat.name.null.role.equip")
                            .add("%name%", name)
                            .addStringTemplate("%type%", type)
                            .addName("%role%", roleKey)
                            .addStringTemplate("%equipment%", extra);
                    }
                }
            } else {
                if (roleKey == null) {
                    if (extra == null) {
                        ret = StringTemplate.template("unitFormat.name.nation.null.null")
                            .add("%name%", name)
                            .addStringTemplate("%type%", type)
                            .addName("%nation%", nationId);
                    } else {
                        ret = StringTemplate.template("unitFormat.name.nation.null.equip")
                            .add("%name%", name)
                            .addStringTemplate("%type%", type)
                            .addName("%nation%", nationId)
                            .addStringTemplate("%equipment%", extra);
                    }
                } else {
                    if (extra == null) {
                        ret = StringTemplate.template("unitFormat.name.nation.role.null")
                            .add("%name%", name)
                            .addStringTemplate("%type%", type)
                            .addName("%nation%", nationId)
                            .addName("%role%", roleKey);
                    } else {
                        ret = StringTemplate.template("unitFormat.name.nation.role.equip")
                            .add("%name%", name)
                            .addStringTemplate("%type%", type)
                            .addName("%nation%", nationId)
                            .addName("%role%", roleKey)
                            .addStringTemplate("%equipment%", extra);
                    }
                }
            }
        }
        return ret;
    }
    
}
