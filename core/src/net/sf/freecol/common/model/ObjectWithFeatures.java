package net.sf.freecol.common.model;

public class ObjectWithFeatures extends ObjectWithId {

	// TODO: te cechy nie moge byc mapy, ewentualnie moga byc mapa list, zle sie liczy warehouse capacity
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
    

    public void addFeatures(ObjectWithFeatures parent) {
        this.modifiers.addAll(parent.modifiers);
        this.abilities.addAll(parent.abilities);
    }
    
}
