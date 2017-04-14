/**  Important to test the error cases in case the
 * AST is not being completely traversed.
 * 
 * Only need to test syntactically correct programs, or
 * program fragments.
 */

package cop5556sp17;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.Statement;
import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;

public class TypeCheckVisitorTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testAssignmentBoolLit0() throws Exception{
		String input = "p {\nboolean y \ny <- false;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
	}

	@Test
	public void testAssignmentBoolLitError0() throws Exception{
		String input = "p {\nboolean y \ny <- 3;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		program.visit(v, null);		
	}		

	
	@Test
	public void testBinaryExpression() throws Exception{
		String input = " main integer i, integer j { image x integer y x<-x*y; i>=j;  } ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		
	}
	
    @Test
public void testBinaryChain() throws Exception{
    	String input = " a url u, file f {integer x x<-1;image img u -> img;  f -> img; while(x>=1){ img -> scale(20);}}";
    	Scanner scanner = new Scanner(input);
    	scanner.scan();
    	Parser parser = new Parser(scanner);
    	ASTNode program = parser.parse();
    	TypeCheckVisitor v = new TypeCheckVisitor();
    	program.visit(v, null);
}
    
@Test
public void testBinaryChain1() throws Exception{
    	//String input = "main { image a frame b a -> b ;}";
    	String input = "p url y {frame x  boolean z \nz <- x == y;}";
    	
    	
    	Scanner scanner = new Scanner(input);
    	scanner.scan();
    	Parser parser = new Parser(scanner);
    	ASTNode program = parser.parse();
    	TypeCheckVisitor v = new TypeCheckVisitor();
    	program.visit(v, null);
}
	

}