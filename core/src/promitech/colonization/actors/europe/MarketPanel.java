package promitech.colonization.actors.europe;

import java.util.HashMap;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.player.MarketData;
import net.sf.freecol.common.model.player.MessageNotification;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.TransactionEffectOnMarket;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.actors.ChangeColonyStateListener;
import promitech.colonization.actors.QuantityGoodActor;
import promitech.colonization.actors.colony.DragAndDropSourceContainer;
import promitech.colonization.actors.colony.DragAndDropTargetContainer;

class MarketPanel extends Table implements DragAndDropTargetContainer<AbstractGoods>, DragAndDropSourceContainer<AbstractGoods> {
	private java.util.Map<String, MarketGoodsActor> goodActorByType = new HashMap<String, MarketGoodsActor>();
	private final DragAndDrop goodsDragAndDrop;
	private Player player;
	private Game game;
	
	private MarketLog marketLog;
	private ChangeColonyStateListener changeColonyStateListener;
	
	MarketPanel(Game game, DragAndDrop goodsDragAndDrop, ChangeColonyStateListener changeColonyStateListener, MarketLog marketLog) {
		this.game = game;
		this.goodsDragAndDrop = goodsDragAndDrop;
		this.marketLog = marketLog;
		this.changeColonyStateListener = changeColonyStateListener;
		
		defaults().space(10, 10, 10, 10);
		
		goodsDragAndDrop.addTarget(new QuantityGoodActor.GoodsDragAndDropTarget(this, this));
	}
	
	public void init(Player player) {
		this.player = player;
		
        for (GoodsType goodsType : Specification.instance.goodsTypes.sortedEntities()) {
        	if (!goodsType.isStorable()) {
        		continue;
        	}
        	
        	MarketGoodsActor marketGoodsActor = goodActorByType.get(goodsType.getId());
        	if (marketGoodsActor == null) {
        		marketGoodsActor = new MarketGoodsActor(goodsType, this);
        		goodActorByType.put(goodsType.getId(), marketGoodsActor);
        		add(marketGoodsActor);
        		goodsDragAndDrop.addSource(new MarketGoodsActor.GoodsDragAndDropSource(marketGoodsActor));
        	}
        	MarketData marketData = player.market().marketGoods.getById(goodsType.getId());
        	marketGoodsActor.initPrice(marketData.getSalePrice(), marketData.getBuyPrice());
        }
	}

	@Override
	public void putPayload(AbstractGoods payload, float x, float y) {
		System.out.println("sell goods " + payload);

		GoodsType goodsType = Specification.instance.goodsTypes.getById(payload.getTypeId());
		TransactionEffectOnMarket transaction = player.market().sellGoods(game, player, goodsType, payload.getQuantity());
		marketLog.logSale(transaction);
		
		if (transaction.isMarketPriceChanged()) {
			System.out.println(
					"market price changed for " + transaction.goodsTypeId + 
					" from " + transaction.sellPriceBeforeTransaction + 
					" to " + transaction.sellPriceAfterTransaction
			);
			changeColonyStateListener.addNotification(MessageNotification.createGoodsPriceChangeNotification(player, transaction));
			init(player);
		}
	}

	@Override
	public boolean canPutPayload(AbstractGoods goods, float x, float y) {
		return player.market().canTradeInEurope(goods.getTypeId());
	}

	@Override
	public void takePayload(AbstractGoods payload, float x, float y) {
		System.out.println("Buy goods " + payload);
		
		// TODO: jesli good type jest zbojkotowany to nie mozna kupic, musi byc zrzucony wyjatek w buyGoods

		GoodsType goodsType = Specification.instance.goodsTypes.getById(payload.getTypeId());

		if (player.hasNotGold(player.market().getBidPrice(goodsType, payload.getQuantity()))) {
			marketLog.logMessage("notEnoughGold");
			payload.makeEmpty();
		} else {
			TransactionEffectOnMarket transaction = player.market().buyGoods(game, player, goodsType, payload.getQuantity());
			marketLog.logPurchase(transaction);
			if (transaction.isMarketPriceChanged()) {
				System.out.println(
						"market price changed for " + transaction.goodsTypeId + 
						" from " + transaction.buyPriceBeforeTransaction + 
						" to " + transaction.buyPriceAfterTransaction
						);
				changeColonyStateListener.addNotification(MessageNotification.createGoodsPriceChangeNotification(player, transaction));
				init(player);
			}
		}
	}
}
