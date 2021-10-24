package net.sf.freecol.common.model;

import org.assertj.core.api.AbstractAssert;

public class MapIdEntitiesAssert extends AbstractAssert<MapIdEntitiesAssert, MapIdEntitiesReadOnly<?>> {

	private final MapIdEntitiesReadOnly<?> preview;

	public MapIdEntitiesAssert(MapIdEntitiesReadOnly<?> actual, Class<?> selfType) {
		super(actual, selfType);
		this.preview = null;
	}

	public MapIdEntitiesAssert(MapIdEntitiesReadOnly<?> actual, MapIdEntitiesReadOnly<?> preview, Class<?> selfType) {
		super(actual, selfType);
		this.preview = preview;
	}

	public static MapIdEntitiesAssert assertThat(MapIdEntitiesReadOnly<?> map) {
		return new MapIdEntitiesAssert(map, MapIdEntitiesAssert.class);
	}

	public static MapIdEntitiesAssert assertThat(MapIdEntitiesReadOnly<?> map, MapIdEntitiesReadOnly<?> previewMap) {
		return new MapIdEntitiesAssert(map, previewMap, MapIdEntitiesAssert.class);
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

    public MapIdEntitiesAssert hasSize(int size) {
        if (actual.size() != size) {
            failWithMessage("expected has size <%s> but has size <%s>", size, actual.size());
        }
        return this;
    }
	
	public MapIdEntitiesAssert hasNewSizeMore(int size) {
		if (preview == null) {
			failWithMessage("preview state not initialized");
		}
		if (preview.size() + size != actual.size()) {
			failWithMessage("expected new size be greater about <%s> then preview size", size);
		}
		return this;
	}
}
