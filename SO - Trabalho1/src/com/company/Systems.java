package com.company;
// PUCRS - Escola Politécnica - Sistemas Operacionais
// Prof. Fernando Dotti
// Código fornecido como parte da solução do projeto de Sistemas Operacionais
//
// Fase 1 - máquina virtual (vide enunciado correspondente)
//


import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Systems {


	public static Semaphore shellNeedInOperation = new Semaphore(0); //FASE 6 E 7 // usado para controlar a sessao critica do trap_in
	public static Semaphore cpuIdle = new Semaphore(0); //FASE 6 E 7  -> avisa o escalonador que a CPU ta livre


	// -------------------------------------------------------------------------------------------------------
	// --------------------- H A R D W A R E - definicoes de HW ----------------------------------------------

	// -------------------------------------------------------------------------------------------------------
	// --------------------- M E M O R I A -  definicoes de opcode e palavra de memoria ----------------------

	public class Word {    // cada posicao da memoria tem uma instrucao (ou um dado)
		public Opcode opc;    //
		public int r1;        // indice do primeiro registrador da operacao (Rs ou Rd cfe opcode na tabela)
		public int r2;        // indice do segundo registrador da operacao (Rc ou Rs cfe operacao)
		public int p;        // parametro para instrucao (k ou A cfe operacao), ou o dado, se opcode = DADO
		//public int interruption //FASE 6 E 7 -> readaptacao do INTERRUPT pra variavel da CPU
		public Word(Opcode _opc, int _r1, int _r2, int _p) {
			opc = _opc;
			r1 = _r1;
			r2 = _r2;
			p = _p;
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

	public enum Progs{
		PROG_MINIMO, FIBBONACI10,
		FATORIAL, BUBBLE_SORT,
		___
	}

	public enum ProgramState {
		READY,RUNNING,
		BLOCKED //FASE 6 E 7 -> adicao do estado blocked
	}

	//endregion

	public class CPU extends Thread{
		private Word ir;              // instruction register,
		private int[] reg;           // registradores da CPU
		private Word[] m;           // CPU acessa MEMORIA, guarda referencia 'm' a ela. memoria nao muda. ee sempre a mesma.
		private ProcessControlBlock pcb;
		private Semaphore cpuSemaphore = new Semaphore(0); //FASE 6 E 7 -> controla o inicio da operacao da CPU
		private int interruptionFlag; //FASE 6 E 7 -> indica interrupcao
		private int timeBetweenInstructions; //FASE 6 E 7 -> funcionalidade de adicao de tempo entre as operacoes

		//construtor
		public CPU(Word[] _m) {     // ref a MEMORIA e interrupt handler passada na criacao da CPU
			m = _m;                // usa o atributo 'm' para acessar a memoria.
			reg = new int[10];        // aloca o espaço dos registradores
			this.start(); //FASE 6 E 7 -> start thread
			cpuIdle.release();
			this.setName("cpuThread");
		}

		public void setContext(ProcessControlBlock program){ //FASE 6 E 7 -> setta contexto pra entao liberar o semaforo p uso da cpu
			this.pcb = program;
			this.reg = program.reg;
		}

		public void setInterruptionFlag(int interruptionFlag) {
			this.interruptionFlag = interruptionFlag;
		}

		//TODO BACKLOG
		@Override
		public void run() {        // execution da CPU supoe que o contexto da CPU, vide acima, esta devidamente setado
			long sum;
			long sub;

			while(true) {

				try {
					this.cpuSemaphore.acquire(); //FASE 6 e 7
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				//System.out.println("CPU THREAD : " +Thread.currentThread().getName());
				int count_round_robin = 0;
				this.setInterruptionFlag(0); //FASE 6 E 7 -> zera no inicio da interrupcao
				this.pcb.programState = ProgramState.RUNNING;

				while (true) {         // ciclo de instrucoes. acaba cfe instrucao, veja cada caso.

					// FETCH
					ir = m[pcb.pc_];
					System.out.println(pcb.programState + " -> " + "Programa[" + pcb.getIdProg() + " - " + pcb.getProgram() + "]" + " PC = " + pcb.pc_);


					if(this.timeBetweenInstructions > 0){
						try {
							Thread.sleep(timeBetweenInstructions);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}


					// Contagem de Q para fazer o Escalonamento Round_Robin
					count_round_robin++;
					if (count_round_robin == vm.rr_q) {
						this.interruptionFlag = 6;
					}

					// EXECUTA INSTRUCAO NO ir
					sum = 0;
					sub = 0;
					switch (ir.opc) { // para cada opcode, sua exetypocução
						case TRAP:
                            this.interruptionFlag = 7;
							break;

						case JMP: //PC ← k
							//error treatment
							if (m[pcb.getLogicAddress(ir.p)].opc == Opcode.___) {
								this.interruptionFlag = 2;
							}
							//execution
							else {
								pcb.jmpPc(ir.p);
							}
							break;

						case JMPI: //PC ← R1
							//error treatment
							if (vm.validRegister(ir.r1) || m[pcb.getLogicAddress(reg[ir.r1])].opc == Opcode.___) {
								this.interruptionFlag = 2;
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
									if ((m[pcb.getLogicAddress(reg[ir.r1])].opc == Opcode.___)) {
										this.interruptionFlag = 2;
									}
									//execution
									else {
										pcb.jmpPc(reg[ir.r1]);
									}
								} else {
									pcb.nextPC();
								}
							} else {
								this.interruptionFlag = 2;
							}
							break;

						case JMPIL: //if R2 < 0 then PC ← R1 Else PC ← PC +1
							if (vm.validRegister(ir.r2)) {
								if (reg[ir.r2] < 0) {
									//error treatment
									if (m[pcb.getLogicAddress(reg[ir.r1])].opc == Opcode.___) {
										this.interruptionFlag = 2;
									}
									//execution
									else {
										pcb.jmpPc(reg[ir.r1]);
									}
								} else {
									pcb.nextPC();
								}
							} else {
								this.interruptionFlag = 2;
							}

							break;

						case JMPIE: //if R2 == 0 then PC ← R1 Else PC ← PC +1
							if (vm.validRegister(ir.r2)) {
								if (reg[ir.r2] == 0) {
									//error treatment
									if (m[pcb.getLogicAddress(reg[ir.r1])].opc == Opcode.___) {
										this.interruptionFlag = 2;
									}
									//execution
									else {
										pcb.jmpPc(reg[ir.r1]);
									}
								} else {
									pcb.nextPC();
								}
							} else {
								this.interruptionFlag = 2;
							}
							break;

						case JMPIM: // PC ← [A]
							//error treatment
							if (m[pcb.getLogicAddress(ir.p)].opc == Opcode.___) {
								this.interruptionFlag = 2;
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
										this.interruptionFlag = 2;
									}
									//execution
									else {
										pcb.jmpPc(ir.p);
									}
								} else {
									pcb.nextPC();
								}
							} else {
								this.interruptionFlag = 2;
							}
							break;

						case JMPILM: //if R2 < 0 then PC ← [A] Else PC ← PC +1
							if (vm.validRegister(ir.r2)) {
								if (reg[ir.r2] < 0) {
									//error treatment
									if (m[pcb.getLogicAddress(ir.p)].opc == Opcode.___) {
										this.interruptionFlag = 2;
									}
									//execution
									else {
										pcb.jmpPc(ir.p);
									}
								} else {
									pcb.nextPC();
								}
							} else {
								this.interruptionFlag = 2;
							}
							break;

						case JMPIEM: //if R2 == 0 then PC ← [A] Else PC ← PC +1
							if (vm.validRegister(ir.r2)) {
								if (reg[ir.r2] == 0) {
									//error treatment
									if (m[pcb.getLogicAddress(ir.p)].opc == Opcode.___) {
										this.interruptionFlag = 2;
									}
									//execution
									else {
										pcb.jmpPc(ir.p);
									}
								} else {
									pcb.nextPC();
								}
							} else {
								this.interruptionFlag = 2;
							}
							break;

						case STOP: // Interruption ← 4
							this.interruptionFlag = 4;
							break;

						case ADDI: // R1 ← R1 + k
							//tramento erro
							if (vm.validRegister(ir.r1)) {
								sum = reg[ir.r1] + ir.p;
								if (sum > Integer.MAX_VALUE) {
									this.interruptionFlag = 1;
								}
								//execution
								else {
									reg[ir.r1] = reg[ir.r1] + ir.p;
									pcb.nextPC();
								}
							} else {
								this.interruptionFlag = 2;
							}
							break;

						case SUBI: // R1 ← R1 – k
							//tratamento erro
							if (vm.validRegister(ir.r1)) {
								sub = reg[ir.r1] - ir.p;
								if (sub < Integer.MIN_VALUE) {
									this.interruptionFlag = 1;
								}
								//execution
								else {
									reg[ir.r1] = reg[ir.r1] - ir.p;
									pcb.nextPC();
								}
							} else {
								this.interruptionFlag = 2;
							}
							break;

						case ADD: // R1 ← R1 + R2
							//tratamento erro
							if (vm.validRegister(ir.r1) && vm.validRegister(ir.r2)) {
								sum = reg[ir.r1] + reg[ir.r2];
								if (sum > Integer.MAX_VALUE) {
									this.interruptionFlag = 1;
								}
								//execution
								else {
									reg[ir.r1] = reg[ir.r1] + reg[ir.r2];
									pcb.nextPC();
								}
							} else {
								this.interruptionFlag = 2;
							}
							break;

						case SUB: // R1 ← R1 - R2
							//tratamento erro
							if (vm.validRegister(ir.r1) && vm.validRegister(ir.r2)) {
								sub = reg[ir.r1] - reg[ir.r2];
								if (sub < Integer.MIN_VALUE) {
									this.interruptionFlag = 1;
								}
								//execution
								else {
									reg[ir.r1] = reg[ir.r1] - reg[ir.r2];
									pcb.nextPC();
								}
							} else {
								this.interruptionFlag = 2;
							}
							break;

						case MULT: // R1 ← R1 * R2
							//tratamento erro
							if (vm.validRegister(ir.r1) && vm.validRegister(ir.r2)) {
								sum = (long) reg[ir.r1] * reg[ir.r2];
								if (sum < Integer.MIN_VALUE || sum > Integer.MAX_VALUE) {
									this.interruptionFlag = 1;
								}
								//execution
								else {
									reg[ir.r1] = reg[ir.r1] * reg[ir.r2];
									pcb.nextPC();
								}
							} else {
								this.interruptionFlag = 2;
							}
							break;

						case LDI: // R1 ← k
							if (vm.validRegister(ir.r1)) {
								reg[ir.r1] = ir.p;
								pcb.nextPC();
							} else {
								this.interruptionFlag = 2;
							}
							break;

						case LDD: // R1 ← [A]
							//validacao
							if (vm.validRegister(ir.r1)) {
								reg[ir.r1] = m[pcb.getLogicAddress(ir.p)].p;
								pcb.nextPC();
							} else {
								this.interruptionFlag = 2;
							}
							break;

						case STD: // [A] ← R1
							if (vm.validRegister(ir.r1)) {
								m[pcb.getLogicAddress(ir.p)].opc = Opcode.DATA;
								m[pcb.getLogicAddress(ir.p)].p = reg[ir.r1];
								pcb.nextPC();
							} else {
								this.interruptionFlag = 2;
							}
							break;

						case LDX: // R1 ← [R2]
							if (vm.validRegister(ir.r1) && vm.validRegister(ir.r2)) {
								reg[ir.r1] = m[pcb.getLogicAddress(reg[ir.r2])].p;
								pcb.nextPC();
							} else {
								this.interruptionFlag = 2;
							}
							break;

						case STX: // [R1] ← R2
							if (vm.validRegister(ir.r1) && vm.validRegister(ir.r2)) {
								m[pcb.getLogicAddress(reg[ir.r1])].opc = Opcode.DATA;
								m[pcb.getLogicAddress(reg[ir.r1])].p = reg[ir.r2];
								pcb.nextPC();
							} else {
								this.interruptionFlag = 2;
							}
							break;

						case SWAP: //T ← Ra Ra ← Rb Rb ←T
							if (vm.validRegister(ir.r1) && vm.validRegister(ir.r2)) {
								int t = reg[ir.r1];
								reg[ir.r1] = reg[ir.r2];
								reg[ir.r2] = t;
								pcb.nextPC();
							} else {
								this.interruptionFlag = 2;
							}

							break;

						default:
							this.interruptionFlag = 3;
							break;
					}

					if (this.interruptionFlag != 0) {
						if (this.interruptionFlag == 1) //Overflow em uma operacao aritmetica
						{
							overflowInterruption(ir);
							vm.memoryManager.resetProgram(this.pcb);
							break;
						}
						else if (this.interruptionFlag == 2) //acessou um endereço invalido de memoria (ArrayOutOfBound)
						{
							invalidAddressInterruption(ir);
							vm.memoryManager.resetProgram(this.pcb);
							break;
						}
						else if (this.interruptionFlag == 3) //Instrucao Invalida
						{
							invalidOpcodeInterruption(ir);
							vm.memoryManager.resetProgram(this.pcb);
							break;
						}
						else if (this.interruptionFlag == 4) //opcode STOP em sí
						{
							stopInterruption();
							vm.memoryManager.resetProgram(this.pcb);
							vm.schedulerManager.endExecution(this.pcb);
							break;
						}
						else if (this.interruptionFlag == 5) //sem memoria disponivel
						{
							noMemoryAvailableInterruption(ir);
							break;
						}
						else if (this.interruptionFlag == 6) { //round robin  //FASE 6 TODO BACKLOG
							vm.schedulerManager.endOfQueue(pcb);
							System.out.println(pcb.programState + " -> " + "Programa[" + pcb.getIdProg() + " - " + pcb.getProgram() + "]" + " PC = " + pcb.pc_);
							break;
						}
						else if(this.interruptionFlag == 7) { //FASE 6 E 7 ->  requisição envio InOut
							System.out.println("Programa [" + pcb.getIdProg() + " - " + pcb.getProgram() + "] foi enviado para o gerenciador de E/S");
                            if (this.reg[8] == 1){
                                if(this.reg[9] != 0) {
                                    vm.ioThread.addNewRequest(this.pcb);
                                    pcb.nextPC();
                                }
                                else{
									inOutOperationInterruption();
                                }
                            }
                            else if (this.reg[8]==2){
								if(this.reg[9] != 0) {
									vm.ioThread.addNewRequest(this.pcb);
									pcb.nextPC();
								}
								else{
									inOutOperationInterruption();
								}
                            }
                            else{
                                invalidOpcodeInterruption(ir);
                            }
							break;
						}
						else if (this.interruptionFlag == 9){ //FASE 6 E 7 -> requisicao retorno do InOut
							vm.schedulerManager.addFirstInQueue(this.pcb);
						}

					}
				}
				cpuIdle.release();
			}
		}

		public void setTimeBetweenInstructions(int time) {
			this.timeBetweenInstructions = time;
		}
	}



	// ------------------ C P U - fim ------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------


	// ------------------- V M  - constituida de CPU e MEMORIA -----------------------------------------------
	// -------------------------- atributos e construcao da VM -----------------------------------------------
	public class VM{

		//atributos
		public int memorySize;
		public Word[] m;
		public CPU cpu;
		public MemoryManager memoryManager;
		public SchedulerManager schedulerManager;
		public InOutManager ioThread; //FASE 6 E 7 -> adicao controle de IO

		public int rr_q;

		//construtor
		public VM() {   // vm deve ser configurada com endereço de tratamento de interrupcoes
			// memória
			int tamFrame = 16;
			this.memorySize = 1024;
			this.m = new Word[memorySize]; // m ee a memoria
			for (int i = 0; i < memorySize; i++) { m[i] = new Word(Opcode.___, -1, -1, -1); }

			//START THREADS
			this.memoryManager = new MemoryManager(memorySize, tamFrame);
			this.schedulerManager = new SchedulerManager();
			this.cpu = new CPU(m);
			this.ioThread = new InOutManager();

			this.rr_q = 5;
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
		System.out.println("!Erro: Houve Overflow na operação aritmética");
	}

	public static void invalidOpcodeInterruption(Word w) {
		interruptionOutput(w);
		System.out.println("!Erro: Operação " + "[ " + w.opc + " ]" + " nao identificada.");
	}

	public static void invalidAddressInterruption(Word w) {
		interruptionOutput(w);
		System.out.println("!Erro: O endereço de memória ou registrador referenciado não existe.");
	}

	public static void stopInterruption() {
		System.out.println("---------------------------------- fim do programa ");
	}

	public static void noMemoryAvailableInterruption(Word w){
		interruptionOutput(w);
		System.out.println("!Erro: Nao há memória livre. Favor, remova um programa da memória");
	}

	public static void noMemoryAvailableInterruption(){
		System.out.println("**Interrupção de Sistema: \n " +
				"!Erro: Nao há memória livre. Favor, remova um programa da memória");
	}

	public static void programNotFoundInterruption() {

		System.out.println("!Erro: Este programa nao foi encontrado em memória.");
	}

	public static void emptyMemoryInterruption() {
		System.out.println("!Erro: Memoria vazia, favor adicione um programa com a opção [ 2 ]");
	}

	public static void invalidMenuOptionInterruption(){
		System.out.println("!Erro: Opcao invalida, tente novamente.");
	}

	private static void interruptionOutput(Word w) {
		System.out.print("**Interrupção de Sistema: \n " +
						"**Erro na intrução: [ ");
		Aux.dumpOutput(w);
		System.out.print("  ] ");
	}

	private static void inOutOperationInterruption(){
		System.out.println("!Erro: Erro ao tentar acessar um dispositivo Externo.");
	} //FASE 6 E 7
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
	public static final Scanner teclado = new Scanner(System.in);

	public static void main(String[] args) throws InterruptedException {
		Systems s = new Systems();
		menuOptions(s);
	}
	//region Main Menu

	public static void menuOptions(Systems s) throws InterruptedException {
		int menuOptions = -1;
		do{
			StringBuilder sb = new StringBuilder();
			sb.append("--------- MENU DE OPCOES S.O. ---------\n")
					.append(" [1] - Rodar programa existente em memoria\n")
					.append(" [2] - Adicionar programa a memoria\n")
					.append(" [3] - Remover programa da memória\n")
					.append(" [4] - Rodar todos programas em memória\n")
					.append(" [5] - Dump de memória de um programa específico\n")
					.append(" [6] - Nao preciso do menu agora\n")
					.append(" [7] - Adicionar tempo(ms) entre as instrucoes\n")
					.append(" [0] - Sair\n");

			s.vm.ioThread.setShellRequest(true,sb.toString()); //FASE 6 E 7 -> pede um input atraves do Gerente de IO
			shellNeedInOperation.release();
			menuOptions = s.vm.ioThread.getReturnShell();

			switch(menuOptions){
				case 0:
					//s.vm.cpu.join();
					//s.vm.schedulerManager.join();
					//s.vm.ioThread.join();
					s.vm.cpu.stop();
					s.vm.schedulerManager.stop();
					s.vm.ioThread.stop();
					System.out.println("---------------------------------- fim do sistema ");
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
				case 6:
					Thread.sleep(5000);
					break;
				case 7:
					timeBetweenInstructions(s);
					break;
				default:
					invalidMenuOptionInterruption();
					break;
			}
		}while (menuOptions >0);
	}

	private static void timeBetweenInstructions(Systems s) throws InterruptedException {
		int time;
		s.vm.ioThread.setShellRequest(true, "Type in milliseconds the time between instructions: ");
		shellNeedInOperation.release();
		time = s.vm.ioThread.getReturnShell();
		s.vm.cpu.setTimeBetweenInstructions(time);
	}


	public static void dumpProgMenu(Systems s) throws InterruptedException {
		if (!s.vm.memoryManager.getProgramsInMemory().isEmpty()){
			Progs progs = Progs.___;
			ArrayList<Integer> reservedFrames = new ArrayList<>();
			StringBuilder sb = new StringBuilder();
			sb.append("------- Programas disponiveis em memória\n");
			for (ProcessControlBlock p : s.vm.memoryManager.getProgramsInMemory()) {
				s.vm.memoryManager.getFramesProg(p.getIdProg(), reservedFrames);
				sb.append(p.toString()).append(" Frames:").append(reservedFrames.toString()).append("\n");
			}
			sb.append("-------\nDigite o numero do programa: ");
			int idProg;
			s.vm.ioThread.setShellRequest(true, sb.toString());
			shellNeedInOperation.release();
			idProg = s.vm.ioThread.getReturnShell();
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

	public static void runProgMenu(Systems s) throws InterruptedException {
		if (!s.vm.memoryManager.getProgramsInMemory().isEmpty()){
			Progs progs = Progs.___;
			ArrayList<Integer> reservedFrames = new ArrayList<>();
			StringBuilder sb = new StringBuilder();
			sb.append("------- Programas disponiveis em memória\n");
			for (ProcessControlBlock p : s.vm.memoryManager.getProgramsInMemory()) {
				s.vm.memoryManager.getFramesProg(p.getIdProg(), reservedFrames);
				sb.append(p.toString()).append(" Frames:").append(reservedFrames.toString()).append("\n");
			}
			sb.append("-------\nDigite o numero do programa: ");
			int idProg;
			s.vm.ioThread.setShellRequest(true,sb.toString()); //FASE 6 E 7 -> adiciona na fila de E/S
			shellNeedInOperation.release(); //FASE 6 E 7 -> libera uma permissao pro gerente olhar a fila
			idProg = s.vm.ioThread.getReturnShell(); //FASE 6 E 7 -> ES vai colocar o resultado nessa variavel ReturnShell
			if (s.vm.memoryManager.existProgram(idProg)){ //valida se a entrada é valida
				if(s.vm.memoryManager.programsInMemory.get(s.vm.memoryManager.getIndexOfProgInMemory(idProg)).programState.equals(ProgramState.READY)) {
					s.vm.schedulerManager.addToExecutionQueue(s.vm.memoryManager.getPCB(idProg)); //FASE 6 add fila escalonador
					s.vm.schedulerManager.schedulerSemaphore.release(); // libera pro escaloandor olhar a fila
				}
				else{
					programIsAlreadyRunning(idProg);
				}
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

	private static void programIsAlreadyRunning(int idProg) {
		System.out.println("!Erro: este programa já está rodando em memória.");
	}

	public static void loadProgMenu(Systems s) throws InterruptedException {
		int menuOptions = -1;
		s.vm.ioThread.setShellRequest(true, "");
		shellNeedInOperation.release();
		System.out.println("--------- ESCOLHA O PROGRAMA ---------");
		System.out.println(" [1] - ProgMin");
		System.out.println(" [2] - Fibonacci");
		System.out.println(" [3] - Fatorial");
		System.out.println(" [4] - BubbleSort");
		System.out.println(" [0] - Sair");

		menuOptions = s.vm.ioThread.getReturnShell();

		switch (menuOptions) {
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

	public static void removeProgMenu(Systems s) throws InterruptedException {
		Progs progs = Progs.___;
		ArrayList<Integer> reservedFrames = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		sb.append("------- Programas disponiveis em memória\n");
		for (ProcessControlBlock p : s.vm.memoryManager.getProgramsInMemory()) {
			s.vm.memoryManager.getFramesProg(p.getIdProg(), reservedFrames);
			sb.append(p.toString()).append(" Frames:").append(reservedFrames.toString()).append("\n");
		}
		sb.append("-------\nDigite o numero do programa: ");
		int idProg;
		s.vm.ioThread.setShellRequest(true,sb.toString());
		shellNeedInOperation.release();
		idProg = s.vm.ioThread.getReturnShell();
		System.out.println("-------");
		if (s.vm.memoryManager.existProgram(idProg)){
			System.out.println("Programa [ " + idProg+ " ] [ " + s.vm.memoryManager.getProgramsInMemory().get(s.vm.memoryManager.getIndexOfProgInMemory(idProg)).getProgram() +  " ] foi removido com sucesso.");
			s.vm.memoryManager.framesDeallocation(idProg);
		}
		else{
			invalidMenuOptionInterruption();
		}
	}

	public static void runAllMenu(Systems s) {
		if (s.vm.memoryManager.existProgramReady()) {
			//s.vm.schedulerManager.run(s.vm.memoryManager.getProgramsInMemory()); //FASE 6 remove
			for (ProcessControlBlock program : s.vm.memoryManager.getProgramsInMemory()) { //FASE 6 add
			    s.vm.schedulerManager.addToExecutionQueue(program);//FASE 6 add
			}
			s.vm.schedulerManager.schedulerSemaphore.release();//FASE 6 add
		}
		else{
			emptyMemoryInterruption();
		}
	}
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
	public enum TypeIORequest{ //FASE 6 E 7 -> controle de quem esta acessando o gerente de E/S
		SHELL,CONSOLE
	}



	public class InOutManager extends Thread { //FASE 6 E 7 -> classe e thread nova

		//region Attributes
		private Semaphore trapInReady = new Semaphore(0);
		private ProcessControlBlock programBeingExecuted; // programa da fila sendo executado
		private ArrayList<ProcessControlBlock> consoleRequestQueue; //fila de pedidos do console
		private boolean shellRequest; //controle de solicitacao do shell
		private int returnShell = -1; // variavel de retorno do shell
		private String shellMessage = ""; // mensagem que aparece para a solicitacao do input
		//endregion


		//region Constructor
		public InOutManager(){
			this.consoleRequestQueue = new ArrayList<>();
			this.start();
			this.setName("InOutThread");
		}
		//endregion


		//region Methods
		@Override
		public void run() {
			while(true){
				if(!consoleRequestQueue.isEmpty() ) {
					this.programBeingExecuted = this.nextBlocked();
					switch (this.programBeingExecuted.reg[8]) {
						case 1:
							vm.m[programBeingExecuted.reg[9]].opc = Opcode.DATA;
							try {
								//DIRECT MEMORY ACCESS
								vm.m[programBeingExecuted.thisGetLogicAddress(programBeingExecuted.reg[9])].p = Trap_IN(TypeIORequest.CONSOLE);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							this.programBeingExecuted.programState = ProgramState.READY;
							vm.schedulerManager.addFirstInQueue(programBeingExecuted);
							vm.cpu.setInterruptionFlag(9); // avisa a CPU que terminou
							this.consoleRequestQueue.remove(programBeingExecuted); // remove da fila
							break;
						case 2:
							Trap_OUT(); //DMA dentro dele
							this.programBeingExecuted.programState = ProgramState.READY;
							vm.cpu.setInterruptionFlag(9); // avisa a CPU que terminou
							this.consoleRequestQueue.remove(programBeingExecuted); // remove da fila
							break;
						default:
							inOutOperationInterruption();
							break;
					}
				}
				else if(shellRequest) {
					try {
						shellNeedInOperation.acquire();
						this.returnShell = Trap_IN(TypeIORequest.SHELL);
						trapInReady.release();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				try {
					sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private ProcessControlBlock nextBlocked() {
			for (ProcessControlBlock pcb : consoleRequestQueue) {
				if(pcb.programState.equals(ProgramState.BLOCKED)){
					return consoleRequestQueue.get(consoleRequestQueue.indexOf(pcb));
				}
			}
			return null;
		}

		public void Trap_OUT() {
			System.out.println(vm.m[this.programBeingExecuted.thisGetLogicAddress(this.programBeingExecuted.reg[9])].p);
		}

		public int Trap_IN(TypeIORequest type) throws InterruptedException {
			int return_;
			if(type.equals(TypeIORequest.CONSOLE)) {
				System.out.println("~E/S Requisicao CONSOLE -> "+vm.memoryManager.programsInMemory.get(
						vm.memoryManager.getIndexOfProgInMemory(
								this.programBeingExecuted.idProg))
						+ " next input = ");
				return_ = teclado.nextInt(); if(teclado.hasNextLine()) teclado.nextLine();
				System.out.println("typed to program");
				return return_;
			}
			else if(type.equals(TypeIORequest.SHELL)){
				System.out.println(this.shellMessage);
				System.out.println("~E/S Requisicao SHELL  -> next input = ");
				return_ = teclado.nextInt(); if(teclado.hasNextLine()) teclado.nextLine();
				shellRequest=false;
				shellMessage = "";
				return return_;
			}
			else{
				inOutOperationInterruption();
				return -1;
			}
		}

		public void addNewRequest(ProcessControlBlock pcb) {
			pcb.programState = ProgramState.BLOCKED;
			this.consoleRequestQueue.add(pcb);

		}

		//endregion

		//region Getter Setter
		public int getReturnShell() throws InterruptedException {
			trapInReady.acquire();
			return returnShell;
		}

		public boolean isShellRequest() {
			return shellRequest;
		}

		public void setShellRequest(boolean shellRequest, String msg) {
			this.shellRequest = shellRequest;
			this.shellMessage = msg;
		}
		//endregion
	}

	public class SchedulerManager extends Thread {

		//region Attributes
		private final ArrayList<ProcessControlBlock> execQueue;
		private final Semaphore schedulerSemaphore = new Semaphore(0);
		//endregion

		//region construtor
		public SchedulerManager(){
			execQueue = new ArrayList<>();
			this.start();
			this.setName("SchedulerThread");
		}
		//endregion

		//region Metodos

		//TODO BACKLOG
		@Override
		public void run() {
			try {
				schedulerSemaphore.acquire(); // TODO BACKLOG ver se da pra remover esse semaforo
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			ProcessControlBlock a;
			while(true){
				if((a = this.hasNNextProgReady())!=null) {
					if(cpuIdle.tryAcquire()) {
						vm.cpu.setContext(a);
						vm.cpu.cpuSemaphore.release();
					}
				}
			}
		}

		private void endOfQueue(ProcessControlBlock program){
			synchronized (execQueue) {
				if (this.execQueue.contains(program)) { // FASE 6 if system is scaloning
					ProcessControlBlock aux_ = this.execQueue.remove(execQueue.indexOf(program));
					this.execQueue.add(aux_);
					this.execQueue.get(this.execQueue.indexOf(aux_)).programState = ProgramState.READY;
				} else { // FASE 6 if the program is new to the line
					this.execQueue.add(program);
					this.execQueue.get(this.execQueue.indexOf(program)).programState = ProgramState.READY;
				}
			}
		}

		private ProcessControlBlock hasNNextProgReady() {
			synchronized (execQueue) {
				for (ProcessControlBlock pcb : execQueue) {
					if (pcb.programState.equals(ProgramState.READY)) {
						return pcb;
					}
				}
			}
			return null;
		}

		public void addToExecutionQueue(ProcessControlBlock pcb) { //FASE 6
			if (pcb.programState.equals(ProgramState.READY)) {
				this.endOfQueue(pcb);
			}
		}

		public void addFirstInQueue(ProcessControlBlock nextReady) {
			synchronized (execQueue) {
				if (this.execQueue.contains(nextReady)) { // FASE 6 if system is scaloning
					ProcessControlBlock aux_ = this.execQueue.remove(execQueue.indexOf(nextReady));
					this.execQueue.add(0, nextReady);
				} else { // FASE 6 if the program is new to the line
					this.execQueue.add(0, nextReady);
				}
				nextReady.programState = ProgramState.READY;
			}
		}

		public void endExecution(ProcessControlBlock pcb) {
			synchronized (execQueue){
				execQueue.remove(pcb);
			}
		}
		//endregion
	}

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
			int dummy;
			for (Frame frame : this.getFrames()) {
				if (frame.getIdProg() == program.getIdProg()){
					for (int j = (frame.getIdFrame() * vm.memoryManager.getframeSize()); j < (((frame.getIdFrame() + 1) * vm.memoryManager.getframeSize()) - 1); j++) {
					    //vm.m[j].interruption = 0; //todo backlog bug frame entrega passada
						dummy=1;
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
		private ProgramState programState;

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
			this.programState = ProgramState.READY;
			this.reg = new int[10];
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
						vm.cpu.interruptionFlag=5;
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
					return ((vm.cpu.pcb.getFramesProg().get(logicFrame))* frameSize)+offset_aux;
				}
				else{
					vm.cpu.interruptionFlag=5;
					return vm.cpu.pcb.pc_;
				}
			}
		}

		public int thisGetLogicAddress(int position){ // used outside the CPU
			int frame_aux =(int)Math.ceil((double) position/ frameSize);
			int offset_aux = position% frameSize;
			int logicFrame = frame_aux-1;
			if (position < frameSize){
				return (this.getFramesProg().get(logicFrame)* frameSize) + offset_aux;
			}
			if (frame_aux<= this.getFramesProg().size()){
				return ((this.getFramesProg().get(logicFrame))* frameSize)+offset_aux;
			}
			else {
				ArrayList<Integer> newFrames = new ArrayList<>();
				if(vm.memoryManager.existsEmptyFrames((frame_aux - this.getFramesProg().size())* this.frameSize,newFrames)) {
					vm.memoryManager.allocateMoreFrames(newFrames, this.getIdProg(), this.getProgram());
					return ((this.getFramesProg().get(logicFrame))* frameSize)+offset_aux;
				}
				else{
					vm.cpu.interruptionFlag=5;
					return this.pc_;
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
				new Word(Opcode.LDI, 9, -1, 50), // setta a posicao onde vai armazenar o valor inputado
            	new Word(Opcode.TRAP,-1,-1,-1),// chama trap para IN
				//new Word(Opcode.STD, 9, -1, 50), // coloca o valor do Registrado9 na posição 20 da memoria
				new Word(Opcode.LDD,1,-1,50), // ler da memoria e colocar no registrador

				new Word(Opcode.LDD,2,-1,50), // ler da memoria e colocar no registrador
				new Word(Opcode.LDI, 0,-1,19), // numero da linha que será pulado no JMP abaixo
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

