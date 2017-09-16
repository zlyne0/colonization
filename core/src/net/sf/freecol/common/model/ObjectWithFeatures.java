package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.Modifier.ModifierPredicate;
import net.sf.freecol.common.model.specification.Scope;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class ObjectWithFeatures extends ObjectWithId {
    
    public static final ObjectFromNodeSetter<ObjectWithFeatures, Modifier> OBJECT_MODIFIER_NODE_SETTER = new ObjectFromNodeSetter<ObjectWithFeatures, Modifier>() {
        @Override
        public void set(ObjectWithFeatures target, Modifier entity) {
            target.addModifier(entity);
        }

		@Override
		public void generateXml(ObjectWithFeatures source, ChildObject2XmlCustomeHandler<Modifier> xmlGenerator) throws IOException {
        	for (Entry<String, List<Modifier>> entry : source.modifiers.entrySet()) {
        		xmlGenerator.generateXmlFromCollection(entry.getValue());
        	}
		}
    };
    
    public static final ObjectFromNodeSetter<ObjectWithFeatures, Ability> OBJECT_ABILITY_NODE_SETTER = new ObjectFromNodeSetter<ObjectWithFeatures, Ability>() {
        @Override
        public void set(ObjectWithFeatures target, Ability entity) {
            target.addAbility(entity);
        }
		@Override
		public void generateXml(ObjectWithFeatures source, ChildObject2XmlCustomeHandler<Ability> xmlGenerator) throws IOException {
			for (Entry<String, List<Ability>> entry : source.abilities.entrySet()) {
				xmlGenerator.generateXmlFromCollection(entry.getValue());
			}
		};
    };
    
    public static final ObjectFromNodeSetter<ObjectWithFeatures, Scope> OBJECT_SCOPE_NODE_SETTER = new ObjectFromNodeSetter<ObjectWithFeatures, Scope>() {
        @Override
        public void set(ObjectWithFeatures target, Scope entity) {
        	target.scopes.add(entity);
        }
		@Override
		public void generateXml(ObjectWithFeatures source, ChildObject2XmlCustomeHandler<Scope> xmlGenerator) throws IOException {
			xmlGenerator.generateXmlFromCollection(source.scopes);
		};
    };
    
    public static final ObjectFromNodeSetter<ObjectWithFeatures, Ability> REQ_ABILITY_NODE_SETTER = new ObjectFromNodeSetter<ObjectWithFeatures, Ability>() {
        @Override
        public void set(ObjectWithFeatures target, Ability entity) {
        	target.requiredAbilities.add(entity);
        }
		@Override
		public void generateXml(ObjectWithFeatures source, ChildObject2XmlCustomeHandler<Ability> xmlGenerator) throws IOException {
			xmlGenerator.generateXmlFromCollection(source.requiredAbilities.entities());
		};
    };
    
    protected final MapIdEntities<Ability> requiredAbilities = new MapIdEntities<Ability>();
    private final java.util.Map<String, List<Modifier>> modifiers = new HashMap<String, List<Modifier>>();
    private final java.util.Map<String, List<Ability>> abilities = new HashMap<String, List<Ability>>();
	private final List<Scope> scopes = new ArrayList<Scope>();
    
	public ObjectWithFeatures(String id) {
		super(id);
	}
	
	public int modifiersAmount() {
	    return modifiers.size();
	}
	
	public int abilitiesAmount() {
	    return abilities.size();
	}
	
	public int requiredAbilitiesAmount() {
		return requiredAbilities.size();
	}
	
	public void addAbility(Ability ability) {
	    addToMapList(abilities, ability.getId(), ability);
	}
	
	public void addAbility(String id, boolean value) {
	    Ability a = new Ability(id, value);
	    addToMapList(abilities, a.getId(), a);
	}

    public void addModifier(Modifier modifier) {
        addToMapList(modifiers, modifier.getId(), modifier);
    }
	
	public void addModifierFrom(ObjectWithFeatures source, String modifierId) {
		List<Modifier> list = source.modifiers.get(modifierId);
		if (list != null) {
			for (Modifier m : list) {
				this.addModifier(m);
			}
		}
	}
	
	public void addModifierFrom(ObjectWithFeatures source, String modifierId, ObjectWithFeatures filterObj) {
        List<Modifier> list = source.modifiers.get(modifierId);
        if (list == null || list.isEmpty()) {
            return;
        }
        for (int i=0; i<list.size(); i++) {
            Modifier m = list.get(i);
            if (m.canAppliesTo(filterObj)) {
            	this.addModifier(m);
            }
        }
	}
    
	private <T> void addToMapList(java.util.Map<String,List<T>> mapList, String id, T obj) {
	    List<T> list = mapList.get(id);
	    if (list == null) {
	        list = new ArrayList<T>();
	        mapList.put(id, list);
	    }
	    list.add(obj);
	}
	
    public boolean hasAbility(String code) {
    	return hasAbility(code, true);
    }

    public void getAbilities(String code, List<Ability> abilities) {
    	getAbilities(code, true, abilities);
    }

	public boolean hasAbility(String abilityCode, boolean value) {
        List<Ability> list = abilities.get(abilityCode);
        if (list == null || list.isEmpty()) {
            return false;
        }
        for (int i=0; i<list.size(); i++) {
        	Ability a = list.get(i);
        	if (a.isValueEquals(value)) {
        		return true;
        	}
        }
        return false;
	}

	public void getAbilities(String code, boolean value, List<Ability> returnedAbilities) {
        List<Ability> list = abilities.get(code);
        if (list == null || list.isEmpty()) {
            return;
        }

        for (int i=0; i<list.size(); i++) {
        	Ability a = list.get(i);
        	if (a.isValueEquals(value)) {
        		returnedAbilities.add(a);
        	}
        }
	}
	
	public boolean hasAbility(Ability ability) {
		List<Ability> list = abilities.get(ability.getId());
        if (list == null || list.isEmpty()) {
            return false;
        }
        for (int i=0; i<list.size(); i++) {
        	Ability a = list.get(i);
        	if (a.isValueEquals(ability)) {
        		return true;
        	}
        }
        return false;
	}
	
	public boolean canApplyAbilityToObject(String abilityCode, ObjectWithFeatures obj) {
		List<Ability> list = abilities.get(abilityCode);
		if (list == null || list.isEmpty()) {
			return false;
		}
		
		boolean foundAbility = false;
        for (int i=0; i<list.size(); i++) {
        	Ability a = list.get(i);
        	if (a.canApplyTo(obj)) {
        		if (a.isValueEquals(false)) {
        			return false;
        		}
        		foundAbility = true;
        	}
        }
        return foundAbility;
	}
	
	public boolean hasRequiredAbility(String reqAbilityCode, boolean reqValue) {
		Ability ability = requiredAbilities.getByIdOrNull(reqAbilityCode);
		if (ability == null) {
			return false;
		}
		return ability.isValueEquals(reqValue);
	}

	public boolean hasAbilitiesRequiredFrom(ObjectWithFeatures requiredAbilities) {
		for (Ability reqAbility : requiredAbilities.requiredAbilities.entities()) {
			if (!hasAbility(reqAbility)) {
				return false;
			}
		}
		return true;
	}
	
    public boolean hasModifier(String code) {
        List<Modifier> list = modifiers.get(code);
        return list != null && !list.isEmpty();
    }
    
	public boolean hasModifier(String code, ModifierPredicate predicate) {
		List<Modifier> list = modifiers.get(code);
		if (list == null || list.isEmpty()) {
			return false;
		}
		for (Modifier m : list) {
			if (predicate.apply(m)) {
				return true;
			}
		}
		return false;
	}
    
    public float applyModifier(String modifierName, float base, ObjectWithFeatures obj) {
        List<Modifier> list = modifiers.get(modifierName);
        if (list == null || list.isEmpty()) {
            return base;
        }
        for (int i=0; i<list.size(); i++) {
            Modifier m = list.get(i);
            if (m.canAppliesTo(obj)) {
                base = m.apply(base);
            }
        }
        return base;
    }
    
    public float applyModifier(String modifierName, float base) {
        List<Modifier> list = modifiers.get(modifierName);
        if (list == null || list.isEmpty()) {
            return base;
        }
        for (int i=0; i<list.size(); i++) {
        	Modifier m = list.get(i);
        	base = m.apply(base);
        }
		return base;
    }

	public float applyModifiers(final float base) {
		float b = base;
		for (Entry<String, List<Modifier>> modifiersEntryList : modifiers.entrySet()) {
			if (modifiersEntryList.getValue() == null || modifiersEntryList.getValue().isEmpty()) {
				continue;
			}
			for (Modifier m : modifiersEntryList.getValue()) {
				b = m.apply(b);
			}
		}
		return b;
	}
    
    public void addFeaturesAndOverwriteExisted(ObjectWithFeatures parent) {
    	for (Entry<String, List<Ability>> entrySet : parent.abilities.entrySet()) {
    		this.abilities.put(entrySet.getKey(), new ArrayList<Ability>(entrySet.getValue()));
    	}
    	for (Entry<String, List<Modifier>> entrySet : parent.modifiers.entrySet()) {
    		this.modifiers.put(entrySet.getKey(), new ArrayList<Modifier>(entrySet.getValue()));
    	}
    }
    
    public void addFeatures(ObjectWithFeatures parent) {
        for (Entry<String, List<Ability>> entrySet : parent.abilities.entrySet()) {
            for (Ability a : entrySet.getValue()) {
                addAbility(a);
            }
        }
        for (Entry<String, List<Modifier>> entrySet : parent.modifiers.entrySet()) {
            for (Modifier m : entrySet.getValue()) {
                addModifier(m);
            }
        }
    }
    
    public void clear() {
        abilities.clear();
        modifiers.clear();
    }
    
    public boolean canApplyAllScopes(ObjectWithFeatures obj) {
    	for (Scope scope : scopes) {
    		if (!scope.isAppliesTo(obj)) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public String modifiersToString() {
    	String st = "";
    	for (Entry<String, List<Modifier>> entry : modifiers.entrySet()) {
    		if (st.length() > 0) {
    			st += ", ";
    		}
    		st += "modifier name: " + entry.getKey();
    		st += "[\n";
    		if (entry.getValue() != null) {
    			for (Modifier m : entry.getValue()) {
    				st += "[" + m.toString() + "],\n";
    			}
    		}
    		st += " ]";
    	}
    	return st;
    }
    
    public static class Xml {
		public static void abstractAddNodes(XmlNodeParser<? extends ObjectWithFeatures> nodeParser) {
			nodeParser.addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
			nodeParser.addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
			nodeParser.addNode("required-ability", Ability.class, ObjectWithFeatures.REQ_ABILITY_NODE_SETTER);
		}
		
		public static void abstractStartElement(XmlNodeAttributes attr, ObjectWithFeatures obj) {
		}

		public static void abstractStartWriteAttr(ObjectWithFeatures obj, XmlNodeAttributesWriter attr) throws IOException {
		}
		
    }
}
