package net.sf.freecol.common.model;

public class ObjectWithFeatures extends ObjectWithId {

    public final MapIdEntities<Modifier> modifiers = new MapIdEntities<Modifier>();
    public final MapIdEntities<Ability> abilities = new MapIdEntities<Ability>();
	
	public ObjectWithFeatures(String id) {
		super(id);
	}
	
	public void addAbility(String id, boolean value) {
		abilities.add(new Ability(id, value));
	}
	
    public boolean hasAbility(String code) {
    	Ability ability = abilities.getByIdOrNull(code);
    	return ability != null && ability.isValue();
    }
    
    public boolean hasModifier(String code) {
    	Modifier modifier = modifiers.getByIdOrNull(code);
    	return modifier != null;
    }
    
    public float applyModifier(String modifierName, float base) {
		Modifier modifier = modifiers.getByIdOrNull(modifierName);
		if (modifier != null) {
			base = modifier.apply(base);
		}
		return base;
    }
}
