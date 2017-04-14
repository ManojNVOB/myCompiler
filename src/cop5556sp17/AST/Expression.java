package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;

public abstract class Expression extends ASTNode {
	
	private TypeName type;
	
	protected Expression(Token firstToken) {
		super(firstToken);
	}
	
	public TypeName getIdentType() {
		return this.type;
	}
	
	public void setIdentType(TypeName type){
		this.type = type;
	}

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
