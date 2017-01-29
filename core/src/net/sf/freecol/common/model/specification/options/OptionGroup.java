package net.sf.freecol.common.model.specification.options;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class OptionGroup extends ObjectWithId {

    public final MapIdEntities<OptionGroup> optionsGroup = new MapIdEntities<OptionGroup>();
    public final MapIdEntities<ObjectWithId> abstractOptions = new MapIdEntities<ObjectWithId>();
    
    private boolean editable = false;
    
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
    
    public static class Xml extends XmlNodeParser {
        
        public Xml() {
            addNodeForMapIdEntities("optionsGroup", OptionGroup.class);

            addNodeForMapIdEntities("abstractOptions", IntegerOption.class);
            addNodeForMapIdEntities("abstractOptions", StringOption.class);
            addNodeForMapIdEntities("abstractOptions", BooleanOption.class);
            addNodeForMapIdEntities("abstractOptions", RangeOption.class);
            addNodeForMapIdEntities("abstractOptions", SelectOption.class);
            addNodeForMapIdEntities("abstractOptions", UnitListOption.class);
        }

        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute("id");
            OptionGroup optionGroup = new OptionGroup(id);
            optionGroup.editable = attr.getBooleanAttribute("editable", false);
            nodeObject = optionGroup;
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
