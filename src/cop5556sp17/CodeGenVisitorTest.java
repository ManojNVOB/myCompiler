
package cop5556sp17;

import java.io.FileOutputStream;
import java.io.OutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.Program;

public class CodeGenVisitorTest {

	static final boolean doPrint = false;
	static void show(Object s) {
		if (doPrint) {
			System.out.println(s);
		}
	}

	boolean devel = false;
	boolean grade = true;
	

	@Test
	public void test() throws Exception {
		String input = "assignImageAndFrame url u {image i image i1 frame f frame f1\nu -> i -> f -> show; frame f2  \ni -> scale (3) -> f2 -> show; \n i1 <- i; \n f2 <- f;\n}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		//show(program);
		
		//generate code
		CodeGenVisitor cv = new CodeGenVisitor(devel,grade,null);
		byte[] bytecode = (byte[]) program.visit(cv, null);
		
		//output the generated bytecode
		//CodeGenUtils.dumpBytecode(bytecode);
		
		//write byte code to file 
		String name = ((Program) program).getName();
		String classFileName = "bin/" + name + ".class";
		OutputStream output = new FileOutputStream(classFileName);
		output.write(bytecode);
		output.close();
		//System.out.println("wrote classfile to " + classFileName);
		String file = "D:/spring 2017 courses/PLP/compiler_project/src/cop5556sp17/images/sample.jpg";
		String url = "file:///D:/spring%202017%20courses/PLP/compiler_project/src/cop5556sp17/images/sample.jpg";
		String bool = "false";
		String int1 = "1";
		// directly execute bytecode
		String[] args = {url}; //create command line argument array to initialize params, none in this case
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		instance.run();
	}
	
	@Test
	public void parameterNoBody() throws Exception {
		//scan, parse, and type check the program
		String progname = "parameterNoBody";
		String input = 		progname + " {integer x x<- 9; integer y y<- x;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		//show(program);
		
		//generate code
		CodeGenVisitor cv = new CodeGenVisitor(devel,grade,null);
		byte[] bytecode = (byte[]) program.visit(cv, null);
		
		//output the generated bytecode
		//CodeGenUtils.dumpBytecode(bytecode);
		
		//write byte code to file 
		String name = ((Program) program).getName();
		String classFileName = "bin/" + name + ".class";
		OutputStream output = new FileOutputStream(classFileName);
		output.write(bytecode);
		output.close();
		//System.out.println("wrote classfile to " + classFileName);
		
		// directly execute bytecode
		String[] args = new String[0]; //create command line argument array to initialize params, none in this case
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		instance.run();
	}
	
	@Test
	public void emptyProg() throws Exception {
		//scan, parse, and type check the program
		String progname = "emptyProg";
		String input = progname + "  {}";		
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		show(program);
		
		//generate code
		CodeGenVisitor cv = new CodeGenVisitor(devel,grade,null);
		
		byte[] bytecode = (byte[]) program.visit(cv, null);
		
		//output the generated bytecode
		CodeGenUtils.dumpBytecode(bytecode);
		
		//write byte code to file 
		String name = ((Program) program).getName();
		String classFileName = "bin/" + name + ".class";
		OutputStream output = new FileOutputStream(classFileName);
		output.write(bytecode);
		output.close();
		System.out.println("wrote classfile to " + classFileName);
		
		// directly execute bytecode
		String[] args = new String[0]; //create command line argument array to initialize params, none in this case
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		instance.run();
	}
}
