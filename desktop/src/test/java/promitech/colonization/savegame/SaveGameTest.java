package promitech.colonization.savegame;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.SavedGame;

public class SaveGameTest {

	@TempDir
	Path tempFolder;
	
    @BeforeAll
    public static void beforeClass() {
        Gdx.files = new Lwjgl3Files();
    }
	
	@BeforeEach
	public void setup() {
	}

	@Test
	public void canCreateXmlAndLoadIt() throws Exception {
		// given
        Game jUnitGame = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
        SavedGame savedGameObj = new SavedGame();
        savedGameObj.game = jUnitGame;
        
        Path tmpFile = Files.createTempFile(tempFolder, "save", "");
        System.out.println("tmp file " + tmpFile);
        
		// when
        Writer newBufferedWriter = Files.newBufferedWriter(tmpFile);
        
        new SaveGameCreator(newBufferedWriter).generateXmlFrom(savedGameObj);
        newBufferedWriter.close();
        
        Game loadedGame = SaveGameParser.loadGameFromFile(tmpFile.toFile());
		// then
	
        new Savegame1600Verifier().verify(loadedGame);
	}
	
    @Test
    public void canLoadSaveGame() throws Exception {
        // given

    	// when
        Game game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");

        // then
        new Savegame1600Verifier().verify(game);
    }

	
}
