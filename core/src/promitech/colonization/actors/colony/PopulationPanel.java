package promitech.colonization.actors.colony;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Nation;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class PopulationPanel extends Table {

    private Label rebelsLabel;
    private Label rebelsPercentageLabel;
    private Label populationLabel;
    private Label prodBonusLabel;
    private Label royalistsLabel;
    private Label royalistsPercentageLabel;
    
    private Image nationImage;
    private Image otherNationImage;
    
    public PopulationPanel() {
        LabelStyle labelStyle = new LabelStyle(FontResource.getPopulationPanelFont(), Color.BLACK);
        
        rebelsLabel = new Label("", labelStyle);
        rebelsPercentageLabel = new Label("", labelStyle);
        
        populationLabel = new Label("", labelStyle); 
        prodBonusLabel = new Label("", labelStyle);
        
        royalistsLabel = new Label("", labelStyle);
        royalistsPercentageLabel = new Label("", labelStyle);

        nationImage = new Image();
        otherNationImage = new Image();
        
        Table tableLayout = new Table();
        tableLayout.defaults().space(0, 10, 0, 10);
        
        tableLayout.add(rebelsLabel).align(Align.left);
        tableLayout.add(populationLabel);
        tableLayout.add(royalistsLabel).align(Align.right).row();
        tableLayout.add(rebelsPercentageLabel).align(Align.left);
        tableLayout.add(prodBonusLabel);
        tableLayout.add(royalistsPercentageLabel).align(Align.right).row();
        
        add(nationImage).bottom();
        add(tableLayout).top();
        add(otherNationImage).bottom();
    }

    public void update(Colony colony) {
        int rebels = colony.rebels();
        int rebelsPercentage = colony.sonsOfLiberty();
        
        int population = colony.getColonyUnitsCount();
        int productionBonus = colony.productionBonus();
        int optimalPopulationGrow = colony.getPreferredSizeChange();
        
        int royalists = colony.getColonyUnitsCount() - colony.rebels();
        int royalistsPercenage = 100 - colony.sonsOfLiberty();
        
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
        
        Nation nation = colony.getOwner().nation();
        setCoatOfArmsActor(nationImage, nation);
        
        Nation otherNation = null;
        if (colony.getOwner().isRoyal()) {
            otherNation = nation.getRebelNation();
        } else {
            otherNation = nation.getRoyalNation();
        }
        if (otherNation != null) {
            setCoatOfArmsActor(otherNationImage, otherNation);
        }
    }

    private void setCoatOfArmsActor(Image imageActor, Nation nation) {
        Frame coatOfArms = GameResources.instance.coatOfArms(nation);
        imageActor.setDrawable(new TextureRegionDrawable(coatOfArms.texture));
        imageActor.setSize(imageActor.getPrefWidth(), imageActor.getPrefHeight());
    }
}
