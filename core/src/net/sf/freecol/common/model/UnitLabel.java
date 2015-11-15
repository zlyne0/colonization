package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.Goods;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class UnitLabel {

	public UnitLabel() {
	}
	
	public String getName(Unit unit) {
		return unit.name;
	}
	
	public String getUnitEquipment(Unit unit) {
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
                    .add("%goods%", equipmentKey)
                    .addAmount("%amount%", 1);
            } else {
                // Other roles can be characterized by their goods.
            	extra = StringTemplate.label("");
            	
            	if (unit.roleCount > 0) {
            		boolean first = true;
            		for (Goods goods : unit.unitRole.requiredGoods.entities()) {
            			if (first) {
            				first = false; 
            			} else {  
            				extra.addName(" ");
            			}
            			extra.addStringTemplate(StringTemplate.template("model.goods.goodsAmount")
            					.addName("%goods%", goods)
            					.addAmount("%amount%", goods.getAmount() *  unit.roleCount)
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

    public String getMovesAsString(Unit unit) {
        StringBuilder sb = new StringBuilder(16);
        int quotient = unit.getMovesLeft() / 3;
        int remainder = unit.getMovesLeft() % 3;
        if (quotient > 0 || remainder == 0) {
        	sb.append(quotient);
        }
        if (remainder > 0) {
            sb.append("(").append(remainder).append("/3) ");
        }
        sb.append("/").append(unit.getInitialMovesLeft() / 3);
        return sb.toString();
    }
    
	public String getUnitType(Unit unit) {
		StringTemplate label = getUnitType(
				unit.unitType.getId(), 1, 
				unit.getOwner().nation.getId(), 
				unit.unitRole.id
		);
		return Messages.message(label);
	}

    private StringTemplate getUnitType(String typeId, int number, String nationId, String roleId) {
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
                .add("%nation%", Messages.nameKey(nationId));
        if (roleKey != null) {
        	ret.add("%role%", Messages.nameKey(roleKey));
        }
        return ret;
    }
}
