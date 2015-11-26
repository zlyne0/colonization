package promitech.colonization.actors.colony;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import net.sf.freecol.common.model.Ability;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.GoodMaxProductionLocation;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitRole;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

class UnitActionOrdersDialog extends Dialog {

    public class UnitActionOrderItem extends HorizontalGroup {
        public UnitActionOrderItem(GoodMaxProductionLocation g) {
            pad(5);
            
            Frame resGoodsImage = GameResources.instance.goodsImage(g.getGoodsType());
            Image image = new Image(resGoodsImage.texture);
            
            StringTemplate t = StringTemplate.template(g.getGoodsType().getId() + ".workAs")
                    .addAmount("%amount%", g.getProduction())
                    .addName("%claim%", "");            
            String msg = Messages.message(t);
            
            LabelStyle labelStyle = GameResources.instance.getUiSkin().get(LabelStyle.class);
            labelStyle.font = FontResource.getGoodsQuantityFont();
            Label label = new Label(msg, labelStyle);
            
            this.addActor(image);
            this.addActor(label);
        }
    }
    
    UnitActionOrdersDialog(Colony colony, Unit unit) {
        super("", GameResources.instance.getUiSkin());
        
        java.util.List<GoodMaxProductionLocation> maxProductionForGoods = colony.determinePotentialMaxGoodsProduction(unit);
        Collections.sort(maxProductionForGoods, new Comparator<GoodMaxProductionLocation>() {
            @Override
            public int compare(GoodMaxProductionLocation o1, GoodMaxProductionLocation o2) {
                return ObjectWithId.INSERT_ORDER_ASC_COMPARATOR.compare(o1.getGoodsType(), o2.getGoodsType());
            }
        });
        
        System.out.println("maxProductionForGoods.size = " + maxProductionForGoods.size());
        for (GoodMaxProductionLocation g : maxProductionForGoods) {
            System.out.println("max prod = " + g);
        }
        
        VerticalGroup verticalList = new VerticalGroup();
        verticalList.align(Align.left);
        for (GoodMaxProductionLocation g : maxProductionForGoods) {
            verticalList.addActor(new UnitActionOrderItem(g));
        }
        
        ScrollPane verticalListScrollPane = new ScrollPane(verticalList, GameResources.instance.getUiSkin());
        verticalListScrollPane.setFlickScroll(false);
        getContentTable().add(verticalListScrollPane);
        
        TextButton cancelButton = new TextButton(Messages.msg("cancel"), GameResources.instance.getUiSkin());
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });
        getButtonTable().add(cancelButton);

        if (unit.hasAbility(Ability.CAN_BE_EQUIPPED)) {
            List<UnitRole> avaliableRoles = unit.avaliableRoles();
            System.out.println("avaliable roles size " + avaliableRoles.size());
            for (UnitRole ur : avaliableRoles) {
                System.out.println("ur " + ur);
            }
        }
        
        addListener(new InputListener() {
            public boolean keyDown (InputEvent event, int keycode2) {
                if (Keys.ENTER == keycode2) {
                    hide();
                }
                if (Keys.ESCAPE == keycode2) {
                    hide();
                }
                return false;
            }
        });
    }
}
