package promitech.colonization.savegame;

import java.io.File;
import java.io.FileWriter;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.SavedGame;

public class SaveGameTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	
    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }
	
	@Before
	public void setup() {
	}

	@Test
	public void canCreateXmlAndLoadIt() throws Exception {
		// given
        Game jUnitGame = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
        SavedGame savedGameObj = new SavedGame();
        savedGameObj.game = jUnitGame;
        
        File tmpSaveFile = tempFolder.newFile();
        System.out.println("tmp file " + tmpSaveFile);
        
		// when
        FileWriter saveXmlWriter = new FileWriter(tmpSaveFile);
        new SaveGameCreator(saveXmlWriter).generateXmlFrom(savedGameObj);
        saveXmlWriter.close();
        
        Game loadedGame = SaveGameParser.loadGameFromFile(tmpSaveFile);
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
