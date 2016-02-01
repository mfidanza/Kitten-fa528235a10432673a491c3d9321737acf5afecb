package types;

import absyn.CodeDeclaration;
import translation.Block;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.MethodGen;

import javaBytecodeGenerator.TestClassGenerator;
public class FixtureSignature extends CodeSignature{
	
	public FixtureSignature(ClassType clazz, CodeDeclaration abstractSyntax) {
		super(clazz, VoidType.INSTANCE, TypeList.EMPTY.push(clazz), "fixture"+count++, abstractSyntax);	}

	@Override
	protected Block addPrefixToCode(Block code) {
		return code;
	}
	
	
private static int count;

public void createFixture(TestClassGenerator classGen) {
	
	MethodGen methodGen = new MethodGen
			(Constants.ACC_PRIVATE | Constants.ACC_STATIC, //le fixture sono private e static
			org.apache.bcel.generic.Type.VOID, //le fixture ritornano void
			this.getParameters().toBCEL(), //parameters (the class name)
			null, // parameters names: non ci interessa
			getName().toString(), // <tt><init></tt>
			classGen.getClassName(), // name of the class
			classGen.generateJavaBytecode(getCode()), // bytecode of the constructor
			classGen.getConstantPool()); // constant pool
	
	methodGen.setMaxStack();
	methodGen.setMaxLocals();
	
	// we add a method to the class that we are generating
	classGen.addMethod(methodGen.getMethod());
}

}
