package com.company;// PUCRS - Escola Politécnica - Sistemas Operacionais
// Prof. Fernando Dotti
// Código fornecido como parte da solução do projeto de Sistemas Operacionais
//
// Fase 1 - máquina virtual (vide enunciado correspondente)
//

import java.util.Objects;
import java.util.Scanner;

//TODO testar tudo com programas teste

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

				//TODO fazer as validações dos possiveis erros em casa Opcode (Já tem um exemplo no JMP)

					switch (ir.opc) { // para cada opcode, sua execução

						case TRAP:
							if (reg[8] == 1) //in
							{
								Scanner teclado = new Scanner(System.in);
								m[reg[9]].opc = Opcode.DATA;
								m[reg[9]].p = teclado.nextInt();
							}
							if (reg[8] == 2) //out
							{
								System.out.println(m[reg[9]].p);
							}
							break;

						case JMP: //PC ← k
							//tratamento de erro
							if(m[ir.p].opc == Opcode.___)
							{
								ir.interruption = 2;
							}
							//execucao jmp
							else
							{
								pc = ir.p;
								break;
							}

						case JMPI: //PC ← Rs
							pc = reg[ir.r1];
							break;

						case JMPIG: // If Rc > 0 Then PC ← Rs Else PC ← PC +1
							if (reg[ir.r2] > 0)
							{
								pc = reg[ir.r1];
							}
							else
							{
								pc++;
							}
							break;


						case JMPIL: //if Rc < 0 then PC ← Rs Else PC ← PC +1
							if (reg[ir.r2] < 0) {
								pc = reg[ir.r1];
							} else {
								pc++;
							}
							break;

						case JMPIE: //if Rc == 0 then PC ← Rs Else PC ← PC +1
							if (reg[ir.r2] == 0) {
								pc = reg[ir.r1];
							} else {
								pc++;
							}
							break;

						case JMPIM: // PC ← [A]
							pc = m[ir.p].p;
							break;

						case JMPIGM: // if Rc > 0 then PC ← [A] Else PC ← PC +1
							if (reg[ir.r2] > 0) {
								pc = ir.p;
							} else {
								pc++;
							}
							break;

						case JMPILM: //if Rc < 0 then PC ← [A] Else PC ← PC +1
							if (reg[ir.r2] < 0) {
								pc = ir.p;
							} else {
								pc++;
							}
							break;

						case JMPIEM: //if Rc == 0 then PC ← [A] Else PC ← PC +1
							if (reg[ir.r2] == 0) {
								pc = ir.p;
							} else {
								pc++;
							}
							break;

						case STOP: // por enquanto, para execucao
							ir.interruption = 4; // interrupcao stop
							break;

						case ADDI: // Rd ← Rd + k
							reg[ir.r1] = reg[ir.r1] + ir.p;
							pc++;
							break;

						case SUBI: // Rd ← Rd – k
							reg[ir.r1] = reg[ir.r1] - ir.p;
							pc++;
							break;

						case ADD: // Rd ← Rd + Rs
							reg[ir.r1] = reg[ir.r1] + reg[ir.r2];
							pc++;
							break;

						case SUB: // Rd ← Rd - Rs
							reg[ir.r1] = reg[ir.r1] - reg[ir.r2];
							pc++;
							break;

						case MULT: // Rd ← Rd * Rs
							reg[ir.r1] = reg[ir.r1] * reg[ir.r2];
							pc++;
							break;


						case LDI: // Rd ← k
							reg[ir.r1] = ir.p;
							pc++;
							break;

						case LDD: // Rd ← [A]
							//validacao
							reg[ir.r1] = m[ir.p].p;
							pc++;
							break;

						case STD: // [A] ← Rs
							m[ir.p].opc = Opcode.DATA;
							m[ir.p].p = reg[ir.r1];
							pc++;
							break;

						case LDX: // Rd ← [Rs]
							reg[ir.r1] = m[reg[ir.r2]].p;
							break;

						case STX: // [Rd] ←Rs
							m[reg[ir.r1]].opc = Opcode.DATA;
							m[reg[ir.r1]].p = reg[ir.r2];
							pc++;
							break;

						case SWAP: //T ← Ra Ra ← Rb Rb ←T
							int t = reg[ir.r1];
							reg[ir.r1] = reg[ir.r2];
							reg[ir.r2] = t;
							break;

						default:
							ir.interruption = 3;
							break;
					}


				if(ir.interruption != 0)
				{
					if (ir.interruption == 1) //Overflow em uma operacao aritmetica
					{
						Tratamento_Overflow();
						break;
					}
					else if (ir.interruption == 2) //acessou um endereço invalido de memoria (ArrayOutOfBound)
					{
						Tratamento_Endereco_Invalido();
						break;
					}
					else if (ir.interruption == 3) //Intrucao Invalida
					{
						Tratamento_Opcode_Invalido();
						break;
					}
					else if (ir.interruption == 4) //opcode STOP em sí
					{
						Trtamento_STOP();
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


	public void Tratamento_Overflow()
	{
		//TODO msg de erro (system.out)
	}
	public void Tratamento_Opcode_Invalido()
	{
		//TODO msg de erro (system.out)
	}
	public void Tratamento_Endereco_Invalido()
	{
		//TODO msg de erro (system.out)
	}
	public void Trtamento_STOP()
	{
		//TODO dizer que está encerrando o programa a ser executado (system.out)
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
		//TODO while loop com menu perguntando pro usuário qual programa ele quer executar.
		//s.test1();
		//s.test2();
		s.test_p3();

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
		//TODO minuto video (31min) criar interface pra escolher qual programa vai rodar e fazer feedbacks em tempo de execucao pra dizer qnd comecou e parou de rodar cada programa.
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

		//TODO tratar aqui as exeções/interrupcoes
   }
   // -------------------------------------------  fim classes e funcoes auxiliares

   //  -------------------------------------------- programas aa disposicao para copiar na memoria (vide aux.carga)
   public class Programas {
    	//TODO fazer os programas que faltam e completar os ja existentes

	   public Word[] progMinimo = new Word[] {
		    new Word(Opcode.LDI, 0, -1, 999),
			new Word(Opcode.STD, 0, -1, 10),
			new Word(Opcode.STD, 0, -1, 11),
			new Word(Opcode.STD, 0, -1, 12),
			new Word(Opcode.STD, 0, -1, 13),
			new Word(Opcode.STD, 0, -1, 14),
			new Word(Opcode.STOP, -1, -1, -1) };

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
			new Word(Opcode.STOP, -1, -1, -1) };

	   public Word[] p3 = new Word[]{
/*1 */       new Word(Opcode.LDI, 1, -1, -1), // joga o valor 10 no Registrador1
/*2 */		 new Word(Opcode.STD, 1, -1, 20), // coloca o valor do Registrado1 na posição 20 da memoria
/*3 */		 new Word(Opcode.LDD,2,-1,20), // ler da memoria e colocar no registrador
/*4 */		 new Word(Opcode.JMPIL,13,2,-1),// comparar se registrador < 0

/*5 */		 new Word(Opcode.SUBI,2,-1,1), //r2 = 9
			 // inicio loop
/*6 */		 new Word(Opcode.ADDI,2,-1,1), // readiona 1 pra que o loop fique certo

/*7 */		 new Word(Opcode.SUBI,2,-1,1), //subtrai pra fazer r1*r2

/*8 */		 new Word(Opcode.MULT,1,2,-1), //multiplica

/*9 */		 new Word(Opcode.SUBI,2,-1,1), // subtrai pra comparar a zero e possivelmente parar
/*10*/		 new Word(Opcode.JMPIGM,-1,2,6),	// compara a zero para ver se precisa parar   x = 6
			 // fim loop

/*11*/		 new Word(Opcode.STD,1,-1,20), // acaba o loop, joga o valor de r1 (resultado do fatorial) na posicao 20 da memoria
/*12*/		 new Word(Opcode.STOP,-1,-1,-1), // acaba o programa
/*13*/       new Word(Opcode.LDI,1,-1,-1), // (se no primeiro jmp, o input for -1, vem pra cá) joga o valor de -1 no registrador 1
/*14*/       new Word(Opcode.STD,1,-1,20), // armazena no valor de r1 na posicao 20 da memoria
/*15*/       new Word(Opcode.STOP,-1,-1,-1)	// acaba o programa
		   };
   }
}


