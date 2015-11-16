package promitech.colonization.actors.colony;

import net.sf.freecol.common.model.Colony;

interface ChangeColonyStateListener {
    void changeUnitAllocation(Colony colony);
    
    void transfereGoods();
}

