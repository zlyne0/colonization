package promitech.colonization.screen.debug;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.IntMap;

import promitech.colonization.DI;
import promitech.colonization.screen.debug.CommandExecutor;
import promitech.colonization.screen.debug.ConsoleOutput;
import promitech.colonization.screen.debug.DebugConsole;
import promitech.colonization.screen.map.MapActor;
import promitech.colonization.screen.map.hud.HudStage;

public class DebugShortcutsKeys {
    private MapActor mapActor;
    private HudStage hudStage;
    private final IntMap<String> commandByKeycode = new IntMap<String>();
    private final ConsoleOutput stdConsoleOutput = new ConsoleOutput() {
        @Override
        public void addConsoleLine(String line) {
            System.out.println("debugconsole: " + line);
        }
    };
    
    private final CommandExecutor commandExecutor;
    
    public DebugShortcutsKeys(HudStage hudStage, DI di, MapActor mapActor) {
        this.hudStage = hudStage;
        this.mapActor = mapActor;
        
        commandExecutor = new CommandExecutor(di, mapActor);
        
        commandByKeycode.put(Input.Keys.NUM_1, "ai attack");
        commandByKeycode.put(Input.Keys.NUM_2, "firstContactDialog");
//        commandByKeycode.put(Input.Keys.NUM_1, "ai move");
//        commandByKeycode.put(Input.Keys.NUM_2, "ai settlements");
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
            commandExecutor.execute(cmd, stdConsoleOutput);
        }
    }

    public void showCheatConsoleDialog() {
        DebugConsole debugConsole = new DebugConsole(commandExecutor);
        debugConsole.setSelectedTile(mapActor.mapDrawModel().selectedTile);
        hudStage.showDialog(debugConsole);
    }
    
}
