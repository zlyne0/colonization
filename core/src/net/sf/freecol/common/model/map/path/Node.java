package net.sf.freecol.common.model.map.path;

import net.sf.freecol.common.model.Tile;

class Node {	
    int totalCost;
	int turns;
	int unitMovesLeft;
	boolean noMove;
	boolean tileBombardedMetaData = false; // whether meta data set
	boolean tileBombarded = false;
	Node preview;
	Node next;
	
	final Tile tile;
	
	public Node(Tile tile) {
		this.tile = tile;
	}
    
	boolean hasBetterCostThen(Node node) {
		return totalCost < node.totalCost;
	}
	
	void reset(int unitMovesLeft, int totalCost) {
		this.totalCost = totalCost;
		turns = Integer.MAX_VALUE;
		this.unitMovesLeft = unitMovesLeft;
		this.noMove = false;
		this.preview = null;
		this.next = null;
		this.tileBombardedMetaData = false;
		this.tileBombarded = false;
	}
	
	public String toString() {
		return "[" + tile.x + "," + tile.y + "], unitMovesLeft = " + unitMovesLeft + ", turns = " + turns + ", totalCost = " + totalCost;
	}
}