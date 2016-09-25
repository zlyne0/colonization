package net.sf.freecol.common.model.specification;

import java.lang.reflect.Method;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.ObjectWithFeatures;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Scope implements Identifiable {

	private boolean matchNegated = false;
	private boolean matchesNull = true;
	private String abilityId = null;
	private boolean abilityValue = true;
	private String type;
	
	private String methodName;
	private String methodValue;
	
	@Override
	public String getId() {
		throw new IllegalStateException("there is no id for feature scope");
	}
	
	public boolean isAppliesTo(ObjectWithFeatures obj) {
		if (obj == null) {
			return matchesNull;
		}
		if (type != null) {
			if (!type.equals(obj.getId())) {
				return matchNegated;
			}
		}
		if (abilityId != null) {
		    if (!obj.hasAbility(abilityId, abilityValue)) {
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
	
	public String toString() {
		return "matchNegated = " + matchNegated + ", matchesNull = " + matchesNull 
				+ ", abilityId = " + abilityId + ", abilityValue = " + abilityValue 
				+ ", type = " + type + ", methodName = " + methodName + ", metchodValue = " + methodValue;
	}
	
	public static class Xml extends XmlNodeParser {

		@Override
		public void startElement(XmlNodeAttributes attr) {
			Scope scope = new Scope();
			scope.matchNegated = attr.getBooleanAttribute("matchNegated", false);
			scope.matchesNull = attr.getBooleanAttribute("matchesNull", true);
			scope.abilityId = attr.getStrAttribute("ability-id");
			scope.abilityValue = attr.getBooleanAttribute("ability-value", true);
			scope.type = attr.getStrAttribute("type");
			
			scope.methodName = attr.getStrAttribute("method-name");
			scope.methodValue = attr.getStrAttribute("method-value");
			
			nodeObject = scope;
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
