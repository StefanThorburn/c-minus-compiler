import absyn.*;

// constructor for initialization and all emitting routines
public class CodeGenerator implements AbsynVisitor {
    private int IADDR_SIZE = 1024;
    private int DADDR_SIZE = 1024;
    private int NO_REGS = 8;
    private int PC_REG = 7;        

    //predefined register numbers
	public static final int ac = 0;
    public static final int ac1 = 1;
    public static final int fp = 5;
    public static final int gp = 6;
    public static final int pc = 7;
    public static final int ofpFO = 0;
    public static final int retFO = -1;
    public static final int initFO = -2;

    //other miscellaneous constants
    private static final int SIZE_OF_INT = 1;
    private static final int GLOBAL_SCOPE = 0;

    // instance variables
    private int emitLoc;
    private int highEmitLoc;
    private int mainEntry;
    private int globalOffset;

    private int offsetTemp;

    // constructor for initialization and all emitting routines

    public CodeGenerator() {
        mainEntry = 0;
        globalOffset = 0;
        emitLoc = 0;
        highEmitLoc = 0;    
        offsetTemp = 0;
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
        //Formatted output for the RM instruction
        System.out.printf("%3d: %6s %d,%3d(%d)\t\t%s%n", emitLoc, op, r1, offset, r2, c);

        emitLoc++;

		if (highEmitLoc < emitLoc)
			highEmitLoc = emitLoc;
	}

    // Print a registers only instruction
	public void emitRO(String op, int r1, int r2, int r3, String c){

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
        if (mainEntry == 0) {
            System.err.println("No reference to 'main' function. Aborting code generation.");
            System.exit(-1);
        }

        //Print finale code at the end
        finale();
    }

    // Visit methods are currently all stubs except traversal is maintained.
    // The offset values sent in 'accept' calls are not necessarily correct currently -- they are just copied from the ShowTreeVisitor for indentation.

    public void visit (NameTy nameTy, int offset, boolean isAddr ) {

    }

    //add editComment
    public void visit (IndexVar var, int offset, boolean isAddr ) {

        emitComment("-> subs");

        var.index.accept( this, offset, isAddr);

        emitComment("<- subs");
    }

    public void visit (SimpleVar var, int offset, boolean isAddr ) {


        //If the simple var is an address (e.g. used as LHS of an assignment)
        if (isAddr == true) {
            //Compute the address of the variable
            emitRM("LDA", ac, var.associatedDec.offset, fp, "compute address of var " + var.name);
            emitRM("ST", ac, offset, fp, "store address in frame offset");
        }
        else {
            //The simple var appears as part of a computation
            emitRM("LD", ac, var.associatedDec.offset, fp, "save value of var " + var.name);
            emitRM("ST", ac, offset, fp, "store value in frame offset");
        }
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

        emitComment("Processing function: " + functionDec.name);
                
        //Save the current instruction location for backpatching the function jump
        int savedLoc = emitSkip(1);

        //The location of the next instruction becomes the function address
        functionDec.funcAddress = emitLoc;

        //For the main function, keep special track of its location
        if (functionDec.name.toLowerCase().equals("main")) {
            mainEntry = functionDec.funcAddress;
        }

        //Move the return address stored in ac to the ret-address location (located right after ofp)
        emitRM("ST", ac, retFO, fp, "move return address from ac to retFO");

        // Reduce the offset to leave space for the book-keeping information
        offset = offset - 2;

        functionDec.params.accept(this, offset, false);
        //The parameters will be integers or addresses (which are also integers)
        //The offsets for any declarations in the body will start after the parameters
        functionDec.body.accept(this, offset - (functionDec.numArguments * SIZE_OF_INT), false);

        // At the end of the function, return to the caller
        emitRM("LD", pc, retFO, fp, "return to caller");

        //At this point, all instructions have been printed for the function and we know how far to jump
        //So complete the backpatching for the jump around the function
        emitComment("jump around function body here");
        int savedLoc2 = emitSkip(0);
        emitBackup(savedLoc);
        emitRM_Abs("LDA", pc, savedLoc2, "jump around " + functionDec.name + " function");
        emitRestore();
    }

    //edit
    public void visit (SimpleDec dec, int offset, boolean isAddr ) {
        dec.offset = offset;

        if (dec.nestLevel == GLOBAL_SCOPE) {
            emitComment("processing global simple var: " + dec.name);
        }
        else {
            emitComment("processing local simple var: " + dec.name);
        }
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
            decList.head.accept( this, offset, false );

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

            // Move to the next declaration
            decList = decList.tail;
        } 

        //Set a temporary offset variable so it can be retrieved in a CompoundExp
        offsetTemp = offset;
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

        emitComment("-> assign");

        if (exp.lhs != null) {
            exp.lhs.accept( this, offset - 1, true );
        }
        
        exp.rhs.accept( this, offset - 2, false );

        emitRM("LD", ac, offset - 1, fp, "load assignment lhs");
        emitRM("LD", ac1, offset - 2, fp, "load assignment rhs");
        emitRM("ST", ac1, ac, ac, "");
        emitRM("ST", ac1, offset, fp, "store result of assignment");

        emitComment("<- assign");
    }

    public void visit (CallExp exp, int offset, boolean isAddr ) {

        emitComment("-> call of function: " + exp.func);

        //TODO evaluate and store arguments
        if (exp.args != null && exp.args.head != null) {
            exp.args.accept(this, offset, false);
        }    

        int funcAddress = 0;

        if (exp.dType instanceof FunctionDec) {
            funcAddress = ((FunctionDec) exp.dType).funcAddress;
        }   
        else {
            //This shouldn't ever happen since the code should be syntactically valid at this point
            //But it's here just in case to prevent a ClassCastException crashing the program
            System.err.println("Something went wrong calling function " + exp.dType.name + " at row " + exp.row + ", col " + exp.col);
        }   

        //After storing arguments, store the current frame pointer
        emitRM("ST", fp, offset+ofpFO, fp, "store current fp");
        emitRM("LDA", fp, offset, fp, "push frame");
        emitRM("LDA", ac, 1, pc, "load ac with ret ptr");
        emitRM_Abs("LDA", pc, funcAddress, "jump to " + exp.dType.name + " loc");
        emitRM("LD", fp, ofpFO, fp, "pop frame");        

        emitComment("<- call");
    }
    
    public void visit (CompoundExp compoundList, int offset, boolean isAddr ) {

        emitComment("-> compound statement");
   
        if (compoundList.decs != null) {
            compoundList.decs.accept( this, offset, false );
            //Adjust offset based on any declarations that were made
            offset = offsetTemp;
        }
        if (compoundList.exps != null) {
            compoundList.exps.accept( this, offset, false );
        }    

        emitComment("<- compound statement");
    }    

    public void visit (ErrorExp compoundList, int offset, boolean isAddr ) {

    }    

    //TODO: Implement assignment and op expressions, then reimplement control structure
    public void visit( IfExp exp, int offset, boolean isAddr ) {

        emitComment("-> if");

        exp.test.accept( this, offset, false );
        //int savedLoc = emitSkip(1);
        exp.thenpart.accept( this, offset, false);
        //int savedLoc2 = emitSkip(0);
        //emitBackup(savedLoc);
        //emitRM_Abs("JEQ", 0, savedLoc2, "if: jump to else part");
        //emitRestore();

        if (exp.elsepart != null ) {
            //emitComment("if: jump to else belongs here");
            exp.elsepart.accept( this, offset, false);
        } 
        
        emitComment("<- if");
    }

    public void visit( IntExp exp, int offset, boolean isAddr ) {

        emitComment("-> constant");

        try{
            //Load the constant into ac
            emitRM("LDC", ac, exp.value, 0, "load const");
            emitRM("ST", ac, offset, fp, "");

        } catch (Exception e){
            System.err.println("Something went wrong when loading value at line " + exp.row + ", col " + exp.col);
        }

        emitComment("<- constant");

    }

    public void visit (NilExp exp, int offset, boolean isAddr ) {

    }

    public void visit( OpExp exp, int offset, boolean isAddr ) {

        emitComment("-> op");
        
        exp.left.accept( this, offset - 1, false );
        exp.right.accept( this, offset - 2, false );

        emitRM("LD", ac, offset - 1, fp, "load left operand");
        emitRM("LD", ac1, offset - 2, fp, "load right operand");

        //Perform the required operation
        //For arithmetic operations, perform "ac OP ac1" and store in ac
        switch ( exp.op ) {
            case OpExp.PLUS:
                emitRO("ADD", ac, ac, ac1, "perform add operation");
                break;
            case OpExp.MINUS:
                emitRO("SUB", ac, ac, ac1, "perform subtract operation");
                break;
            case OpExp.MUL:
                emitRO("MUL", ac, ac, ac1, "perform multiply operation");
                break;
            case OpExp.DIV:
                emitRO("DIV", ac, ac, ac1, "perform division operation");
                break;
            case OpExp.LT:
                emitRO("SUB", ac, 1, ac, "OP <");
                emitRM("JLT", ac, 2, pc, "");
                emitRM("LDC", ac, 0, 0, "false case");
                emitRM("LDA", pc, 1, pc, "unconditional jump");
                emitRM("LDC", ac, 1, 0, "true case");
                break;
            case OpExp.LE:
                emitRO("SUB", ac, 1, ac, "OP <=");
                emitRM("JLE", ac, 2, pc, "");
                emitRM("LDC", ac, 0, 0, "false case");
                emitRM("LDA", pc, 1, pc, "unconditional jump");
                emitRM("LDC", ac, 1, 0, "true case");
                break;
            case OpExp.GT:
                emitRO("SUB", ac, 1, ac, "OP >");
                emitRM("JGT", ac, 2, pc, "");
                emitRM("LDC", ac, 0, 0, "false case");
                emitRM("LDA", pc, 1, pc, "unconditional jump");
                emitRM("LDC", ac, 1, 0, "true case");
                break;
            case OpExp.GE:
                emitRO("SUB", ac, 1, ac, "OP >=");
                emitRM("JLE", ac, 2, pc, "");
                emitRM("LDC", ac, 0, 0, "false case");
                emitRM("LDA", pc, 1, pc, "unconditional jump");
                emitRM("LDC", ac, 1, 0, "true case");
                break;
            case OpExp.EQ:
                emitRO("SUB", ac, 1, ac, "OP ==");
                emitRM("JEQ", ac, 2, pc, "");
                emitRM("LDC", ac, 0, 0, "false case");
                emitRM("LDA", pc, 1, pc, "unconditional jump");
                emitRM("LDC", ac, 1, 0, "true case");
                break;
            case OpExp.NE:
                emitRO("SUB", ac, 1, ac, "OP !=");
                emitRM("JNE", ac, 2, pc, "");
                emitRM("LDC", ac, 0, 0, "false case");
                emitRM("LDA", pc, 1, pc, "unconditional jump");
                emitRM("LDC", ac, 1, 0, "true case");
                break;
        }        

        emitComment("<- op");
        //After performing the operation, store it in the result location
        emitRM("ST", ac, offset, fp, "store op result");
    }

    public void visit (ReturnExp exp, int offset, boolean isAddr ) {

        emitComment("-> return");

        exp.exp.accept(this, offset, false);

        emitComment("<- return");
    }  

    public void visit( VarExp exp, int offset, boolean isAddr ) {

        exp.variable.accept(this, offset, isAddr);
    }

    //TODO: Implement assignment and op expressions, then reimplement control structure
    public void visit (WhileExp exp, int offset, boolean isAddr ) {

        emitComment("-> while");
        emitComment("while: jump after body comes back here");

        //int savedLoc = emitSkip(0);

        if(exp.test != null) {
            exp.test.accept(this, offset, false);
            emitComment("while: jump to end belongs here");
        }
        
        //int savedLoc2 = emitSkip(1);

        if(exp.body != null) {
            exp.body.accept(this, offset, false);
        }

        /*emitRM_Abs("LDA", pc, savedLoc, "while: absolute jump to test");
        int savedLoc3 = emitSkip(0);
        emitBackup(savedLoc2);
        emitRM_Abs("JEQ", 0, savedLoc3, "while: jump to end");
        emitRestore();*/
        
        emitComment("<- while");
    }
}
