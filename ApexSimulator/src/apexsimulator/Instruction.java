/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apexsimulator;

/**
 *
 * @author Paritosh Fulara
 */
public class Instruction {

    String opcode = "";
    String destRegister = "";
    String src1Register = "";
    String src2Register = "";
    boolean checkliteral = false;
    int literal = 0;
    String instructionString = "";

    /*
    This method is used for parsing the instruction string and convert it to a instruction class object & return that object 
    */
    public Instruction ProcessInstruction(String instruction) {
        if (instruction != null) {
            instruction = instruction.replace(",", " ");
            String[] Instruction = instruction.split("\\s+");
            this.opcode = Instruction[0];
            if (this.opcode.equals("STORE")) {
                this.src1Register = Instruction[1];
                this.src2Register = Instruction[2];
                this.literal = Integer.parseInt(Instruction[3].substring(1));

            } else if (this.opcode.equals("LOAD")) {
                this.destRegister = Instruction[1];
                this.src1Register = Instruction[2];
                this.literal = Integer.parseInt(Instruction[3].substring(1));
            } else if (this.opcode.equals("MOVC")) {
                this.destRegister = Instruction[1];
                this.literal = Integer.parseInt(Instruction[2].substring(1));
            } else if (this.opcode.equals("JUMP") || this.opcode.equals("BAL")) {
                this.src1Register = Instruction[1];
                this.literal = Integer.parseInt(Instruction[2].substring(1));

            } else if (this.opcode.equals("BZ") || this.opcode.equals("BNZ")) {
                this.literal = Integer.parseInt(Instruction[1].substring(1));
            } else if (this.opcode.equals("HALT")) {

            } else {
                this.destRegister = Instruction[1];
                this.src1Register = Instruction[2];
                this.src2Register = Instruction[3];
            }
            this.instructionString = instruction;
            return this;
        } else {
            return null;
        }

    }
}
