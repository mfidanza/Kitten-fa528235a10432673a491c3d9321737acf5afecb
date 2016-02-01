package absyn;

import java.io.FileWriter;
import java.io.IOException;

import types.ClassType;
import semantical.TypeChecker;
import translation.Block;
import bytecode.NEWSTRING;
import bytecode.RETURN;

public class Assert extends Command{

	private final Expression expression;
	private String errorPosition;
	
	public Assert(int pos, Expression expr) {
		super(pos);
		this.expression=expr;
	}
	
	protected void toDotAux(FileWriter where) throws IOException {
		linkToNode("expr", expression.toDot(where), where);
	}

	@Override
	protected TypeChecker typeCheckAux(TypeChecker checker) {
		errorPosition= checker.getPosition(getPos());
		expression.mustBeBoolean(checker);
		
		//controllo se l'assert si trova all'interno di un test
		if(!checker.isAssertAllowed()){
			error("Assert can be used only in tests");
		}
		
		// we return the original type-checker. Hence local declarations
		// inside the then or _else are not visible after the conditional
		return checker;
	}

	@Override
	public boolean checkForDeadcode() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Block translate(Block continuation) {
		continuation.doNotMerge();
		//se fallisce un assert, non vado avanti a controllare gli altri assert. viene ritornata la riga di errore
		Block no= new NEWSTRING(""+errorPosition).followedBy(new Block(new RETURN(ClassType.mk("String"))));

		//traduco assert come un if. Continuation è il blocco yes, no il blocco no
		return expression.translateAsTest(continuation, no);
	}

}