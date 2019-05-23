package promitech.colonization.screen.debug

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach

class CommandsTest {
	var commands : Commands = Commands()
	var commandExecuted : String = ""
	
	@BeforeEach
	fun setup() {
    	commands = Commands().define {
        	commandArg("add_gold") { args -> 
        	    System.out.println("execute command " + this.name + ", arg[0]=${args[0]}, argsSize " + args.size)
				commandExecuted = "exec add_gold"
				if (args.size > 1) {
					commandExecuted += " ${args[1]}"
				}
        	}
        	command("add_immigration") { 
				System.out.println("execute command " + this.name)
			}
			commandArg("add_foundingFather") { args ->
				commandExecuted = "exec add_foundingFather " + args[1]
			}.addParams {
				listOf<String>("model.foundingFather.adamSmith", "model.foundingFather.jacobFugger", "model.foundingFather.peterMinuit")
			} 
        	command("map_show") { 
    			System.out.println("execute command " + this.name)
        	}
        	command("map_generate") { 
				System.out.println("execute command " + this.name)
			}
        	command("pools") { 
    			System.out.println("execute command " + this.name)
        	}
    	}
	}
	
	@Test
	fun `can find commands by prefix`() {
		// when
		val prefixedCmds = commands.filterCommandsByPrefix("add")
		
		// then
		assertThat(prefixedCmds)
			.hasSize(3)
			.containsExactly("add_gold", "add_immigration", "add_foundingFather")
	}
	
	@Test
	fun `can enlarge hint command`() {
		// when
		var enlargedCommand = commands.enlargeHintCommandToBetterMatch("ad")
		
		// then
		assertThat(enlargedCommand).isEqualTo("add_")
	}

	@Test
	fun `can enlarge hint command with param`() {
		// given
		
		// when
		var enlargedCommand = commands.enlargeHintCommandToBetterMatch("add_foundingFather mode")
		
		// then
		assertThat(enlargedCommand).isEqualTo("add_foundingFather model.foundingFather.")
	}
	
	@Test
	fun `can hint command arguments`() {
		// given
		
		// when
		var hintCmds = commands.filterCommandsByPrefix("add_foundingFather mode")
		
		// then
		assertThat(hintCmds)
			.hasSize(3)
			.containsExactly(
				"add_foundingFather model.foundingFather.adamSmith",
				"add_foundingFather model.foundingFather.jacobFugger",
				"add_foundingFather model.foundingFather.peterMinuit"
			)
	}
	
	@Test
	fun `can execute command with param`() {
		// given
		
		// when
		commands.execute("add_foundingFather model.foundingFather.adamSmith")
		
		// then
		assertThat(commandExecuted).isEqualTo("exec add_foundingFather model.foundingFather.adamSmith")
	}

	@Test
	fun `can execute command without args`() {
		// given
		
		// when
		commands.execute("add_gold")
		
		// then
		assertThat(commandExecuted).isEqualTo("exec add_gold")
	}
	
	@Test
	fun `can execute command with argument`() {
		// given
		
		// when
		commands.execute("add_gold 345")
		
		// then
		assertThat(commandExecuted).isEqualTo("exec add_gold 345")
	}
	
}