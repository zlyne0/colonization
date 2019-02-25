package net.sf.freecol.common.model.specification.options;

import java.io.IOException;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class OptionGroup extends ObjectWithId {

    public final MapIdEntities<OptionGroup> optionsGroup = MapIdEntities.linkedMapIdEntities();
    public final MapIdEntities<ObjectWithId> abstractOptions = MapIdEntities.linkedMapIdEntities();
    
    private boolean editable = true;
    
    public OptionGroup(String id) {
        super(id);
    }

    public int getIntValue(String code) {
        IntegerOption option = (IntegerOption)abstractOptions.getById(code);
        return option.getValue();
    }

    public String getStringValue(String code) {
        StringOption option = (StringOption)abstractOptions.getById(code);
        return option.getValue();
    }

	public boolean isEditable() {
		return editable;
	}
    
    public static class Xml extends XmlNodeParser<OptionGroup> {
        
        private static final String ATTR_EDITABLE = "editable";

		public Xml() {
            addNodeForMapIdEntities("optionsGroup", OptionGroup.class);
            
            addNodeForMapIdEntities("abstractOptions", IntegerOption.class);
            addNodeForMapIdEntities("abstractOptions", StringOption.class);
            addNodeForMapIdEntities("abstractOptions", BooleanOption.class);
            addNodeForMapIdEntities("abstractOptions", RangeOption.class);
            addNodeForMapIdEntities("abstractOptions", PercentageOption.class);
            addNodeForMapIdEntities("abstractOptions", SelectOption.class);
            addNodeForMapIdEntities("abstractOptions", UnitListOption.class);
        }

        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute(ATTR_ID);
            OptionGroup optionGroup = new OptionGroup(id);
            optionGroup.editable = attr.getBooleanAttribute(ATTR_EDITABLE, true);
            nodeObject = optionGroup;
        }

        @Override
        public void startWriteAttr(OptionGroup og, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(og);
        	attr.set(ATTR_EDITABLE, og.editable);
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "optionGroup";
        }
        
    }

}
