package net.sf.freecol.common.model.map.path;

class NavyWithoutThreatCostDecider extends NavyCostDecider {

    @Override
    protected boolean isTileThreatForUnit(Node moveNode) {
        return false;
    }
}
