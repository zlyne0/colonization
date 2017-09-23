package promitech.colonization.savegame;

import org.assertj.core.api.AbstractAssert;

import net.sf.freecol.common.model.ObjectWithFeatures;

class ObjectWithFeaturesAssert extends AbstractAssert<ObjectWithFeaturesAssert, ObjectWithFeatures> {

	public ObjectWithFeaturesAssert(ObjectWithFeatures features, Class<?> selfType) {
		super(features, selfType);
	}
	
	public static ObjectWithFeaturesAssert assertThat(ObjectWithFeatures features) {
		return new ObjectWithFeaturesAssert(features, ObjectWithFeaturesAssert.class);
	}

	public ObjectWithFeaturesAssert hasModifier(String modCode) {
		isNotNull();
		if (!actual.hasModifier(modCode)) {
			failWithMessage("ObjectWithFeatures %s has not modifier %s", actual.getId(), modCode);
		}
		return this;
	}
}