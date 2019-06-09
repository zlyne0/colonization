package promitech.colonization.screen.debug

interface ConsoleOutput {
	fun out(line: String)
	fun keepOpen()
}