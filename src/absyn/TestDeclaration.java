package absyn;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import bytecode.NEWSTRING;
import bytecode.RETURN;
import semantical.TypeChecker;
import translation.Block;
import types.ClassMemberSignature;
import types.ClassType;
import types.TestSignature;
import types.VoidType;
public class TestDeclaration extends CodeDeclaration{

	private final String name;
	
	public TestDeclaration(int pos, String name, Command body,
			ClassMemberDeclaration next) {
		super(pos, null, body, next);
		this.name=name;
	}

	@Override
	protected void toDotAux(FileWriter where) throws IOException {
		linkToNode("name", toDot(name, where), where);
		linkToNode("body", getBody().toDot(where), where);
	}

	@Override
	protected void addTo(ClassType clazz) {
		// TODO Auto-generated method stub
		TestSignature tSign = new TestSignature(clazz, name, this);
		clazz.addTest(tSign);
		setSignature(tSign);
	}

	@Override
	public void translate(Set<ClassMemberSignature> done) {
		if (done.add(getSignature())) {
    		process(getSignature().getDefiningClass(), done);
    		
    		//è lo stesso metodo translate di CodeDeclaration con l'aggiunta che
    		//viene ritornata una stringa vuota se tutti gli assert di getBody sono passati
    		//stringa vuota = test passato. Se un assert non passa, stringa non vuota
    		getSignature().setCode(getBody().translate(new NEWSTRING("").followedBy(new Block(new RETURN(ClassType.mk("String"))))));

    		translateReferenced(getSignature().getCode(), done, new HashSet<Block>());
    	}
	}
	
	
	protected void typeCheckAux(ClassType currentClass) {
		// TODO Auto-generated method stub
		TypeChecker checker = new TypeChecker(VoidType.INSTANCE, currentClass.getErrorMsg(), true);
		checker = checker.putVar("this", currentClass);
		  				  		
		// type check sul body del Test
		getBody().typeCheck(checker);

		// controllo non ci sia codice morto
		getBody().checkForDeadcode();

	}

}