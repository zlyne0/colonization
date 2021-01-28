package net.sf.freecol.common.model.colonyproduction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.ai.missions.Savegame1600BaseClass;

class ColonyProductionTest extends Savegame1600BaseClass {

	@Test
	public void compareColonyProductionForTwoAlgorythms() throws Exception {
		for (Settlement settlement : dutch.settlements) {
			// given
			Colony colony = settlement.asColony();
			
			// when
			ProductionSummary oldColonyProduction = colony.productionSummary();
			ColonyProduction newColonyProduction = new ColonyProduction(colony);
			
			// then
			assertThat(oldColonyProduction)
				.as("incorrect colony production for colony %s", colony.getName())
				.isEqualTo(newColonyProduction.globalProductionConsumption());
		}
	}

	@Test
	public void compareColonyProductionForTwoAlgorythmsWithColonyBonus() throws Exception {
		for (Settlement settlement : dutch.settlements) {
			// given
			Colony colony = settlement.asColony();
	        colony.setLiberty(Colony.LIBERTY_PER_REBEL * ((colony.getColonyUnitsCount()) + 1));
	        colony.updateColonyFeatures();
	        colony.calculateSonsOfLiberty();
	        
			// when
			ProductionSummary oldColonyProduction = colony.productionSummary();
			ColonyProduction newColonyProduction = new ColonyProduction(colony);
			
			// then
			assertThat(oldColonyProduction)
				.as("incorrect colony production for colony %s", colony.getName())
				.isEqualTo(newColonyProduction.globalProductionConsumption());
		}
	}
	
	
}
