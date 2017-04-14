package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import cop5556sp17.AST.Type.TypeName;
import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int argIndex=0;
	int runLocArrSlot;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params)
			dec.visit(this, mv);
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		
		runLocArrSlot =1;
		
		Block blk = program.getB();
		blk.visit(this, null);
		mv.visitInsn(RETURN);
		
		Label endRun = new Label();
		mv.visitLabel(endRun);
		
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		
		blk.setStartLabel(startRun);
		blk.setEndLabel(endRun);
		visitBlockLocals(blk,startRun,endRun);
		
//TODO  visit the local variables
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method
		
		
		cw.visitEnd();//end of class
		
		//generate classfile and return it
		return cw.toByteArray();
	}

	public void visitBlockLocals(Block blk,Label start, Label end){
		Block stmtBlk = null;
		Label tempStart,tempEnd;
		String ident;
		List<Dec> decs = blk.getDecs();
		List<Statement> stmts = blk.getStatements();
		for(Dec dec:decs){
			ident = dec.getIdent().getText();
			mv.visitLocalVariable(ident, classDesc, null, start, end, dec.getSlot());
		}
		for(Statement stmt : stmts){
			if(stmt instanceof IfStatement){
				stmtBlk = ((IfStatement) stmt).getB();
				tempStart = stmtBlk.getStartLabel();
				tempEnd = stmtBlk.getEndLabel();
				visitBlockLocals(stmtBlk,start,end);
			}
			else if(stmt instanceof WhileStatement){
				stmtBlk = ((WhileStatement) stmt).getB();
				tempEnd = stmtBlk.getStartLabel();
				tempEnd = stmtBlk.getEndLabel();
				visitBlockLocals(stmtBlk,start,end);
			}

		}
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		mv.visitVarInsn(ALOAD, 0);
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getIdentType());
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		
		Token operator = binaryExpression.getOp();
		Expression exp0 = binaryExpression.getE0();		
		Expression exp1 = binaryExpression.getE1();
		Label goIn, goOut;
		exp0.visit(this, arg);
		exp1.visit(this, arg);
		
		if(operator.kind==PLUS){
			mv.visitInsn(IADD);
		}
		else if(operator.kind==MINUS){
			mv.visitInsn(ISUB);
		}
		else if(operator.kind==TIMES){
			mv.visitInsn(IMUL);
		}
		else if(operator.kind==MOD){
			mv.visitInsn(IREM);
		}
		else if(operator.kind==OR){
			mv.visitInsn(IOR);
		}
		else if(operator.kind==AND){
			mv.visitInsn(IAND);
		}
		else if(operator.kind==EQUAL){
			goIn = new Label();
			mv.visitJumpInsn(IF_ICMPNE, goIn);
			mv.visitInsn(ICONST_1);
			goOut = new Label();
			mv.visitJumpInsn(GOTO, goOut);
			mv.visitLabel(goIn);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(goOut);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
		}
		else if(operator.kind==NOTEQUAL){
			goIn = new Label();
			mv.visitJumpInsn(IF_ICMPEQ, goIn);
			mv.visitInsn(ICONST_1);
			goOut = new Label();
			mv.visitJumpInsn(GOTO, goOut);
			mv.visitLabel(goIn);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(goOut);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
		}
		else if(operator.kind==LT){
			goIn = new Label();
			mv.visitJumpInsn(IF_ICMPGE, goIn);
			mv.visitInsn(ICONST_1);
			goOut = new Label();
			mv.visitJumpInsn(GOTO, goOut);
			mv.visitLabel(goIn);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(goOut);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
		}
		else if(operator.kind==GT){
			goIn = new Label();
			mv.visitJumpInsn(IF_ICMPLE, goIn);
			mv.visitInsn(ICONST_1);
			goOut = new Label();
			mv.visitJumpInsn(GOTO, goOut);
			mv.visitLabel(goIn);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(goOut);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
		}
		else if(operator.kind==LE){
			goIn = new Label();
			mv.visitJumpInsn(IF_ICMPGT, goIn);
			mv.visitInsn(ICONST_1);
			goOut = new Label();
			mv.visitJumpInsn(GOTO, goOut);
			mv.visitLabel(goIn);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(goOut);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
		}
		else if(operator.kind==GE){
			goIn = new Label();
			mv.visitJumpInsn(IF_ICMPLT, goIn);
			mv.visitInsn(ICONST_1);
			goOut = new Label();
			mv.visitJumpInsn(GOTO, goOut);
			mv.visitLabel(goIn);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(goOut);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
		}

		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//TODO  Implement this
		ArrayList<Dec> decs = block.getDecs();
		ArrayList<Statement> statements = block.getStatements();
		int noOfDecs = decs.size();
 		Label blockStartLabel = new Label();
		mv.visitLabel(blockStartLabel);
		
		for(Dec dec:decs){
			dec.visit(this, arg);
		}
		for(Statement statement: statements){
			statement.visit(this, arg);
		}
		Label blockEndLabel = new Label();
		mv.visitLabel(blockEndLabel);
		block.setStartLabel(blockStartLabel);
		block.setEndLabel(blockEndLabel);
		runLocArrSlot = runLocArrSlot- noOfDecs;
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		
		boolean boolValue = booleanLitExpression.getValue();
		
		if(boolValue==true){
			mv.visitInsn(ICONST_1);
		}		
		else{
			mv.visitInsn(ICONST_0);
		}		
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		declaration.setSlot(runLocArrSlot++);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		Dec declaration = identExpression.getDec();
		String ident =   declaration.getIdent().getText();
		int slotNum = declaration.getSlot();
		TypeName identType = declaration.getIdentType();
		
		if(slotNum==-1){
			mv.visitVarInsn(ALOAD, 0);
			if(identType.equals(TypeName.INTEGER)){
				mv.visitFieldInsn(GETFIELD, className, ident, "I");
			}
			else if(identType.equals(TypeName.BOOLEAN)){
				mv.visitFieldInsn(GETFIELD, className, ident, "Z");
			}
		}
		else{
			mv.visitVarInsn(ILOAD, slotNum);
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		
		Dec declaration = identX.getDec();
		String ident =   declaration.getIdent().getText();
		int slotNum = declaration.getSlot();
		TypeName identType = declaration.getIdentType();
		
		if(slotNum==-1){
			
			if(identType.equals(TypeName.INTEGER)){
				mv.visitFieldInsn(PUTFIELD, className, ident, "I");
			}
			else if(identType.equals(TypeName.BOOLEAN)){
				mv.visitFieldInsn(PUTFIELD, className, ident, "Z");
			}
		}
		else{
			mv.visitVarInsn(ISTORE, slotNum);
			mv.visitInsn(POP);
		}
		
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {

		Block blk = ifStatement.getB();
		Expression exp = ifStatement.getE();
		exp.visit(this, arg);		
		
		Label jumpOutLabel = new Label();
		mv.visitJumpInsn(IFEQ, jumpOutLabel);
		
		Label goInsideLabel = new Label();
		mv.visitLabel(goInsideLabel);
		blk.visit(this, arg);
		
		mv.visitLabel(jumpOutLabel);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);		
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		int expVal = intLitExpression.getValue();
		mv.visitLdcInsn(expVal);
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//TODO Implement this
		//For assignment 5, only needs to handle integers and booleans
		
/*		Label l0 = new Label();
		mv.visitLabel(l0);*/
		TypeName decType = paramDec.getIdentType();
		String ident = paramDec.getIdent().getText();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitIntInsn(BIPUSH, argIndex++);
		mv.visitInsn(AALOAD);
		if(decType.equals(TypeName.INTEGER)){
			FieldVisitor fv = cw.visitField(ACC_PUBLIC, ident, "I", null, null);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(Ljava/lang/String;)Ljava/lang/Integer;", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
			mv.visitFieldInsn(PUTFIELD, className, ident, "I");
			
		}
		else if(decType.equals(TypeName.BOOLEAN)){
			FieldVisitor fv = cw.visitField(ACC_PUBLIC, ident, "Z", null, null);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Ljava/lang/String;)Ljava/lang/Boolean;", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
			mv.visitFieldInsn(PUTFIELD, className, ident, "Z");
		}
		return null;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		
		Block blk = whileStatement.getB();
		Expression exp = whileStatement.getE();
		
		Label checkCondition = new Label();
		mv.visitJumpInsn(GOTO, checkCondition);
		
		Label whileBody = new Label();
		mv.visitLabel(whileBody);		
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		blk.visit(this, arg);
		
		mv.visitLabel(checkCondition);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		exp.visit(this, arg);
		mv.visitJumpInsn(IFNE, whileBody);
		
		return null;
	}

}
