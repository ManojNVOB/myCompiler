package cop5556sp17;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;


public class ParserTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testFactor() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		for(Scanner.Token t:scanner.tokens){
			System.out.println(t.getText());
		}
		Parser parser = new Parser(scanner);
		parser.factor();
	}

	@Test
	public void testArg() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "  (3,5) ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		for(Scanner.Token t:scanner.tokens){
			System.out.println(t.getText());
		}
		Parser parser = new Parser(scanner);
        parser.arg();
	}

	@Test
	public void testArgerror() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "  (3,) ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		thrown.expect(Parser.SyntaxException.class);
		parser.arg();
	}


	@Test
	public void testProgram0() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "prog0 {}";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}

	@Test
	public void testParamDecRec() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = " main integer x,integer y,integer z{}";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}
	
	@Test
	public void testBarArrowBlur() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = " A{x->blur;}";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}
	
	@Test
	public void testWhile() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = " abc{ while(a<b<c ){a<-b;}}";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}
	
	@Test
	public void testOpSleep() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = " a { sleep (a);}";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}
	
	@Test
	public void testChain() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "a->a->a";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.chain();
	}
	
	@Test
	public void testArgRecur() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "(a,a,a)";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.arg();
	}

}
