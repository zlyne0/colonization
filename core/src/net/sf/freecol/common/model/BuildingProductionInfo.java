package net.sf.freecol.common.model;

import java.util.HashMap;

public class BuildingProductionInfo {
    public java.util.Map<String,Integer> goods = new HashMap<String, Integer>();
    
    public String toString() {
    	return goods.toString();
    }
}
