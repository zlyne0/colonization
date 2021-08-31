package promitech.colonization.screen.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.IntMap;

import promitech.colonization.DI;
import promitech.colonization.screen.colony.ColonyApplicationScreen;
import promitech.colonization.screen.map.MapActor;

public class DebugShortcutsKeys {
    private Stage stage;
    private final IntMap<String> commandByKeycode = new IntMap<String>();
    
    private final ConsoleOutput routeOutput = new ConsoleOutput() {
        @Override
        public ConsoleOutput out(String line) {
            if (actualOutput != null) {
                actualOutput.out(line);
            }
            return this;
        }

        @Override
        public ConsoleOutput keepOpen() {
            if (actualOutput != null) {
                actualOutput.keepOpen();
            }
            return this;
        }
    };

    private final ConsoleOutput stdout = new ConsoleOutput() {
        @Override
        public ConsoleOutput out(String line) {
            System.out.println("debugconsole: " + line);
            return this;
        }

        @Override
        public ConsoleOutput keepOpen() {
        	return this;
        }
    };
    
    private ConsoleOutput actualOutput;
    private Commands commands;

    public DebugShortcutsKeys(Stage stage, DI di, ColonyApplicationScreen colonyAppScreen) {
    	this.stage = stage;
    	
        commands = CommandDefinitionKt.createCommands(di, routeOutput, null, colonyAppScreen);
        
        commandByKeycode.put(Input.Keys.NUM_1, "colony_plan MostValuable");
        commandByKeycode.put(Input.Keys.NUM_2, "colony_plan Bell");
        commandByKeycode.put(Input.Keys.NUM_3, "colony_plan Food");
        commandByKeycode.put(Input.Keys.NUM_4, "colony_plan Building");
    }
    
    public DebugShortcutsKeys(Stage stage, DI di, MapActor mapActor) {
        this.stage = stage;
        
        commands = CommandDefinitionKt.createCommands(di, routeOutput, mapActor, null);
        
        commandByKeycode.put(Input.Keys.NUM_1, "player_turn_as_ai");
        commandByKeycode.put(Input.Keys.NUM_2, "ai_generateTheBestPlaceToBuildColony");
        commandByKeycode.put(Input.Keys.NUM_3, "ai_generateWorkerReqScoreByValue");
        commandByKeycode.put(Input.Keys.NUM_4, "ai_generateWorkerReqScoreByPriceToValue");
        //commandByKeycode.put(Input.Keys.NUM_5, "aiTransportUnitsFromEuropeToNewWorld");
        //commandByKeycode.put(Input.Keys.NUM_6, "colonies_gold_value_production");
        //commandByKeycode.put(Input.Keys.NUM_9, "map show");
        //commandByKeycode.put(Input.Keys.NUM_6, "firstContactDialog");
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
    	Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				DebugConsole debugConsole = new DebugConsole(commands);
				actualOutput = debugConsole;
				debugConsole.show(stage);
			}
    	});
    }
    
}
