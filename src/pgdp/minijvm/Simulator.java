package pgdp.minijvm;

import jdk.dynalink.linker.support.SimpleLinkRequest;

public class Simulator {

	private Instruction[] code;
	private int programCounter = 0;
	private Stack stack;
	private boolean halted;

	/**
	 * Erstellt einen Simulator mit der Stackgröße {@code stackSize} und dem
	 * MiniJava-Code {@code code}.
	 *
	 * @param stackSize
	 * @param code
	 */
	public Simulator(int stackSize, Instruction[] code) {
		stack = new Stack(stackSize);
		this.code = code;
	}


	//Exceutes the next instruction in the code array (Instruction array)
	public boolean executeNextInstruction() {
		if (halted) {
			return false;
		}
		Instruction instr = code[programCounter];
		programCounter++;
		instr.execute(this);
		return !halted;
	}

	/**
	 * Liefert den Stack des Simulators.
	 */
	public Stack getStack() {
		return stack;
	}

	public void setStack(Stack stack) {
		this.stack = stack;
	}

	/**
	 * Setzt den Programmzähler des Simulators auf den übergebenen Wert.
	 *
	 * @param programCounter Der neue Wert des Programmzählers.
	 */
	public void setProgramCounter(int programCounter) {
		this.programCounter = programCounter;
	}

	/**
	 * Liefert den Wert des Programmzählers des Simulators.
	 */
	public int getProgramCounter() {
		return programCounter;
	}

	/**
	 * Setzt das {@code halted}-Attribut
	 *
	 * @param halted Der neue Wert des Attribus.
	 */
	public void setHalted(boolean halted) {
		this.halted = halted;
	}

	/**
	 * Liefert den Wert des {@code halted}-Attributs.
	 */
	public boolean isHalted() {
		return halted;
	}

	@Override
	public String toString() {
		return String.format("Halted: %b%nProgram counter: %d%n%s%n", this.halted, this.programCounter, this.stack);
	}

	//Makes a copy of the Simulator Object
	public Simulator createCopy(){
		return this;
	}




}
