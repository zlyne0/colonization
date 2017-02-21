package promitech.colonization.savegame;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;

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
		SaveGameParser saveGameParser = new SaveGameParser("maps/savegame_1600_for_jtests.xml");
        Game jUnitGame = saveGameParser.parse();
        SavedGame savedGameObj = new SavedGame();
        savedGameObj.game = jUnitGame;
        
        File tmpSaveFile = tempFolder.newFile();
        System.out.println("tmp file " + tmpSaveFile);
        
		// when
        FileWriter saveXmlWriter = new FileWriter(tmpSaveFile);
        new SaveGameCreator(saveXmlWriter).generateXmlFrom(savedGameObj);
        saveXmlWriter.close();
        
        Game loadedGame = new SaveGameParser("bla bla bla").parse(new FileInputStream(tmpSaveFile));
		// then
	
        new Savegame1600Verifier().verify(loadedGame);
	}
	
    @Test
    public void canLoadSaveGame() throws Exception {
        // given
        SaveGameParser saveGameParser = new SaveGameParser("maps/savegame_1600_for_jtests.xml");
        
        // when
        Game game = saveGameParser.parse();

        // then
        new Savegame1600Verifier().verify(game);
    }
	
}
