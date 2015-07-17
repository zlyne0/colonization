package net.sf.freecol.common.model;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;

public class UnitType implements Identifiable {
	
	public static final String FREE_COLONIST = "model.unit.freeColonist";
	
    private static final int DEFAULT_LINE_OF_SIGHT = 1;
    public static final int DEFAULT_MOVEMENT = 3;
    public static final int DEFAULT_OFFENCE = 0;
    public static final int DEFAULT_DEFENCE = 1;

    private String id;
    
    /**
     * The offence of this UnitType. Only Units with an offence value
     * greater than zero can attack.
     */
    private int offence = DEFAULT_OFFENCE;

    /** The defence of this UnitType. */
    private int defence = DEFAULT_DEFENCE;

    /** The capacity of this UnitType. */
    private int space = 0;

    /**
     * The number of hit points this UnitType has. At the moment, this
     * is only used for ships. All other UnitTypes are downgraded or
     * destroyed if they lose a battle.
     */
    private int hitPoints = 0;

    /** The space taken by this UnitType. */
    private int spaceTaken = 1;

    /** The skill level of this UnitType. */
    private int skill = Xml.UNDEFINED;

    /** The price of this UnitType. */
    private int price = Xml.UNDEFINED;

    /** The initial moves of this UnitType. */
    private int movement = DEFAULT_MOVEMENT;

    /** The maximum distance of tiles this UnitType can observe. */
    private int lineOfSight = DEFAULT_LINE_OF_SIGHT;

    /** The probability of recruiting a Unit of this type in Europe. */
    private int recruitProbability = 0;

    /** How much a Unit of this type contributes to the Player's score. */
    private int scoreValue = 0;

    /** The maximum experience a unit of this type can accumulate. */
    private int maximumExperience = 0;

    /**
     * The maximum attrition this UnitType can accumulate without
     * being destroyed.
     */
    private int maximumAttrition = Xml.INFINITY;
    
    public UnitType(String id) {
        this.id = id;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    public String toString() {
        return id;
    }
    
    public static class Xml extends XmlNodeParser {
        public Xml(XmlNodeParser parent) {
            super(parent);
        }

        @Override
        public void startElement(String qName, Attributes attributes) {
            String id = getStrAttribute(attributes, "id");
            
            UnitType ut = new UnitType(id);
            ut.offence = getIntAttribute(attributes, "offence", DEFAULT_OFFENCE);
            ut.defence = getIntAttribute(attributes, "defence", DEFAULT_DEFENCE);
            ut.movement = getIntAttribute(attributes, "movement", DEFAULT_MOVEMENT);
            ut.lineOfSight = getIntAttribute(attributes, "lineOfSight", DEFAULT_LINE_OF_SIGHT);
            ut.scoreValue = getIntAttribute(attributes, "scoreValue", 0);
            ut.space = getIntAttribute(attributes, "space", 0);
            ut.spaceTaken = getIntAttribute(attributes, "spaceTaken", 1);
            ut.hitPoints = getIntAttribute(attributes, "hitPoints", 0);
            ut.maximumExperience = getIntAttribute(attributes, "maximumExperience", 0);
            ut.recruitProbability = getIntAttribute(attributes, "recruitProbability", 0);
            ut.skill = getIntAttribute(attributes, "skill", Xml.UNDEFINED);
            ut.price = getIntAttribute(attributes, "price", Xml.UNDEFINED);
            
            specification.unitTypes.add(ut);
        }

        @Override
        public String getTagName() {
            return "unit-type";
        }
    }
}
