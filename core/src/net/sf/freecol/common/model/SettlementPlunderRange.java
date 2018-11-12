package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.specification.RandomRange;
import net.sf.freecol.common.model.specification.Scope;
import net.sf.freecol.common.model.specification.ScopeAppliable;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class SettlementPlunderRange extends RandomRange {

    private final List<Scope> scopes = new ArrayList<Scope>();
    
    public List<Scope> getScopes() {
        return scopes;
    }

	public boolean canApplyTo(ScopeAppliable appliable) {
		for (Scope s : scopes) {
			if (s.isAppliesTo(appliable)) {
				return true;
			}
		}
		return false;
	}
    
    public static class Xml extends XmlNodeParser<SettlementPlunderRange> {

        public Xml() {
            addNode(Scope.class, new ObjectFromNodeSetter<SettlementPlunderRange, Scope>() {
                @Override
                public void set(SettlementPlunderRange target, Scope entity) {
                    target.scopes.add(entity);
                }
                
                @Override
                public void generateXml(SettlementPlunderRange source, ChildObject2XmlCustomeHandler<Scope> xmlGenerator) throws IOException {
                    xmlGenerator.generateXmlFromCollection(source.scopes);
                }
            });            
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            SettlementPlunderRange plunderRange = new SettlementPlunderRange();
            RandomRange.Xml.startElement(plunderRange, attr);
            
            nodeObject = plunderRange;
        }

        @Override
        public void startWriteAttr(SettlementPlunderRange plunderRange, XmlNodeAttributesWriter attr) throws IOException {
        	RandomRange.Xml.startWriteAttr(plunderRange, attr);
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "plunder";
        }
    }
}
