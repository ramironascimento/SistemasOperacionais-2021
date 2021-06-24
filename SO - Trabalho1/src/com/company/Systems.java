package com.company;
// PUCRS - Escola Politécnica - Sistemas Operacionais
// Prof. Fernando Dotti
// Código fornecido como parte da solução do projeto de Sistemas Operacionais
//
// Fase 1 - máquina virtual (vide enunciado correspondente)
//


import java.util.ArrayList;
import java.util.Scanner;

public class Systems {

	// -------------------------------------------------------------------------------------------------------
	// --------------------- H A R D W A R E - definicoes de HW ----------------------------------------------

	// -------------------------------------------------------------------------------------------------------
	// --------------------- M E M O R I A -  definicoes de opcode e palavra de memoria ----------------------

	public class Word {    // cada posicao da memoria tem uma instrucao (ou um dado)
		public Opcode opc;    //
		public int r1;        // indice do primeiro registrador da operacao (Rs ou Rd cfe opcode na tabela)
		public int r2;        // indice do segundo registrador da operacao (Rc ou Rs cfe operacao)
		public int p;        // parametro para instrucao (k ou A cfe operacao), ou o dado, se opcode = DADO
		public int interruption; // controle de interrupcao da instrucao

		public Word(Opcode _opc, int _r1, int _r2, int _p) {
			opc = _opc;
			r1 = _r1;
			r2 = _r2;
			p = _p;
			interruption = 0;
		}
	}
	// -------------------------------------------------------------------------------------------------------

	// -------------------------------------------------------------------------------------------------------
	// --------------------- C P U  -  definicoes da CPU -----------------------------------------------------
	// -------------------------------------------------------------------------------------------------------

	//region ENUMERATIONS
	public enum Opcode {
		DATA, ___,            // se memoria nesta posicao tem um dado, usa DATA, se nao usada ee NULO ___
		JMP, JMPI, JMPIG, JMPIL, JMPIE, JMPIM, JMPIGM, JMPILM, JMPIEM, STOP,   // desvios e parada
		ADDI, SUBI, ADD, SUB, MULT,          // matematicos
		LDI, LDD, STD, LDX, STX, SWAP,        // movimentacao
		TRAP                                 // added opcode

	}

	// FASE 4 (START)
	public enum Progs{
		PROG_MINIMO, FIBBONACI10,
		FATORIAL, BUBBLE_SORT,
		___
	}
	// FASE 4 (FINISH)

	// FASE 5* (START)
	public enum ProgramState {
		READY,RUNNING,
		// FASE 6 (START)
		BLOCKED
		// FASE 6 (FINISH)
	}
	// FASE 5* (FINISH)

	//endregion

	public class CPU {
		private Word ir;              // instruction register,
		private int[] reg;           // registradores da CPU
		private Word[] m;           // CPU acessa MEMORIA, guarda referencia 'm' a ela. memoria nao muda. ee sempre a mesma.
		//FASE 4 (START)
		private ProcessControlBlock pcb;
		//FASE 4 (FINISH)

		//construtor
		public CPU(Word[] _m) {     // ref a MEMORIA e interrupt handler passada na criacao da CPU
			m = _m;                // usa o atributo 'm' para acessar a memoria.
			reg = new int[10];        // aloca o espaço dos registradores
		}


		//FASE 4 (START)
		public void run(ProcessControlBlock program) {        // execution da CPU supoe que o contexto da CPU, vide acima, esta devidamente setado
			long sum;
			long sub;

			pcb = program; //fetch programa
			reg = pcb.reg;
			// FASE 5* (START)
			int count_round_robin = 0;
			pcb.programState = ProgramState.RUNNING;
			// FASE 5* (FINISH)


			while (true) {         // ciclo de instrucoes. acaba cfe instrucao, veja cada caso.
				// FETCH
				ir = m[pcb.pc_];        // busca posicao da memoria apontada por pc, guarda em ir


				// FASE 5* (START)
				// Contagem de Q para fazer o Escalonamento Round_Robin
				count_round_robin++;
				if (count_round_robin == vm.rr_q){
					ir.interruption=6;
				}
				// FASE 5* (FINISH)

				// EXECUTA INSTRUCAO NO ir
				sum = 0;
				sub = 0;
				switch (ir.opc) { // para cada opcode, sua exetypocução
					case TRAP:
						switch (reg[8]) {
							case 1: //in
								reg[9] = Trap_IN();
								break;
							case 2: //out
								Trap_OUT(m[pcb.getLogicAddress(reg[9])].p);
								break;
							default:
								break;
						}
						pcb.nextPC();
						break;

					case JMP: //PC ← k
						//error treatment
						if (m[pcb.getLogicAddress(ir.p)].opc == Opcode.___) {
							ir.interruption = 2;
						}
						//execution
						else {
							pcb.jmpPc(ir.p);
						}
						break;

					case JMPI: //PC ← R1
						//error treatment
						if (vm.validRegister(ir.r1) || m[pcb.getLogicAddress(reg[ir.r1])].opc == Opcode.___) {
							ir.interruption = 2;
						}
						//execution
						else {
							pcb.jmpPc(reg[ir.r1]);
						}
						break;

					case JMPIG: // If R2 > 0 Then PC ← R1 Else PC ← PC +1
						if (vm.validRegister(ir.r1) && vm.validRegister(ir.r2)) {
							if (reg[ir.r2] > 0) {
								//error treatment
								if ((m[pcb.getLogicAddress(reg[ir.r1])].opc == Opcode.___)){
									ir.interruption = 2;
								}
								//execution
								else {
									pcb.jmpPc(reg[ir.r1]);
								}
							} else {
								pcb.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;

					case JMPIL: //if R2 < 0 then PC ← R1 Else PC ← PC +1
						if (vm.validRegister(ir.r2)) {
							if (reg[ir.r2] < 0) {
								//error treatment
								if (m[pcb.getLogicAddress(reg[ir.r1])].opc == Opcode.___) {
									ir.interruption = 2;
								}
								//execution
								else {
									pcb.jmpPc(reg[ir.r1]);
								}
							} else {
								pcb.nextPC();
							}
						} else {
							ir.interruption = 2;
						}

						break;

					case JMPIE: //if R2 == 0 then PC ← R1 Else PC ← PC +1
						if (vm.validRegister(ir.r2)) {
							if (reg[ir.r2] == 0) {
								//error treatment
								if (m[pcb.getLogicAddress(reg[ir.r1])].opc == Opcode.___) {
									ir.interruption = 2;
								}
								//execution
								else {
									pcb.jmpPc(reg[ir.r1]);
								}
							} else {
								pcb.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;

					case JMPIM: // PC ← [A]
						//error treatment
						if (m[pcb.getLogicAddress(ir.p)].opc == Opcode.___) {
							ir.interruption = 2;
						}
						//execution
						else {
							pcb.jmpPc(m[ir.p].p);
						}
						break;

					case JMPIGM: // if R2 > 0 then PC ← [A] Else PC ← PC +1
						if (vm.validRegister(ir.r2)) {
							if (reg[ir.r2] > 0) {
								//error treatment
								if (m[pcb.getLogicAddress(ir.p)].opc == Opcode.___) {
									ir.interruption = 2;
								}
								//execution
								else {
									pcb.jmpPc(ir.p);
								}
							} else {
								pcb.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;

					case JMPILM: //if R2 < 0 then PC ← [A] Else PC ← PC +1
						if (vm.validRegister(ir.r2)) {
							if (reg[ir.r2] < 0) {
								//error treatment
								if (m[pcb.getLogicAddress(ir.p)].opc == Opcode.___) {
									ir.interruption = 2;
								}
								//execution
								else {
									pcb.jmpPc(ir.p);
								}
							} else {
								pcb.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;

					case JMPIEM: //if R2 == 0 then PC ← [A] Else PC ← PC +1
						if (vm.validRegister(ir.r2)) {
							if (reg[ir.r2] == 0) {
								//error treatment
								if (m[pcb.getLogicAddress(ir.p)].opc == Opcode.___) {
									ir.interruption = 2;
								}
								//execution
								else {
									pcb.jmpPc(ir.p);
								}
							} else {
								pcb.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;

					case STOP: // Interruption ← 4
						ir.interruption = 4;
						break;

					case ADDI: // R1 ← R1 + k
						//tramento erro
						if (vm.validRegister(ir.r1)) {
							sum = reg[ir.r1] + ir.p;
							if (sum > Integer.MAX_VALUE) {
								ir.interruption = 1;
							}
							//execution
							else {
								reg[ir.r1] = reg[ir.r1] + ir.p;
								pcb.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;

					case SUBI: // R1 ← R1 – k
						//tratamento erro
						if (vm.validRegister(ir.r1)) {
							sub = reg[ir.r1] - ir.p;
							if (sub < Integer.MIN_VALUE) {
								ir.interruption = 1;
							}
							//execution
							else {
								reg[ir.r1] = reg[ir.r1] - ir.p;
								pcb.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;

					case ADD: // R1 ← R1 + R2
						//tratamento erro
						if (vm.validRegister(ir.r1) && vm.validRegister(ir.r2)) {
							sum = reg[ir.r1] + reg[ir.r2];
							if (sum > Integer.MAX_VALUE) {
								ir.interruption = 1;
							}
							//execution
							else {
								reg[ir.r1] = reg[ir.r1] + reg[ir.r2];
								pcb.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;

					case SUB: // R1 ← R1 - R2
						//tratamento erro
						if (vm.validRegister(ir.r1) && vm.validRegister(ir.r2)) {
							sub = reg[ir.r1] - reg[ir.r2];
							if (sub < Integer.MIN_VALUE) {
								ir.interruption = 1;
							}
							//execution
							else {
								reg[ir.r1] = reg[ir.r1] - reg[ir.r2];
								pcb.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;

					case MULT: // R1 ← R1 * R2
						//tratamento erro
						if (vm.validRegister(ir.r1) && vm.validRegister(ir.r2)) {
							sum = (long) reg[ir.r1] * reg[ir.r2];
							if (sum < Integer.MIN_VALUE || sum > Integer.MAX_VALUE) {
								ir.interruption = 1;
							}
							//execution
							else {
								reg[ir.r1] = reg[ir.r1] * reg[ir.r2];
								pcb.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;

					case LDI: // R1 ← k
						if (vm.validRegister(ir.r1)) {
							reg[ir.r1] = ir.p;
							pcb.nextPC();
						} else {
							ir.interruption = 2;
						}
						break;

					case LDD: // R1 ← [A]
						//validacao
						if (vm.validRegister(ir.r1)) {
							reg[ir.r1] = m[pcb.getLogicAddress(ir.p)].p;
							pcb.nextPC();
						} else {
							ir.interruption = 2;
						}
						break;

					case STD: // [A] ← R1
						if (vm.validRegister(ir.r1)) {
							m[pcb.getLogicAddress(ir.p)].opc = Opcode.DATA;
							m[pcb.getLogicAddress(ir.p)].p = reg[ir.r1];
							pcb.nextPC();
						} else {
							ir.interruption = 2;
						}
						break;

					case LDX: // R1 ← [R2]
						if (vm.validRegister(ir.r1) && vm.validRegister(ir.r2)) {
							reg[ir.r1] = m[pcb.getLogicAddress(reg[ir.r2])].p;
							pcb.nextPC();
						} else {
							ir.interruption = 2;
						}
						break;

					case STX: // [R1] ← R2
						if (vm.validRegister(ir.r1) && vm.validRegister(ir.r2)) {
							m[pcb.getLogicAddress(reg[ir.r1])].opc = Opcode.DATA;
							m[pcb.getLogicAddress(reg[ir.r1])].p = reg[ir.r2];
							pcb.nextPC();
						} else {
							ir.interruption = 2;
						}
						break;

					case SWAP: //T ← Ra Ra ← Rb Rb ←T
						if (vm.validRegister(ir.r1) && vm.validRegister(ir.r2)) {
							int t = reg[ir.r1];
							reg[ir.r1] = reg[ir.r2];
							reg[ir.r2] = t;
							pcb.nextPC();
						} else {
							ir.interruption = 2;
						}

						break;

					default:
						ir.interruption = 3;
						break;
				}

				if (ir.interruption != 0) {
					if (ir.interruption == 1) //Overflow em uma operacao aritmetica
					{
						overflowInterruption(ir);
						vm.memoryManager.resetProgram(program);
						break;
					} else if (ir.interruption == 2) //acessou um endereço invalido de memoria (ArrayOutOfBound)
					{
						invalidAddressInterruption(ir);
						vm.memoryManager.resetProgram(program);
						break;
					} else if (ir.interruption == 3) //Instrucao Invalida
					{
						invalidOpcodeInterruption(ir);
						vm.memoryManager.resetProgram(program);
						break;
					} else if (ir.interruption == 4) //opcode STOP em sí
					{
						stopInterruption();
						vm.memoryManager.resetProgram(program);
						break;
					} else if (ir.interruption == 5) //sem memoria disponivel
					{
						noMemoryAvailableInterruption(ir);
						break;
					}
					// FASE 5* (START)
					else if (ir.interruption == 6){
						program.reg = this.reg;
						vm.schedulerManager.endOfLine(program);
						System.out.println(program.programState +" -> " + "Programa["+ program.getIdProg() +" - "+ program.getProgram() +"]"+" PC = "+ pcb.pc_);
						break;
					}
					// FASE 5* (FINISH)
				}
				// FASE 5* (START)
				System.out.println(program.programState +" -> " + "Programa["+ program.getIdProg() +" - "+ program.getProgram() +"]"+" PC = "+ pcb.pc_);
				// FASE 5* (FINISH)
			}
		}
		//FASE 4 (FINISH)
	}



	// ------------------ C P U - fim ------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------


	// ------------------- V M  - constituida de CPU e MEMORIA -----------------------------------------------
	// -------------------------- atributos e construcao da VM -----------------------------------------------
	public class VM {

		//atributos
		public int tamMem;
		public Word[] m;
		public CPU cpu;
		// FASE 4 (START)
		public MemoryManager memoryManager;
		// FASE 4 (FINISH)
		// FASE 5* (START)
		public int rr_q;
		public SchedulerManager schedulerManager;
		// FASE 5* (FINISH)

		//construtor
		public VM() {   // vm deve ser configurada com endereço de tratamento de interrupcoes
			// memória
			tamMem = 1024;
			// FASE 4 (START)
			int tamFrame = 16;
			memoryManager = new MemoryManager(tamMem, tamFrame);
			// FASE 4 (FINISH)
			m = new Word[tamMem]; // m ee a memoria
			for (int i = 0; i < tamMem; i++) { m[i] = new Word(Opcode.___, -1, -1, -1); }

			// FASE 5* (START)
			rr_q = 5;
			this.schedulerManager = new SchedulerManager();
			// FASE 5* (FINISH)

			// cpu
			cpu = new CPU(m);
		}

		public boolean validRegister(int reg_n) {
			return (reg_n >= 0 && reg_n <= 9);
		}
	}

	// ------------------- V M  - fim ------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------

	// --------------------H A R D W A R E - fim -------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------

	// -------------------------------------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------
	// ------------------- S O F T W A R E - inicio ----------------------------------------------------------

	// region Tratamento Interrupcao
	public static void overflowInterruption(Word w) {
		interruptionOutput(w);
		System.out.println("There was an overflow in a arithmetic operation.");
	}

	public static void invalidOpcodeInterruption(Word w) {
		interruptionOutput(w);
		System.out.println("Operation " + "[ " + w.opc + " ]" + " invalid.");
	}

	public static void invalidAddressInterruption(Word w) {
		interruptionOutput(w);
		System.out.println("The memory address or register tried to be accessed does not exists.");
	}

	public static void stopInterruption() {
		System.out.println("---------------------------------- System end ");
	}

	public static void noMemoryAvailableInterruption(Word w){
		interruptionOutput(w);
		System.out.println("There is no memory left. Please, remove a program from the memory.");
	}

	public static void noMemoryAvailableInterruption(){
		System.out.println("**System Interruption: \n " +
				           "There is no memory left. Please, remove a program from the memory.");
	}

	public static void programNotFoundInterruption() {
		System.out.println("This program was not found in memory.");
	}

	public static void emptyMemoryInterruption() {
		System.out.println("Empty memory, please add a program to it with the option [ 2 ] from the main menu.");
	}

	public static void invalidMenuOptionInterruption(){
		System.out.println(" Invalid option, try again.");
	}

	private static void interruptionOutput(Word w) {
		System.out.print("**System Interruption: \n " +
				         "**Instruction Error: [ ");
		Aux.dumpOutput(w);
		System.out.print("  ] ");
	}
	//endregion

	//region TRAP
	public void Trap_OUT(int p) {
		System.out.println(p);
	}

	public int Trap_IN() {
		Scanner teclado = new Scanner(System.in);
		return teclado.nextInt();
	}
	//endregion


	// -------------------------------------------------------------------------------------------------------
	// -------------------  S I S T E M A --------------------------------------------------------------------

	public VM vm;

	public Systems(){   // a VM com tratamento de interrupções
		vm = new VM();
	}

	// -------------------  S I S T E M A - fim --------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------


	// -------------------------------------------------------------------------------------------------------
	// ------------------- instancia e testa sistema
	public static Scanner teclado = new Scanner(System.in);

	public static void main(String[] args){
		Systems s = new Systems();
		menuOptions(s);
	}
	//region Main Menu

	//FASE 4 (START)
	public static void menuOptions(Systems s){
		int menuOptions = -1;
		do{
			System.out.println("--------- MENU DE OPCOES S.O. ---------");
			System.out.println(" [1] - Rodar programa existente em memoria");
			System.out.println(" [2] - Adicionar programa a memoria");
			System.out.println(" [3] - Remover programa da memória");
			System.out.println(" [4] - Rodar todos programas em memória");
			System.out.println(" [5] - Dump de memória de um programa específico");
			System.out.println(" [0] - Sair");
			menuOptions = teclado.nextInt();

			switch(menuOptions){
				case 0:
					System.out.println("---------------------------------- Systems end ");
					break;
				case 1:
					runProgMenu(s);
					break;
				case 2:
					loadProgMenu(s);
					break;
				case 3:
					removeProgMenu(s);
					break;
				case 4:
					runAllMenu(s);
					break;
				case 5:
					dumpProgMenu(s);
					break;
				default:
					invalidMenuOptionInterruption();
					break;
			}

		}while (menuOptions >0);
	}


	public static void dumpProgMenu(Systems s) {
		if (!s.vm.memoryManager.getProgramsInMemory().isEmpty()){
			Progs progs = Progs.___;
			ArrayList<Integer> reservedFrames = new ArrayList<>();
			System.out.println("------- Programs available in memory");
			for (ProcessControlBlock p : s.vm.memoryManager.getProgramsInMemory()) {
				s.vm.memoryManager.getFramesProg(p.getIdProg(), reservedFrames);
				System.out.println(p.toString() + " Frames:" + reservedFrames.toString());
			}
			System.out.println("-------");
			System.out.println("Type the program's number:");
			int idProg = teclado.nextInt();
			teclado.nextLine();
			System.out.println("-------");
			if (s.vm.memoryManager.existProgram(idProg)){
				Aux aux = new Aux();
				aux.dump(s.vm,idProg);
			}
			else
			{
				programNotFoundInterruption();
			}
		}
		else
		{
			emptyMemoryInterruption();
		}
	}

	public static void runProgMenu(Systems s){
		if (!s.vm.memoryManager.getProgramsInMemory().isEmpty()){
			Progs progs = Progs.___;
			ArrayList<Integer> reservedFrames = new ArrayList<>();
			System.out.println("------- Programs available in memory");
			for (ProcessControlBlock p : s.vm.memoryManager.getProgramsInMemory()) {
				s.vm.memoryManager.getFramesProg(p.getIdProg(), reservedFrames);
				System.out.println(p.toString() + " Frames:" + reservedFrames.toString());
			}
			System.out.println("-------");
			System.out.println("Type the program's number: ");
			int idProg = teclado.nextInt();
			teclado.nextLine();
			System.out.println("-------");
			if (s.vm.memoryManager.existProgram(idProg)){
				ArrayList<ProcessControlBlock> array_aux = new ArrayList<>();
				array_aux.add(s.vm.memoryManager.getPCB(idProg));
				s.vm.schedulerManager.run(array_aux);

			    //add the PCB to first at scheduler's ready line
                //and signal the CPU to run
			}
			else
			{
                programNotFoundInterruption();
            }
		}
		else
		{
			emptyMemoryInterruption();
		}
	}

	public static void loadProgMenu(Systems s) {
		int opcao_menu = -1;
		System.out.println("--------- ESCOLHA O PROGRAMA ---------");
		System.out.println(" [1] - ProgMin");
		System.out.println(" [2] - Fibonacci");
		System.out.println(" [3] - Fatorial");
		System.out.println(" [4] - BubbleSort");
		System.out.println(" [0] - Sair");
		opcao_menu = teclado.nextInt();

		switch (opcao_menu) {
			case 0:
				System.out.println("---------------------------------- voltando ao menu principal ");
				break;
			case 1:
				s.test_prog_minimo();
				break;
			case 2:
				s.test_fibonacci10();
				break;
			case 3:
				s.test_fatorial();
				break;
			case 4:
				//TODO BACKLOG adicionar o BubbleSort aqui
				s.test_bubble_sort();
				break;
			default:
				invalidMenuOptionInterruption();
				break;
		}
	}

	public static void removeProgMenu(Systems s){
		Progs progs = Progs.___;
		ArrayList<Integer> reservedFrames = new ArrayList<>();
		System.out.println("------- Programs available in memory");
		for (ProcessControlBlock p : s.vm.memoryManager.getProgramsInMemory()) {
			s.vm.memoryManager.getFramesProg(p.getIdProg(), reservedFrames);
			System.out.println(p.toString() + " Frames:" + reservedFrames.toString());
		}
		System.out.println("-------");
		System.out.println("Type the program's number: ");
		int idProg = teclado.nextInt();
		teclado.nextLine();
		System.out.println("-------");
		if (s.vm.memoryManager.existProgram(idProg)){
			System.out.println("Program [ " + idProg+ " ] [ " + s.vm.memoryManager.getProgramsInMemory().get(s.vm.memoryManager.getIndexOfProgInMemory(idProg)).getProgram() +  " ] was removed with success.");
			s.vm.memoryManager.framesDeallocation(idProg);
		}
		else{
			invalidMenuOptionInterruption();
		}
	}
	// FASE 4 (FINISH)

	// FASE 5* (START)
	public static void runAllMenu(Systems s) {
		if (s.vm.memoryManager.existProgramReady()) {
			s.vm.schedulerManager.run(s.vm.memoryManager.getProgramsInMemory());
		}
		else{
			emptyMemoryInterruption();
		}
	}
	// FASE 5* (FINISH)
	//endregion


	// -------------------------------------------------------------------------------------------------------
	// --------------- TUDO ABAIXO DE MAIN É AUXILIAR PARA FUNCIONAMENTO DO SISTEMA - nao faz parte

	// -------------------------------------------- teste do sistema ,  veja classe de programas

	//region testes
	public void test_prog_minimo(){
		Aux aux = new Aux();
		Word[] p = new Programas().progMinimo;
		aux.memoryLoad(p,Progs.PROG_MINIMO, vm);
	}

	public void test_fibonacci10(){
		Aux aux = new Aux();
		Word[] p = new Programas().fibonacci10;
		aux.memoryLoad(p,Progs.FIBBONACI10, vm);
	}

	// teste p3

	public void test_fatorial(){
		Aux aux = new Aux();
		Word[] p = new Programas().p3;
		aux.memoryLoad(p,Progs.FATORIAL, vm);
	}

	public void test_bubble_sort(){
		Aux aux = new Aux();
		Word[] p = new Programas().p3;
		aux.memoryLoad(p,Progs.BUBBLE_SORT, vm);
	}
	//endregion

	// -------------------------------------------  classes e funcoes auxiliares
	
	public static class Aux {
		public void dump(Word w) {
			System.out.print("[ ");
			dumpOutput(w);
			System.out.println("  ] ");
		}

		private static void dumpOutput(Word w) {
			System.out.print(w.opc);
			System.out.print(", ");
			System.out.print(w.r1);
			System.out.print(", ");
			System.out.print(w.r2);
			System.out.print(", ");
			System.out.print(w.p);
		}

		public void dump(Word[] m, int ini, int fim) {
			for (int i = ini; i <= fim; i++) {
				System.out.print(i);
				System.out.print(":  ");
				dump(m[i]);
			}
		}

		// FASE 4 (START)
		public void dump(VM vm, int idProg) {
			ArrayList<Integer> frames_reservados = new ArrayList<>();
			if(vm.memoryManager.getFramesProg(idProg,frames_reservados)){
				for (int i = 0; i < frames_reservados.size(); i++) {
					System.out.println("---- Frame [" + frames_reservados.get(i) + " ]");
					dump(vm.m, (frames_reservados.get(i) * vm.memoryManager.getframeSize()), (((frames_reservados.get(i) + 1) * vm.memoryManager.getframeSize()) - 1));
				}
			}
			else
			{
				programNotFoundInterruption();
			}
		}

		public void memoryLoad(Word[] p, Progs nomePrograma, VM vm) {
			ArrayList<Integer> frames_reservados = new ArrayList<>();
			if (vm.memoryManager.existsEmptyFrames(p.length, frames_reservados)) {
				vm.memoryManager.framesAllocation(frames_reservados, p, nomePrograma);
				System.out.println("Programa carregado com sucesso.");
				System.out.print("Frames Reservados ");
				for (int i : frames_reservados) {
					System.out.print("- [ " + i + " ]");
				}
				System.out.println();
			}
			else{
				noMemoryAvailableInterruption();
			}
		}
	}



	// region Classes

	//FASE 5* (START)
	public class SchedulerManager {

		//atributos
		private ArrayList<ProcessControlBlock> fila_exec;

		//construtor
		public SchedulerManager(){
			fila_exec = new ArrayList<>();
		}

		//region Metodos
		public void endOfLine(ProcessControlBlock program){
			ProcessControlBlock aux_ = this.fila_exec.remove(fila_exec.indexOf(program));
			this.fila_exec.add(aux_);
			this.fila_exec.get(this.fila_exec.indexOf(aux_)).programState = ProgramState.READY;
		}

		public ProcessControlBlock getProximoProgReady() {
			for (ProcessControlBlock program : fila_exec) {
				if (program.programState.equals(ProgramState.READY)){
					return program;
				}
			}
			return null;
		}

		public void run(ArrayList<ProcessControlBlock> programas) {
			this.fila_exec.addAll(programas); // adiciona programas na fila de exercucao
			while(this.hasNextProgReady()){ // check se tem prog em ready
				vm.cpu.run(this.getProximoProgReady()); //roda o proximo ready da fila
			}
			// qnd acaba, limpa fila e reseta os programas
			this.fila_exec.clear();
			for (ProcessControlBlock pcb : programas) {
			    vm.memoryManager.resetProgram(pcb);
			    pcb.programState = ProgramState.READY;
			}
			
		}

		private boolean hasNextProgReady() {
			for (ProcessControlBlock pcb : fila_exec) {
			    if(pcb.programState.equals(ProgramState.READY)){
			    	return true;
				}
			}
			return false;
		}

		//endregion
	}
	//FASE 5* (FINISH)

	public class MemoryManager {

		//region atributos
		private Frame[] frames;
		private int sizeFrame;
		private ArrayList<ProcessControlBlock> programsInMemory;
		//endregion

		// construtor
		public MemoryManager(int memorySize, int sizeFrame) {
			setFrames(new Frame[(memorySize / sizeFrame)]);
			setProgramsInMemory(new ArrayList<>());
			for (int i = 0; i < memorySize/ sizeFrame; i++) {
				getFrames()[i] = new Frame(i,true,Progs.___,0);
			}
			this.setSizeFrame(sizeFrame);
		}

		// region Metodos

		//region getters e setters

		public ArrayList<ProcessControlBlock> getProgramsInMemory() {
			return programsInMemory;
		}

		public void setProgramsInMemory(ArrayList<ProcessControlBlock> programsInMemory) {
			this.programsInMemory = programsInMemory;
		}

		public int getframeSize() {
			return sizeFrame;
		}

		public void setSizeFrame(int sizeFrame) {
			this.sizeFrame = sizeFrame;
		}

		public Frame[] getFrames() {
			return frames;
		}

		public void setFrames(Frame[] frames) {
			this.frames = frames;
		}

		public ProcessControlBlock getPCB(int idProg){
			for (ProcessControlBlock pcb : getProgramsInMemory()) {
				if(pcb.getIdProg() ==idProg){
					return pcb;
				}
			}
			return null;
		}

		public int getIndexOfProgInMemory(int idProg){
			for (ProcessControlBlock progs : getProgramsInMemory()) {
				if (progs.getIdProg() ==idProg){
					return getProgramsInMemory().indexOf(progs);
				}
			}
			return -1;
		}

		public int getNextId() {
			int maior=0;
			for (ProcessControlBlock prog : getProgramsInMemory()) {
				if(prog.getIdProg() >maior){
					maior = prog.getIdProg();
				}
			}
			return maior+1;
		}

		public boolean getFramesProg(int idProg, ArrayList<Integer> frames_reservados) {
			for (ProcessControlBlock prog : getProgramsInMemory()) {
				if(prog.getIdProg() ==idProg){
					frames_reservados.clear();
					frames_reservados.addAll(prog.getFramesProg());
					return true;
				}
			}
			return false;
		}
		//endregion

		public boolean existProgram(int idProg){
			for (ProcessControlBlock prog : getProgramsInMemory()) {
				if (prog.getIdProg() ==idProg){
					return true;
				}
			}
			return false;
		}


		public boolean existsEmptyFrames(double tamProg, ArrayList<Integer> frames_reservados) {
			int qnt_frames_necessarios = (int)Math.ceil(tamProg / vm.memoryManager.getframeSize());
			int cont = 0;
			for (int i = 0; i < vm.memoryManager.getFrames().length; i++) {
				if (vm.memoryManager.getFrames()[i].isEmptyFrame()) {
					frames_reservados.add(i);
					cont++;
					if (cont == qnt_frames_necessarios) {
						return true;
					}
				}
			}
			return false;
		}

		public void framesAllocation(ArrayList<Integer> frames_reservados, Word[] p, Progs nomePrograma){
			int cont=0;
			int max_id = getNextId();
			for (Integer frames_reservado : frames_reservados) {
				for (int j = (frames_reservado * vm.memoryManager.getframeSize()); j <= (((frames_reservado + 1) * vm.memoryManager.getframeSize()) - 1) && cont < p.length ; j++) {
					vm.m[j].opc = p[cont].opc;
					vm.m[j].r1 = p[cont].r1;
					vm.m[j].r2 = p[cont].r2;
					vm.m[j].p = p[cont].p;
					cont++;
				}
				vm.memoryManager.getFrames()[frames_reservado].setPrograma(nomePrograma, max_id);
			}
			vm.memoryManager.getProgramsInMemory().add(new ProcessControlBlock(nomePrograma,max_id,frames_reservados));
		}

		public void allocateMoreFrames(ArrayList<Integer> frames_reservados, int idProg, Progs nomePrograma){
			int cont=0;
			for (Integer frames_reservado : frames_reservados) {
				vm.memoryManager.getFrames()[frames_reservado].setPrograma(nomePrograma, idProg);
			}
			this.getProgramsInMemory().get(this.getProgramsInMemory().indexOf(getPCB(idProg))).getFramesProg().addAll(frames_reservados);
		}

		public void framesDeallocation(int idProg){
			for (Frame frame : getFrames()) {
				if (frame.getIdProg() == idProg){
					resetFrames(frame);
				}
			}
			int aux_ = getIndexOfProgInMemory(idProg);
			if (aux_>=0) {
				getProgramsInMemory().remove(aux_);
			}
		}

		private void resetFrames(Frame frame) {
			for (int j = (frame.getIdFrame() * vm.memoryManager.getframeSize()); j < (((frame.getIdFrame() + 1) * vm.memoryManager.getframeSize()) - 1); j++) {
				vm.m[j].opc = Opcode.___;
				vm.m[j].r1 = -1;
				vm.m[j].r2 = -1;
				vm.m[j].p = -1;
			}
			frame.setIdProg(0);
			frame.setEmptyFrame(true);
		}

		public boolean existProgramReady() {
			for (ProcessControlBlock program : getProgramsInMemory()) {
			    if (program.programState.equals(ProgramState.READY)){
			    	return true;
				}
			}
			return false;
		}

		public void resetProgram(ProcessControlBlock program) {
			ProcessControlBlock prog = getProgramsInMemory().get(getProgramsInMemory().indexOf(program));
			prog.pc_ = 0;
			prog.countCurrentFrame =0;
			prog.offset =0;
			for (Frame frame : this.getFrames()) {
				if (frame.getIdProg() == program.getIdProg()){
					for (int j = (frame.getIdFrame() * vm.memoryManager.getframeSize()); j < (((frame.getIdFrame() + 1) * vm.memoryManager.getframeSize()) - 1); j++) {
					    vm.m[j].interruption = 0;
					}
				}
			}
		}



		// endregion

		// region SubClasses: Frame
		public class Frame {
			private int idFrame;
			private boolean emptyFrame;
			private int idProg;

			//contrutor
			public Frame(int idFrame, boolean emptyFrame, Progs nomeProg, int idProg) {
				this.setIdFrame(idFrame);
				this.setEmptyFrame(emptyFrame);
				this.setIdProg(idProg);
			}

			//region Metodos
			public boolean isEmptyFrame() {
				return emptyFrame;
			}

			public void setEmptyFrame(boolean emptyFrame) {
				this.emptyFrame = emptyFrame;
			}

			public void setPrograma (Progs nomeProg, int idProg){
				this.setIdProg(idProg);
				this.setEmptyFrame(false);
			}

			public int getIdFrame() {
				return idFrame;
			}

			public void setIdFrame(int idFrame) {
				this.idFrame = idFrame;
			}

			public int getIdProg() {
				return idProg;
			}

			public void setIdProg(int idProg) {
				this.idProg = idProg;
			}
			// endregion
		}
		//endregion

	}

	public class ProcessControlBlock {
		private Progs program;
		private int idProg;
		private ArrayList<Integer> framesProg;
		private final int frameSize;
		private int countCurrentFrame;
		private int offset;
		private int pc_;
		private int[] reg;
		// FASE 5* (START)
		private ProgramState programState;
		// FASE 5* (FINISH)

		//construtor
		public ProcessControlBlock(Progs program, int idProg, ArrayList<Integer> framesProg){
			this.setProgram(program);
			this.setIdProg(idProg);
			this.setFramesProg(new ArrayList<>());
			this.getFramesProg().addAll(framesProg);
			this.frameSize = vm.memoryManager.getframeSize();
			this.countCurrentFrame = 0;
			this.offset = 0;
			this.pc_ = framesProg.get(0)* frameSize;
			// FASE 5* (START)
			this.programState = ProgramState.READY;
			this.reg = new int[10];
			// FASE 5* (FINISH)
		}


		//region Metodos

		@Override
		public String toString() {
			return " [ " + getIdProg() + " ] " +	"Programa: " + getProgram();
		}


		public void nextPC(){
			if((offset +1)==16){
				countCurrentFrame++;
				offset =0;
			}
			else {
				offset++;
			}
			pc_ = (getFramesProg().get(countCurrentFrame)* frameSize)+ offset;
		}


		public void jmpPc(int pc){
			if (pc< frameSize){
				this.offset =pc;
				this.countCurrentFrame = 0;
			}
			else {
				this.offset =pc% frameSize;
				if (Math.ceil((double) pc / frameSize) > getFramesProg().size()) {
					ArrayList<Integer> moreFrames = new ArrayList<>();
					if (vm.memoryManager.existsEmptyFrames(16.0, moreFrames)) {
						vm.memoryManager.allocateMoreFrames(moreFrames, this.getIdProg(), vm.memoryManager.getProgramsInMemory().get(vm.memoryManager.getIndexOfProgInMemory(getIdProg())).getProgram());
						this.getFramesProg().addAll(moreFrames);
					} else {
						vm.cpu.ir.interruption=5;
					}
				}
				this.countCurrentFrame = (int)Math.floor((double) pc / frameSize);
			}
			this.pc_ = (getFramesProg().get(countCurrentFrame) * frameSize) + offset;
		}


		public int getLogicAddress(int position){
			int frame_aux =(int)Math.ceil((double) position/ frameSize);
			int offset_aux = position% frameSize;
			int logicFrame = frame_aux-1;
			if (position < frameSize){
				return (getFramesProg().get(logicFrame)* frameSize) + offset_aux;
			}
			if (frame_aux<= vm.cpu.pcb.getFramesProg().size()){
				return ((vm.cpu.pcb.getFramesProg().get(logicFrame))* frameSize)+offset_aux;
			}
			else {
				ArrayList<Integer> newFrames = new ArrayList<>();
				if(vm.memoryManager.existsEmptyFrames((frame_aux - vm.cpu.pcb.getFramesProg().size())* frameSize,newFrames)) {
					vm.memoryManager.allocateMoreFrames(newFrames, this.getIdProg(), vm.memoryManager.getProgramsInMemory().get(vm.memoryManager.getIndexOfProgInMemory(this.getIdProg())).getProgram());
					return ((1+ vm.cpu.pcb.getFramesProg().get(logicFrame))* frameSize)+offset_aux;
				}
				else{
					vm.cpu.ir.interruption=5;
					return vm.cpu.pcb.pc_;
				}
			}
		}

		//region Getter Setters


		public int getIdProg() {
			return idProg;
		}

		public void setIdProg(int idProg) {
			this.idProg = idProg;
		}

		public ArrayList<Integer> getFramesProg() {
			return framesProg;
		}

		public void setFramesProg(ArrayList<Integer> framesProg) {
			this.framesProg = framesProg;
		}

		public Progs getProgram() {
			return program;
		}

		public void setProgram(Progs program) {
			this.program = program;
		}

		//endregion
		
		//endregion

	}

	//endregion


	// FASE 4 (FINISH)

	// -------------------------------------------  fim classes e funcoes auxiliares

	//  -------------------------------------------- programas aa disposicao para copiar na memoria (vide aux.carga)
	public class Programas { //TODO PROGRAMA ADICIONADO DEVE SER INSERID NO ENUMERATION "Progs".
		public Word[] progMinimo = new Word[] {
				new Word(Opcode.LDI, 0, -1, 999),
				new Word(Opcode.STD, 0, -1, 10),
				new Word(Opcode.STD, 0, -1, 11),
				new Word(Opcode.STD, 0, -1, 12),
				new Word(Opcode.STD, 0, -1, 13),
				new Word(Opcode.STD, 0, -1, 14),
				new Word(Opcode.STOP, -1, -1, -1) };

		//TODO BACKLOG inserir o esquema de trap no programa
		public Word[] fibonacci10 = new Word[] { // mesmo que prog exemplo, so que usa r0 no lugar de r8
				new Word(Opcode.LDI, 1, -1, 0),
				new Word(Opcode.STD, 1, -1, 20), //50
				new Word(Opcode.LDI, 2, -1, 1),
				new Word(Opcode.STD, 2, -1, 21), //51
				new Word(Opcode.LDI, 0, -1, 22), //52
				new Word(Opcode.LDI, 6, -1, 6),
				new Word(Opcode.LDI, 7, -1, 31), //61
				new Word(Opcode.LDI, 3, -1, 0),
				new Word(Opcode.ADD, 3, 1, -1),
				new Word(Opcode.LDI, 1, -1, 0),
				new Word(Opcode.ADD, 1, 2, -1),
				new Word(Opcode.ADD, 2, 3, -1),
				new Word(Opcode.STX, 0, 2, -1),
				new Word(Opcode.ADDI, 0, -1, 1),
				new Word(Opcode.SUB, 7, 0, -1),
				new Word(Opcode.JMPIG, 6, 7, -1),
				new Word(Opcode.STOP, -1, -1, -1)};


		public Word[] p3 = new Word[]{
				new Word(Opcode.LDI, 8, -1, 1), // joga o valor 10 no Registrador1
            	new Word(Opcode.TRAP,-1,-1,-1),// chama trap para IN
				new Word(Opcode.STD, 9, -1, 50), // coloca o valor do Registrado9 na posição 20 da memoria
				new Word(Opcode.LDD,1,-1,50), // ler da memoria e colocar no registrador
				new Word(Opcode.LDD,2,-1,50), // ler da memoria e colocar no registrador
				new Word(Opcode.LDI, 0,-1,18), // numero da linha que será pulado no JMP abaixo
				new Word(Opcode.JMPIL,0,2,-1),// comparar se registrador < 0
				new Word(Opcode.SUBI,2,-1,1), //r2 = 9

				// inicio loop
				new Word(Opcode.ADDI,2,-1,1), // readiona 1 pra que o loop fique certo
				new Word(Opcode.SUBI,2,-1,1), // subtrai pra fazer r1*r2
				new Word(Opcode.MULT,1,2,-1), // multiplica
				new Word(Opcode.SUBI,2,-1,1), // subtrai pra comparar a zero e possivelmente parar
				new Word(Opcode.JMPIGM,-1,2,8),	// compara a zero para ver se precisa parar   x = 6
				// fim loop

      			new Word(Opcode.STD,1,-1,50), // acaba o loop, joga o valor de r1 (resultado do fatorial) na posicao 20 da memoria
				new Word(Opcode.LDI,8,-1,2), // setta o registrador 8 para o valor de OUT
				new Word(Opcode.LDI, 9, -1,50), // poe no registrador 9 a posicao de memoria que vai ser acessada no TRAP
				new Word(Opcode.TRAP, -1,-1,-1), // TRAP OUT
				new Word(Opcode.STOP,-1,-1,-1), // acaba o programa
				new Word(Opcode.LDI,1,-1,-1), // (se no primeiro jmp, o input for -1, vem pra cá) joga o valor de -1 no registrador 1
				new Word(Opcode.STD,1,-1,50), // armazena no valor de r1 na posicao 20 da memoria
				new Word(Opcode.LDI,8,-1,2), // setta o registrador 8 para o valor de OUT
				new Word(Opcode.LDI, 9, -1,50), // poe no registrador 9 a posicao de memoria que vai ser acessada no TRAP
				new Word(Opcode.TRAP, -1,-1,-1), // TRAP OUT
				new Word(Opcode.STOP,-1,-1,-1)	// acaba o programa
		};

		//todo backlog bubblesort
		//public Word[] BubbleSort = new Word{

		// le a quantidade de elementos
		// armazena os elementos na memoria (ex: 50,51,52,53,54)
		//

		//}
	}
}

