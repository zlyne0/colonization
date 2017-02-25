package promitech.colonization.savegame;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.SavedGame;

class SaveGameListData {
	protected List<SaveGameElement> saves = new ArrayList<SaveGameElement>();
	
	public void add(SaveGameElement ele) {
		saves.add(ele);
	}

	public SaveGameElement last() {
		if (saves.isEmpty()) {
			return null;
		}
		Collections.sort(saves, SaveGameElement.LAST_ON_FIRST);
		return saves.get(0);
	}
	
	public SaveGameElement getSaveByName(String name) {
		for (SaveGameElement save : saves) {
			if (save.name.equals(name)) {
				return save;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "SaveGameListData [saves=" + saves + "]";
	}
}

class SaveGameElement {
	
	public static Comparator<SaveGameElement> LAST_ON_FIRST = new Comparator<SaveGameElement>() {
		@Override
		public int compare(SaveGameElement o1, SaveGameElement o2) {
			return (int)(o2.date - o1.date);
		}
	};
	
	protected String fileName;
	protected String name;
	protected long date;
	
	public SaveGameElement() {
	}
	
	public SaveGameElement(String fileName, String name) {
		this.fileName = fileName;
		this.name = name;
		this.date = System.currentTimeMillis();
	}
	
	public SaveGameElement updateDateToNow() {
		this.date = System.currentTimeMillis();
		return this;
	}
	
	public String toString() {
		return "" + fileName + " " + name + " " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(date));
	}
}

public class SaveGameList {

	private static final String QUICK_SAVE_NAME = "Quick save";
	private static final String SAVE_GAME_LIST_FILE_NAME = "saveGameList";

	public void saveAsQuick(Game game) {
		
		SaveGameListData saveGameList = loadGameList();
		SaveGameElement quickSaveElement = saveGameList.getSaveByName(QUICK_SAVE_NAME);
		if (quickSaveElement == null) {
			quickSaveElement = new SaveGameElement("" + System.currentTimeMillis() + ".xml", QUICK_SAVE_NAME);
			saveGameList.add(quickSaveElement);
		} else {
			quickSaveElement.updateDateToNow();
		}

        SavedGame savedGameObj = new SavedGame();
        savedGameObj.game = game;
		
		FileHandle sgfh = Gdx.files.local(quickSaveElement.fileName);
		Writer writer = sgfh.writer(false);
		try {
			new SaveGameCreator(writer).generateXmlFrom(savedGameObj);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		
		saveGameList(saveGameList);
	}
	
	public Game loadLast() {
		SaveGameListData loadGameList = loadGameList();
		
		SaveGameElement lastElement = loadGameList.last();
		if (lastElement == null) {
			return null;
		}
		
		FileHandle sgfh = Gdx.files.local(lastElement.fileName);
		
		try {
			return new SaveGameParser("").parse(sgfh.read());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	private SaveGameListData loadGameList() {
		FileHandle fh = Gdx.files.local(SAVE_GAME_LIST_FILE_NAME);
		System.out.println("load saves list file " + fh.file().getAbsolutePath());
		if (fh.isDirectory()) {
			throw new IllegalStateException("can not write to " + SAVE_GAME_LIST_FILE_NAME);
		}
		if (!fh.exists()) {
			return new SaveGameListData();
		}
		
		Json jsonObj = new Json();
		jsonObj.setOutputType(OutputType.json);
		return jsonObj.fromJson(SaveGameListData.class, fh);
	}
	
	private void saveGameList(SaveGameListData saveGameListData) {
		FileHandle fh = Gdx.files.local(SAVE_GAME_LIST_FILE_NAME);
		System.out.println("save list file " + fh.file().getAbsolutePath());
		if (fh.isDirectory()) {
			throw new IllegalStateException("can not write to " + SAVE_GAME_LIST_FILE_NAME);
		}
		
		Json jsonObj = new Json();
		jsonObj.setOutputType(OutputType.json);
		String jsonStr = jsonObj.prettyPrint(saveGameListData);
		
		OutputStreamWriter outputStream = new OutputStreamWriter(fh.write(false));
		try {
			outputStream.write(jsonStr);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}
	
}
