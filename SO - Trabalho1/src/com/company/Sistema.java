package com.company;
// PUCRS - Escola Politécnica - Sistemas Operacionais
// Prof. Fernando Dotti
// Código fornecido como parte da solução do projeto de Sistemas Operacionais
//
// Fase 1 - máquina virtual (vide enunciado correspondente)
//

import java.util.ArrayList;
import java.util.Scanner;

//TODO BACKLOG testar tudo com programas teste

public class Sistema {

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

	public class CPU {
		// característica do processador: contexto da CPU ...
		//private int pc;            // ... composto de program counter,
		private Word ir;            // instruction register,
		private int[] reg;        // registradores da CPU
		private Word[] m;   // CPU acessa MEMORIA, guarda referencia 'm' a ela. memoria nao muda. ee sempre a mesma.
		//FASE 4 (START)
		private ProgramCounterAux pca;
		//FASE 4 (FINISH)

		public CPU(Word[] _m) {     // ref a MEMORIA e interrupt handler passada na criacao da CPU
			m = _m;                // usa o atributo 'm' para acessar a memoria.
			reg = new int[10];        // aloca o espaço dos registradores
		}

		//FASE 4 (START)
		public void run(int idProg) {        // execucao da CPU supoe que o contexto da CPU, vide acima, esta devidamente setado
			long sum = 0;
			long sub = 0;
			pca = new ProgramCounterAux(idProg);
			while (true) {         // ciclo de instrucoes. acaba cfe instrucao, veja cada caso.
				// FETCH
				ir = m[pca.pc_];        // busca posicao da memoria apontada por pc, guarda em ir
				// EXECUTA INSTRUCAO NO ir
				sum = 0;
				sub = 0;
				switch (ir.opc) { // para cada opcode, sua execução
					case TRAP:
						switch (reg[8]) {
							case 1: //in
								reg[9] = Trap_IN();
								break;
							case 2: //out
								Trap_OUT(m[pca.get_Endereco_Logico(reg[9])].p);
								break;
							default:
								break;
						}
						pca.nextPC();
						break;

					case JMP: //PC ← k
						//tratamento de erro
						if (m[pca.get_Endereco_Logico(ir.p)].opc == Opcode.___) {
							ir.interruption = 2;
						}
						//execucao
						else {
							pca.JMP_PC(ir.p);
						}
						break;

					case JMPI: //PC ← R1
						//tratamento de erro
						if (Registrador_Valido(ir.r1) || m[pca.get_Endereco_Logico(reg[ir.r1])].opc == Opcode.___) {
							ir.interruption = 2;
						}
						//execucao
						else {
							pca.JMP_PC(reg[ir.r1]);
						}
						break;

					case JMPIG: // If R2 > 0 Then PC ← R1 Else PC ← PC +1
						if (Registrador_Valido(ir.r1) && Registrador_Valido(ir.r2)) {
							if (reg[ir.r2] > 0) {
								//tratamento de erro
								if (m[pca.get_Endereco_Logico(reg[ir.r1])].opc == Opcode.___) {
									ir.interruption = 2;
								}
								//execucao
								else {
									pca.JMP_PC(reg[ir.r1]);
								}
							} else {
								pca.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;

					case JMPIL: //if R2 < 0 then PC ← R1 Else PC ← PC +1
						if (Registrador_Valido(ir.r2)) {
							if (reg[ir.r2] < 0) {
								//tratamento de erro
								if (m[pca.get_Endereco_Logico(reg[ir.r1])].opc == Opcode.___) {
									ir.interruption = 2;
								}
								//execucao
								else {
									pca.JMP_PC(reg[ir.r1]);
								}
							} else {
								pca.nextPC();
							}
						} else {
							ir.interruption = 2;
						}

						break;

					case JMPIE: //if R2 == 0 then PC ← R1 Else PC ← PC +1
						if (Registrador_Valido(ir.r2)) {
							if (reg[ir.r2] == 0) {
								//tratamento de erro
								if (m[pca.get_Endereco_Logico(reg[ir.r1])].opc == Opcode.___) {
									ir.interruption = 2;
								}
								//execucao
								else {
									pca.JMP_PC(reg[ir.r1]);
								}
							} else {
								pca.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;

					case JMPIM: // PC ← [A]
						//tratamento de erro
						if (m[pca.get_Endereco_Logico(ir.p)].opc == Opcode.___) {
							ir.interruption = 2;
						}
						//execucao
						else {
							pca.JMP_PC(m[ir.p].p);
						}
						break;

					case JMPIGM: // if R2 > 0 then PC ← [A] Else PC ← PC +1
						if (Registrador_Valido(ir.r2)) {
							if (reg[ir.r2] > 0) {
								//tratamento de erro
								if (m[pca.get_Endereco_Logico(ir.p)].opc == Opcode.___) {
									ir.interruption = 2;
								}
								//execucao
								else {
									pca.JMP_PC(ir.p);
								}
							} else {
								pca.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;

					case JMPILM: //if R2 < 0 then PC ← [A] Else PC ← PC +1
						if (Registrador_Valido(ir.r2)) {
							if (reg[ir.r2] < 0) {
								//tratamento de erro
								if (m[pca.get_Endereco_Logico(ir.p)].opc == Opcode.___) {
									ir.interruption = 2;
								}
								//execucao
								else {
									pca.JMP_PC(ir.p);
								}
							} else {
								pca.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;

					case JMPIEM: //if R2 == 0 then PC ← [A] Else PC ← PC +1
						if (Registrador_Valido(ir.r2)) {
							if (reg[ir.r2] == 0) {
								//tratamento de erro
								if (m[pca.get_Endereco_Logico(ir.p)].opc == Opcode.___) {
									ir.interruption = 2;
								}
								//execucao
								else {
									pca.JMP_PC(ir.p);
								}
							} else {
								pca.nextPC();
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
						if (Registrador_Valido(ir.r1)) {
							sum = reg[ir.r1] + ir.p;
							if (sum > Integer.MAX_VALUE) {
								ir.interruption = 1;
							}
							//execucao
							else {
								reg[ir.r1] = reg[ir.r1] + ir.p;
								pca.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;

					case SUBI: // R1 ← R1 – k
						//tratamento erro
						if (Registrador_Valido(ir.r1)) {
							sub = reg[ir.r1] - ir.p;
							if (sub < Integer.MIN_VALUE) {
								ir.interruption = 1;
							}
							//execucao
							else {
								reg[ir.r1] = reg[ir.r1] - ir.p;
								pca.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;

					case ADD: // R1 ← R1 + R2
						//tratamento erro
						if (Registrador_Valido(ir.r1) && Registrador_Valido(ir.r2)) {
							sum = reg[ir.r1] + reg[ir.r2];
							if (sum > Integer.MAX_VALUE) {
								ir.interruption = 1;
							}
							//execucao
							else {
								reg[ir.r1] = reg[ir.r1] + reg[ir.r2];
								pca.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;

					case SUB: // R1 ← R1 - R2
						//tratamento erro
						if (Registrador_Valido(ir.r1) && Registrador_Valido(ir.r2)) {
							sub = reg[ir.r1] - reg[ir.r2];
							if (sub < Integer.MIN_VALUE) {
								ir.interruption = 1;
							}
							//execucao
							else {
								reg[ir.r1] = reg[ir.r1] - reg[ir.r2];
								pca.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;

					case MULT: // R1 ← R1 * R2
						//tratamento erro
						if (Registrador_Valido(ir.r1) && Registrador_Valido(ir.r2)) {
							sum = (long) reg[ir.r1] * reg[ir.r2];
							if (sum < Integer.MIN_VALUE || sum > Integer.MAX_VALUE) {
								ir.interruption = 1;
							}
							//execucao
							else {
								reg[ir.r1] = reg[ir.r1] * reg[ir.r2];
								pca.nextPC();
							}
						} else {
							ir.interruption = 2;
						}
						break;


					case LDI: // R1 ← k
						if (Registrador_Valido(ir.r1)) {
							reg[ir.r1] = ir.p;
							pca.nextPC();
						} else {
							ir.interruption = 2;
						}
						break;

					case LDD: // R1 ← [A]
						//validacao
						if (Registrador_Valido(ir.r1)) {
							reg[ir.r1] = m[pca.get_Endereco_Logico(ir.p)].p;
							pca.nextPC();
						} else {
							ir.interruption = 2;
						}
						break;

					case STD: // [A] ← R1
						if (Registrador_Valido(ir.r1)) {
							m[pca.get_Endereco_Logico(ir.p)].opc = Opcode.DATA;
							m[pca.get_Endereco_Logico(ir.p)].p = reg[ir.r1];
							pca.nextPC();
						} else {
							ir.interruption = 2;
						}
						break;

					case LDX: // R1 ← [R2]
						if (Registrador_Valido(ir.r1) && Registrador_Valido(ir.r2)) {
							reg[ir.r1] = m[pca.get_Endereco_Logico(reg[ir.r2])].p;
							pca.nextPC();
						} else {
							ir.interruption = 2;
						}
						break;

					case STX: // [R1] ← R2
						if (Registrador_Valido(ir.r1) && Registrador_Valido(ir.r2)) {
							m[pca.get_Endereco_Logico(reg[ir.r1])].opc = Opcode.DATA;
							m[pca.get_Endereco_Logico(reg[ir.r1])].p = reg[ir.r2];
							pca.nextPC();
						} else {
							ir.interruption = 2;
						}
						break;

					case SWAP: //T ← Ra Ra ← Rb Rb ←T
						if (Registrador_Valido(ir.r1) && Registrador_Valido(ir.r2)) {
							int t = reg[ir.r1];
							reg[ir.r1] = reg[ir.r2];
							reg[ir.r2] = t;
							pca.nextPC();
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
						Tratamento_Overflow(ir);
						break;
					} else if (ir.interruption == 2) //acessou um endereço invalido de memoria (ArrayOutOfBound)
					{
						Tratamento_Endereco_Invalido(ir);
						break;
					} else if (ir.interruption == 3) //Intrucao Invalida
					{
						Tratamento_Opcode_Invalido(ir);
						break;
					} else if (ir.interruption == 4) //opcode STOP em sí
					{
						Tratamento_STOP(ir);
						break;
					}
				}

			}
		}
		//FASE 4 (FINISH)
	}

	// ------------------ C P U - fim ------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------


	// ------------------- V M  - constituida de CPU e MEMORIA -----------------------------------------------
	// -------------------------- atributos e construcao da VM -----------------------------------------------
	public class VM {
		public int tamMem;
		public Word[] m;
		public CPU cpu;
		// FASE 4 (START)
		public ControleMemoria controleMemoria;

		// FASE 4 (FINISH)


		public VM() {   // vm deve ser configurada com endereço de tratamento de interrupcoes
			// memória
			tamMem = 1024;
			// FASE 4 (START)
			int tamFrame = 16;
			controleMemoria = new ControleMemoria(tamMem, tamFrame);
			// FASE 4 (FINISH)
			m = new Word[tamMem]; // m ee a memoria
			for (int i = 0; i < tamMem; i++) { m[i] = new Word(Opcode.___, -1, -1, -1); }

			// cpu
			cpu = new CPU(m);
		}
	}

	// ------------------- V M  - fim ------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------

	// --------------------H A R D W A R E - fim -------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------

	// -------------------------------------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------
	// ------------------- S O F T W A R E - inicio ----------------------------------------------------------

	// region Tratamento
	public void Tratamento_Overflow(Word w) {
		OutputTratamento(w);
		System.out.println("Houve Overflow na operação aritmética");
	}

	public void Tratamento_Opcode_Invalido(Word w) {
		OutputTratamento(w);
		System.out.println("Operação " + "[ " + w.opc + " ]" + " nao identificada.");
	}

	public void Tratamento_Endereco_Invalido(Word w) {
		OutputTratamento(w);
		System.out.println("O endereço de memória ou registrador referenciado não existe.");
	}

	public void Tratamento_STOP(Word w) {
		System.out.println("---------------------------------- fim do programa ");
	}

	private void OutputTratamento(Word w) {
		System.out.print("**Erro na intrução: [ ");
		System.out.print(w.opc);
		System.out.print(", ");
		System.out.print(w.r1);
		System.out.print(", ");
		System.out.print(w.r2);
		System.out.print(", ");
		System.out.print(w.p);
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


	public boolean Registrador_Valido(int reg_n) {
		return (reg_n >= 0 && reg_n <= 9);
	}






	// -------------------------------------------------------------------------------------------------------
	// -------------------  S I S T E M A --------------------------------------------------------------------

	public VM vm;

	public Sistema(){   // a VM com tratamento de interrupções
		vm = new VM();
	}

	// -------------------  S I S T E M A - fim --------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------


	// -------------------------------------------------------------------------------------------------------
	// ------------------- instancia e testa sistema
	public static Scanner teclado = new Scanner(System.in);

	public static void main(String[] args){
		Sistema s = new Sistema();
		Menu_Opcoes(s);
	}

	//FASE 4 (START)
	public static void Menu_Opcoes(Sistema s){
		int menu_opcoes = -1;
		do{
			System.out.println("--------- MENU DE OPCOES S.O. ---------");
			System.out.println(" [1] - Rodar programa existente em memoria");
			System.out.println(" [2] - Adicionar programa a memoria");
			System.out.println(" [3] - Remover programa da memória");
			System.out.println(" [0] - Sair");
			menu_opcoes = teclado.nextInt();

			switch(menu_opcoes){
				case 0:
					System.out.println("---------------------------------- fim sistema ");
					break;
				case 1:
					Menu_Roda_Prog(s);
					break;
				case 2:
					Menu_Carrega_Prog(s);
					break;
				case 3:
					Menu_Remove_Prog(s);
					break;
				default:
					System.out.println(" Opcao invalida, tente novamente.");
					break;
			}

		}while (menu_opcoes >0);
	}

	public static void Menu_Roda_Prog(Sistema s){
		if (!s.vm.controleMemoria.ProcessControlBlock.isEmpty()){
			Progs progs = Progs.___;
			boolean excecao = false;
			System.out.println("------- Programas disponiveis em memória");
			for (ProcessControlBlock p : s.vm.controleMemoria.ProcessControlBlock) {
				System.out.println(p.toString() + " Frames:" + s.vm.controleMemoria.getFramesProg(p.idProg).toString());
			}
			System.out.println("-------");
			System.out.println("Digite o numero do programa e o nome dele em seguida");
			System.out.println("Numero: ");
			int idProg = teclado.nextInt();
			teclado.nextLine();
			System.out.println("-------");
			if (s.vm.controleMemoria.Existe_Programa(idProg)){
				s.vm.cpu.run(idProg);
			}
			else
			{
				if(!excecao) {
					System.out.println("Este programa nao existe.");
				}
			}
		}
		else
		{
			System.out.println("Não há programas em memória.");
		}
	}

	public static void Menu_Carrega_Prog(Sistema s) {
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
				s.test1();
				break;
			case 2:
				s.test2();
				break;
			case 3:
				s.test_p3();
				break;
			case 4:
				//TODO BACKLOG adicionar o BubbleSort aqui
				s.test_bubble_sort();
				break;
			default:
				System.out.println(" Opcao invalida, tente novamente.");
				break;
		}
	}

	public static void Menu_Remove_Prog(Sistema s){
		Progs progs = Progs.___;
		System.out.println("------- Programas disponiveis em memória");
		for (ProcessControlBlock p : s.vm.controleMemoria.ProcessControlBlock) {
			System.out.println(p.toString() + " Frames:" + s.vm.controleMemoria.getFramesProg(p.idProg).toString());
		}
		System.out.println("-------");
		System.out.println("Digite o numero do programa e o nome dele em seguida");
		System.out.println("Numero: ");
		int idProg = teclado.nextInt();
		teclado.nextLine();
		System.out.println("-------");
		if (s.vm.controleMemoria.Existe_Programa(idProg)){
			System.out.println("Programa [ " + idProg+ " ] [ " + s.vm.controleMemoria.ProcessControlBlock.get(s.vm.controleMemoria.getProgEmMemoria(idProg)).programa +  " ] foi excluído com sucesso.");
			s.vm.controleMemoria.Desaloca_Frames(idProg);
		}
	}
	// FASE 4 (FINISH)



	// -------------------------------------------------------------------------------------------------------
	// --------------- TUDO ABAIXO DE MAIN É AUXILIAR PARA FUNCIONAMENTO DO SISTEMA - nao faz parte

	// -------------------------------------------- teste do sistema ,  veja classe de programas
	public void test1(){
		Aux aux = new Aux();
		Word[] p = new Programas().progMinimo;
		aux.carga_atualizada(p,Progs.PROG_MINIMO, vm);
	}

	public void test2(){
		Aux aux = new Aux();
		Word[] p = new Programas().fibonacci10;
		aux.carga_atualizada(p,Progs.FIBBONACI10, vm);
	}

	// teste p3

	public void test_p3(){
		Aux aux = new Aux();
		Word[] p = new Programas().p3;
		aux.carga_atualizada(p,Progs.FATORIAL, vm);
	}

	public void test_bubble_sort(){
		Aux aux = new Aux();
		Word[] p = new Programas().p3;
		aux.carga_atualizada(p,Progs.BUBBLE_SORT, vm);
	}


	// -------------------------------------------  classes e funcoes auxiliares
	
	public class Aux {
		public void dump(Word w) {
			System.out.print("[ ");
			System.out.print(w.opc);
			System.out.print(", ");
			System.out.print(w.r1);
			System.out.print(", ");
			System.out.print(w.r2);
			System.out.print(", ");
			System.out.print(w.p);
			System.out.println("  ] ");
		}

		public void dump(Word[] m, int ini, int fim) {
			for (int i = ini; i < fim; i++) {
				System.out.print(i);
				System.out.print(":  ");
				dump(m[i]);
			}
		}

		public void carga(Word[] p, Word[] m) {
			for (int i = 0; i < p.length; i++) {
				m[i].opc = p[i].opc;
				m[i].r1 = p[i].r1;
				m[i].r2 = p[i].r2;
				m[i].p = p[i].p;
			}
		}

		// FASE 4 (START)
		public void dump_atualizado(VM vm, Progs nomePrograma, int idProg) {
			ArrayList<Integer> frames_reservados = new ArrayList<>();
			frames_reservados = vm.controleMemoria.getFramesProg(idProg);
			for (int i = 0; i < frames_reservados.size(); i++) {
				System.out.println("---- Frame [" + frames_reservados.get(i) + " ]");
				dump(vm.m, (frames_reservados.get(i) * vm.controleMemoria.tamanho_frame), (((frames_reservados.get(i) + 1) * vm.controleMemoria.tamanho_frame) - 1));
			}
		}

		public void carga_atualizada(Word[] p, Progs nomePrograma, VM vm) {
			ArrayList<Integer> frames_reservados = new ArrayList<>();
			if (vm.controleMemoria.Existe_Frames_Livres(p.length, frames_reservados)) {
				vm.controleMemoria.Aloca_Frames(frames_reservados, p, nomePrograma);
				System.out.println("Programa carregado com sucesso.");
				System.out.print("Frames Reservados ");
				for (int i : frames_reservados) {
					System.out.print("- [ " + i + " ]");
				}
				System.out.println();
			}
		}
	}

	// region Controle Memoria
	public class ControleMemoria {

		//atributos
		public Frame[] frames;
		public int tamanho_frame;
		public ArrayList<ProcessControlBlock> ProcessControlBlock;

		// construtor
		public ControleMemoria(int tamanho_memoria, int tamanho_frame) {
			frames = new Frame[(tamanho_memoria / tamanho_frame)];
			ProcessControlBlock = new ArrayList<>();
			for (int i = 0; i < tamanho_memoria/tamanho_frame; i++) {
				frames[i] = new Frame(i,true,Progs.___,0);
			}
			this.tamanho_frame = tamanho_frame;
		}

		// region Metodos
		public ProcessControlBlock getPCB(int idProg){
			for (ProcessControlBlock pcb : ProcessControlBlock) {
				if(pcb.idProg==idProg){
					return pcb;
				}
			}
			return null;
		}

		public void addFrames(ArrayList<Integer> frames_prog,int idProg){
			this.ProcessControlBlock.get(this.ProcessControlBlock.indexOf(getPCB(idProg))).frames_prog.addAll(frames_prog);
		}

		public int getProgEmMemoria (int idProg){
			for (ProcessControlBlock progs : ProcessControlBlock) {
				if (progs.idProg==idProg){
					return ProcessControlBlock.indexOf(progs);
				}
			}
			return -1;
		}

		public boolean Existe_Programa(int idProg){
			for (ProcessControlBlock prog : ProcessControlBlock) {
				if (prog.idProg==idProg){
					return true;
				}
			}
			return false;
		}

		public int getNextId() {
			int maior=0;
			for (ProcessControlBlock prog : ProcessControlBlock) {
				if(prog.idProg>maior){
					maior = prog.idProg;
				}
			}
			return maior+1;
		}

		public ArrayList<Integer>  getFramesProg(int idProg) {
			for (ProcessControlBlock prog : ProcessControlBlock) {
				if(prog.idProg==idProg){
					return prog.frames_prog;
				}
			}
			throw(new IndexOutOfBoundsException());
		}

		public int getContextoPrograma(Progs p, int i) {
			return 0;
		}


		public boolean Existe_Frames_Livres(double tamProg, ArrayList<Integer> frames_reservados) {
			int qnt_frames_necessarios = (int)Math.ceil(tamProg / vm.controleMemoria.tamanho_frame);
			int cont = 0;
			for (int i = 0; i < vm.controleMemoria.frames.length; i++) {
				if (vm.controleMemoria.frames[i].isFrame_livre()) {
					frames_reservados.add(i);
					cont++;
					if (cont == qnt_frames_necessarios) {
						return true;
					}
				}
			}
			return false;
		}

		public void Aloca_Frames(ArrayList<Integer> frames_reservados, Word[] p, Progs nomePrograma){
			int cont=0;
			int max_id = getNextId();
			for (Integer frames_reservado : frames_reservados) {
				for (int j = (frames_reservado * vm.controleMemoria.tamanho_frame); j <= (((frames_reservado + 1) * vm.controleMemoria.tamanho_frame) - 1) && cont < p.length ; j++) {
					vm.m[j].opc = p[cont].opc;
					vm.m[j].r1 = p[cont].r1;
					vm.m[j].r2 = p[cont].r2;
					vm.m[j].p = p[cont].p;
					cont++;
				}
				vm.controleMemoria.frames[frames_reservado].setPrograma(nomePrograma, max_id);
			}
			vm.controleMemoria.ProcessControlBlock.add(new ProcessControlBlock(nomePrograma,max_id,frames_reservados));
		}

		public void Aloca_Mais_Frames(ArrayList<Integer> frames_reservados, int idProg, Progs nomePrograma){
			int cont=0;
			for (Integer frames_reservado : frames_reservados) {
				vm.controleMemoria.frames[frames_reservado].setPrograma(nomePrograma, idProg);
			}
			vm.controleMemoria.addFrames(frames_reservados,idProg);
		}

		public void Desaloca_Frames(int idProg){
			for (Frame frame : frames) {
				if (frame.idProg == idProg){
					ResetFrames(frame);
				}
			}
			int aux_ = getProgEmMemoria(idProg);
			if (aux_>=0) {
				ProcessControlBlock.remove(aux_);
			}
		}

		private void ResetFrames(Frame frame) {
			for (int j = (frame.id_frame * vm.controleMemoria.tamanho_frame); j < (((frame.id_frame + 1) * vm.controleMemoria.tamanho_frame) - 1); j++) {
				vm.m[j].opc = Opcode.___;
				vm.m[j].r1 = -1;
				vm.m[j].r2 = -1;
				vm.m[j].p = -1;
			}
			frame.idProg = 0;
			frame.nomeProg = Progs.___;
			frame.frame_livre=true;
		}

		// endregion

		// region SubClasses: ProcessControlBlock, Frame
		public class Frame {
			public int id_frame;
			public boolean frame_livre;
			public Progs nomeProg;
			public int idProg;

			//contrutor
			public Frame(int id_frame, boolean frame_livre, Progs nomeProg, int idProg) {
				this.id_frame = id_frame;
				this.frame_livre = frame_livre;
				this.nomeProg = nomeProg;
				this.idProg = idProg;
			}

			//region Metodos
			public boolean isFrame_livre() {
				return frame_livre;
			}

			public void setFrame_livre(boolean frame_livre) {
				this.frame_livre = frame_livre;
			}

			public void setPrograma (Progs nomeProg){
				this.nomeProg = nomeProg;
				this.idProg = getNextId();
			}
			public void setPrograma (Progs nomeProg, int idProg){
				this.nomeProg = nomeProg;
				this.idProg = idProg;
				this.frame_livre = false;
			}
			// endregion
		}
		//endregion

	}

	public class ProcessControlBlock {
		public Progs programa;
		public int idProg;
		public ArrayList<Integer> frames_prog;

		public ProcessControlBlock(Progs programa, int idProg, ArrayList<Integer> frames_prog){
			this.programa = programa;
			this.idProg = idProg;
			this.frames_prog = new ArrayList<>();
			this.frames_prog.addAll(frames_prog);
		}

		public int getPrimeiroPC(){
			return frames_prog.get(0)*vm.controleMemoria.tamanho_frame;
		}

		@Override
		public String toString() {
			return " [ " + idProg + " ] " +	"Programa: " + programa;
		}

	}
	// endregion

	//region Subclasse: ProgramCounterAux
	public class ProgramCounterAux{

		//region Atributos
		private final int tamanho_frame;

		private int idProg;
		private ArrayList<Integer> frames;
		private int pc_;

		private int contFrameAtual;
		private int index_frames;
		//endregion

		//region Construtor
		public ProgramCounterAux(int idProg){
			this.frames = vm.controleMemoria.getFramesProg(idProg);
			this.idProg = idProg;
			this.tamanho_frame = vm.controleMemoria.tamanho_frame;
			this.contFrameAtual = 0;
			this.index_frames = 0;
			this.pc_ = frames.get(0)*tamanho_frame;
		}
		//endregion

		//region Metodos
		public void nextPC(){
			if((index_frames+1)==16){
				contFrameAtual++;
				index_frames=0;
			}
			else {
				index_frames++;
			}
			pc_ = (frames.get(contFrameAtual)*tamanho_frame)+index_frames;
		}

		public void JMP_PC(int pc){
			if (pc<tamanho_frame){
				this.index_frames=pc;
				this.contFrameAtual = 0;
				this.pc_ = (frames.get(contFrameAtual) * tamanho_frame) + index_frames;
			}
			else {
				this.index_frames=pc%tamanho_frame;
				if (Math.ceil((double) pc / tamanho_frame) > frames.size()) {
					ArrayList<Integer> mais_frames = new ArrayList<>();
					if (vm.controleMemoria.Existe_Frames_Livres(16.0, mais_frames)) {
						vm.controleMemoria.Aloca_Mais_Frames(mais_frames, this.idProg, vm.controleMemoria.ProcessControlBlock.get(vm.controleMemoria.getProgEmMemoria(idProg)).programa);
						this.frames.addAll(mais_frames);
					} else {
						System.out.println("Nao há mais memoria disponivel");
						//fixme interrupcao
					}
				}
				this.contFrameAtual = (int)Math.floor((double) pc / tamanho_frame);
				this.pc_ = (frames.get(contFrameAtual) * tamanho_frame) + index_frames;
			}
		}

		public int get_Endereco_Logico(int posicao){
			int frame_aux =(int)Math.ceil((double) posicao/tamanho_frame);
			int index_aux = posicao%tamanho_frame;
			int frame_certo = frame_aux-1;
			if (posicao < tamanho_frame){
				return (frames.get(frame_certo)*tamanho_frame) + index_aux;
			}
			if (frame_aux<=vm.cpu.pca.frames.size()){

				return ((vm.cpu.pca.frames.get(frame_aux-1))*tamanho_frame)+index_aux;
			}
			else {
				ArrayList<Integer> framesNovos = new ArrayList<>();
				if(vm.controleMemoria.Existe_Frames_Livres((frame_aux-vm.cpu.pca.frames.size())*tamanho_frame,framesNovos)) {
					vm.controleMemoria.Aloca_Mais_Frames(framesNovos, this.idProg, vm.controleMemoria.ProcessControlBlock.get(vm.controleMemoria.getProgEmMemoria(this.idProg)).programa );
					return ((1+vm.cpu.pca.frames.get(frame_aux-1))*tamanho_frame)+index_aux;
				}
				else{
					System.out.println("Nao há mais memoria livre."); // FIXME interurpcao
					return -1;
				}
			}

		}

		//region Getter Setters

		public int getTamanho_frame() {
			return tamanho_frame;
		}

		public int getIdProg() {
			return idProg;
		}

		public void setIdProg(int idProg) {
			this.idProg = idProg;
		}

		public ArrayList<Integer> getFrames() {
			return frames;
		}

		public void setFrames(ArrayList<Integer> frames) {
			this.frames = frames;
		}

		public int getPc_() {
			return pc_;
		}

		public void setPc_(int pc_) {
			this.pc_ = pc_;
		}

		public int getContFrameAtual() {
			return contFrameAtual;
		}

		public void setContFrameAtual(int contFrameAtual) {
			this.contFrameAtual = contFrameAtual;
		}

		public int getIndex_frames() {
			return index_frames;
		}

		public void setIndex_frames(int index_frames) {
			this.index_frames = index_frames;
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
				new Word(Opcode.STD, 9, -1, 80), // coloca o valor do Registrado9 na posição 20 da memoria
				new Word(Opcode.LDD,1,-1,80), // ler da memoria e colocar no registrador
				new Word(Opcode.LDD,2,-1,80), // ler da memoria e colocar no registrador
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
				new Word(Opcode.JMP,-1,-1,40),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
  				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
				new Word(Opcode.ADD,-1,-1,-1),
/*40*/			new Word(Opcode.STD,1,-1,80), // acaba o loop, joga o valor de r1 (resultado do fatorial) na posicao 20 da memoria
				new Word(Opcode.LDI,8,-1,2), // setta o registrador 8 para o valor de OUT
				new Word(Opcode.LDI, 9, -1,80), // poe no registrador 9 a posicao de memoria que vai ser acessada no TRAP
				new Word(Opcode.TRAP, -1,-1,-1), // TRAP OUT
				new Word(Opcode.STOP,-1,-1,-1), // acaba o programa
				new Word(Opcode.LDI,1,-1,-1), // (se no primeiro jmp, o input for -1, vem pra cá) joga o valor de -1 no registrador 1
				new Word(Opcode.STD,1,-1,80), // armazena no valor de r1 na posicao 20 da memoria
				new Word(Opcode.LDI,8,-1,2), // setta o registrador 8 para o valor de OUT
				new Word(Opcode.LDI, 9, -1,80), // poe no registrador 9 a posicao de memoria que vai ser acessada no TRAP
				new Word(Opcode.TRAP, -1,-1,-1), // TRAP OUT
				new Word(Opcode.STOP,-1,-1,-1)	// acaba o programa
		};
		//public Word[] BubbleSort = new Word{

		// le a quantidade de elementos
		// armazena os elementos na memoria (ex: 50,51,52,53,54)
		//

		//}
	}
}


