package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;


public abstract class Chain extends Statement {
	
	private TypeName type;
	
	public Chain(Token firstToken) {
		super(firstToken);
	}

	public TypeName getIdentType() {
		return this.type;
	}
	
	public void setIdentType(TypeName type){
		this.type = type;
	}
	
}
