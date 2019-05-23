package promitech.colonization.screen.debug;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.IntMap;

import promitech.colonization.DI;
import promitech.colonization.screen.map.MapActor;
import promitech.colonization.screen.map.hud.HudStage;

public class DebugShortcutsKeys {
    private MapActor mapActor;
    private HudStage hudStage;
    private final IntMap<String> commandByKeycode = new IntMap<String>();
    
    private final ConsoleOutput routeOutput = new ConsoleOutput() {
        @Override
        public void out(String line) {
            if (actualOutput != null) {
                actualOutput.out(line);
            }
        }

        @Override
        public void keepOpen() {
            if (actualOutput != null) {
                actualOutput.keepOpen();
            }
        }
    };

    private final ConsoleOutput stdout = new ConsoleOutput() {
        @Override
        public void out(String line) {
            System.out.println("debugconsole: " + line);
        }

        @Override
        public void keepOpen() {
        }
    };
    
    private ConsoleOutput actualOutput;
    private Commands commands;
    
    public DebugShortcutsKeys(HudStage hudStage, DI di, MapActor mapActor) {
        this.hudStage = hudStage;
        this.mapActor = mapActor;
        
        commands = CommandDefinitionKt.createCommands(di, routeOutput, mapActor);
        
        commandByKeycode.put(Input.Keys.NUM_1, "foundingFather");
        commandByKeycode.put(Input.Keys.NUM_2, "firstContactDialog");
        commandByKeycode.put(Input.Keys.NUM_3, "ai attack");
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
            actualOutput = stdout;
            commands.execute(cmd);
        }
    }

    public void showCheatConsoleDialog() {
        DebugConsole debugConsole = new DebugConsole(commands);
        actualOutput = debugConsole;
        debugConsole.setSelectedTile(mapActor.mapDrawModel().selectedTile);
        hudStage.showDialog(debugConsole);
    }
    
}
