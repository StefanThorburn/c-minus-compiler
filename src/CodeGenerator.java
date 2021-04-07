import absyn.*;

// constructor for initialization and all emitting routines
public class CodeGenerator implements AbsynVisitor {
    private int IADDR_SIZE = 1024;
    private int DADDR_SIZE = 1024;
    private int NO_REGS = 8;
    private int PC_REG = 7;        

    //predefined register numbers
	public static final int  ac = 0;
    public static final int ac1 = 1;
    public static final int fp = 5;
    public static final int gp = 6;
    public static final int pc = 7;
    public static final int ofpFO = 0;
    public static final int retFO = -1;
    public static final int initFO = -2;

    //other miscellaneous constants
    private static final int SIZE_OF_INT = 1;
    private static final int GLOBAL_SCOPE = 1;

    // instance variables
    private int emitLoc;
    private int highEmitLoc;
    private int mainEntry;
    private int globalOffset;

    // constructor for initialization and all emitting routines

    public CodeGenerator() {
        mainEntry = 0;
        globalOffset = 0;
        emitLoc = 0;
        highEmitLoc = 0;    
    }
    
    public int emitSkip( int distance ) {
        int i = emitLoc;
        emitLoc += distance;

        if( highEmitLoc < emitLoc) 
            highEmitLoc = emitLoc;

        return i;
    }
    
    public void emitBackup( int loc) {
        if( loc > highEmitLoc)
            emitComment( "BUG in emitBackup" );
        emitLoc = loc;
    }

    public void emitRestore() {
        emitLoc = highEmitLoc;
    }
    
    public void emitRM_Abs( String op, int r, int a, String c ) {
        //System.out.println(emitLoc + ": " + op + " " + r + ", " + (a - (emitLoc + 1)) + "(" + pc + ") \t" + c);

        //Formatted output for the RM instruction
        System.out.printf("%3d: %6s %d,%3d(%d)\t\t%s%n", emitLoc, op, r, a - (emitLoc + 1), pc, c);

        emitLoc++;

        if( highEmitLoc < emitLoc)
            highEmitLoc = emitLoc;
    }
    
    public void emitComment(String c)
	{
		System.out.println("* " + c);
	}

    // Print a register-memory instruction
    public void emitRM(String op, int r1, int offset, int r2, String c) {
		//System.out.println(emitLoc + ": " + op + " " + r1 + ", " + offset + "(" + r2 + ") \t" + c);
        //Formatted output for the RM instruction
        System.out.printf("%3d: %6s %d,%3d(%d)\t\t%s%n", emitLoc, op, r1, offset, r2, c);

        emitLoc++;

		if (highEmitLoc < emitLoc)
			highEmitLoc = emitLoc;
	}

    // Print a registers only instruction
	public void emitRO(String op, int r1, int r2, int r3, String c){
		//System.out.println(emitLoc + ": " + op + " " + r1 + ", " + r2 + ", " + r3 + " \t" + c);

        //Formatted output for the RO instruction
        System.out.printf("%3d: %6s %d,%d,%d\t\t\t%s%n", emitLoc, op, r1, r2, r3, c);

        emitLoc++;

		if (highEmitLoc < emitLoc)
			highEmitLoc = emitLoc;
	}

    //Generate all prelude code which is the same format for every program
    public void prelude(String fileName)
    {
        //Printing prelude
        emitComment("C-Minus Compilation to TM Code");
        emitComment("File: " + fileName);
        emitComment("Standard prelude:");
        emitRM("LD", gp, 0, 0, "load gp with maxaddr");
        emitRM("LDA", fp, 0, gp, "Copy gp to fp");
        emitRM("ST", 0, 0, 0, "Clear content at loc");
        int savedLoc = emitSkip(1);
        
        //Printing predefined input/output functions        
        emitComment("Jump around i/o routines here");
        emitComment("code for input routine");
        emitRM("ST", 0, -1, fp, "store return");
        emitRO("IN", 0, 0, 0, "input");
        emitRM("LD", pc, -1, fp, "return to caller");
        emitComment("code for output routine");
        emitRM("ST", 0, -1, fp, "store return");
        emitRM("LD", 0,-2,fp, "load output value");
        emitRO("OUT", 0, 0, 0, "output");
        emitRM("LD", pc, -1, fp, "return to caller");
        emitBackup(savedLoc);
        emitRM("LDA", pc, 7, pc, "jump around i/o code");
        emitRestore();
        emitComment("End of standard prelude");
    }

    //Generate finale code which follows the same format for every program
    public void finale()
    {
        emitComment("start of finale");
        //Printing finale
        emitRM("ST", fp, globalOffset+ofpFO, fp, "push ofp");
        emitRM("LDA", fp, globalOffset, fp, "push frame");
        emitRM("LDA", ac, 1, pc, "load ac with ret ptr");
        emitRM_Abs("LDA", pc, mainEntry, "jump to main loc");
        emitRM("LD", fp, ofpFO, fp, "pop frame");
        emitComment("end of execution.");
        emitRO("HALT", 0, 0, 0, "");
          
    }

    //Wrapper function for tree visit, generates boilerplate code like setup and finale
    public void visit (Absyn decs, String fileName) {
        
        prelude(fileName);

        //Traverse the tree
        visit((DecList)decs, 0, false);

        // If main is not provided (mainEntry will be 0) then terminate execution
        // Then stop execution
        /*if (mainEntry == 0) {
            System.err.println("No reference to 'main' function. Aborting code generation.");
            System.exit(-1);
        }*/

        //Print finale code at the end
        finale();
    }

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

        if (array.nestLevel == GLOBAL_SCOPE) {
            emitComment("processing global array var: " + array.name);
        }
        else {
            emitComment("processing local array var: " + array.name);
        }

        array.offset = offset;
    }

    public void visit (ErrorDec compoundList, int offset, boolean isAddr ) {

    }    
    
    //edit
    public void visit (FunctionDec functionDec, int offset, boolean isAddr ) {

        //The location of the next instruction becomes the function address
        functionDec.funcAddress = emitLoc;

        functionDec.params.accept(this, offset, false);
        //The parameters will be integers or addresses (which are also integers)
        //The offsets for any declarations in the body will start after the parameters
        functionDec.body.accept(this, offset - (functionDec.numArguments * SIZE_OF_INT), false);
    }

    //edit
    public void visit (SimpleDec dec, int offset, boolean isAddr ) {
        dec.offset = offset;
    }  

    public void visit (DecList decList, int offset, boolean isAddr ) {

        while( decList != null ) {
            decList.head.accept( this, offset, false );
            decList = decList.tail;
        } 
    }

    //In a variable declaration list, visit each of the declarations, updating the offset accordingly
    public void visit (VarDecList decList, int offset, boolean isAddr ) {

        while( decList != null ) {
            //Iterate over variable declarations, decrementing the offset for each one
            if (decList.head instanceof SimpleDec) {
                //For a simple integer, decrease the offset by just the size of one integer
                offset = offset - (1 * SIZE_OF_INT);

                if (decList.head.nestLevel == GLOBAL_SCOPE) {
                    //In case of global declarations, decrement global offset as well
                    globalOffset = globalOffset - (1 * SIZE_OF_INT);
                }
            }
            else { //It is an array dec (since at the point of CodeGeneration we know it is error free)

                //Decrease the offset based on the number of elements in the array
                offset = offset - (((ArrayDec) decList.head).size.value * SIZE_OF_INT);
                //Decrease by an additional element to store the size of the array
                offset = offset - (1 * SIZE_OF_INT);

                if (decList.head.nestLevel == GLOBAL_SCOPE) {
                    //In case of global declarations, decrement global offset as well
                    globalOffset = globalOffset - (((ArrayDec) decList.head).size.value * SIZE_OF_INT);
                    globalOffset = globalOffset - (1 * SIZE_OF_INT);
                }
            }

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

