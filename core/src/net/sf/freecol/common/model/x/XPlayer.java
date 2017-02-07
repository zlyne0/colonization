package net.sf.freecol.common.model.x;

import java.io.IOException;

import com.badlogic.gdx.utils.XmlWriter;

import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.player.EventsNotifications;
import net.sf.freecol.common.model.player.HighSeas;
import net.sf.freecol.common.model.player.Market;
import net.sf.freecol.common.model.player.Monarch;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Stance;
import net.sf.freecol.common.model.player.Tension;
import net.sf.freecol.common.model.player.Player.PlayerType;
import net.sf.freecol.common.model.specification.FoundingFather;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class XPlayer extends ObjectWithId {

	public XPlayer(String id) {
		super(id);
	}

	public static class Xml extends XmlNodeParser<XPlayer> {
        public Xml() {
//            addNode(Europe.class, new ObjectFromNodeSetter<Player, Europe>() {
//                @Override
//                public void set(Player target, Europe entity) {
//                    target.europe = entity;
//                    entity.setOwner(target);
//                }
//            });
//            addNode(Monarch.class, new ObjectFromNodeSetter<Player, Monarch>() {
//                @Override
//                public void set(Player target, Monarch entity) {
//                    target.monarch = entity;
//                    entity.setPlayer(target);
//                }
//            });
//            addNode(Market.class, "market");
//            addNode(EventsNotifications.class, "eventsNotifications");
//            addNode(HighSeas.class, "highSeas");
        }

        @Override
        public void startElement(XmlNodeAttributes attr) {
            String idStr = attr.getStrAttribute("id");
            String nationIdStr = attr.getStrAttribute("nationId");
            String nationTypeStr = attr.getStrAttribute("nationType");
            
            XPlayer player = new XPlayer(idStr);
            nodeObject = player;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
        }
        
        @Override
        public void startWriteAttr(XPlayer player, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(player);
        }        
        
        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "player";
        }
    }
	
	
}
