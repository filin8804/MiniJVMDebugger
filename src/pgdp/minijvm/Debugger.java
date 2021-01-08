package pgdp.minijvm;

import pgdp.MiniJava;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedMap;

public class Debugger {
    private Instruction[] code;
    private int breakpoint;
    private SimulatorStack simulatorStack;
    private Simulator simulator;

    //Constructors
    public Debugger(int stackSize, String[] instructionsAsStrings) {
        this(stackSize, stringToInstructionArray(instructionsAsStrings));
    }


    public Debugger(int stackSize, Instruction[] code) {
        this.simulator = new Simulator(stackSize, code);
        this.simulatorStack = new SimulatorStack();
        this.code = code;
    }

    //Transforms the String array to Instruction Array
    private static Instruction[] stringToInstructionArray(String[] stringArray) {
        Instruction[] stringToInstruction = new Instruction[stringArray.length];

        for (int i = 0; i < stringToInstruction.length; i++) {
            String command = removeNumber(stringArray[i]);
            int numberInCommand = findInteger(stringArray[i]);
            stringToInstruction[i] = parseInstruction(command, numberInCommand);
        }
        System.out.println(Arrays.toString(stringToInstruction));
        return stringToInstruction;
    }

    //Transforms string to Instruction objects
    public static Instruction parseInstruction(String instruction, int integer) {
        String instructionCaps = instruction.toUpperCase();
        return switch (instructionCaps) {
            case "ADD" -> new Add();
            case "ALLOC" -> new Alloc(integer);
            case "CONST" -> new Const(integer);
            case "FJUMP" -> new FJump(integer);
            case "HALT" -> new Halt();
            case "JUMP" -> new Jump(integer);
            case "LOAD" -> new Load(integer);
            case "STORE" -> new Store(integer);
            case "SUB" -> new Sub();
            default -> null;
        };

    }

    public String setBreakpoint(int index) {
        if (index < 0 || index >= code.length)
            return "Invalid breakpoint index!";
        else if (this.breakpoint == index) {
            return "Breakpoint already set!";
        } else {
            this.breakpoint = index;
            return null;
        }

    }

    private void changeSimulator(Simulator toChange){
        simulator.setProgramCounter(toChange.getProgramCounter());
        simulator.setStack(toChange.getStack());
    }

    // TODO: How am I supposed to remove a breakpoint??
    public String removeBreakpoint(int index) {
        if (index < 0 || index > code.length)
            return "Invalid breakpoint index!";
        else if (index != breakpoint)
            return "No breakpoint to remove!";
        else {
            this.breakpoint = code.length;
            return null;
        }
    }


    private boolean notMeetBreakpoint(int programCounter) {
        if (programCounter <= breakpoint)
            return true;
        else
            return false;
    }


    public String run() {
        do {
            simulator.executeNextInstruction();
            simulatorStack.push(getSimulator());
        } while (simulator.getProgramCounter() < code.length && simulator.getProgramCounter() < breakpoint);
        return "No more instructions to execute!";

    }

    public String next(int k) {
        int originalPC = simulator.getProgramCounter();
        if (k < 0)
            return "Instruction count must be positive!";
        else if (k >= 0) {
            do {
                //TODO : Add the case with breakpoint pls
                simulatorStack.push(simulator.createCopy());
                simulator.executeNextInstruction();
            } while (simulator.getProgramCounter() < originalPC + 2 && simulator.getProgramCounter() != code.length - 1);
            return null;
        }
        return "No more instructions to execute!";
    }

    //TODO : Check if the program counter is at the last program
    //Push seems to be working fine
    public String step() {
        if (!simulator.isHalted() && simulator.getProgramCounter() != this.code.length - 1) {
            simulatorStack.push(simulator.createCopy());
            System.out.println("Size : " + simulatorStack.size());
            simulator.executeNextInstruction();
            //simulatorStack.push(getSimulator());
            return null;
        }
        return "No more instructions to execute!";
    }

    public String reset() {
        while (!simulatorStack.isEmpty()) {
            this.simulator = simulatorStack.pop();
        }
        return null;
    }

    //Pop is just going crazy for some reason.
    public String back() {
        //System.out.println("Methode called!");
        if (!this.simulatorStack.isEmpty()) {
            simulator = simulatorStack.pop();
            return null;
        }
        return "Cannot go back an instruction, none left!";
    }

    public String undo() {
        if (simulatorStack.isEmpty())
            return "No debugger command to undo!";
        else {
            while (simulator.getProgramCounter() >= 0) {
                simulator.setProgramCounter(simulator.getProgramCounter() - 1);
                this.simulator = simulatorStack.pop();
            }
        }
        return null;
    }

    public Simulator getSimulator() {
        return this.simulator;
    }

    public String executeDebuggerCommand(String command) {
        String commandCaps = command.toUpperCase();
        int numbersInCommand = findInteger(commandCaps);
        String commandNoNumber = removeNumber(command);

        return switch (commandNoNumber) {
            case "SET-BREAKPOINT" -> setBreakpoint(numbersInCommand);
            case "REMOVE-BREAKPOINT" -> removeBreakpoint(numbersInCommand);
            case "RUN" -> run();
            case "NEXT" -> next(numbersInCommand);
            case "STEP" -> step();
            case "RESET" -> reset();
            case "BACK" -> back();
            case "UNDO" -> undo();
            default -> "Unknown debugger command!";
        };
    }

    //Finding the integer in the command
    private static int findInteger(String command) {
        command = command.replaceAll("[^\\d]", " ");
        command = command.trim();
        command = command.replaceAll(" +", " ");
        if (command.equals(""))
            return -1;
        return Integer.parseInt(command);
    }

    private static String removeNumber(String command) {
        command = command.replaceAll("[0-9]", "");
        command = command.replaceAll("\\s+", "");
        return command;
    }

    public static void main(String[] args) {
        ArrayList<String> stringInstructions = new ArrayList<String>();
        String instruction;
        do {
            instruction = MiniJava.readString("Please enter the next instruction or press Enter to complete the input:");
            stringInstructions.add(instruction);
        } while (!instruction.equals(""));

        String[] stringArray = stringInstructions.toArray(new String[0]);
        int stackSize = MiniJava.read("Enter stack size : ");

        Debugger newDebugger = new Debugger(stackSize, stringArray);
        newDebugger.debugInteractively();

    }

    private int getActualLength() {
        int count = 0;
        for (Instruction inst : code) {
            if (inst != null)
                count++;
        }
        return count;
    }

    public void debugInteractively() {
        System.out.println(simulator.toString());
        String command = MiniJava.readString("Input debugger command:");
        while (true) {
            command = command.toUpperCase();
            if (command.equals("EXIT")) {
                System.exit(0);
            } else {
                String message;
                message = executeDebuggerCommand(command);
                if (message != null)
                    System.out.println(message);
                debugInteractively();
            }
        }
    }


}
