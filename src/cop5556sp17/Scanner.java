package cop5556sp17;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import cop5556sp17.Scanner.Kind;

public class Scanner {

	//pos: the position of the next character to be processed
	int pos = 0; 
		
	// create state enum object with its state as START
	State state = State.START;
	
	//line position of the current token
	int linePosition = 0;
	// Position of current character being processed. Initially no character is being processed. so, intitialized to -1. 
	int currCharPosition = -1;
	
	//start position of token
	int startPosition = 0;
	
	LinePos posInLine;
	
	int tokenLength=0;
	
	//current character that is being processed.
	char currChar;
	
	/**
	 * Kind enum
	 */	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}
	
	//Deterministic Finite Automaton States
	private enum State {
		START,
		GOTO_DIV,
		GOTO_DIGIT,
		GOTO_ID_START,
		GOTO_EQUAL,
		GOTO_NOT,
		GOTO_OR,
		GOTO_BARARROW,
		GOTO_LESS,
		GOTO_GREATER,
		GOTO_MINUS,
		EOF
	}
/**
 * Thrown by Scanner when an illegal character is encountered
 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	
	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}
		

	public class Token {
		public final Kind kind;
		//store line number of the token and first character position in line 
		public final LinePos tokenPosition;
		public final int pos;  //position in input string
		public final int length;
		
		@Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }

		 

		  private Scanner getOuterType() {
		   return Scanner.this;
		  }
		
		//returns the text of this Token
		public String getText() {
			if(kind == Kind.EOF){
				return "eof";	
			}else{
				String tokenString = chars.substring(pos, pos+length);
				return tokenString;
			}
			
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			return tokenPosition;
		}

		Token(Kind kind, int pos, int length, LinePos tokenPosition) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.tokenPosition = tokenPosition;
		}

		public boolean exceedsLength(){
			if(length>10){
				return true;
			}
			else{
				return false;
			}
			 
		}

		
		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 * @throws IllegalNumberException 
		 */
		public int intVal() throws NumberFormatException, IllegalNumberException{
			if(kind != Kind.INT_LIT)
				throw new NumberFormatException();
			long number = Long.parseLong(chars.substring(pos, pos+length));
			if(exceedsLength() || number > Integer.MAX_VALUE)
				throw new IllegalNumberException("number is out of integer range");
			// type cast the number to integer
			return (int) number;
		}
		
		public boolean isKind(Kind kind) {
			if(this.kind == kind ){
				return true;
				}
				else{
					return false;
				}
			}
		
		public boolean isOneOfKinds(Kind... kinds){
			for(Kind kind:kinds){
				if(this.kind == kind ){
					return true;
				}			
			} 
			return false;
		}
		
		}
	


	 
		Scanner(String chars) {
			this.chars = chars;
			tokens = new ArrayList<Token>();
		}
	
	private char getChar(int pos){
		
		if(pos < chars.length()){
			return chars.charAt(pos);
		}else{
			return (char)-1;
		}				
	}

	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	/**
	 * @return
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	/**
	 * @return
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		
		Token t = null;
		pos = 0;
		state = State.START;
		initializeKeywords();
		do{
		//perform next
			t = nextTokenExtracted();
			tokens.add(t);		
		}while(t.kind!=Kind.EOF);
		
		return this;		
		}

	public Token nextTokenExtracted() throws IllegalCharException, IllegalNumberException {
		Token token =null;
		int length = chars.length();
		//State transitions happen in the while loop processing the tokens
		while(token==null){			
			currChar = getChar(pos);
			
			switch(state){
				case START:
					currCharPosition++;
					int numWhiteSpace = whiteSpaceCount(pos);
					pos += numWhiteSpace;
					currCharPosition += numWhiteSpace;
					currChar = pos < length ? chars.charAt(pos): (char) -1;
					startPosition = pos; 
					switch(currChar){
						case (char)-1:
						    state = State.EOF;
							break;
						case '\n':
							linePosition++;
							currCharPosition = -1;
							pos++;
							break;
						case '\r':
							pos++;
							break;
						case '!':
							state = State.GOTO_NOT;
							pos++;
							break;
						case '&':
							posInLine = new LinePos(linePosition, currCharPosition);
							token = new Token(Kind.AND, startPosition, 1,posInLine );
							pos++;
							break;

						case ',':
							posInLine = new LinePos(linePosition, currCharPosition);
							token = new Token(Kind.COMMA, startPosition, 1,posInLine );
							pos++;
							break;
						case ';':
							posInLine = new LinePos(linePosition, currCharPosition);
							token = new Token(Kind.SEMI, startPosition, 1,posInLine );
							pos++;
							break;
						case '{':
							posInLine = new LinePos(linePosition, currCharPosition);
							token = new Token(Kind.LBRACE, startPosition, 1,posInLine );
							pos++;
							break;
						case '}':
							posInLine = new LinePos(linePosition, currCharPosition);
							token = new Token(Kind.RBRACE, startPosition, 1,posInLine );
							pos++;
							break;
						case '(':
							posInLine = new LinePos(linePosition, currCharPosition);
							token = new Token(Kind.LPAREN, startPosition, 1,posInLine );
							pos++;
							break;
						case ')':
							posInLine = new LinePos(linePosition, currCharPosition);
							token = new Token(Kind.RPAREN, startPosition, 1,posInLine );
							pos++;
							break;
						case '|':
							state = State.GOTO_OR;
							pos++;
							break;
						case '>':
							state = State.GOTO_GREATER;
							pos++;
							break;
						case '<':
							state = State.GOTO_LESS;
							pos++;
							break;
						case '=':
							state = State.GOTO_EQUAL;
							pos++;
							break;
						case '-':
							state = State.GOTO_MINUS;
							pos++;
							break;
						case '%':
							posInLine = new LinePos(linePosition, currCharPosition);
							token = new Token(Kind.MOD, startPosition, 1,posInLine );
							pos++;
							break;
						case '+':
							posInLine = new LinePos(linePosition, currCharPosition);
							token = new Token(Kind.PLUS, startPosition, 1,posInLine );
							pos++;
							break;
						case '*':
							posInLine = new LinePos(linePosition, currCharPosition);
							token = new Token(Kind.TIMES, startPosition, 1,posInLine );
							pos++;
							break;
						case '/':
							state = State.GOTO_DIV;
							pos++;
							break;

						case '0':
							posInLine = new LinePos(linePosition, currCharPosition);
							token = new Token(Kind.INT_LIT, startPosition, 1,posInLine );
							pos++;
							break;
						default:
							if(Character.isDigit(currChar)){
								state = State.GOTO_DIGIT;
								pos++;
							}
							else if(Character.isJavaIdentifierStart(currChar)){
								state = State.GOTO_ID_START;
								pos++;
							}
							else{
								throw new IllegalCharException("illegal character ");
							}
					}
					break;
				case EOF:
					 token = new Token(Kind.EOF,pos,0, new LinePos(linePosition, currCharPosition));
					 break;
				case GOTO_DIV:
					if(currChar == '*'){
						pos++;
						currCharPosition++;
						int commentChars;
						int cntNewLines = -1;
						do{
							cntNewLines++;
							commentChars = getCommentCharsCount(pos);
							pos += commentChars;
						}while(chars.charAt(pos-1) == '\n');
						linePosition += cntNewLines;
						if(cntNewLines == 0)
							currCharPosition += (commentChars);
						else
							currCharPosition = (commentChars);
					}
					else{
						posInLine = new LinePos(linePosition, currCharPosition);
						token = new Token(Kind.DIV, startPosition, 1,posInLine );
						//pos++;
					}
					state = State.START;
					break;
					
				case GOTO_DIGIT:
					if(Character.isDigit(currChar)){
						pos++;
						currCharPosition++;
					}
					else{
						tokenLength = pos-startPosition;
						posInLine = new LinePos(linePosition, currCharPosition-tokenLength+1);
						token = new Token(Kind.INT_LIT, startPosition, tokenLength, posInLine );
						token.intVal();
						state = State.START;
					}
					break;
				case GOTO_ID_START:
					
					if(Character.isJavaIdentifierPart(currChar)){
						pos++;
						currCharPosition++;
					}
					else{
						Kind kind = getKind(chars.substring(startPosition, pos));
						tokenLength = pos-startPosition;
						posInLine = new LinePos(linePosition, currCharPosition-tokenLength+1);
						token = new Token(kind, startPosition, tokenLength, posInLine );
						state = State.START;
					}					
					break;
				case GOTO_EQUAL:
					if(currChar == '='){
						tokenLength = pos-startPosition;
						posInLine = new LinePos(linePosition, currCharPosition-tokenLength+1);
						token = new Token(Kind.EQUAL, startPosition, tokenLength+1, posInLine );
						pos++;
						currCharPosition++;
						state = State.START;
					}
					else{
						throw new IllegalCharException("illegal operator");
					}
					break;
				case GOTO_NOT:
					if(currChar == '='){
						tokenLength = pos-startPosition;
						posInLine = new LinePos(linePosition, currCharPosition-tokenLength+1);
						token = new Token(Kind.NOTEQUAL, startPosition, tokenLength, posInLine );
						pos++;
						currCharPosition++;
					}
					else{
						posInLine = new LinePos(linePosition, currCharPosition);
						token = new Token(Kind.NOT, startPosition, 1, posInLine );
					}
					state = State.START;
					break;
				case GOTO_OR:
					if(currChar == '-'){
						state = State.GOTO_BARARROW;
						pos++;
						currCharPosition++;
					}
					else{
						posInLine = new LinePos(linePosition, currCharPosition);
						token = new Token(Kind.OR, startPosition, 1, posInLine );
						state = State.START;
					}
					break;
				case GOTO_BARARROW:
					if(currChar == '>'){
						tokenLength = pos-startPosition;
						posInLine = new LinePos(linePosition, currCharPosition-tokenLength+1);
						token = new Token(Kind.BARARROW, startPosition, tokenLength+1, posInLine );
						pos++;
						currCharPosition++;
					}
					else{
						tokenLength  = 1;
						posInLine = new LinePos(linePosition, currCharPosition-tokenLength);
						token = new Token(Kind.OR,startPosition,tokenLength,posInLine );
						pos--;
						currCharPosition--;
					}
					state = State.START;
					break;

				case GOTO_GREATER:
					if(currChar == '='){
						tokenLength = pos-startPosition;
						posInLine = new LinePos(linePosition, currCharPosition-tokenLength+1);
						token = new Token(Kind.GE, startPosition, tokenLength+1, posInLine );
						pos++;
						currCharPosition++;
					}
					else{
						posInLine = new LinePos(linePosition, currCharPosition);
						token = new Token(Kind.GT, startPosition, 1, posInLine );
					}
					state = State.START;
					break;
				case GOTO_LESS:
					if(currChar == '='){
						tokenLength = pos-startPosition;
						posInLine = new LinePos(linePosition, currCharPosition-tokenLength+1);
						token = new Token(Kind.LE, startPosition, tokenLength+1, posInLine );
						pos++;
						currCharPosition++;
					}
					else if(currChar == '-'){
						tokenLength = pos-startPosition;
						posInLine = new LinePos(linePosition, currCharPosition-tokenLength+1);
						token = new Token(Kind.ASSIGN, startPosition, tokenLength+1, posInLine );
						pos++;
						currCharPosition++;
					}
					else{
						posInLine = new LinePos(linePosition, currCharPosition);
						token = new Token(Kind.LT, startPosition, 1, posInLine );
					}
					state = State.START;
					break;
				case GOTO_MINUS:
					if(currChar == '>'){
						tokenLength = pos-startPosition;
						posInLine = new LinePos(linePosition, currCharPosition-tokenLength+1);
						token = new Token(Kind.ARROW, startPosition, tokenLength+1, posInLine );
						pos++;
						currCharPosition++;
					}
					else{
						posInLine = new LinePos(linePosition, currCharPosition);
						token = new Token(Kind.MINUS, startPosition, 1, posInLine );
					}
					state = State.START;
					break;		
				
			}
		}
		return token;
	}
	

	private int getCommentCharsCount(int currLoc) {
		if(currLoc>chars.length()){
			System.out.println("invalid:current char location exceeds input length");
			System.exit(1);
		}
		else{
		char currChar = chars.charAt(currLoc);
		int startLoc = currLoc;
		while(currChar  != (char)-1){
			if(currChar=='/'){
				if( currLoc!=startLoc && chars.charAt(currLoc-1) == '*'  ){
					currLoc++;
					return currLoc - startLoc;
				}
				else{
					currLoc++;
				}
			}
			else if(currChar=='\n'){
				currLoc++;
				return currLoc - startLoc;
			}
			else{
				currLoc++;
			}
			if(currLoc<chars.length()){
				currChar = chars.charAt(currLoc);
			}
			else{
				break;
			}
		}
		return currLoc - startLoc;
		}
		return -1;
	}
	
		
	// hash map to store all the keywords
	Map<String, Kind> keywordMap;
	//store all the keywords in the keywordMap
	private void initializeKeywords(){
		
		keywordMap = new HashMap<String, Kind>();
		keywordMap.put("integer", Kind.KW_INTEGER);
		keywordMap.put("boolean", Kind.KW_BOOLEAN );
		keywordMap.put("show", Kind.KW_SHOW);
		keywordMap.put("file", Kind.KW_FILE);
		keywordMap.put("frame", Kind.KW_FRAME);
		keywordMap.put("if", Kind.KW_IF);
		keywordMap.put("hide", Kind.KW_HIDE);
		keywordMap.put("move", Kind.KW_MOVE);
		keywordMap.put("true", Kind.KW_TRUE);
		keywordMap.put("false", Kind.KW_FALSE);
		keywordMap.put("url", Kind.KW_URL);
		keywordMap.put("while", Kind.KW_WHILE);
		keywordMap.put("blur", Kind.OP_BLUR);
		keywordMap.put("convolve", Kind.OP_CONVOLVE);
		keywordMap.put("gray", Kind.OP_GRAY);
		keywordMap.put("height", Kind.OP_HEIGHT);
		keywordMap.put("image", Kind.KW_IMAGE);
		keywordMap.put("sleep", Kind.OP_SLEEP);
		keywordMap.put("width", Kind.OP_WIDTH);
		keywordMap.put("screenheight", Kind.KW_SCREENHEIGHT);
		keywordMap.put("screenwidth", Kind.KW_SCREENWIDTH);
		keywordMap.put("xloc", Kind.KW_XLOC);
		keywordMap.put("yloc", Kind.KW_YLOC);
		keywordMap.put("scale", Kind.KW_SCALE);


	}
	
	 Kind getKind(String keyword) {
		Kind kind = keywordMap.get(keyword);
		if(kind != null){
			return kind;
		}	
		else{
			return Kind.IDENT;
		}			
	}


	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}
	

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		return t.tokenPosition;
	}
	
	// get the count of wihte spaces
	private int whiteSpaceCount(int start) {
		int end = start;
		int spaceCount = 0;
		while(end < chars.length() && Character.isWhitespace(chars.charAt(end)) && chars.charAt(end) != '\n' )
			end++;
		spaceCount = end-start; 
		return spaceCount;
	}


}
