import absyn.*;

public class CodeGenerator implements AbsynVisitor {
    private int IADDR_SIZE = 1024;
    private int DADDR_SIZE = 1024;
    private int NO_REGS = 8;
    private int PC_REG = 7;
    public static int mainEntry = 0;
    public static int globalOffset = 0;
    // constructor for initialization and all emitting routines

    //predifined registers
	public static int pc = 7;
	public static int gp = 6;
	public static int fp = 5;
	public static int ac = 0;
	public static int ac1 = 1;

    /* functions to maintain code space: some methods like emitRO, emitRM, prelude, and finale need to be added */
    public static int emitLoc= 0;
    public static int highEmitLoc= 0;

    public CodeGenerator() {
        
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

    /*
    public void visit(Absyntrees) {   // wrapper for post-order traversal
        // generate the prelude
        
        // generate the i/o routines
        
        // call the visit method for DecList
        visit(trees, 0, false);
        // generate finale
        
    }

    // implement all visit methods in AbsynVisitor
    public void visit(DecListdecs, int offset, Boolean isAddress) {
             
    }
    */
        //...

    // Visit methods are currently all stubs except traversal is maintained.
    // The offset values sent in 'accept' calls are not necessarily correct currently -- they are just copied from the ShowTreeVisitor for indentation.

    public void visit (NameTy nameTy, int offset, boolean isAddr ) {

    }

    public void visit (IndexVar var, int offset, boolean isAddr ) {

        var.index.accept( this, offset + 1, false);
    }

    public void visit (SimpleVar var, int offset, boolean isAddr ) {

    }

    public void visit (ArrayDec array, int offset, boolean isAddr ) {

    }

    public void visit (ErrorDec compoundList, int offset, boolean isAddr ) {

    }    

    public void visit (FunctionDec functionDec, int offset, boolean isAddr ) {

        functionDec.params.accept(this, offset, false);
        functionDec.body.accept(this, offset, false);
    }

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

    public void visit( AssignExp exp, int offset, boolean isAddr ) {

        if (exp.lhs != null) {
            exp.lhs.accept( this, offset, false );
        }
        
        exp.rhs.accept( this, offset, false );
    }

    public void visit (CallExp exp, int offset, boolean isAddr ) {

        if (exp.args != null && exp.args.head != null) {
            exp.args.accept(this, offset, false);
        }    
    }
    
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

    public void visit( IfExp exp, int offset, boolean isAddr ) {
 
        exp.test.accept( this, offset, false );
        exp.thenpart.accept( this, offset +1, false);

        if (exp.elsepart != null ) {
            exp.elsepart.accept( this, offset +1, false);
        }               
    }

    public void visit( IntExp exp, int offset, boolean isAddr ) {

    }

    public void visit (NilExp exp, int offset, boolean isAddr ) {

    }

    public void visit( OpExp exp, int offset, boolean isAddr ) {

        exp.left.accept( this, offset, false );
        exp.right.accept( this, offset, false );
    }

    public void visit (ReturnExp exp, int offset, boolean isAddr ) {

        exp.exp.accept(this, offset+1, false);
    }  

    public void visit( VarExp exp, int offset, boolean isAddr ) {

        exp.variable.accept(this, offset+1, false);
    }

    public void visit (WhileExp exp, int offset, boolean isAddr ) {

        exp.test.accept(this, offset+1, false);
        exp.body.accept(this, offset+1, false);
    }
}

