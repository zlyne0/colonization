package net.sf.freecol.common.model;


public class Game {

	public Map map;
	public Specification specification;
	
	public String toString() {
		return "map[" + map + "], specification = " + specification;
	}
	
	
}
