package net.sf.freecol.common.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;

import promitech.colonization.savegame.SaveGameParser;
import promitech.map.isometric.IsometricMapAssert;
import promitech.map.isometric.NeighbourIterableTile;

public class MapTest {
    
    static Game game; 
    
    @BeforeAll
    public static void beforeClass() throws Exception {
        Gdx.files = new Lwjgl3Files();
        
        game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    }

    @AfterAll
    public static void afterClass() {
        IsometricMapAssert.hasAutoFreePoolableTileIterablePoolSize(1);        
        game = null;
    }
    
    @Test
    public void shouldReturnOnlyNeighboursTiles() throws Exception {
        // given
        Tile sourceTile = game.map.getSafeTile(24, 78);
        List<String> tiles = new ArrayList<String>();
        
        // when
        for (NeighbourIterableTile<Tile> it : game.map.neighbourTiles(sourceTile)) {
            tiles.add(it.tile.getId());
        }

        // then
        assertThat(tiles).containsExactly(
            "tile:3312", 
            "tile:3352", 
            "tile:3393", 
            "tile:3432", 
            "tile:3472", 
            "tile:3431", 
            "tile:3391", 
            "tile:3351"
        );
    }

    @Test
    public void shouldReturnNeighbourLandTiles() throws Exception {
        // given
        Tile sourceTile = game.map.getSafeTile(24, 78);
        List<String> tiles = new ArrayList<String>();
        
        // when
        for (NeighbourIterableTile<Tile> it : game.map.neighbourLandTiles(sourceTile)) {
            tiles.add(it.tile.getId());
        }

        // then
        assertThat(tiles).containsExactly(
            "tile:3312",
            "tile:3352",
            "tile:3431",
            "tile:3391",
            "tile:3351"             
        );
    }
    
    @Test
    public void shouldReturnNeighbourSeaTiles() throws Exception {
        // given
        Tile sourceTile = game.map.getSafeTile(24, 78);
        List<String> tiles = new ArrayList<String>();
        
        // when
        for (NeighbourIterableTile<Tile> it : game.map.neighbourWaterTiles(sourceTile)) {
            tiles.add(it.tile.getId());
        }

        // then
        assertThat(tiles).containsExactly(
            "tile:3393",
            "tile:3432",
            "tile:3472"                
        );
    }
    
    @Test
    public void canReturnNeighbourTilesOnMapEdge() throws Exception {
        // given
        Tile sourceTile = game.map.getSafeTile(39, 99);
        List<String> tiles = new ArrayList<String>();
        
        // when
        for (NeighbourIterableTile<Tile> it : game.map.neighbourTiles(sourceTile)) {
            tiles.add(it.tile.getId());
        }

        // then
        assertThat(tiles).containsExactly(
            "tile:4167",
            "tile:4246",
            "tile:4207"                
        );
    }
    
    @Test
    public void canReturnTilesByRadius() throws Exception {
        // given
        Tile sourceTile = game.map.getSafeTile(24, 78);
        List<String> tiles = new ArrayList<String>();

        // when
        for (Tile tile : game.map.neighbourTiles(sourceTile, 1)) {
            tiles.add(tile.getId());
        }

        // then
        assertThat(tiles).contains(
            "tile:3312", 
            "tile:3352", 
            "tile:3393", 
            "tile:3432", 
            "tile:3472", 
            "tile:3431", 
            "tile:3391", 
            "tile:3351"
        );
    }
    
    @Test
    public void canReturnTilesByRadiusOnMapEdge() throws Exception {
        // given
        Tile sourceTile = game.map.getSafeTile(39, 99);
        List<String> tiles = new ArrayList<String>();
        
        // when
        for (Tile tile : game.map.neighbourTiles(sourceTile, 1)) {
            tiles.add(tile.getId());
        }

        // then
        assertThat(tiles).contains(
            "tile:4167",
            "tile:4246",
            "tile:4207"                
        );
    }

    @Test
    public void canReturnTilesByRadiusOnMapCorner() throws Exception {
        // given
        Tile sourceTile = game.map.getSafeTile(0, 0);
        List<String> tiles = new ArrayList<String>();
        
        // when
        for (Tile tile : game.map.neighbourTiles(sourceTile, 2)) {
            tiles.add(tile.getId());
        }

        // then
        assertThat(tiles).containsExactly(
            "tile:249",
            "tile:288",
            "tile:328",
            "tile:250",
            "tile:289",
            "tile:329",
            "tile:368",
            "tile:408"
        );
    }
    
}
