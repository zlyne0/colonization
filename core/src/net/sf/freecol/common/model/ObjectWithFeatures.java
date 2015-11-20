package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import promitech.colonization.savegame.ObjectFromNodeSetter;

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
    
    private java.util.Map<String, List<Modifier>> modifiers = new HashMap<String, List<Modifier>>();
    private java.util.Map<String, List<Ability>> abilities = new HashMap<String, List<Ability>>();
    
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
	
    public boolean hasAbility(String code) {
        List<Ability> list = abilities.get(code);
        if (list == null || list.isEmpty()) {
            return false;
        }
        for (Ability a : list) {
            if (a.isValue()) {
                return true;
            }
        }
        return false;
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
        for (Modifier m : list) {
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
}
