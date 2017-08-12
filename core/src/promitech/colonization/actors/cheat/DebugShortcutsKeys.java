package promitech.colonization.actors.cheat;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.IntMap;

import promitech.colonization.DI;
import promitech.colonization.actors.map.MapActor;
import promitech.colonization.ui.hud.HudStage;

public class DebugShortcutsKeys {
    private MapActor mapActor;
    private HudStage hudStage;
    private final IntMap<String> commandByKeycode = new IntMap<String>();
    
    private final CommandExecutor commandExecutor;
    
    public DebugShortcutsKeys(HudStage hudStage, DI di, MapActor mapActor) {
        this.hudStage = hudStage;
        this.mapActor = mapActor;
        
        commandExecutor = new CommandExecutor(di, mapActor);
        
        commandByKeycode.put(Input.Keys.NUM_1, "ai move");
        commandByKeycode.put(Input.Keys.NUM_2, "ai settlements");
        commandByKeycode.put(Input.Keys.NUM_5, "map show");
    }

    public boolean canHandleKey(int keycode) {
        if (keycode == Input.Keys.GRAVE) {
            return true;
        }
        return commandByKeycode.containsKey(keycode); 
    }
    
    public void handleKey(int keycode) {
        if (keycode == Input.Keys.GRAVE) {
            showCheatConsoleDialog();
            return;
        }
        if (commandByKeycode.containsKey(keycode)) {
            String cmd = commandByKeycode.get(keycode);
            commandExecutor.execute(cmd);
        }
    }

    public void showCheatConsoleDialog() {
        DebugConsole debugConsole = new DebugConsole(commandExecutor);
        debugConsole.setSelectedTile(mapActor.mapDrawModel().selectedTile);
        hudStage.showDialog(debugConsole);
    }
    
}
