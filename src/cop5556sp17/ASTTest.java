package cop5556sp17;

import static cop5556sp17.Scanner.Kind.*;
import static org.junit.Assert.assertEquals;

import java.awt.Frame;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
//import cop5556sp17.AST.ASTNode;
//import cop5556sp17.AST.BinaryExpression;
//import cop5556sp17.AST.IdentExpression;
//import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.*;

public class ASTTest {

	static final boolean doPrint = true;
	static void show(Object s){
		if(doPrint){System.out.println(s);}
	}
	

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testFactor0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IdentExpression.class, ast.getClass());
	}

	@Test
	public void testFactor1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "123";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IntLitExpression.class, ast.getClass());
	}
	
	@Test
	public void testFactor2() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "true";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BooleanLitExpression.class, ast.getClass());
	}

	@Test
	public void testBinaryExpr0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "1 >= screenheight";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		assertEquals(IntLitExpression.class, be.getE0().getClass());
		assertEquals(ConstantExpression.class, be.getE1().getClass());
		assertEquals(GE, be.getOp().kind);
	}
	
	@Test
	public void testStatement0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "sleep 100;";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(SleepStatement.class, ast.getClass());
	}
	
	@Test
	public void testStatement1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "while(x > 100){}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		WhileStatement wst = (WhileStatement) ast;
		assertEquals(BinaryExpression.class, wst.getE().getClass());
		assertEquals(Block.class, wst.getB().getClass());
	}

	@Test
	public void testStatement2() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = " convolve -> width(10) |-> move(100,200);";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		BinaryChain be = (BinaryChain) ast;
		assertEquals(BinaryChain.class, be.getE0().getClass());
		assertEquals(FrameOpChain.class, be.getE1().getClass());
		assertEquals(BARARROW, be.getArrow().kind);
		
		BinaryChain be_sub = (BinaryChain)be.getE0();
		assertEquals(FilterOpChain.class, be_sub.getE0().getClass());
		assertEquals(ImageOpChain.class, be_sub.getE1().getClass());
		assertEquals(ARROW, be_sub.getArrow().kind);
		
	}
	
	@Test
	public void testAssign0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = " x <- x*y;";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		AssignmentStatement asst = (AssignmentStatement) ast;
		assertEquals(IdentLValue.class, asst.getVar().getClass());
		assertEquals(BinaryExpression.class, asst.getE().getClass());
	}	
	
	@Test
	public void testDec() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = " x { integer x }";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.program();
		assertEquals(Program.class, ast.getClass());
	}	
	
	@Test
	public void testBlock() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = " {integer x x <- x*y;convolve -> width(10) |-> move(100,200);}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.block();
		Block b = (Block) ast;
		assertEquals(Block.class, ast.getClass());
		assertEquals(1, b.getDecs().size());
		assertEquals(2, b.getStatements().size());
		
	}	
	
	@Test
	public void testParamDec() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = " file  f";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.paramDec();
		assertEquals(ParamDec.class, ast.getClass());
	}
	
	@Test
	public void testProgram0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = " main {integer x x <- x*y;convolve -> width(10) |-> move(100,200);}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.program();
		assertEquals(Program.class, ast.getClass());
		Program p = (Program) ast ;
		BinaryChain bc = (BinaryChain)p.getB().getStatements().get(1);
		FrameOpChain f = (FrameOpChain) bc.getE1();
		assertEquals(2, f.getArg().getExprList().size());
	}
	
	@Test
	public void testProgram1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = " main integer i, boolean b, url u{integer x x <- x*y;convolve -> width(10) |-> move(100,200);}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.parse();
		Program p = (Program) ast;
		assertEquals(Program.class, ast.getClass());
		assertEquals(3, p.getParams().size());
		assertEquals(1, p.getB().getDecs().size());
		assertEquals(2, p.getB().getStatements().size());
	}
	
}
