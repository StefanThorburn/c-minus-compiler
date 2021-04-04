import absyn.*;

public class CodeGenerator implements AbsynVisitor {
    int IADDR_SIZE = 1024;
    int DADDR_SIZE = 1024;
    int NO_REGS = 8;
    int PC_REG = 7;
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
}

