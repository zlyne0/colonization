package net.sf.freecol.common.model.specification;

import java.io.IOException;
import java.lang.reflect.Method;

import net.sf.freecol.common.model.ObjectWithFeatures;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class Scope {

	private boolean matchNegated = false;
	private boolean matchesNull = true;
	private String abilityId = null;
	private boolean abilityValue = true;
	private String type;
	
	private String methodName;
	private String methodValue;
	
	public boolean isAppliesTo(ScopeAppliable obj) {
		if (obj == null) {
			return matchesNull;
		}
		if (type != null) {
			if (!type.equals(obj.getId())) {
				return matchNegated;
			}
		}
		if (abilityId != null) {
			// case when obj has not ability and ability value  is false so its ok
		    if (obj.hasAbility(abilityId) != abilityValue) {
		        return matchNegated;
		    }
		}
		if (methodName != null) {
			try {
				Method method = obj.getClass().getMethod(methodName);
				Object retObj = method.invoke(obj);
				return methodValue.equalsIgnoreCase(String.valueOf(retObj));
			} catch (Exception e) {
				e.printStackTrace();
				return matchNegated;
			}
		}
		return !matchNegated;
	}

	public String getType() {
		return type;
	}
	
	public String toString() {
		return "matchNegated = " + matchNegated + ", matchesNull = " + matchesNull 
				+ ", abilityId = " + abilityId + ", abilityValue = " + abilityValue 
				+ ", type = " + type + ", methodName = " + methodName + ", metchodValue = " + methodValue;
	}
	
	public static class Xml extends XmlNodeParser<Scope> {

		private static final String METHOD_VALUE = "method-value";
		private static final String METHOD_NAME = "method-name";
		private static final String TYPE2 = "type";
		private static final String ABILITY_VALUE = "ability-value";
		private static final String ABILITY_ID = "ability-id";
		private static final String MATCHES_NULL = "matchesNull";
		private static final String MATCH_NEGATED = "matchNegated";

		@Override
		public void startElement(XmlNodeAttributes attr) {
			Scope scope = new Scope();
			scope.matchNegated = attr.getBooleanAttribute(MATCH_NEGATED, false);
			scope.matchesNull = attr.getBooleanAttribute(MATCHES_NULL, true);
			scope.abilityId = attr.getStrAttribute(ABILITY_ID);
			scope.abilityValue = attr.getBooleanAttribute(ABILITY_VALUE, true);
			scope.type = attr.getStrAttribute(TYPE2);
			
			scope.methodName = attr.getStrAttribute(METHOD_NAME);
			scope.methodValue = attr.getStrAttribute(METHOD_VALUE);
			
			nodeObject = scope;
		}

		@Override
		public void startWriteAttr(Scope scope, XmlNodeAttributesWriter attr) throws IOException {
			attr.set(MATCH_NEGATED, scope.matchNegated);
			attr.set(MATCHES_NULL, scope.matchesNull);
			attr.set(ABILITY_ID, scope.abilityId);
			attr.set(ABILITY_VALUE, scope.abilityValue);
			attr.set(TYPE2, scope.type);
			
			attr.set(METHOD_NAME, scope.methodName);
			attr.set(METHOD_VALUE, scope.methodValue);
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "scope";
		}
	}
}
