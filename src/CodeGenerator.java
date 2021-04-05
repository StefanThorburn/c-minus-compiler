import absyn.*;

// constructor for initialization and all emitting routines
public class CodeGenerator implements AbsynVisitor {
    private int IADDR_SIZE = 1024;
    private int DADDR_SIZE = 1024;
    private int NO_REGS = 8;
    private int PC_REG = 7;

    //predifined registers
	public static final int ac = 0;
	public static final int ac1 = 1;
    public static final int fp = 5;
    public static final int gp = 6;
    public static final int pc = 7;
    public static final int ofpOF = 0;
    public static final int retOF = -1;
    public static final int initOF = -1;

    private static int mainEntry;
    private static int globalOffset; 
    private static int emitLoc;
    private static int highEmitLoc;

    public CodeGenerator() {
        mainEntry = 0;
        globalOffset = 0;
        emitLoc = 0;
        highEmitLoc = 0;
    }
    
    public static int emitSkip( int distance ) {
        int i = emitLoc;
        emitLoc += distance;

        if( highEmitLoc < emitLoc) 
            highEmitLoc = emitLoc;

        return i;
    }
    
    public static void emitBackup( int loc) {
        if( loc > highEmitLoc)
            emitComment( "BUG in emitBackup" );
        emitLoc = loc;
    }

    public static void emitRestore() {
        emitLoc = highEmitLoc;
    }
    
    public static void emitRM_Abs( String op, int r, int a, String c ) {
        System.out.println(emitLoc + ": " + op + " " + r + ", " + (a - (emitLoc + 1)) + "(" + pc + ") \t" + c);
        ++emitLoc;

        if( highEmitLoc < emitLoc)
            highEmitLoc = emitLoc;
    }
    
    public static void emitComment(String c)
	{
		System.out.println("* " + c);
	}

    public static void emitRM(String op, int r1, int offset, int r2, String c) {
		System.out.println(emitLoc + ": " + op + " " + r1 + ", " + offset + "(" + r2 + ") \t" + c);
        ++emitLoc;

		if (highEmitLoc < emitLoc)
			highEmitLoc = emitLoc;
	}

	public static void emitRO(String op, int r1, int r2, int r3, String c){
		System.out.println(emitLoc + ": " + op + " " + r1 + ", " + r2 + ", " + r3 + " \t" + c);
        ++emitLoc;

		if (highEmitLoc < emitLoc)
			highEmitLoc = emitLoc;
	}

    public static void prelude(String fileName)
    {
          //Printing prelude
          emitComment("C-Minus Compilation to TM Code");
          emitComment("File: " + fileName);
          emitComment("Standard prelude:");
          emitRM("LD", 6, 0, 0, "load gp with maxaddr");
          emitRM("LDA", 5, 0, 6, "Copy gp to fp");
          emitRM("ST", 0, 0, 0, "Clear content at loc");
          int savedLoc = emitSkip(1);
          emitComment("Jump around i/o routines here");
          emitComment("code for input routine");
          emitRM("ST", 0, -1, 5, "store return");
          emitRO("IN", 0, 0, 0, "input");
          emitRM("LD", 7, -1, 5, "return to caller");
          emitComment("code for output routine");
          emitRM("ST", 0, -1, 5, "store return");
          emitRM("ST", 0,-1,5, "load output value");
          emitRO("OUT", 0, 0, 0, "output");
          emitRM("LD", 7, -1, 5, "return to caller");
          emitBackup(savedLoc);
          emitRM("LDA", 7, 7, 7, "jump around i/o code");
          emitRestore();
          emitComment("End of standard prelude");
    }

    public static void finale(PrintStream console)
    {
          //Printing finale
          emitRM("ST", fp, globalOffset+ ofpOF, fp, "push ofp");
          emitRM("LDA", fp, globalOffset, fp, "push frame");
          emitRM_Abs("LDA", pc, entry, "jump to main loc");
          emitRM("LD", fp, ofpOF, fp, "pop frame");
          emitComment("end of execution.");
          emitRO("HALT", 0, 0, 0, "");
          //reset to stdout
          System.setOut(console); //Reset output to terminal
    }

    /* 
    // wrapper method for post-order traversal
    public void visit(Absyntrees) {   
        // generate the prelude
        
        // generate the i/o routines
        
        // call the visit method for DecList
       
        visit(trees, 0, false);
        
        //check if "main" is given
        // if 0, output message , terminate
        
        // generate finale     
    }

    */

    // Visit methods are currently all stubs except traversal is maintained.
    // The offset values sent in 'accept' calls are not necessarily correct currently -- they are just copied from the ShowTreeVisitor for indentation.

    public void visit (NameTy nameTy, int offset, boolean isAddr ) {

    }

    //add editComment
    public void visit (IndexVar var, int offset, boolean isAddr ) {

        var.index.accept( this, offset + 1, false);
    }

    public void visit (SimpleVar var, int offset, boolean isAddr ) {

    }

    //edit
    public void visit (ArrayDec array, int offset, boolean isAddr ) {

    }

    public void visit (ErrorDec compoundList, int offset, boolean isAddr ) {

    }    
    
    //edit
    public void visit (FunctionDec functionDec, int offset, boolean isAddr ) {

        functionDec.params.accept(this, offset, false);
        functionDec.body.accept(this, offset, false);
    }

    //edit
    public void visit (SimpleDec dec, int offset, boolean isAddr ) {

    }  

    public void visit (DecList decList, int offset, boolean isAddr ) {

        while( decList != null ) {
            decList.head.accept( this, offset, false );
            decList = decList.tail;
        } 
    }

    public void visit (VarDecList decList, int offset, boolean isAddr ) {

        while( decList != null ) {
            decList.head.accept( this, offset, false );
            decList = decList.tail;
        } 
    }

    public void visit( ExpList expList, int offset, boolean isAddr ) {

        if (expList.head != null) {
            while( expList != null ) {
                expList.head.accept( this, offset, false );
                expList = expList.tail;
            } 
        }    
    }  

    //edit
    public void visit( AssignExp exp, int offset, boolean isAddr ) {

        if (exp.lhs != null) {
            exp.lhs.accept( this, offset, false );
        }
        
        exp.rhs.accept( this, offset, false );
    }

    //edit
    public void visit (CallExp exp, int offset, boolean isAddr ) {

        if (exp.args != null && exp.args.head != null) {
            exp.args.accept(this, offset, false);
        }    
    }
    
    //edit
    public void visit (CompoundExp compoundList, int offset, boolean isAddr ) {
   
        if (compoundList.decs != null) {
            compoundList.decs.accept( this, offset+1, false );
        }
        if (compoundList.exps != null) {
            compoundList.exps.accept( this, offset+1, false );
        }    
    }    

    public void visit (ErrorExp compoundList, int offset, boolean isAddr ) {

    }    

    //edit
    public void visit( IfExp exp, int offset, boolean isAddr ) {
 
        exp.test.accept( this, offset, false );
        exp.thenpart.accept( this, offset +1, false);

        if (exp.elsepart != null ) {
            exp.elsepart.accept( this, offset +1, false);
        }               
    }

    //edit
    public void visit( IntExp exp, int offset, boolean isAddr ) {

    }

    public void visit (NilExp exp, int offset, boolean isAddr ) {

    }

    //add editComment
    public void visit( OpExp exp, int offset, boolean isAddr ) {

        exp.left.accept( this, offset, false );
        exp.right.accept( this, offset, false );
    }

    //add editComment
    public void visit (ReturnExp exp, int offset, boolean isAddr ) {

        exp.exp.accept(this, offset+1, false);
    }  

    public void visit( VarExp exp, int offset, boolean isAddr ) {

        exp.variable.accept(this, offset+1, false);
    }

    //edit
    public void visit (WhileExp exp, int offset, boolean isAddr ) {

        exp.test.accept(this, offset+1, false);
        exp.body.accept(this, offset+1, false);
    }
}

