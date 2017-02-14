package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cop5556sp17.Scanner.Token;

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
	void parse() throws SyntaxException {
		program();
		matchEOF();
		return;
	}


	/**
	 *  Productions: program = IDENT block
					 program = IDENT param_dec ( , param_dec )* block
	 * @throws SyntaxException
	 */
	void program() throws SyntaxException {
		match(IDENT);
		if(t.isKind(LBRACE)){
			block();
		}
		else if(oneOfKinds(KW_URL,KW_FILE,KW_INTEGER,KW_BOOLEAN )){
			do{
				if(t.isKind(COMMA)){
					consume();
					paramDec();
				}
				else{
					paramDec();	
				}
			}while(t.isKind(COMMA));
			block();
		}
		else{
			throw new SyntaxException("saw " + t.kind + " expected one of KW_URL,KW_FILE,KW_INTEGER,KW_BOOLEAN");
		}
	}
	
	
	/**
	 *  Production: block = { ( dec | statement) * }
	 * @throws SyntaxException
	 */
	void block() throws SyntaxException {
		match(LBRACE);
			while(true){
				if(oneOfKinds(OP_SLEEP,KW_WHILE,KW_IF,IDENT,OP_BLUR,OP_GRAY,OP_CONVOLVE,KW_SHOW,KW_HIDE,KW_MOVE,
						KW_XLOC,KW_YLOC,OP_WIDTH,OP_HEIGHT,KW_SCALE )){
					statement();
				}
				else if(oneOfKinds(KW_INTEGER,KW_BOOLEAN,KW_IMAGE,KW_FRAME)){
					dec();
				}
				else{
					break;
				}
			}
		match(RBRACE);
	}
	

	/**
	 *  Production: paramDec = ( KW_URL | KW_FILE | KW_INTEGER | KW_BOOLEAN ) IDENT
	 * @throws SyntaxException
	 */
	void paramDec() throws SyntaxException {
		match(KW_URL,KW_FILE,KW_INTEGER,KW_BOOLEAN);
		match(IDENT);		
	}


	/**
	 *  Production: statement = OP_SLEEP expression SEMI | whileStatement | ifStatement | chain SEMI | assign SEMI
	 * @throws SyntaxException
	 */
	void statement() throws SyntaxException {
		if(t.isKind(OP_SLEEP)){
			consume();
			expression();
			match(SEMI);
		}
		else if(oneOfKinds(KW_WHILE,KW_IF)){
			match(KW_WHILE,KW_IF);
			match(LPAREN);
			expression();
			match(RPAREN);
			block();
		}
		else if(oneOfKinds(OP_BLUR, OP_GRAY,OP_CONVOLVE,KW_SHOW,KW_HIDE,KW_MOVE,KW_XLOC,KW_YLOC,OP_WIDTH,OP_HEIGHT,KW_SCALE)){
			chain();
			match(SEMI);
		}
		else if(t.isKind(IDENT)){
			Token next = scanner.peek();
			if (next.isKind(ASSIGN)){
				match(IDENT);
				match(ASSIGN);
				expression();
			}
			else{
				chain();
			}
			match(SEMI);
		}
	}


	/**
	 *  Production: chain = chainElem arrowOp chainElem ( arrowOp chainElem)*
	 * @throws SyntaxException
	 */
	void chain() throws SyntaxException {
		boolean firstIter = true;
		chainElem();		
		loop:while(true){
			if(firstIter){
				match(ARROW,BARARROW);
				firstIter = false;
			}
			else{
				
				if(oneOfKinds(ARROW,BARARROW)){
					consume();
				}
				else{
					break loop;
				}
				
			}
			chainElem();
		}
	}


	/**
	 *  Production: chainElem = IDENT | filterOp arg | frameOp arg | imageOp arg
	 * @throws SyntaxException
	 */
	void chainElem() throws SyntaxException {
		if(t.isKind(IDENT)){
			consume();
		}
		else{
			match(OP_BLUR,OP_GRAY,OP_CONVOLVE,KW_SHOW,KW_HIDE,KW_MOVE,KW_XLOC,KW_YLOC,OP_WIDTH,OP_HEIGHT,KW_SCALE);
			arg();
		}
	}


	/**
	 *  Production: arg = epsilon | ( expression ( , expression)* )
	 * @throws SyntaxException
	 */
	void arg() throws SyntaxException {
		if(t.isKind(LPAREN)){
			do{
				consume();
				expression();
			}while(t.isKind(COMMA));
			match(RPAREN);
		}
	}
	

	/**
	 * Production: expression = term ( relOp term)*
	 * @throws SyntaxException
	 */
	void expression() throws SyntaxException {		
		term();
		while(true){
			if(oneOfKinds(GE,GT,LE,LT,EQUAL,NOTEQUAL)){
				consume();
				term();
			}
			else{
				break;
			}
		}
	}


	/**
	 *  Production: term = elem ( weakOp elem)*
	 * @throws SyntaxException
	 */
	void term() throws SyntaxException {
		elem();
		while(true){
			if(oneOfKinds(PLUS,MINUS,OR)){
				consume();
				elem();
			}
			else{
				break;
			}
		}
	}
	
	
	/**
	 *  Production: elem = factor ( strongOp factor)*
	 * @throws SyntaxException
	 */
	void elem() throws SyntaxException {
		factor();
		while(true){
			if(oneOfKinds(TIMES,DIV,MOD,AND)){
				consume();
				elem();
			}
			else{
				break;
			}
		}
	}

	
	/**
	 *  Production: factor = IDENT | INT_LIT | KW_TRUE | KW_FALSE| KW_SCREENWIDTH | KW_SCREENHEIGHT | ( expression )
	 * @throws SyntaxException
	 */
	void factor() throws SyntaxException {
		Kind kind = t.kind;
		Token previous = match(IDENT,INT_LIT,KW_TRUE,KW_FALSE,KW_SCREENWIDTH,KW_SCREENHEIGHT,LPAREN);
		if(previous.isKind(LPAREN)){
			expression();
			match(RPAREN);
		}
	}

	/**
	 *  Production: dec = ( KW_INTEGER | KW_BOOLEAN | KW_IMAGE | KW_FRAME ) IDENT
	 * @throws SyntaxException
	 */
	void dec() throws SyntaxException {
		match(KW_IMAGE,KW_FRAME,KW_INTEGER,KW_BOOLEAN);
		match(IDENT);
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
