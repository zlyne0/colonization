package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.BuildableType;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.Scope;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class ObjectWithFeatures extends ObjectWithId {
    
    public static final ObjectFromNodeSetter<ObjectWithFeatures, Modifier> OBJECT_MODIFIER_NODE_SETTER = new ObjectFromNodeSetter<ObjectWithFeatures, Modifier>() {
        @Override
        public void set(ObjectWithFeatures target, Modifier entity) {
            target.addModifier(entity);
        }
    };
    
    public static final ObjectFromNodeSetter<ObjectWithFeatures, Ability> OBJECT_ABILITY_NODE_SETTER = new ObjectFromNodeSetter<ObjectWithFeatures, Ability>() {
        @Override
        public void set(ObjectWithFeatures target, Ability entity) {
            target.addAbility(entity);
        }
    };
    
    public static final ObjectFromNodeSetter<ObjectWithFeatures, Scope> OBJECT_SCOPE_NODE_SETTER = new ObjectFromNodeSetter<ObjectWithFeatures, Scope>() {
        @Override
        public void set(ObjectWithFeatures target, Scope entity) {
        	target.scopes.add(entity);
        }
    };
    
    
    public final MapIdEntities<Ability> requiredAbilities = new MapIdEntities<Ability>();
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
	
	private <T> void addToMapList(java.util.Map<String,List<T>> mapList, String id, T obj) {
	    List<T> list = mapList.get(id);
	    if (list == null) {
	        list = new ArrayList<T>();
	        mapList.put(id, list);
	    }
	    list.add(obj);
	}
	
    public boolean isAvailableTo(ObjectWithFeatures features) {
        if (requiredAbilities != null) {
            for (Ability aa : requiredAbilities.entities()) {
                boolean found = features.hasAbility(aa.getId());
                if (aa.isValueNotEquals(found)) {
                    return false;
                }
            }
        }
        return true;
    }
	
    public boolean hasAbility(String abilityCode) {
    	return hasAbility(abilityCode, true);
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
        for (int i=0; i<list.size(); i++) {
        	Ability a = list.get(i);
        	if (a.canApplyTo(obj)) {
        		return true;
        	}
        }
        return false;
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

    public void addFeaturesAndOverwriteExisted(ObjectWithFeatures parent) {
        this.modifiers.putAll(parent.modifiers);
        this.abilities.putAll(parent.abilities);
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
    	for (Entry<String, List<Ability>> entrySet : abilities.entrySet()) {
    		entrySet.getValue().clear();
    	}
        for (Entry<String, List<Modifier>> entrySet : modifiers.entrySet()) {
        	entrySet.getValue().clear();
        }
        abilities.clear();
        modifiers.clear();
    }
    
    public boolean canApplyAllScopes(Identifiable obj) {
    	for (Scope scope : scopes) {
    		if (!scope.isAppliesTo(obj)) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public static class Xml {
		public static void abstractAddNodes(XmlNodeParser nodeParser) {
			nodeParser.addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
			nodeParser.addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
			nodeParser.addNode("required-ability", Ability.class, "requiredAbilities");
		}
		
		public static void abstractStartElement(XmlNodeAttributes attr, BuildableType bt) {
		}
    }
}
