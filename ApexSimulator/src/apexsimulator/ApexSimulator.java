/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apexsimulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author Paritosh Fulara
 */
public class ApexSimulator {

    public static int ZeroFlag = 0;
    public static int programCounter = 4000;
    public static HashMap Instruction;
    public static int cycleCount = 1;
    public static int cycles = 0;
    public static int LoadResult = 0;
    public static int StoreResult = 0;
    public static HashMap<Integer, String> InstructionMap = new HashMap<Integer, String>();
    public static HashMap<String, Integer> TempResult = new HashMap<String, Integer>();
    public static boolean forwardSrc1 = false;
    public static boolean forwardSrc2 = false;
    public static HashMap<String, Boolean> BusyRegister = new HashMap<String, Boolean>();
    public static Instruction[][] pipeline;
    public static boolean HALTFLAG = false;
    public static int src1Value = 0;
    public static int src2Value = 0;
    public static boolean BranchTaken = false;
    public static int BranchPcValue = 0;
    public static int NextInstructionBAL = 0;
    public static int specialRegisterX = 0;
    public static boolean isStall = false;
    public static int buffer = 0;
    public static boolean branchStall = false;
    public static boolean Instrutionbranch = false;
    public static Instruction branch = null;

    /*
    This function take the file path as a argument read the file line by line 
    and put the instructions in an Hash Map 
   
     */
    public static void ReadInstructionfile(String path) throws IOException {
        FileReader fr = new FileReader(path);
        BufferedReader reader = new BufferedReader(fr);
        int i = 0;
        int instructionCount = 0;
        int pc = programCounter;
        while (reader.readLine() != null) {
            instructionCount++;
        }
        fr = new FileReader(path);
        reader = new BufferedReader(fr);
        for (i = 0; i < instructionCount; i++) {
            InstructionMap.put(pc, reader.readLine());
            pc = pc + 4;
        }
        reader.close();

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        do {
            System.out.print("Press 1 to Initialize:\n");
            System.out.print("Press 2 Simulate(n) Intruction:\n");
            System.out.print("Press 3 Display:\n");
            System.out.print("Press 4 Exit:\n");

            System.out.print("Please Enter your choice:\n\n");

            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    //Initialize Register and Memory
                    Initialize(args[0]);
                    break;

                case 2:
                    //Simulate the Instruction
                    Simulate();
                    break;

                case 3:
                    // Display the register and memory contents
                    Display();
                    break;

                case 4:
                    System.exit(1);
                    break;
            }
        } while (true);

    }

    /*
   This function initializes both the 16 registers as well as memory to 0 .
     */
    public static void Initialize(String path) throws IOException {
        ReadInstructionfile(path);
        for (int i = 0; i < 16; i++) {
            TempResult.put("R" + i, 0);
        }
        TempResult.put("X", 0);
        for (int i = 0; i < 16; i++) {
            BusyRegister.put("R" + i, false);
        }
        BusyRegister.put("X", false);
        GlobalVariables instantiate = new GlobalVariables();
        instantiate.Initialize();
        System.out.println("All Initialization Complete\n");
    }
/*
    This function is used for simulating the instructions in the pipeline and so all the 8 stages method are called from writeback to fetch .
    */
    public static void Simulate() {
        System.out.println("Please enter the total number of cycles you want to simulate:");
        Scanner sc = new Scanner(System.in);
        cycles = sc.nextInt();
        pipeline = new Instruction[8][cycles + 1];
        for (int k = 0; k < 8; k++) {
            for (int l = 0; l < cycles + 1; l++) {
                pipeline[k][l] = null;
            }
        }
        for (int i = 1; i <= cycles; i++) {
            if (HALTFLAG == true) {
                break;
            }
            WriteBack();
            MemoryStage();
            Execute2Stage();
            Execute1Stage();
            Delay();
            Branch();
            DecodeStage();
            FetchStage();
            cycleCount++;

        }

    }
    /*
    This function is used for fetching the instuction and pushing it into the pipeline .
    */

    public static void FetchStage() {
        if (BranchTaken == true) {
            BranchTaken = false;
        } else if (isStall == false && HALTFLAG==false) {
            if (pipeline[0][cycleCount] == null) {
                String ins = InstructionMap.get(programCounter);
                Instruction instruction = new Instruction();
                instruction = instruction.ProcessInstruction(ins);
                if (instruction != null) {
                    if (instruction.opcode.equals("BNZ") || instruction.opcode.equals("BZ")) {
                        BranchPcValue = programCounter;
                    }
                    if (instruction.opcode.equals("BAL")) {
                        NextInstructionBAL = programCounter + 4;
                    }
                    pipeline[0][cycleCount] = instruction;
                    programCounter = programCounter + 4;
                }
            }
        } else {
            pipeline[0][cycleCount] = pipeline[0][cycleCount - 1];
        }

    }

    /*
    Dedoding the instructions  and checking for dependency 
     */
    public static void DecodeStage() {
        if (pipeline[1][cycleCount] == null && BranchTaken == false && HALTFLAG==false) {

            if (isStall == false) {
                if (pipeline[0][cycleCount - 1] != null) {

                    pipeline[1][cycleCount] = pipeline[0][cycleCount - 1];

                    Instruction instructionObject1 = pipeline[1][cycleCount];

                    if (instructionObject1 != null) {
                        // Do decoding and dependency check here
                        Instruction instructionObject2 = pipeline[4][cycleCount];
                        Instruction instructionObject3 = pipeline[5][cycleCount];
                        Instruction instructionObject4 = pipeline[2][cycleCount];
                        Instruction instructionObject5 = pipeline[3][cycleCount];

                        if (instructionObject1.opcode.equals("BNZ") || instructionObject1.opcode.equals("BZ")) {
                            if (instructionObject2 != null) {
                                branch = instructionObject2;
                                branchStall = true;
                            }

                        }

                        // Check for dependecy at Branch ALU
                        if (instructionObject4 != null && instructionObject1.opcode.equals("JUMP") && instructionObject1.src1Register.equals(instructionObject4.destRegister)) {
                            forwardSrc1 = true;
                            BusyRegister.put(instructionObject1.src1Register, true);
                        }
                        if (instructionObject5 != null && instructionObject1.opcode.equals("JUMP") && instructionObject1.src1Register.equals(instructionObject5.destRegister)) {
                            forwardSrc1 = true;
                            BusyRegister.put(instructionObject1.src1Register, false);
                        }

                        // Check for dependecy at INTALU2 
                        if (instructionObject3 != null) {
                            if (instructionObject1.src1Register.equals(instructionObject3.destRegister)) {
                                forwardSrc1 = true;
                                BusyRegister.put(instructionObject1.src1Register, true);
                            }

                            if (instructionObject1.src1Register != "" && !instructionObject1.src1Register.equals(instructionObject3.destRegister)) {
                                BusyRegister.put(instructionObject1.src1Register, false);
                            }
                            if (instructionObject1.src2Register.equals(instructionObject3.destRegister)) {
                                forwardSrc2 = true;
                                BusyRegister.put(instructionObject1.src2Register, true);
                            }
                            if (instructionObject1.src2Register != "" && !instructionObject1.src2Register.equals(instructionObject3.destRegister)) {
                                BusyRegister.put(instructionObject1.src2Register, false);
                            }
                        }

                        // Check for dependecy at INTALU1
                        if (instructionObject2 != null) {

                            if (instructionObject1.src1Register.equals(instructionObject2.destRegister) && !instructionObject1.opcode.equals("STORE")) {
                                forwardSrc1 = true;
                                BusyRegister.put(instructionObject1.src1Register, true);

                            }

                            if (instructionObject1.src2Register.equals(instructionObject2.destRegister)) {
                                forwardSrc2 = true;
                                BusyRegister.put(instructionObject1.src2Register, true);

                            }
                        }
                    }

                }

            } else {
                pipeline[1][cycleCount] = pipeline[1][cycleCount - 1];

            }

        }
    }
    /*
    function to check if the dependency is resolved for the instruction in decode stage if yes then push the instruction in execute 1
    */
    public static void Execute1Stage() {

        if (pipeline[4][cycleCount] == null && HALTFLAG==false) {
            if (pipeline[1][cycleCount - 1] != null
                    && !pipeline[1][cycleCount - 1].opcode.equals("BNZ")
                    && !pipeline[1][cycleCount - 1].opcode.equals("BZ") && !pipeline[1][cycleCount - 1].opcode.equals("BAL")
                    && !pipeline[1][cycleCount - 1].opcode.equals("JUMP")&&!pipeline[1][cycleCount - 1].opcode.equals("HALT")) {
                Instruction atDecode = new Instruction();
                atDecode = pipeline[1][cycleCount - 1];

                switch (atDecode.opcode) {
                    case "ADD":
                        if (BusyRegister.get(atDecode.src1Register) == false && BusyRegister.get(atDecode.src2Register) == false) {
                            isStall = false;
                            pipeline[4][cycleCount] = pipeline[1][cycleCount - 1];
                            if (forwardSrc1 == true) {
                                src1Value = TempResult.get(atDecode.src1Register);
                            } else {
                                src1Value = GlobalVariables.hmap.get(atDecode.src1Register);
                            }
                            if (forwardSrc2 == true) {
                                src2Value = TempResult.get(atDecode.src2Register);
                            } else {
                                src2Value = GlobalVariables.hmap.get(atDecode.src2Register);
                            }
                        } else {
                            isStall = true;
                        }

                        break;
                    case "SUB":
                        if (BusyRegister.get(atDecode.src1Register) == false && BusyRegister.get(atDecode.src2Register) == false) {

                            isStall = false;
                            pipeline[4][cycleCount] = pipeline[1][cycleCount - 1];
                            if (forwardSrc1 == true) {
                                src1Value = TempResult.get(atDecode.src1Register);
                            } else {
                                src1Value = GlobalVariables.hmap.get(atDecode.src1Register);
                            }
                            if (forwardSrc2 == true) {
                                src2Value = TempResult.get(atDecode.src2Register);
                            } else {
                                src2Value = GlobalVariables.hmap.get(atDecode.src2Register);
                            }
                        } else {
                            isStall = true;
                        }
                        break;
                    case "MUL":
                        if (BusyRegister.get(atDecode.src1Register) == false && BusyRegister.get(atDecode.src2Register) == false) {

                            isStall = false;
                            pipeline[4][cycleCount] = pipeline[1][cycleCount - 1];
                            if (forwardSrc1 == true) {
                                src1Value = TempResult.get(atDecode.src1Register);
                            } else {
                                src1Value = GlobalVariables.hmap.get(atDecode.src1Register);
                            }
                            if (forwardSrc2 == true) {
                                src2Value = TempResult.get(atDecode.src2Register);
                            } else {
                                src2Value = GlobalVariables.hmap.get(atDecode.src2Register);
                            }
                        } else {
                            isStall = true;
                        }
                        break;
                    case "AND":
                        if (BusyRegister.get(atDecode.src1Register) == false && BusyRegister.get(atDecode.src2Register) == false) {

                            isStall = false;
                            pipeline[4][cycleCount] = pipeline[1][cycleCount - 1];
                            if (forwardSrc1 == true) {
                                src1Value = TempResult.get(atDecode.src1Register);
                            } else {
                                src1Value = GlobalVariables.hmap.get(atDecode.src1Register);
                            }
                            if (forwardSrc2 == true) {
                                src2Value = TempResult.get(atDecode.src2Register);
                            } else {
                                src2Value = GlobalVariables.hmap.get(atDecode.src2Register);
                            }
                        } else {
                            isStall = true;
                        }
                        break;
                    case "OR":
                        if (BusyRegister.get(atDecode.src1Register) == false && BusyRegister.get(atDecode.src2Register) == false) {

                            isStall = false;
                            pipeline[4][cycleCount] = pipeline[1][cycleCount - 1];
                            if (forwardSrc1 == true) {
                                src1Value = TempResult.get(atDecode.src1Register);
                                forwardSrc1 = false;
                            } else {
                                src1Value = GlobalVariables.hmap.get(atDecode.src1Register);
                            }
                            if (forwardSrc2 == true) {
                                src2Value = TempResult.get(atDecode.src2Register);
                                forwardSrc2 = false;
                            } else {
                                src2Value = GlobalVariables.hmap.get(atDecode.src2Register);
                            }
                        } else {
                            isStall = true;
                        }
                        break;
                    case "EX-OR":
                        if (BusyRegister.get(atDecode.src1Register) == false && BusyRegister.get(atDecode.src2Register) == false) {

                            isStall = false;
                            pipeline[4][cycleCount] = pipeline[1][cycleCount - 1];
                            if (forwardSrc1 == true) {
                                src1Value = TempResult.get(atDecode.src1Register);
                                forwardSrc1 = false;
                            } else {
                                src1Value = GlobalVariables.hmap.get(atDecode.src1Register);
                            }
                            if (forwardSrc2 == true) {
                                src2Value = TempResult.get(atDecode.src2Register);
                                forwardSrc2 = false;
                            } else {
                                src2Value = GlobalVariables.hmap.get(atDecode.src2Register);
                            }
                        } else {
                            isStall = true;
                        }
                        break;
                    case "MOVC":
                        pipeline[4][cycleCount] = pipeline[1][cycleCount - 1];
                        buffer = pipeline[4][cycleCount].literal + 0;
                        break;
                    case "LOAD":
                        if (BusyRegister.get(atDecode.src1Register) == false) {
                            isStall = false;
                            pipeline[4][cycleCount] = pipeline[1][cycleCount - 1];
                            if (forwardSrc1 == true) {
                                src1Value = TempResult.get(pipeline[4][cycleCount].src1Register);
                                forwardSrc1 = false;
                            } else {
                                src1Value = GlobalVariables.hmap.get(pipeline[4][cycleCount].src1Register);
                            }

                            LoadResult = pipeline[4][cycleCount].literal + src1Value;
                        } else {
                            isStall = true;
                        }

                        break;
                    case "STORE":
                        if (BusyRegister.get(atDecode.src2Register) == false) {
                            isStall = false;
                            pipeline[4][cycleCount] = pipeline[1][cycleCount - 1];
                            if (forwardSrc2 == true) {
                                src2Value = TempResult.get(pipeline[4][cycleCount].src2Register);
                                forwardSrc2 = false;
                            } else {
                                src2Value = GlobalVariables.hmap.get(pipeline[4][cycleCount].src2Register);
                            }
                            StoreResult = pipeline[4][cycleCount].literal + src2Value;
                        } else {
                            isStall = true;
                        }
                        break;
                   
                }
            }

        }

    }
   /*
    This method is used for  performing arithmetic and logical operations and putting that value in a temperory result hashmap to 
    be used for forwarding
    */
    public static void Execute2Stage() {
        if (pipeline[5][cycleCount] == null && HALTFLAG==false) {
            if (pipeline[4][cycleCount - 1] != null) {
                pipeline[5][cycleCount] = pipeline[4][cycleCount - 1];
                //Excute Instruction here
                Instruction instructionObject1 = pipeline[5][cycleCount];

                switch (instructionObject1.opcode) {
                    case "ADD":
                        TempResult.put(instructionObject1.destRegister, src1Value + src2Value);
                        // BusyRegister.put(instructionObject1.destRegister, false);
                        if (src1Value + src2Value == 0) {
                            ZeroFlag = 1;
                        } else {
                            ZeroFlag = 0;
                        }
                        src1Value = 0;
                        src2Value = 0;
                        break;
                    case "SUB":
                        TempResult.put(instructionObject1.destRegister, src1Value - src2Value);
                        // BusyRegister.put(instructionObject1.destRegister, false);

                        if (src1Value - src2Value == 0) {
                            ZeroFlag = 1;
                        } else {
                            ZeroFlag = 0;
                        }
                        src1Value = 0;
                        src2Value = 0;
                        break;
                    case "MUL":
                        TempResult.put(instructionObject1.destRegister, src1Value * src2Value);
                        // BusyRegister.put(instructionObject1.destRegister, false);
                        if (src1Value * src2Value == 0) {
                            ZeroFlag = 1;
                        } else {
                            ZeroFlag = 0;
                        }
                        src1Value = 0;
                        src2Value = 0;
                        break;
                    case "AND":
                        TempResult.put(instructionObject1.destRegister, src1Value & src2Value);
                       
                        src1Value = 0;
                        src2Value = 0;
                        break;
                    case "OR":
                        TempResult.put(instructionObject1.destRegister, src1Value | src2Value);
                        src1Value = 0;
                        src2Value = 0;
                        break;
                    case "EX-OR":
                        TempResult.put(instructionObject1.destRegister, src1Value ^ src2Value);
                        src1Value = 0;
                        src2Value = 0;
                        break;
                    case "MOVC":
                        TempResult.put(instructionObject1.destRegister, buffer);
                        buffer = 0;
                        break;

                }
            }
        }

    }
    /*
    This method is used for taking branching decision and setting up program counter value appropriatly 
    */
    public static void Branch() {

        if (pipeline[2][cycleCount] == null && HALTFLAG==false) {
            if (pipeline[1][cycleCount - 1] != null && (pipeline[1][cycleCount - 1].opcode.equals("BNZ")
                    || pipeline[1][cycleCount - 1].opcode.equals("BZ") || pipeline[1][cycleCount - 1].opcode.equals("BAL")
                    || pipeline[1][cycleCount - 1].opcode.equals("JUMP")||pipeline[1][cycleCount - 1].opcode.equals("HALT"))) {
                if (branchStall == false) {
                    isStall = false;
                    pipeline[2][cycleCount] = pipeline[1][cycleCount - 1];
                    Instruction instructionObject1 = pipeline[2][cycleCount];
                    switch (instructionObject1.opcode) {

                        case "BZ":
                            if (ZeroFlag == 1) {
                                //Instruction flushed at fetch and decode    
                                int offset = instructionObject1.literal;
                                int pcValueForBranch = BranchPcValue;
                                programCounter = pcValueForBranch + offset;
                                BranchPcValue = 0;
                                BranchTaken = true;

                            }
                            break;
                        case "BNZ":
                            if (ZeroFlag != 1) {
                                //Instruction flushed at fetch and decode    
                                int offset = instructionObject1.literal;
                                int pcValueForBranch = BranchPcValue;
                                programCounter = pcValueForBranch + offset;
                                BranchPcValue = 0;
                                BranchTaken = true;

                            }
                            break;
                        case "JUMP":
                            //Flush out the Instruction in fetch and decode
                            int registerValue = 0;
                            if (forwardSrc1 == true) {
                                registerValue = TempResult.get(instructionObject1.src1Register);
                            } else {
                                registerValue = GlobalVariables.hmap.get(instructionObject1.src1Register);
                            }
                            programCounter = registerValue + instructionObject1.literal;
                            BranchTaken = true;
                            break;
                        case "BAL":
                            int Value = 0;
                            TempResult.put("X", NextInstructionBAL);
                            if (forwardSrc1 == true) {
                                Value = TempResult.get(instructionObject1.src1Register);
                            } else {
                                Value = GlobalVariables.hmap.get(instructionObject1.src1Register);
                            }
                            programCounter = Value + instructionObject1.literal;
                            BranchTaken = true;
                            break;
                                                 
                    }
                } else {
                    isStall = true;
                }
            }
        }
    }
  /*
  Pushing Branch instruction in delay stage
 */
public static void Delay() {
        if (pipeline[3][cycleCount] == null && HALTFLAG==false) {
            if (pipeline[2][cycleCount - 1] != null) {
                pipeline[3][cycleCount] = pipeline[2][cycleCount - 1];
            }
        }
    }
   /*
    For reading out memory or store to memory array for LOAD and STORE instructions
   */    
public static void MemoryStage() {

        if (pipeline[6][cycleCount] == null && HALTFLAG==false)  {
            if (pipeline[5][cycleCount - 1] != null) {
                pipeline[6][cycleCount] = pipeline[5][cycleCount - 1];
                Instruction instructionObject1 = pipeline[6][cycleCount];

                if (branch != null) {
                    if (instructionObject1.instructionString.equals(branch.instructionString)) {
                        branchStall = false;
                        branch = null;
                    }
                }
                if (BusyRegister.containsKey(instructionObject1.destRegister) && !instructionObject1.opcode.equals("LOAD")) {
                    BusyRegister.put(instructionObject1.destRegister, false);
                }
                if (instructionObject1.opcode.equals("LOAD")) {
                    int memoryData = GlobalVariables.memory[LoadResult];
                    TempResult.put(instructionObject1.destRegister, memoryData);
                }
                if (instructionObject1.opcode.equals("STORE")) {
                    int value = 0;

                    value = GlobalVariables.hmap.get(instructionObject1.src1Register);
                    GlobalVariables.memory[StoreResult] = value;
                    StoreResult = 0;
                }
            } else if (pipeline[3][cycleCount - 1] != null) {
                pipeline[6][cycleCount] = pipeline[3][cycleCount - 1];
            }
        }

    }
   /*
   this method is used for setting up the values in 16 architectural as well as X register
   */
    public static void WriteBack() {

        if (pipeline[7][cycleCount] == null) {
            if (pipeline[6][cycleCount - 1] != null) {
                pipeline[7][cycleCount] = pipeline[6][cycleCount - 1];
                Instruction instructionObject1 = pipeline[7][cycleCount];
                if (!instructionObject1.opcode.equals("STORE") && !instructionObject1.opcode.equals("HALT")
                        && !instructionObject1.opcode.equals("BZ") && !instructionObject1.opcode.equals("BNZ") && !instructionObject1.opcode.equals("BAL")
                        && !instructionObject1.opcode.equals("JUMP")) {
                    GlobalVariables.hmap.put(instructionObject1.destRegister, TempResult.get(instructionObject1.destRegister));
                }
                if (instructionObject1.opcode.equals("HALT")) {
                    HALTFLAG = true;
                    cycles = cycleCount;
                }
                if (instructionObject1.opcode.equals("BAL")) {
                    GlobalVariables.hmap.put("X", TempResult.get("X"));
                }
                if (instructionObject1.opcode.equals("LOAD")) {
                    BusyRegister.put(instructionObject1.destRegister, false);
                }
            }
        }
    }
   /*
    This functions displays the instructions currently in the pipeline for particular cycle along with registers and memory contents  
    */
    public static void Display() {
        String[] stages = {"FETCH", "DECODE", "BRANCH_FU", "DELAY_FU", "INT_ALU1", "INT_ALU2", "MEMORY", "WRITEBACK"};
        System.out.println("Instruction currently in the pipeline:\n");
        for (int i = 0; i < 8; i++) {
            if (pipeline[i][cycles] != null) {
                System.out.println("Instruction in " + stages[i] + ":" + pipeline[i][cycles].instructionString);

            } else {
                System.out.println(stages[i] + " " + "is empty");
            }

        }
        System.out.print("****************************************************************************************************" + "\n");
        
        System.out.println("Register File :\n");

        for (Map.Entry<String, Integer> entry : GlobalVariables.hmap.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ": Value: " + entry.getValue());
        }

        System.out.print("****************************************************************************************************" + "\n");
        System.out.println("Memory Content :\n");
        for (int i = 0; i <= 100; i++) {
            System.out.println("MemoryLocation-" + i + "=> " + GlobalVariables.memory[i]);
        }
    }

}
