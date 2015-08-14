/**
 *  Copyright (C) 2002-2015   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.model.specification;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.SettlementType;

/**
 * Represents the type of one of the nations present in the game.
 */
public abstract class NationType implements Identifiable {

    public static enum SettlementNumber { LOW, AVERAGE, HIGH }
    public static enum AggressionLevel { LOW, AVERAGE, HIGH }

    protected boolean european = false;
    protected String id;
    public final MapIdEntities<SettlementType> settlementTypes = new MapIdEntities<SettlementType>();

    @Override
    public String getId() {
        return id;
    }

    public boolean isEuropean() {
    	return european;
    }
    
    public String toString() {
    	return "nationType: " + id;
    }
}
