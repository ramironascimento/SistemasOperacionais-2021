package com.company;

public class OpcodeEnum {//region ENUMERATIONS

    enum Opcode {
        DATA, ___,
        JMP, JMPI, JMPIG, JMPIL, JMPIE, JMPIM, JMPIGM, JMPILM, JMPIEM, STOP,
        ADDI, SUBI, ADD, SUB, MULT,
        LDI, LDD, STD, LDX, STX, SWAP,
        TRAP

    }
}