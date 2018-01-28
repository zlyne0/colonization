package net.sf.freecol.common.model;

import org.assertj.core.api.AbstractAssert;

public class MapIdEntitiesAssert extends AbstractAssert<MapIdEntitiesAssert, MapIdEntitiesReadOnly<?>> {

	public MapIdEntitiesAssert(MapIdEntitiesReadOnly<?> actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static MapIdEntitiesAssert assertThat(MapIdEntitiesReadOnly<?> map) {
		return new MapIdEntitiesAssert(map, MapIdEntitiesAssert.class);
	}
	
	public MapIdEntitiesAssert containsId(String id) {
		isNotNull();
		if (!actual.containsId(id)) {
			failWithMessage("expected contain entity id <%s>", id);
		}
		return this;
	}

	public MapIdEntitiesAssert containsId(Identifiable identifiable) {
		isNotNull();
		if (!actual.containsId(identifiable)) {
			failWithMessage("expected contain entity id <%s>", identifiable.getId());
		}
		return this;
	}

	public MapIdEntitiesAssert isEmpty() {
		if (actual.isNotEmpty()) {
			failWithMessage("expected to be empty");
		}
		return this;
	}
	
	
}
