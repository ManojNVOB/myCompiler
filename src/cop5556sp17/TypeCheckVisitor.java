package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;
import cop5556sp17.AST.Type;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		
		Chain chain =  binaryChain.getE0();
		ChainElem chainElem = binaryChain.getE1();
		
		chain.visit(this, null);
		chainElem.visit(this, null);
		
		TypeName chainType =  chain.getIdentType();
		TypeName chainElemType = chainElem.getIdentType();
		
		Token firstToken = chainElem.getFirstToken();
		Token op = binaryChain.getArrow();
				
		
		if(chainType.isType(IMAGE) && (chainElem instanceof FilterOpChain) && firstToken.isOneOfKinds(OP_GRAY,OP_BLUR,OP_CONVOLVE)){
			binaryChain.setIdentType(IMAGE);
		}
		else if(op.isKind(ARROW)){
			if(chainType.isType(URL) && chainElemType.isType(IMAGE)){
				binaryChain.setIdentType(IMAGE);
			}
			else if(chainType.isType(FILE) && chainElemType.isType(IMAGE)){
				binaryChain.setIdentType(IMAGE);
			}
			else if(chainType.isType(FRAME)){
				if((chainElem instanceof FrameOpChain) && firstToken.isOneOfKinds(KW_XLOC, KW_YLOC) ){
					binaryChain.setIdentType(INTEGER);
				}
				else if((chainElem instanceof FrameOpChain) && firstToken.isOneOfKinds(KW_SHOW, KW_HIDE, KW_MOVE)){
					binaryChain.setIdentType(FRAME);
				}
				else{
					throw new TypeCheckException("expected (url,image) or (file, image) etc with arrow");
				}
			}
			else if(chainType.isType(IMAGE)){
				if((chainElem instanceof ImageOpChain) && firstToken.isOneOfKinds(OP_WIDTH,OP_HEIGHT)){
					binaryChain.setIdentType(INTEGER);
				}
				else if((chainElem instanceof IdentChain) && chainElemType.isType(IMAGE) ){
					binaryChain.setIdentType(IMAGE);
				}				
				else if(chainElemType.isType(FRAME)){
					binaryChain.setIdentType(FRAME);
				}
				else if(chainElemType.isType(FILE)){
					binaryChain.setIdentType(NONE);
				}
				else if((chainElem instanceof ImageOpChain) && firstToken.isKind(KW_SCALE)){
					binaryChain.setIdentType(IMAGE);
				}
				else{
					throw new TypeCheckException("expected frame or file etc.  with image");
				}
			}
			else if(chainType.isType(INTEGER)){
				 if((chainElem instanceof IdentChain) && chainElemType.isType(INTEGER) ){
						binaryChain.setIdentType(INTEGER);
					}
				 else{
					 throw new TypeCheckException("expected chain type as Integer, chain elem as instance of IdentChain and chain elem type as integer");
				 }
			}
			else{
				throw new TypeCheckException("expected chain type as image or frame etc.");
			}		
		}

		else{
			throw new TypeCheckException("expected arrow or bar arrow"); 
		}
		return binaryChain;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		Expression exp0 =  binaryExpression.getE0();
		Expression exp1 =  binaryExpression.getE1();
		
		exp0.visit(this, null);
		exp1.visit(this, null);
		
		TypeName exp0Type = exp0.getIdentType();
		TypeName exp1Type = exp1.getIdentType();
		Token op = binaryExpression.getOp();
		
		
		
		if(op.isOneOfKinds(PLUS,MINUS)){
			if(exp0Type==INTEGER && exp1Type==INTEGER){
				binaryExpression.setIdentType(INTEGER);
			}
			else if(exp0Type==IMAGE && exp1Type==IMAGE){
				binaryExpression.setIdentType(IMAGE);
			}
			else{
				throw new TypeCheckException("Expected both as Integer operands or Image operands but found "+exp0Type+", "+ exp1Type);
			}
		}
		else if(op.isKind(TIMES)){
			if(exp0Type==INTEGER && exp1Type==INTEGER){
				binaryExpression.setIdentType(INTEGER);
			}
			else if((exp0Type==IMAGE && exp1Type==INTEGER)|| (exp0Type==INTEGER && exp1Type==IMAGE)){
				binaryExpression.setIdentType(TypeName.IMAGE);
			}
			else{
				throw new TypeCheckException("Expected operands of type Image or Integer but found "+exp0Type+", "+ exp1Type);
			}
		}
		else if(op.isOneOfKinds(DIV,MOD)){
			if(exp0Type==INTEGER && exp1Type==INTEGER){
				binaryExpression.setIdentType(INTEGER);
			}
			else if(exp0Type==IMAGE && exp1Type==INTEGER){
				binaryExpression.setIdentType(IMAGE);
			}
			else{
				throw new TypeCheckException("Expected both as Integer operands but found "+exp0Type+", "+ exp1Type);
			}
		}
		else if(op.isOneOfKinds(LT,GT,LE,GE)){
			if((exp0Type==INTEGER && exp1Type==INTEGER)|| (exp0Type==BOOLEAN && exp1Type==BOOLEAN)){
				binaryExpression.setIdentType(BOOLEAN);
			}
		}
		else if(op.isOneOfKinds(EQUAL,NOTEQUAL)){
			if(exp0Type.equals(exp1Type)){
				binaryExpression.setIdentType(BOOLEAN);
			}
			else{
				throw new TypeCheckException("left and right of equals should be of same type but found "+exp0Type+", "+exp1Type);
			}
		}
		else if(op.isOneOfKinds(AND,OR)){
			if(exp0Type==INTEGER && exp1Type==INTEGER){
				binaryExpression.setIdentType(INTEGER);
			}
			else if(exp0Type==BOOLEAN && exp1Type==BOOLEAN){
				binaryExpression.setIdentType(BOOLEAN);
			}
			else{
				throw new TypeCheckException("Expected both as Integer operands but found "+exp0Type+", "+ exp1Type);
			}
		}
		
		else{
			throw new TypeCheckException("Expected binary operand but found "+op.getText());
		}
		return binaryExpression;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		symtab.enterScope();
		ArrayList<Dec> decs = block.getDecs();
		ArrayList<Statement> statements = block.getStatements();
		for(Dec dec:decs){
			dec.visit(this, null);
		}
		for(Statement statement: statements){
			statement.visit(this, null);
		}
		symtab.leaveScope();
		return block;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		booleanLitExpression.setIdentType(BOOLEAN);
		return booleanLitExpression;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		Tuple tuple =filterOpChain.getArg();
		tuple.visit(this, null);
		int size = tuple.getExprList().size();
		if(size==0){
			filterOpChain.setIdentType(IMAGE);
		}
		else{
			throw new TypeCheckException("Expected zero expressions with filterOp chain but found  "+size+" expressions");
		}
		return filterOpChain;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		Token frameOp = frameOpChain.getFirstToken();
		frameOpChain.setKind(frameOp.kind);
		Tuple tuple =frameOpChain.getArg();
		tuple.visit(this, null);
		int size = tuple.getExprList().size();
		if (frameOp.isOneOfKinds(KW_SHOW, KW_HIDE)){		
			if( size== 0){
				frameOpChain.setIdentType(NONE);
			}
			else{
				throw new TypeCheckException("Expected zero expressions with KW_SHOW, KW_HIDE but found "+size+" expressions");
			}
		}
		else if(frameOp.isOneOfKinds(KW_XLOC,KW_YLOC)){
			if(size==0){
				frameOpChain.setIdentType(INTEGER);
			}
			else{
				throw new TypeCheckException("Expected zero expressions with KW_XLOC, KW_YLOC but found "+size+" expressions");
			}
		}
		else if(frameOp.isKind(KW_MOVE)){
			if(size==2){
				frameOpChain.setIdentType(NONE);
			}
			else{
				throw new TypeCheckException("Expected zero expressions with KW_MOVE but found "+size+" expressions");
			}
		}
		else{
			throw new TypeCheckException("found invalid frameOp "+frameOp + " bug in parser code");
		}
		return frameOpChain;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		Token ident = identChain.getFirstToken();
		
		String identifier = ident.getText();
		Dec dec = symtab.lookup(identifier);
		identChain.setDec(dec);
		if(dec!=null){
			identChain.setIdentType(dec.getIdentType());
		}
		else{
			throw new TypeCheckException(identifier+" is not declared in current scope");
		}
		
		return identChain;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		Token ident = identExpression.getFirstToken();	
		String identifier = ident.getText();
				
		
		Dec dec = symtab.lookup(identifier);
		
		if(dec!=null){
			identExpression.setDec(dec);
			identExpression.setIdentType(dec.getIdentType());
		}
		else{
			throw new TypeCheckException(identifier+" is not declared in current scope");
		}
		
		return identExpression;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		
		Expression exp = ifStatement.getE();
		exp.visit(this, null);
		
		Block blk = ifStatement.getB();
		blk.visit(this, null);
		
		TypeName expType = exp.getIdentType();
		if(! expType.isType(BOOLEAN)){
			throw new TypeCheckException("expected boolean expression but found "+expType);
		}
		return ifStatement;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		
		intLitExpression.setIdentType(INTEGER);
		return intLitExpression;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		
		Expression exp = sleepStatement.getE();
		exp.visit(this, null);
		
		TypeName expType =  exp.getIdentType();
		
		if(! expType.isType(INTEGER)){
			throw new TypeCheckException("expected INTEGER expression but found "+expType);
		}
		return sleepStatement;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		
		Expression exp = whileStatement.getE();
		exp.visit(this, null);
		
		Block blk = whileStatement.getB();
		blk.visit(this, null);
		
		TypeName expType = exp.getIdentType();
		
		if(! expType.isType(BOOLEAN)){
			throw new TypeCheckException("expected boolean expression but found "+expType);
		}
		return whileStatement;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		
		Token type = declaration.getType();
		declaration.setIdentType(Type.getTypeName(type));
		
		String identText = declaration.getIdent().getText();
		boolean isInserted = symtab.insert(identText,declaration);
		
		if(!isInserted){
			throw new TypeCheckException("declaring two identifiers of same name in same scope");
		}
		return declaration;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		ArrayList<ParamDec> paramsList = program.getParams();
		Block block = program.getB();
		symtab.enterScope();
		
		for(ParamDec param:paramsList){
			param.visit(this, null);
		}
		block.visit(this, null);
		symtab.leaveScope();
		return program;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		
		IdentLValue var = assignStatement.getVar();
		var.visit(this, null);
		
		Expression exp = assignStatement.getE();
		exp.visit(this, null);
		
		TypeName varType = var.getDec().getIdentType();
		TypeName expType = exp.getIdentType();
		
		if(!varType.equals(expType)){
			throw new TypeCheckException("IdentLvalue type and expression type did not match, they are "+varType+" "+expType);
		}
		return assignStatement;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		
		String identifier = identX.getText();
		Dec dec = symtab.lookup(identifier);
		if(dec!=null){
			identX.setDec(dec);
		}
		else{
			throw new TypeCheckException(identifier+" is not declared in current scope");
		}
		return identX;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		
		Token type = paramDec.getType();
		paramDec.setIdentType(Type.getTypeName(type));
		
		String identText = paramDec.getIdent().getText();
		boolean isInserted = symtab.insert(identText,paramDec);
		
		if(!isInserted){
			throw new TypeCheckException("declaring two identifiers of same name in same scope");
		}
		return paramDec;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		
		constantExpression.setIdentType(TypeName.INTEGER);
		return constantExpression;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		Token imageOp = imageOpChain.getFirstToken();
		imageOpChain.setKind(imageOp.kind);
		
		Tuple tuple =imageOpChain.getArg();
		tuple.visit(this, null);
		
		int size = tuple.getExprList().size();
		if (imageOp.isOneOfKinds(OP_WIDTH, OP_HEIGHT)){		
			if( size== 0){
				imageOpChain.setIdentType(INTEGER);
			}
			else{
				throw new TypeCheckException("Expected zero expressions with OP_WIDTH, OP_HEIGHT but found "+size+" expressions");
			}
		}
		else if(imageOp.isKind(KW_SCALE)){
			if(size==1){
				imageOpChain.setIdentType(IMAGE);
			}
			else{
				throw new TypeCheckException("Expected one expressions with KW_SCALE but found "+size+" expressions");
			}
		}	
		return imageOpChain;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub
		List<Expression> expList = tuple.getExprList();
		for(Expression exp: expList){			
			exp.visit(this, null);			
			if(exp.getIdentType()!=INTEGER){
				throw new TypeCheckException("Expected Integer type but found"+exp.getIdentType());
			}
		}
		return tuple;
	}


}
