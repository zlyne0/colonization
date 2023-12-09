package net.sf.freecol.common.model.map.path;

import net.sf.freecol.common.model.Tile;

public interface TileConsumer {

    TileConsumer EMPTY = new TileConsumer() {
        @Override
        public Status consume(Tile tile, int turns) {
            return Status.PROCESS;
        }
    };

    enum Status {
        END, PROCESS;
    }

    Status consume(Tile tile, int turns);
}
