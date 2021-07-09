package com.company;

import java.util.ArrayList;

public class Programs {
    enum Progs {
        PROG_MINIMO, FIBBONACI10,
        FATORIAL, BUBBLE_SORT,
        ___
    }// region Classes

    static class Aux {
        public void dump(Systems.Word w) {
            System.out.print("[ ");
            dumpOutput(w);
            System.out.println("  ] ");
        }

        static void dumpOutput(Systems.Word w) {
            System.out.print(w.opc);
            System.out.print(", ");
            System.out.print(w.r1);
            System.out.print(", ");
            System.out.print(w.r2);
            System.out.print(", ");
            System.out.print(w.p);
        }

        public void dump(Systems.Word[] m, int ini, int fim) {
            for (int i = ini; i <= fim; i++) {
                System.out.print(i);
                System.out.print(":  ");
                dump(m[i]);
            }
        }

        public void dump(Systems.VM vm, int idProg) {
            ArrayList<Integer> frames_reservados = new ArrayList<>();
            if (vm.memoryManager.getFramesProg(idProg, frames_reservados)) {
                for (Integer frames_reservado : frames_reservados) {
                    System.out.println("---- Frame [" + frames_reservado + " ]");
                    dump(vm.m, (frames_reservado * vm.memoryManager.getframeSize()), (((frames_reservado + 1) * vm.memoryManager.getframeSize()) - 1));
                }
            } else {
                Systems.programNotFoundInterruption();
            }
        }

        public void memoryLoad(Systems.Word[] p, Programs.Progs nomePrograma, Systems.VM vm) {
            ArrayList<Integer> frames_reservados = new ArrayList<>();
            if (vm.memoryManager.existsEmptyFrames(p.length, frames_reservados)) {
                vm.memoryManager.framesAllocation(frames_reservados, p, nomePrograma);
                System.out.println("Programa carregado com sucesso.");
                System.out.print("Frames Reservados ");
                for (int i : frames_reservados) {
                    System.out.print("- [ " + i + " ]");
                }
                System.out.println();
            } else {
                Systems.noMemoryAvailableInterruption();
            }
        }

    }

    static class Programas { //TODO PROGRAMA ADICIONADO DEVE SER INSERID NO ENUMERATION "Progs".
        public Systems.Word[] progMinimo = new Systems.Word[]{
                new Systems.Word(OpcodeEnum.Opcode.LDI, 0, -1, 999),
                new Systems.Word(OpcodeEnum.Opcode.STD, 0, -1, 10),
                new Systems.Word(OpcodeEnum.Opcode.STD, 0, -1, 11),
                new Systems.Word(OpcodeEnum.Opcode.STD, 0, -1, 12),
                new Systems.Word(OpcodeEnum.Opcode.STD, 0, -1, 13),
                new Systems.Word(OpcodeEnum.Opcode.STD, 0, -1, 14),
                new Systems.Word(OpcodeEnum.Opcode.STOP, -1, -1, -1)};

        //TODO BACKLOG inserir o esquema de trap no programa
        public Systems.Word[] fibonacci10 = new Systems.Word[]{ // mesmo que prog exemplo, so que usa r0 no lugar de r8
                new Systems.Word(OpcodeEnum.Opcode.LDI, 1, -1, 0),
                new Systems.Word(OpcodeEnum.Opcode.STD, 1, -1, 20), //50
                new Systems.Word(OpcodeEnum.Opcode.LDI, 2, -1, 1),
                new Systems.Word(OpcodeEnum.Opcode.STD, 2, -1, 21), //51
                new Systems.Word(OpcodeEnum.Opcode.LDI, 0, -1, 22), //52
                new Systems.Word(OpcodeEnum.Opcode.LDI, 6, -1, 6),
                new Systems.Word(OpcodeEnum.Opcode.LDI, 7, -1, 31), //61
                new Systems.Word(OpcodeEnum.Opcode.LDI, 3, -1, 0),
                new Systems.Word(OpcodeEnum.Opcode.ADD, 3, 1, -1),
                new Systems.Word(OpcodeEnum.Opcode.LDI, 1, -1, 0),
                new Systems.Word(OpcodeEnum.Opcode.ADD, 1, 2, -1),
                new Systems.Word(OpcodeEnum.Opcode.ADD, 2, 3, -1),
                new Systems.Word(OpcodeEnum.Opcode.STX, 0, 2, -1),
                new Systems.Word(OpcodeEnum.Opcode.ADDI, 0, -1, 1),
                new Systems.Word(OpcodeEnum.Opcode.SUB, 7, 0, -1),
                new Systems.Word(OpcodeEnum.Opcode.JMPIG, 6, 7, -1),
                new Systems.Word(OpcodeEnum.Opcode.STOP, -1, -1, -1)};


        public Systems.Word[] p3 = new Systems.Word[]{
                new Systems.Word(OpcodeEnum.Opcode.LDI, 8, -1, 1), // joga o valor 10 no Registrador1
                new Systems.Word(OpcodeEnum.Opcode.LDI, 9, -1, 50), // setta a posicao onde vai armazenar o valor inputado
                new Systems.Word(OpcodeEnum.Opcode.TRAP, -1, -1, -1),// chama trap para IN
                //new Word(Opcode.STD, 9, -1, 50), // coloca o valor do Registrado9 na posição 20 da memoria
                new Systems.Word(OpcodeEnum.Opcode.LDD, 1, -1, 50), // ler da memoria e colocar no registrador

                new Systems.Word(OpcodeEnum.Opcode.LDD, 2, -1, 50), // ler da memoria e colocar no registrador
                new Systems.Word(OpcodeEnum.Opcode.LDI, 0, -1, 19), // numero da linha que será pulado no JMP abaixo
                new Systems.Word(OpcodeEnum.Opcode.JMPIL, 0, 2, -1),// comparar se registrador < 0
                new Systems.Word(OpcodeEnum.Opcode.SUBI, 2, -1, 1), //r2 = 9
                // inicio loop
                new Systems.Word(OpcodeEnum.Opcode.ADDI, 2, -1, 1), // readiona 1 pra que o loop fique certo

                new Systems.Word(OpcodeEnum.Opcode.SUBI, 2, -1, 1), // subtrai pra fazer r1*r2
                new Systems.Word(OpcodeEnum.Opcode.MULT, 1, 2, -1), // multiplica
                new Systems.Word(OpcodeEnum.Opcode.SUBI, 2, -1, 1), // subtrai pra comparar a zero e possivelmente parar
                new Systems.Word(OpcodeEnum.Opcode.JMPIGM, -1, 2, 8),    // compara a zero para ver se precisa parar   x = 6
                // fim loop
                new Systems.Word(OpcodeEnum.Opcode.STD, 1, -1, 50), // acaba o loop, joga o valor de r1 (resultado do fatorial) na posicao 20 da memoria

                new Systems.Word(OpcodeEnum.Opcode.LDI, 8, -1, 2), // setta o registrador 8 para o valor de OUT
                new Systems.Word(OpcodeEnum.Opcode.LDI, 9, -1, 50), // poe no registrador 9 a posicao de memoria que vai ser acessada no TRAP
                new Systems.Word(OpcodeEnum.Opcode.TRAP, -1, -1, -1), // TRAP OUT
                new Systems.Word(OpcodeEnum.Opcode.STOP, -1, -1, -1), // acaba o programa
                new Systems.Word(OpcodeEnum.Opcode.LDI, 1, -1, -1), // (se no primeiro jmp, o input for -1, vem pra cá) joga o valor de -1 no registrador 1

                new Systems.Word(OpcodeEnum.Opcode.STD, 1, -1, 50), // armazena no valor de r1 na posicao 20 da memoria
                new Systems.Word(OpcodeEnum.Opcode.LDI, 8, -1, 2), // setta o registrador 8 para o valor de OUT
                new Systems.Word(OpcodeEnum.Opcode.LDI, 9, -1, 50), // poe no registrador 9 a posicao de memoria que vai ser acessada no TRAP
                new Systems.Word(OpcodeEnum.Opcode.TRAP, -1, -1, -1), // TRAP OUT
                new Systems.Word(OpcodeEnum.Opcode.STOP, -1, -1, -1)    // acaba o programa
        };

        //todo backlog bubblesort
        //public Word[] BubbleSort = new Word{

        // le a quantidade de elementos
        // armazena os elementos na memoria (ex: 50,51,52,53,54)
        //

        //}
    }
}