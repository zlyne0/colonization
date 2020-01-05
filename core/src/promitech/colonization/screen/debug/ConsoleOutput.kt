package promitech.colonization.screen.debug

interface ConsoleOutput {
	fun out(line: String) : ConsoleOutput
	fun keepOpen() : ConsoleOutput
}