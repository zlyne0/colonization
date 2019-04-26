package promitech.colonization.screen.debug

class Commands() {
	val commands : MutableList<Command> = mutableListOf<Command>()

	private constructor (cmd : Commands) : this() {
		commands.addAll(cmd.commands)
	}
	
	private constructor (cmds : List<Command>) : this() {
		commands.addAll(cmds)
	}
	
	fun define(cmds : Commands.() -> Unit) : Commands {
	    cmds(this)
	    return this
	}
	
	fun command(name : String, cmdBody : Command.() -> Unit) {
	    commands.add(Command(name, {
			cmdBody()
		}))
	}

	fun commandArg(name : String, cmdBody : Command.(args : List<String>) -> Unit) : Command {
		val command = Command(name, cmdBody)
	    commands.add(command)
		return command
	}
		
	fun cmdName(cmd : String) : String {
	    var cmdName = cmd
        if (cmd.indexOf(" ") != -1) {
            cmdName = cmd.substring(0, cmd.indexOf(" "))
        }
		return cmdName
	}
	
	fun execute(cmd : String) {
		val cmdName = cmdName(cmd)
				
		val filteredCmds = commands.filter {
		    it -> it.name.equals(cmdName, true)
		}
		if (filteredCmds.isEmpty() || filteredCmds.size > 1) {
			println("not single Command for name '${cmdName}' but ${filteredCmds.size}")
			return
		}
		val args : List<String> = cmd.split(" ").map { it -> it.trim() }
		filteredCmds.first().execute(args)
	}
	
	fun filterCommandsByPrefix(prefix : String) : Commands {
		return Commands(
			commands.filter { it -> it.name.startsWith(prefix) }
		)
	}
	
	fun enlargeHintCommandToBetterMatch(cmd : String) : String {
		// command has arguments
		if (cmd.indexOf(" ") != -1) {
			val args = cmd.split(" ").map { it -> it.trim() }
			val cmdName = args[0]
			
			val filteredCmds = commands.filter { it -> it.name == cmdName }
			if (filteredCmds.isEmpty() || filteredCmds.size > 1) {
				println("not single Command for name '${cmdName}' but ${filteredCmds.size}")
				return cmd
			}
			val execCmd : Command = filteredCmds.first()
			println("execCmd ${execCmd.name}, params ${execCmd.params}")
			
			val cmdDefinedArgs = execCmd.params()
			if (cmdDefinedArgs.isEmpty()) {
				return cmd
			}
			return cmdName + " " + enlargeHintCommandToBetterMatch(cmdDefinedArgs, args[1])
		}
		
		val cmds = commands.map { it -> it.name }
		return enlargeHintCommandToBetterMatch(cmds, cmd)
	}
		
	private fun enlargeHintCommandToBetterMatch(allCmds : List<String>, cmd : String) : String {
		val filteredCmds = allCmds.filter { it -> it.startsWith(cmd) }
		if (filteredCmds.isEmpty()) {
			return cmd
		}
		
		val theShortestCommand = filteredCmds.sortedBy { it -> it.length }.first()
		if (cmd.length == theShortestCommand.length) {
			return cmd
		}

		fun allCmdsStartsWithPrefix(prefix : String) : Boolean {
			return filteredCmds.filterNot { it -> it.startsWith(prefix) }.isEmpty()
		}
		
		var enlargeCommand = cmd
		do {
			if (enlargeCommand.length >= theShortestCommand.length) {
				break
			}
		    var enlargeCommandCandidate = enlargeCommand + theShortestCommand[enlargeCommand.length]
    		if (allCmdsStartsWithPrefix(enlargeCommandCandidate)) {
				enlargeCommand = enlargeCommandCandidate 
    		} else {
    			break;
    		}
		} while (true)
		return enlargeCommand
	}
}

class Command(val name : String, val cmdBody : Command.(args : List<String>) -> Unit) {
	var closeConsole = true
	var params : () -> List<String> = { listOf() }
		
	fun execute(args : List<String>) {
		cmdBody(this, args)
	}
	
	fun doNotCloseConsole() {
		closeConsole = false
	}
	
	fun addParams(params : () -> List<String>) : Command {
		this.params = params
		return this
	}
}
