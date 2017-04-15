package cop5556sp17;

import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;

public class Parser {


	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	
	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}


	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	ASTNode parse() throws SyntaxException {
		ASTNode rootNode = program();
		matchEOF();
		return rootNode;
	}


	/**
	 *  Productions: program = IDENT block
					 program = IDENT param_dec ( , param_dec )* block
	 * @throws SyntaxException
	 */
	Program program() throws SyntaxException {
		Block block;
		ArrayList<ParamDec> list = new ArrayList();
		Token firstToken = t;
		match(IDENT);
		if(t.isKind(LBRACE)){
			block = block();
		}
		else if(oneOfKinds(KW_URL,KW_FILE,KW_INTEGER,KW_BOOLEAN )){			
			do{
				if(t.isKind(COMMA)){
					consume();
					list.add(paramDec());
				}
				else{
					list.add(paramDec());	
				}
			}while(t.isKind(COMMA));
			block = block();
		}
		else{
			throw new SyntaxException("saw " + t.kind + " expected one of KW_URL,KW_FILE,KW_INTEGER,KW_BOOLEAN");
		}
		return new Program(firstToken, list, block);
	}
	
	
	/**
	 *  Production: block = { ( dec | statement) * }
	 * @throws SyntaxException
	 */
	Block block() throws SyntaxException {
		Token firstToken = t;
		ArrayList<Dec> decList = new ArrayList();
		ArrayList<Statement> statementList = new ArrayList();
		match(LBRACE);
			while(true){
				if(oneOfKinds(OP_SLEEP,KW_WHILE,KW_IF,IDENT,OP_BLUR,OP_GRAY,OP_CONVOLVE,KW_SHOW,KW_HIDE,KW_MOVE,
						KW_XLOC,KW_YLOC,OP_WIDTH,OP_HEIGHT,KW_SCALE )){
					
					statementList.add(statement());
				}
				else if(oneOfKinds(KW_INTEGER,KW_BOOLEAN,KW_IMAGE,KW_FRAME)){
					decList.add(dec());
				}
				else{
					break;
				}
			}
		match(RBRACE);
		return new Block(firstToken, decList, statementList);
	}
	

	/**
	 *  Production: paramDec = ( KW_URL | KW_FILE | KW_INTEGER | KW_BOOLEAN ) IDENT
	 * @throws SyntaxException
	 */
	ParamDec paramDec() throws SyntaxException {
		Token type = match(KW_URL,KW_FILE,KW_INTEGER,KW_BOOLEAN);
		Token ident = match(IDENT);
		return new ParamDec(type,ident);
	}


	/**
	 *  Production: statement = OP_SLEEP expression SEMI | whileStatement | ifStatement | chain SEMI | assign SEMI
	 * @throws SyntaxException
	 */
	Statement statement() throws SyntaxException {
		Token firstToken = t;
		Statement statement=null;
		if(t.isKind(OP_SLEEP)){
			consume();
			Expression exp = expression();
			match(SEMI);
			statement = new SleepStatement(firstToken, exp);
		}
		else if(oneOfKinds(KW_WHILE,KW_IF)){
			match(KW_WHILE,KW_IF);
			match(LPAREN);
			Expression exp = expression();
			match(RPAREN);
			Block block = block();
			if(firstToken.isKind(KW_IF)){
				statement =  new IfStatement(firstToken, exp, block);
			}
			else if(firstToken.isKind(KW_WHILE)){
				statement =  new WhileStatement(firstToken, exp, block);
			}
		}
		else if(oneOfKinds(OP_BLUR, OP_GRAY,OP_CONVOLVE,KW_SHOW,KW_HIDE,KW_MOVE,KW_XLOC,KW_YLOC,OP_WIDTH,OP_HEIGHT,KW_SCALE)){
			statement = chain();
			match(SEMI);
		}
		else if(t.isKind(IDENT)){
			Token next = scanner.peek();
			if (next.isKind(ASSIGN)){
				IdentLValue ident = new IdentLValue(firstToken);
				match(IDENT);
				match(ASSIGN);
				Expression exp = expression();
				statement = new AssignmentStatement(firstToken, ident, exp);
			}
			else{
				statement = chain();
			}
			match(SEMI);
		}
		else{
			throw new SyntaxException("expected sleepOp or if or while or filterOp or frameOP or imageOp or ident but found " + t.kind);
		}
		return statement;
	}


	/**
	 *  Production: chain = chainElem arrowOp chainElem ( arrowOp chainElem)*
	 * @throws SyntaxException
	 */
	Chain chain() throws SyntaxException {
		Token firstToken = t, arrow;
		Chain currChain;
		ChainElem nextChain;
		boolean firstIter = true;
		
		currChain = chainElem();
		
		loop:while(true){
			if(firstIter){
				arrow =t;
				match(ARROW,BARARROW);
				firstIter = false;
			}
			else{
				
				if(oneOfKinds(ARROW,BARARROW)){
					arrow = t;
					consume();
				}
				else{
					break loop;
				}
				
			}
			nextChain = chainElem();
			currChain = new BinaryChain(firstToken, currChain, arrow, nextChain);
		}
		return currChain;
	}


	/**
	 *  Production: chainElem = IDENT | filterOp arg | frameOp arg | imageOp arg
	 * @throws SyntaxException
	 */
	ChainElem chainElem() throws SyntaxException {
		Token firstToken = t;
		ChainElem chainElem;
		Tuple arg;
		if(t.isKind(IDENT)){
			chainElem = new IdentChain(firstToken);
			consume();
		}
		else if(oneOfKinds(OP_BLUR,OP_GRAY,OP_CONVOLVE)){
			consume();
			arg = arg();
			chainElem = new FilterOpChain(firstToken, arg);
		}
		else if(oneOfKinds(KW_SHOW,KW_HIDE,KW_MOVE,KW_XLOC,KW_YLOC)){
			consume();
			arg = arg();
			chainElem = new FrameOpChain(firstToken, arg);
		}
		else if(oneOfKinds(OP_WIDTH,OP_HEIGHT,KW_SCALE)){
			consume();
			arg = arg();
			chainElem = new ImageOpChain(firstToken, arg);
		}		
		else throw new SyntaxException("expected frameOps or Ident or ImageOps but found " + t.kind);
		return chainElem;
	}


	/**
	 *  Production: arg = epsilon | ( expression ( , expression)* )
	 * @throws SyntaxException
	 */
	Tuple arg() throws SyntaxException {
		Token firstToken = t;
		List<Expression> args = new ArrayList();
		Expression exp;
		if(t.isKind(LPAREN)){
			do{
				consume();
				exp = expression();
				args.add(exp);
			}while(t.isKind(COMMA));
			match(RPAREN);
		}
		return new Tuple(firstToken,args);
	}
	

	/**
	 * Production: expression = term ( relOp term)*
	 * @throws SyntaxException
	 */
	Expression expression() throws SyntaxException {
		Token firstToken=t, relOP;
		Expression prevExp, currExp, nextExp;
		currExp = term();
		prevExp = currExp;
		while(true){
			if(oneOfKinds(GE,GT,LE,LT,EQUAL,NOTEQUAL)){
				relOP = t;
				consume();
				nextExp = term();
				currExp =  new BinaryExpression(firstToken, prevExp, relOP, nextExp);
				prevExp =currExp;
			}
			else{
				break;
			}
		}
		return currExp;
	}


	/**
	 *  Production: term = elem ( weakOp elem)*
	 * @throws SyntaxException
	 */
	Expression term() throws SyntaxException {
		Token firstToken=t, weakOp;
		Expression currExp, prevExp, nextExp;
		currExp = elem();
		prevExp = currExp;
		while(true){
			if(oneOfKinds(PLUS,MINUS,OR)){
				weakOp =t;
				consume();
				nextExp = elem();
				currExp = new BinaryExpression(firstToken, prevExp, weakOp, nextExp);
				prevExp = currExp;
			}
			else{
				break;
			}
		}
		return currExp;
	}
	
	
	/**
	 *  Production: elem = factor ( strongOp factor)*
	 * @throws SyntaxException
	 */
	Expression elem() throws SyntaxException {
		Token firstToken=t, strongOp;
		Expression currExp, prevExp, nextExp;
		currExp = factor();
		prevExp  = currExp;
		while(true){
			if(oneOfKinds(TIMES,DIV,MOD,AND)){
				strongOp =t;
				consume();
				nextExp =  factor();
				currExp = new BinaryExpression(firstToken, prevExp, strongOp, nextExp);
				prevExp = currExp;
			}
			else{
				break;
			}
		}
		return currExp;
	}

	
	/**
	 *  Production: factor = IDENT | INT_LIT | KW_TRUE | KW_FALSE| KW_SCREENWIDTH | KW_SCREENHEIGHT | ( expression )
	 * @throws SyntaxException
	 */
	Expression factor() throws SyntaxException {
		Token firstToken = t;
		Expression exp = null;
		//Token previous = match(IDENT,INT_LIT,KW_TRUE,KW_FALSE,KW_SCREENWIDTH,KW_SCREENHEIGHT,LPAREN);
		switch (t.kind) {
		case IDENT: 
			exp = new IdentExpression(firstToken);
			consume();
			break;
		case INT_LIT: 
			try {
				exp = new IntLitExpression(firstToken);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalNumberException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			consume();
			break;
		case KW_TRUE:
		case KW_FALSE: 
			exp = new BooleanLitExpression(firstToken);
			consume();
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: 
			exp = new ConstantExpression(firstToken);
			consume();
			break;
		case LPAREN: 
			consume();
			exp = expression();
			match(RPAREN);
			break;
		default:
			throw new SyntaxException("expected one of IDENT, INT_LIT, KW_TRUE, KW_FALSE, KW_SCREENWIDTH, KW_SCREENHEIGHT, LPAREN but found " + t.kind);
		}
		return exp;
	}

	/**
	 *  Production: dec = ( KW_INTEGER | KW_BOOLEAN | KW_IMAGE | KW_FRAME ) IDENT
	 * @throws SyntaxException
	 */
	Dec dec() throws SyntaxException {
		Token type = match(KW_IMAGE,KW_FRAME,KW_INTEGER,KW_BOOLEAN);
		Token ident = match(IDENT);
		return new Dec(type,ident);
	}
	

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	
	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + " expected " + kind);
	}

	
	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {		
		for(Kind kind:kinds){
			if(t.isKind(kind)){
				return consume();
			}			
		}
		throw new SyntaxException("saw " + t.kind + " expected one of " + Arrays.toString(kinds));
	}


	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * function return true or else false.
	 *  @param kinds
	 *           list of kinds, matches any one
	 * @return
	 */
	private boolean oneOfKinds(Kind... kinds){
		for(Kind kind:kinds){
			if(t.isKind(kind)){
				return true;
			}			
		} 
		return false;
	}
	

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
