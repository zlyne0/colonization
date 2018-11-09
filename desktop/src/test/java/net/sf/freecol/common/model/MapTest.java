package net.sf.freecol.common.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import promitech.colonization.savegame.SaveGameParser;

public class MapTest {
    
    Game game; 
    
    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }

    @Before
    public void before() throws Exception {
        game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    }
    
    
    @Test
    public void shouldReturnOnlyNeighboursTiles() throws Exception {
        // given
        Tile sourceTile = game.map.getSafeTile(24, 78);
        List<String> tiles = new ArrayList<String>();
        
        // when
        for (Tile tile : game.map.neighbourTiles(sourceTile)) {
            tiles.add(tile.getId());
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
        for (Tile tile : game.map.neighbourLandTiles(sourceTile)) {
            tiles.add(tile.getId());
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
        for (Tile tile : game.map.neighbourWaterTiles(sourceTile)) {
            tiles.add(tile.getId());
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
        for (Tile tile : game.map.neighbourTiles(sourceTile)) {
            tiles.add(tile.getId());
        }

        // then
        assertThat(tiles).containsExactly(
            "tile:4167",
            "tile:4246",
            "tile:4207"                
        );
    }
}
