package com.company;// PUCRS - Escola Politécnica - Sistemas Operacionais
// Prof. Fernando Dotti
// Código fornecido como parte da solução do projeto de Sistemas Operacionais
//
// Fase 1 - máquina virtual (vide enunciado correspondente)
//

import java.util.Scanner;

//TODO BACKLOG testar tudo com programas teste

public class Sistema {

	// -------------------------------------------------------------------------------------------------------
	// --------------------- H A R D W A R E - definicoes de HW ---------------------------------------------- 

	// -------------------------------------------------------------------------------------------------------
	// --------------------- M E M O R I A -  definicoes de opcode e palavra de memoria ---------------------- 

	public class Word { 	// cada posicao da memoria tem uma instrucao (ou um dado)
		public Opcode opc; 	//
		public int r1; 		// indice do primeiro registrador da operacao (Rs ou Rd cfe opcode na tabela)
		public int r2; 		// indice do segundo registrador da operacao (Rc ou Rs cfe operacao)
		public int p; 		// parametro para instrucao (k ou A cfe operacao), ou o dado, se opcode = DADO
		public int interruption; // controle de interrupcao da instrucao

		public Word(Opcode _opc, int _r1, int _r2, int _p) {
			opc = _opc;   r1 = _r1;    r2 = _r2;	p = _p; interruption = 0;
		}
	}
    // -------------------------------------------------------------------------------------------------------

	// -------------------------------------------------------------------------------------------------------
    // --------------------- C P U  -  definicoes da CPU -----------------------------------------------------
	// -------------------------------------------------------------------------------------------------------
	public enum Opcode {
		DATA, ___,		    // se memoria nesta posicao tem um dado, usa DATA, se nao usada ee NULO ___
		JMP, JMPI, JMPIG, JMPIL, JMPIE,  JMPIM, JMPIGM, JMPILM, JMPIEM, STOP,   // desvios e parada
		ADDI, SUBI,  ADD, SUB, MULT,          // matematicos
		LDI, LDD, STD,LDX, STX, SWAP,        // movimentacao
		TRAP; 						 		 // added opcode

	}

	public class CPU {
							// característica do processador: contexto da CPU ...
		private int pc; 			// ... composto de program counter,
		private Word ir; 			// instruction register,
		private int[] reg;       	// registradores da CPU

		private Word[] m;   // CPU acessa MEMORIA, guarda referencia 'm' a ela. memoria nao muda. ee sempre a mesma.

		public CPU(Word[] _m) {     // ref a MEMORIA e interrupt handler passada na criacao da CPU
			m = _m; 				// usa o atributo 'm' para acessar a memoria.
			reg = new int[10]; 		// aloca o espaço dos registradores
		}

		public void setContext(int _pc) {  // no futuro esta funcao vai ter que ser 
			pc = _pc;                                              // limite e pc (deve ser zero nesta versao)
		}

		public void run() { 		// execucao da CPU supoe que o contexto da CPU, vide acima, esta devidamente setado
			while (true) { 			// ciclo de instrucoes. acaba cfe instrucao, veja cada caso.
				// FETCH
					ir = m[pc]; 	// busca posicao da memoria apontada por pc, guarda em ir
				// EXECUTA INSTRUCAO NO ir

				//TODO DONE validar se os registradores usados em casa operação existe (fazer metodo pra isso)
				long sum = 0;
				long sub = 0;
					switch (ir.opc) { // para cada opcode, sua execução

						case TRAP:
							if (reg[8] == 1) //in
							{
								Scanner teclado = new Scanner(System.in);
								reg[9] = teclado.nextInt();
							}
							if (reg[8] == 2) //out
							{
								System.out.println(m[reg[9]].p);
							}
							pc++;
							break;

						case JMP: //PC ← k
							//tratamento de erro
							if(m[ir.p].opc == Opcode.___)
							{
								ir.interruption = 2;
							}
							//execucao
							else
							{
								pc = ir.p;
							}
							break;

						case JMPI: //PC ← R1
							//tratamento de erro
							if (Registrador_Valido(ir.r1) || m[reg[ir.r1]].opc == Opcode.___)
							{
								ir.interruption = 2;
							}
							//execucao
							else
							{
								pc = reg[ir.r1];
							}
							break;

						case JMPIG: // If R2 > 0 Then PC ← R1 Else PC ← PC +1
							if (Registrador_Valido(ir.r1) && Registrador_Valido(ir.r2))
							{
								if (reg[ir.r2] > 0)
								{
									//tratamento de erro
									if (m[reg[ir.r1]].opc == Opcode.___)
									{
										ir.interruption = 2;
									}
									//execucao
									else
									{
										pc = reg[ir.r1];
									}
								}
								else {
									pc++;
								}
							}
							else
							{
								ir.interruption = 2;
							}
							break;

						case JMPIL: //if R2 < 0 then PC ← R1 Else PC ← PC +1
							if (Registrador_Valido(ir.r2))
							{
								if (reg[ir.r2] < 0)
								{
									//tratamento de erro
									if(m[reg[ir.r1]].opc == Opcode.___)
									{
										ir.interruption = 2;
									}
									//execucao
									else
									{
										pc = reg[ir.r1];
									}
								}
								else
								{
									pc++;
								}
							}
							else
							{
								ir.interruption = 2;
							}

							break;

						case JMPIE: //if R2 == 0 then PC ← R1 Else PC ← PC +1
							if ( Registrador_Valido(ir.r2)) {
								if (reg[ir.r2] == 0) {
									//tratamento de erro
									if (m[reg[ir.r1]].opc == Opcode.___) {
										ir.interruption = 2;
									}
									//execucao
									else {
										pc = reg[ir.r1];
									}
								} else {
									pc++;
								}
							}
							else{
								ir.interruption = 2;
							}
							break;

						case JMPIM: // PC ← [A]
							//tratamento de erro
							if(m[ir.p].opc == Opcode.___)
							{
								ir.interruption = 2;
							}
							//execucao
							else
							{
								pc = m[ir.p].p;
							}
							break;

						case JMPIGM: // if R2 > 0 then PC ← [A] Else PC ← PC +1
							if (Registrador_Valido(ir.r2)) {
								if (reg[ir.r2] > 0) {
									//tratamento de erro
									if (m[ir.p].opc == Opcode.___) {
										ir.interruption = 2;
									}
									//execucao
									else {
										pc = ir.p;
									}
								} else {
									pc++;
								}
							}
							else
							{
								ir.interruption = 2;
							}
							break;

						case JMPILM: //if R2 < 0 then PC ← [A] Else PC ← PC +1
							if (Registrador_Valido(ir.r2)) {
								if (reg[ir.r2] < 0) {
									//tratamento de erro
									if (m[ir.p].opc == Opcode.___) {
										ir.interruption = 2;
									}
									//execucao
									else {
										pc = ir.p;
									}
								} else {
									pc++;
								}
							}
							else
							{
								ir.interruption = 2;
							}
							break;

						case JMPIEM: //if R2 == 0 then PC ← [A] Else PC ← PC +1
							if (Registrador_Valido(ir.r2)) {
								if (reg[ir.r2] == 0) {
									//tratamento de erro
									if (m[ir.p].opc == Opcode.___) {
										ir.interruption = 2;
									}
									//execucao
									else {
										pc = ir.p;
									}
								} else {
									pc++;
								}
							}
							else
							{
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
									pc++;
								}
							}
							else
							{
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
									pc++;
								}
							}
							else
							{
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
									pc++;
								}
							}
							else
							{
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
									pc++;
								}
							}
							else
							{
								ir.interruption =2;
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
									pc++;
								}
							}
							else
							{
								ir.interruption = 2;
							}
							break;


						case LDI: // R1 ← k
							if (Registrador_Valido(ir.r1)) {
								reg[ir.r1] = ir.p;
								pc++;
							}
							else
							{
								ir.interruption = 2;
							}
							break;

						case LDD: // R1 ← [A]
							//validacao
							if (Registrador_Valido(ir.r1)) {
								reg[ir.r1] = m[ir.p].p;
								pc++;
							}
							else
							{
								ir.interruption = 2;
							}
							break;

						case STD: // [A] ← R1
							if (Registrador_Valido(ir.r1)) {
								m[ir.p].opc = Opcode.DATA;
								m[ir.p].p = reg[ir.r1];
								pc++;
							}
							else
							{
								ir.interruption = 2;
							}
							break;

						case LDX: // R1 ← [R2]
							if (Registrador_Valido(ir.r1) && Registrador_Valido(ir.r2)) {
								reg[ir.r1] = m[reg[ir.r2]].p;
								pc++;
							}
							else
							{
								ir.interruption = 2;
							}
							break;

						case STX: // [R1] ← R2
							if (Registrador_Valido(ir.r1) && Registrador_Valido(ir.r2)) {
								m[reg[ir.r1]].opc = Opcode.DATA;
								m[reg[ir.r1]].p = reg[ir.r2];
								pc++;
							}
							else
							{
								ir.interruption = 2;
							}
							break;

						case SWAP: //T ← Ra Ra ← Rb Rb ←T
							if (Registrador_Valido(ir.r1) && Registrador_Valido(ir.r2)) {
								int t = reg[ir.r1];
								reg[ir.r1] = reg[ir.r2];
								reg[ir.r2] = t;
								pc++;
							}
							else
							{
								ir.interruption = 2;
							}

							break;

						default:
							ir.interruption = 3;
							break;
					}


				if(ir.interruption != 0)
				{
					if (ir.interruption == 1) //Overflow em uma operacao aritmetica
					{
						Tratamento_Overflow(ir);
						break;
					}
					else if (ir.interruption == 2) //acessou um endereço invalido de memoria (ArrayOutOfBound)
					{
						Tratamento_Endereco_Invalido(ir);
						break;
					}
					else if (ir.interruption == 3) //Intrucao Invalida
					{
						Tratamento_Opcode_Invalido(ir);
						break;
					}
					else if (ir.interruption == 4) //opcode STOP em sí
					{
						Tratamento_STOP(ir);
						break;
					}

				}

			}
		}
	}

    // ------------------ C P U - fim ------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------


    // ------------------- V M  - constituida de CPU e MEMORIA -----------------------------------------------
    // -------------------------- atributos e construcao da VM -----------------------------------------------
	public class VM {
		public int tamMem;
        public Word[] m;
        public CPU cpu;

        public VM(){   // vm deve ser configurada com endereço de tratamento de interrupcoes
	     // memória
  		 	 tamMem = 1024;
			 m = new Word[tamMem]; // m ee a memoria
			 for (int i=0; i<tamMem; i++) { m[i] = new Word(Opcode.___,-1,-1,-1); };
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


	public void Tratamento_Overflow(Word w)
	{
		System.out.print("**Erro na intrução: "); System.out.print("[ ");
		System.out.println("[ ");
		System.out.print(w.opc); System.out.print(", ");
		System.out.print(w.r1);  System.out.print(", ");
		System.out.print(w.r2);  System.out.print(", ");
		System.out.print(w.p);  System.out.println("  ] ");

		System.out.println("Houve Overflow na operação aritmética");
	}
	public void Tratamento_Opcode_Invalido(Word w)
	{
		System.out.print("**Erro na intrução: "); System.out.print("[ ");
		System.out.print(w.opc); System.out.print(", ");
		System.out.print(w.r1);  System.out.print(", ");
		System.out.print(w.r2);  System.out.print(", ");
		System.out.print(w.p);  System.out.println("  ] ");

		System.out.println("Operação " + "[ " + w.opc + " ]" + " nao identificada.");
	}
	public void Tratamento_Endereco_Invalido(Word w)
	{
		System.out.print("**Erro na intrução: [ ");
		System.out.print(w.opc); System.out.print(", ");
		System.out.print(w.r1);  System.out.print(", ");
		System.out.print(w.r2);  System.out.print(", ");
		System.out.print(w.p);  System.out.println("  ] ");

		System.out.println("O endereço de memória ou registrador referenciado não existe.");
	}
	public void Tratamento_STOP(Word w)
	{
		System.out.println("---------------------------------- fim do programa ");
	}


	public boolean Registrador_Valido(int reg_n)
	{
		return (reg_n >=0 && reg_n <=9);
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
	public static void main(String args[]) {
		Sistema s = new Sistema();
		//TODO DONE while loop com menu perguntando pro usuário qual programa ele quer executar.
		menu_main(s);

	}

	public static void menu_main(Sistema s)
	{
		int opcao_menu = -1;
		Scanner teclado = new Scanner(System.in);
		do{
			System.out.println("--------- ESCOLHA O PROGRAMA ---------");
			System.out.println(" [1] - ProgMin");
			System.out.println(" [2] - Fibonacci");
			System.out.println(" [3] - Fatorial");
			System.out.println(" [4] - BubbleSort");
			System.out.println(" [0] - Sair");
			opcao_menu = teclado.nextInt();

			switch (opcao_menu) {
				case 0:
					System.out.println("---------------------------------- fim sistema ");
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
					break;
				default:
					System.out.println(" Opcao invalida, tente novamente.");
					break;
			}

		} while (opcao_menu != 0);
	}



    // -------------------------------------------------------------------------------------------------------
    // --------------- TUDO ABAIXO DE MAIN É AUXILIAR PARA FUNCIONAMENTO DO SISTEMA - nao faz parte

	// -------------------------------------------- teste do sistema ,  veja classe de programas
	public void test1(){
		Aux aux = new Aux();
		Word[] p = new Programas().fibonacci10;
		aux.carga(p, vm.m);
		vm.cpu.setContext(0);
		System.out.println("---------------------------------- programa carregado ");
		aux.dump(vm.m, 0, 33);
		System.out.println("---------------------------------- após execucao ");
		vm.cpu.run();
		aux.dump(vm.m, 0, 33);
	}

	public void test2(){
		Aux aux = new Aux();
		Word[] p = new Programas().progMinimo;
		aux.carga(p, vm.m);
		vm.cpu.setContext(0);
		System.out.println("---------------------------------- programa carregado ");
		aux.dump(vm.m, 0, 15);
		System.out.println("---------------------------------- após execucao ");
		vm.cpu.run();
		aux.dump(vm.m, 0, 15);

	}

	// teste p3

	public void test_p3(){
		Aux aux = new Aux();
		Word[] p = new Programas().p3;
		aux.carga(p, vm.m);
		vm.cpu.setContext(0);
		System.out.println("---------------------------------- programa carregado ");
		aux.dump(vm.m, 0, 15);
		System.out.println("---------------------------------- após execucao ");
		vm.cpu.run();
		aux.dump(vm.m, 0, 15);
	}


	// -------------------------------------------  classes e funcoes auxiliares
    public class Aux {
		public void dump(Word w) {
			System.out.print("[ ");
			System.out.print(w.opc); System.out.print(", ");
			System.out.print(w.r1);  System.out.print(", ");
			System.out.print(w.r2);  System.out.print(", ");
			System.out.print(w.p);  System.out.println("  ] ");
		}
		public void dump(Word[] m, int ini, int fim) {
			for (int i = ini; i < fim; i++) {
				System.out.print(i); System.out.print(":  ");  dump(m[i]);
			}
		}
		public void carga(Word[] p, Word[] m) {
			for (int i = 0; i < p.length; i++) {
				m[i].opc = p[i].opc;     m[i].r1 = p[i].r1;     m[i].r2 = p[i].r2;     m[i].p = p[i].p;
			}
		}
   }
   // -------------------------------------------  fim classes e funcoes auxiliares

   //  -------------------------------------------- programas aa disposicao para copiar na memoria (vide aux.carga)
   public class Programas {

	   //TODO DONE nao precisa fazer
	   public Word[] progMinimo = new Word[] {
		    new Word(Opcode.LDI, 0, -1, 999),
			new Word(Opcode.STD, 0, -1, 10),
			new Word(Opcode.STD, 0, -1, 11),
			new Word(Opcode.STD, 0, -1, 12),
			new Word(Opcode.STD, 0, -1, 13),
			new Word(Opcode.STD, 0, -1, 14),
			new Word(Opcode.STOP, -1, -1, -1) };

	   //TODO DOING inserir o esquema de trap no programa
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

      // TODO DONE P3 feito
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

		 new Word(Opcode.STD,1,-1,80), // acaba o loop, joga o valor de r1 (resultado do fatorial) na posicao 20 da memori
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

	  //TODO DOING fazer o P4


   }
}


