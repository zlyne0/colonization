package promitech.colonization.actors.europe;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.Align;

import net.sf.freecol.common.model.player.Market;
import net.sf.freecol.common.model.player.TransactionEffectOnMarket;
import promitech.colonization.GameResources;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class MarketLog extends ScrollPane implements Market.MarketTransactionLogger {
    private final Label label; 
    
    public MarketLog() {
        super(null, GameResources.instance.getUiSkin());

        label = new Label("", GameResources.instance.getUiSkin());
        label.setAlignment(Align.top | Align.left);
        
        setWidget(label);
        
        setForceScroll(false, false);
        setFadeScrollBars(false);
        setOverscroll(true, true);
        setScrollBarPositions(false, true);
    }

    public void logSale(TransactionEffectOnMarket transaction) {
        StringTemplate t1 = StringTemplate.template("transaction.sale")
        		.addName("%goods%", transaction.goodsTypeId)
                .addAmount("%amount%", transaction.quantity)
                .addAmount("%gold%", transaction.sellPrice);
        
        StringTemplate t2 = StringTemplate.template("transaction.price")
            .addAmount("%gold%", transaction.grossPrice);
        
        StringTemplate t3 = StringTemplate.template("transaction.tax")
            .addAmount("%tax%", transaction.taxPercent)
            .addAmount("%gold%", transaction.tax);
        
        StringTemplate t4 = StringTemplate.template("transaction.net")
            .addAmount("%gold%", transaction.netPrice);
            
        appendLine(Messages.message(t1)); 
        appendLine("      " + Messages.message(t2));
        appendLine("      " + Messages.message(t3));
        appendLine("      " + Messages.message(t4));
    }
    
    public void logPurchase(TransactionEffectOnMarket transaction) {
        StringTemplate t1 = StringTemplate.template("transaction.purchase")
            .addName("%goods%", transaction.goodsTypeId)
            .addAmount("%amount%", transaction.quantity)
            .addAmount("%gold%", transaction.buyPriceBeforeTransaction);
        StringTemplate t2 = StringTemplate.template("transaction.price")
            .addAmount("%gold%", transaction.grossPrice);
        
        appendLine(Messages.message(t1)); 
        appendLine("      " + Messages.message(t2));
    }
    
    public void appendLine(String str) {
        if (label.getText().length > 0) {
            label.setText(label.getText() + "\n");
        }
        label.setText(label.getText() + str);
        
        setScrollPercentY(100);
        layout();
    }
    
    @Override
    public float getPrefHeight() {
        return 300;
    }
    
    @Override
    public float getPrefWidth() {
        return 200;
    }

	public void logMessage(String msgCode) {
		appendLine(Messages.msg(msgCode));
	}

}

