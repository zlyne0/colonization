package promitech.colonization.actors.colony;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;

import net.sf.freecol.common.model.Colony;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class PopulationPanel extends HorizontalGroup {

    private Label rebelsLabel;
    private Label rebelsPercentageLabel;
    private Label populationLabel;
    private Label prodBonusLabel;
    private Label royalistsLabel;
    private Label royalistsPercentageLabel;
    
    public PopulationPanel() {
        LabelStyle labelStyle = new LabelStyle(FontResource.getPopulationPanelFont(), Color.BLACK);
        
        rebelsLabel = new Label("", labelStyle);
        rebelsPercentageLabel = new Label("", labelStyle);
        
        populationLabel = new Label("", labelStyle); 
        prodBonusLabel = new Label("", labelStyle);
        
        royalistsLabel = new Label("", labelStyle);
        royalistsPercentageLabel = new Label("", labelStyle);
        
        space(10);
        addActor(rebelsLabel);
        addActor(rebelsPercentageLabel);
        addActor(populationLabel);
        addActor(prodBonusLabel);
        addActor(royalistsLabel);
        addActor(royalistsPercentageLabel);
    }

    public void update(Colony colony) {
        int rebels = colony.rebels();
        int rebelsPercentage = colony.sonsOfLiberty();
        
        int population = colony.getColonyUnitsCount();
        int productionBonus = colony.productionBonus();
        int optimalPopulationGrow = colony.getPreferredSizeChange();
        
        int royalists = colony.getColonyUnitsCount() - colony.rebels();
        int royalistsPercenage = colony.tories();
        
        StringTemplate t;
        t = StringTemplate.template("colonyPanel.rebelLabel").addAmount("%number%", rebels);
        rebelsLabel.setText(Messages.message(t));
        rebelsPercentageLabel.setText(Integer.toString(rebelsPercentage) + "%");
        
        t = StringTemplate.template("colonyPanel.populationLabel").addAmount("%number%", population);
        populationLabel.setText(Messages.message(t));
        t = StringTemplate.template("colonyPanel.bonusLabel")
                .addAmount("%number%", productionBonus)
                .addName("%extra%", (optimalPopulationGrow == 0) ? "" : "(" + Integer.toString(optimalPopulationGrow) + ")");
        prodBonusLabel.setText(Messages.message(t));
        
        t = StringTemplate.template("colonyPanel.royalistLabel").addAmount("%number%", royalists);
        royalistsLabel.setText(Messages.message(t));
        royalistsPercentageLabel.setText(Integer.toString(royalistsPercenage) + "%");
    }
    
}
