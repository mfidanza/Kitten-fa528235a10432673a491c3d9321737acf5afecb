package javaBytecodeGenerator;

import java.util.Set;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IFEQ;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

import types.ClassMemberSignature;
import types.ClassType;
import types.FixtureSignature;
import types.TestSignature;

/**
 * A Java bytecode generator. It transforms the Kitten intermediate language
 * into Java bytecode that can be dumped to Java class files and run.
 * It uses the BCEL library to represent Java classes and dump them on the file-system.
 *
 * @author <A HREF="mailto:fausto.spoto@univr.it">Fausto Spoto</A>
 */

@SuppressWarnings("serial")
public class TestClassGenerator extends JavaClassGenerator {

	/**
	 * Builds a class generator for the given class type.
	 *
	 * @param clazz the class type
	 * @param sigs a set of class member signatures. These are those that must be
	 *             translated. If this is {@code null}, all class members are translated
	 */

	public TestClassGenerator(ClassType clazz, Set<ClassMemberSignature> sigs) {
		super(clazz.getName()+"Test", // name of the class
			"java.lang.Object", // the superclass of the Kitten Object class is set to be the Java java.lang.Object class
			clazz.getName() + ".kit", // source file
			Constants.ACC_PUBLIC, // Java attributes: public!
			noInterfaces, // no interfaces
			new ConstantPoolGen()); // empty constant pool, at the beginning

		for(FixtureSignature fixSign: clazz.getFixtures())
			if(sigs == null || sigs.contains(fixSign))
				fixSign.createFixture(this);
		
		for(TestSignature testSign: clazz.getTests())
			if(sigs == null || sigs.contains(testSign))
				testSign.createTest(this);
		
		this.createTestMain(clazz);
	}

	private void createTestMain(ClassType clazz){
		Type strType=ClassType.mk("String").toBCEL();
		Type arg_str[]={strType};
		Type arg_C[]={clazz.toBCEL()};
		final int VAR_PASSED=1, VAR_FAILED=2, VAR_TIME=3
				, VAR_TOPRINT=5, VAR_TOTALTIME=6; //VAR_TIME è un long e quindi occupa due slot
		
		InstructionList il=new InstructionList();
		//inizializzo il numero di test passati a 0
		il.append(InstructionFactory.ICONST_0);
		il.append(InstructionFactory.createStore(Type.INT, VAR_PASSED));
		//inizializzo il numero di test falliti a 0
		il.append(InstructionFactory.ICONST_0);
		il.append(InstructionFactory.createStore(Type.INT, VAR_FAILED));
		//inizializzo la variabile contenente il tempo a 0
		il.append(InstructionFactory.LCONST_0);
		il.append(InstructionFactory.createStore(Type.LONG, VAR_TIME));
		//inizializzo il tempo totale a 0
		il.append(InstructionFactory.FCONST_0);
		il.append(InstructionFactory.createStore(Type.FLOAT, VAR_TOTALTIME));
		//inizio a concatenare la stringa di output che verrà stampata alla fine 
		this.pushRunTimeString(il, "\nTest execution for class "+clazz.getName()+":\n");
		il.append(InstructionFactory.createStore(strType, VAR_TOPRINT));
		
		//Per ogni singolo test di clazz
		for(TestSignature ts:clazz.getTests()){
			
			//addConcatToVar() è un metodo che riceve in ingresso un instructionlist, una variabile e una stringa e 
			//concatena la stringa alla variabile
			this.addConcatToVar(il,VAR_TOPRINT,"  - "+ts.getName()+": ");
			
			//---C obj=new C()---
			il.append(this.getFactory().createNew((ObjectType)clazz.toBCEL()));
			il.append(InstructionFactory.DUP);
			il.append(this.getFactory().createInvoke(clazz.getName(), "<init>"
					, Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
			//---
			
			//per ogni fixture
			for(FixtureSignature fs:clazz.fixtureLookup()){
				
				
				
				//COSA FA QUESTO CICLO???
				//NomeClasseTest???
				
				
				il.append(InstructionFactory.DUP);
				il.append(this.getFactory().createInvoke(clazz.getName()+"Test", fs.getName()
						, Type.VOID, arg_C, Constants.INVOKESTATIC));
			}
			
			
			//inizio a cronometrare il test (var_time di tipo LONG)
			il.append(this.getFactory().createInvoke(System.class.getName(), "nanoTime"
					, Type.LONG, Type.NO_ARGS, Constants.INVOKESTATIC));
			il.append(InstructionFactory.createStore(Type.LONG, VAR_TIME));
			
			
			//Ho obj sullo stack in questo momento
			//String result=test(obj)
			//eseguo il test e sullo stack mi ritrovo la stringa risultato del test
			il.append(this.getFactory().createInvoke(clazz.getName()+"Test", ts.getName()
					, strType, arg_C, Constants.INVOKESTATIC));
			
			//STACK |result|
			
			//Prendo il tempo dopo il test e tempotrascorso = tempoattuale-startTest
			il.append(this.getFactory().createInvoke(System.class.getName(), "nanoTime"
					, Type.LONG, Type.NO_ARGS, Constants.INVOKESTATIC));
			il.append(InstructionFactory.createLoad(Type.LONG, VAR_TIME));
			il.append(InstructionFactory.LSUB);
			
			//converto in millisecondi e prendo solo 2 cifre dopo la virgola
			//divisione intera e poi divisione float
			il.append(this.getFactory().createConstant(10000L));
			il.append(InstructionFactory.LDIV);
			il.append(InstructionFactory.L2F);
			il.append(this.getFactory().createConstant(100.0f));
			il.append(InstructionFactory.FDIV);
			
			//---Aggiungo a totaltime---
			il.append(InstructionFactory.DUP);
			il.append(InstructionFactory.createLoad(Type.FLOAT, VAR_TOTALTIME));
			il.append(InstructionFactory.FADD);
			il.append(InstructionFactory.createStore(Type.FLOAT, VAR_TOTALTIME));
			//---
			
			//STACK |result|float|
			//Concateno il tempo del test con le sole due cifre dopo la virgola
			this.pushRunTimeString(il, "[");
			il.append(InstructionFactory.SWAP);
			il.append(this.getFactory().createInvoke("runTime.String", "concat"
							, strType, new Type[]{Type.FLOAT}, Constants.INVOKEVIRTUAL));
			this.pushRunTimeString(il, "ms] ");
			il.append(this.getFactory().createInvoke("runTime.String", "concat"
					, strType, arg_str, Constants.INVOKEVIRTUAL));
		
			
			//STACK |result|timeStr|			
			//if result.equal("")
			il.append(InstructionFactory.SWAP);
			il.append(InstructionFactory.DUP);
			this.pushRunTimeString(il, "");
			il.append(this.getFactory().createInvoke("runTime.String", "equals"
					, Type.BOOLEAN, arg_str, Constants.INVOKEVIRTUAL));
			//
			
			//Etichetta nel caso il test fallisca
			InstructionList il_emptyresult=new InstructionList();
			InstructionHandle ih_emptyresult=this.addConcatToVar(il_emptyresult, VAR_TOPRINT, "failed ");
			//---
			//Etichetta di end
			InstructionHandle ih_emptyresultend=il_emptyresult.append(InstructionFactory.NOP);
			
			il.append(new IFEQ(ih_emptyresult));
			
			//Se il test e' passato
			this.addConcatToVar(il, VAR_TOPRINT, "passed ");
			il.append(new GOTO(ih_emptyresultend));
			il.append(il_emptyresult);
			
			//concat time
			il.append(InstructionFactory.SWAP);
			//STACK |result|time|
			this.addConcatToVar(il, VAR_TOPRINT);
			//STACK |result|
			
			il.append(InstructionFactory.DUP);
			this.pushRunTimeString(il, "");
			il.append(this.getFactory().createInvoke("runTime.String", "equals"
					, Type.BOOLEAN, arg_str, Constants.INVOKEVIRTUAL));
			
			//equal("")== true -> test fallito
			//scrivo "at <riga>" e incremento failed
			InstructionList il_adderrorline=new InstructionList();
			InstructionHandle ih_adderrorlinestart=
					this.addConcatToVar(il_adderrorline, VAR_TOPRINT," at ");
			this.addConcatToVar(il_adderrorline, VAR_TOPRINT);
			this.addConcatToVar(il_adderrorline, VAR_TOPRINT,"\n");
			il_adderrorline.append(new IINC(VAR_FAILED, 1));
			InstructionHandle ih_adderrorlineend=il_adderrorline.append(InstructionFactory.NOP);
			//
			
			il.append(new IFEQ(ih_adderrorlinestart));
			
			//equal("")== false -> test passato
			//rimuovo result dallo stack visto che e' vuoto e non server
			il.append(InstructionFactory.POP);

			this.addConcatToVar(il, VAR_TOPRINT,"\n");
			il.append(new IINC(VAR_PASSED, 1));
			il.append(new GOTO(ih_adderrorlineend));
			
			
			il.append(il_adderrorline);
		}

		//stack vuoto. Concateno il resoconto finale
		this.addConcatToVar(il, VAR_TOPRINT,"\n");
		il.append(InstructionFactory.createLoad(strType, VAR_TOPRINT));
		
		Type[] arg_int={Type.INT};
		il.append(InstructionFactory.createLoad(Type.INT, VAR_PASSED));
		il.append(this.getFactory().createInvoke("runTime.String", "concat"
				, strType, arg_int, Constants.INVOKEVIRTUAL));
		
		this.pushRunTimeString(il, " tests passed, ");
		il.append(this.getFactory().createInvoke("runTime.String", "concat"
				, strType, arg_str, Constants.INVOKEVIRTUAL));
		
		il.append(InstructionFactory.createLoad(Type.INT, VAR_FAILED));
		il.append(this.getFactory().createInvoke("runTime.String", "concat"
				, strType, arg_int, Constants.INVOKEVIRTUAL));
		
		this.pushRunTimeString(il, " failed [");
		il.append(this.getFactory().createInvoke("runTime.String", "concat"
				, strType, arg_str, Constants.INVOKEVIRTUAL));
		
		il.append(InstructionFactory.createLoad(Type.FLOAT, VAR_TOTALTIME));
		il.append(this.getFactory().createInvoke("runTime.String", "concat"
				, strType, new Type[]{Type.FLOAT}, Constants.INVOKEVIRTUAL));
		
		this.pushRunTimeString(il, "ms]\n");
		il.append(this.getFactory().createInvoke("runTime.String", "concat"
				, strType, arg_str, Constants.INVOKEVIRTUAL));
		
		il.append(this.getFactory().createInvoke("runTime.String", "output"
				, Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
		
		il.append(InstructionFactory.createReturn(Type.VOID));
		
		//Attach all in the main method
		this.attachToMain(il);
	}
	
	//inserimento sullo stack della stringa passata come argomento del metodo
	private void pushRunTimeString(InstructionList il,String toPush){
		il.append(this.getFactory().createNew("runTime/String"));
		il.append(InstructionFactory.DUP);
		il.append(this.getFactory().createConstant(toPush));
		il.append(this.getFactory().createInvoke("runTime.String", "<init>"
				, Type.VOID, new Type[]{Type.getType(String.class)}
				,Constants.INVOKESPECIAL));
	}
	
	
	//concateno la variabile locale numero varIndex con la stringa toConcat
	private InstructionHandle addConcatToVar(InstructionList il,int varIndex, String toConcat){
		Type strType=ClassType.mk("String").toBCEL();
		Type arg_str[]={strType};
		//carica variabile
		InstructionHandle toRet= il.append(InstructionFactory.createLoad(strType, varIndex));
		//carica stringa
		this.pushRunTimeString(il, toConcat);
		//chiama concat()
		il.append(this.getFactory().createInvoke("runTime.String", "concat"
						, strType, arg_str, Constants.INVOKEVIRTUAL));
		//salva in variabile
		il.append(InstructionFactory.createStore(strType, varIndex));
		return toRet;
	}
	
	
	//concateno la stringa in cima allo stack con la variabile locale numero varIndex
	private InstructionHandle addConcatToVar(InstructionList il,int varIndex){
		Type strType=ClassType.mk("String").toBCEL();
		Type arg_str[]={strType};
		
		//carica variabile
		InstructionHandle toRet= il.append(InstructionFactory.createLoad(strType, varIndex));
		
		//swap
		il.append(InstructionFactory.SWAP);
		
		//chiama concat()
		il.append(this.getFactory().createInvoke("runTime.String", "concat", strType, arg_str, Constants.INVOKEVIRTUAL));
		
		//salva in variabile
		il.append(InstructionFactory.createStore(strType, varIndex));
		return toRet;
	}

	//Attacca la lista delle istruzioni al main
	private void attachToMain(InstructionList il){
		MethodGen methodGen = new MethodGen
				(Constants.ACC_PUBLIC | Constants.ACC_STATIC, // public and static
				org.apache.bcel.generic.Type.VOID, // return type
				new org.apache.bcel.generic.Type[] // parameters
					{ new org.apache.bcel.generic.ArrayType("java.lang.String", 1) },
				null, // parameters names: we do not care
				"main", // method's name
				this.getClassName(), // defining class
				il, // bytecode of the method
				this.getConstantPool()); // constant pool
		methodGen.setMaxStack();
		methodGen.setMaxLocals();
		this.addMethod(methodGen.getMethod());
	}
}