
package types;

import absyn.CodeDeclaration;
import translation.Block;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.MethodGen;
import javaBytecodeGenerator.TestClassGenerator;
public class TestSignature extends CodeSignature{

	public TestSignature(ClassType clazz, String name, CodeDeclaration abstractSyntax) {
		//VoidType because we put the return String statement in the translate method: when
		//we translate in kitten bytecode. If we put StringType here the typechecker tell us
		//that there are some problem because it find no return of string because we put the
		//return after the type-checking phase
		super(clazz, VoidType.INSTANCE, TypeList.EMPTY.push(clazz), name, abstractSyntax);	}

	@Override
	protected Block addPrefixToCode(Block code) {
		return code;
	}
	
	public void createTest(TestClassGenerator classGen) {
		
		MethodGen methodGen = new MethodGen
				(Constants.ACC_PRIVATE | Constants.ACC_STATIC, //le fixture sono private e static
				ClassType.mk("String").toBCEL(), //le fixture ritornano void
				this.getParameters().toBCEL(), //parameters (the class name)
				null, // parameters names: we do not care
				getName(), // <tt><init></tt>
				classGen.getClassName(), // name of the class
				classGen.generateJavaBytecode(getCode()), // bytecode of the constructor
				classGen.getConstantPool()); // constant pool
		
		methodGen.setMaxStack();
		methodGen.setMaxLocals();
		
		// we add a method to the class that we are generating
		classGen.addMethod(methodGen.getMethod());
	}

}