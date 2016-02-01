package absyn;

import java.io.FileWriter;
import java.io.IOException;

import types.ClassType;
import types.FixtureSignature;

import types.VoidType;
import semantical.TypeChecker;
public class FixtureDeclaration extends CodeDeclaration{

	public FixtureDeclaration(int pos, Command body, ClassMemberDeclaration next) {
		super(pos, null, body, next);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void toDotAux(FileWriter where) throws IOException {
		linkToNode("body", getBody().toDot(where), where);
	}

	@Override
	protected void addTo(ClassType clazz) {
		// TODO Auto-generated method stub
		FixtureSignature fSign = new FixtureSignature(clazz, this);
		clazz.addFixture(fSign);
		setSignature(fSign);
		
	}

	@Override
	protected void typeCheckAux(ClassType currentClass) {
		// TODO Auto-generated method stub
		TypeChecker checker = new TypeChecker(VoidType.INSTANCE, currentClass.getErrorMsg());
		checker = checker.putVar("this", currentClass);

		getBody().typeCheck(checker);
		
		// controlla che non ci sia dead-code nel corpo della fixture
		getBody().checkForDeadcode();
	}

}