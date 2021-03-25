import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import absyn.*;

//TODO: More error recovery -- attempt to avoid cascading errors (possibly replacing invalid types with permissible ones after reporting error)
//TODO: More type checking -- get as creative as you can. Always test on both valid and invalid files

public class SemanticAnalyzer implements AbsynVisitor {

  HashMap<String, ArrayList<NodeType>> table;

  private static final String BLOCK_ENTRY = "Entering a new block: ";
  private static final String BLOCK_EXIT = "Leaving the block";
  private static final int GLOBAL_SCOPE = 1;

  public SemanticAnalyzer() {
    table = new HashMap<String, ArrayList<NodeType>>();

    //Insert predefined methods
    // int input(void), and
    // void output(int)
    int dummyPos = -1;
    Dec inputDec = new FunctionDec(dummyPos, dummyPos, new NameTy(dummyPos, dummyPos, NameTy.INT), "input", null, null);

    VarDecList outputParams = new VarDecList(new SimpleDec(dummyPos, dummyPos, new NameTy(dummyPos, dummyPos, NameTy.INT), "outputValue"), null);
    Dec outputDec = new FunctionDec(dummyPos, dummyPos, new NameTy(dummyPos, dummyPos, NameTy.VOID), "output", outputParams, null);

    insert (inputDec, GLOBAL_SCOPE);
    insert(outputDec, GLOBAL_SCOPE);
  }

  private void printError (int row, int col, String msg) {
    StringBuilder sb = new StringBuilder("Error at line " + (row + 1) + ", col " + (col + 1) + ": ");
    sb.append(msg);
    System.err.println(sb.toString());
  }

  //Accessed by the utility methods for “insert”, ”lookup”, and “delete” operations
  private void insert(Dec newDec, int level) {

    String name = newDec.name;
    NodeType toInsert = new NodeType(name, newDec, level);

    //Check if there's already a declaration with this name
    if (table.containsKey(name)) {
      //If there is, add the new one to the list

      ArrayList<NodeType> nodes = table.get(name);
      NodeType n;

      //Sort the list from most specific scope to least specific (global)
      //Also sort from most recent declaration to least recent (due to >=)
      for (int i = 0; i < nodes.size(); i++) {        
        n = nodes.get(i);
        if (toInsert.level >= n.level) {
          if (toInsert.level == n.level && toInsert.name.equals(n.name)) {
            //Redeclaration error: same variable name used in 2 declarations in same scope
            printError(toInsert.def.row, toInsert.def.col, "redeclaration of variable '" + toInsert.name +"'");
          }
          nodes.add(i, toInsert);
          break;
        }
        //Scope is smaller than every element
        else if (i == nodes.size() - 1) {
          nodes.add(toInsert);
          break;
        }
      }
    }
    else {
      ArrayList<NodeType> nodes = new ArrayList<NodeType>();
      nodes.add(toInsert);
      table.put(name, nodes);
    }
  }

  /*
  Given the provided name of a variable or function, retrieve its return type and return it.
  If isFunc is false, return the type of the most recent declaration
  If isFunc is true, return the type of a function declaration, even if it is not the most recent (as it is valid to have a var and func by the same name)

  Types are returned in the form of the NameTy constants (including NameTy.VOID, NameTy.INT, NameTy.INT_ARR, NameTy.NO_DEC)
  Types are returned in the form of the declaration which created the variable (or null)
  */
  private Dec lookup (String name, Boolean isFunc, int row, int col) {
    
    if (table.get(name) != null) {
      for (Iterator<NodeType> iter = table.get(name).iterator(); iter.hasNext();) {
        NodeType n = iter.next();
  
        //If searching for a function, skip over any non-function matches
        if (isFunc && !(n.def instanceof FunctionDec)) {
          continue;
        }
        else {
          //Otherwise return the first one, format depends on if its an array or not
          /*if (n.def.getClass().getName().equals("ArrayDec") && n.def.type.type == NameTy.INT) {
            return NameTy.INT_ARR;
          }
          else {
            return n.def.type.type;
          } */
          return n.def;       
        }
      }
    }

    printError(row, col, "undefined reference to '" + name + "'");
    //return NameTy.NO_DEC;
    return new ErrorDec(row, col);
  }

  private void deleteScope(int levelToRemove) {

    //Iterate over the symbol table using its entry set
    Iterator<Map.Entry<String, ArrayList<NodeType>>> it = table.entrySet().iterator();

    while (it.hasNext()) {
      //Pair refers to a key-value pair in the hashmap
      Map.Entry<String, ArrayList<NodeType>> pair = (Map.Entry<String, ArrayList<NodeType>>) it.next();
      List<NodeType> nodes = pair.getValue();

      // For each key, check all the nodes, printing and removing those at the current scope
      for (Iterator<NodeType> nodeIter = nodes.iterator(); nodeIter.hasNext();) {
        NodeType n = nodeIter.next();
        if (n.level == levelToRemove) {
          indent(levelToRemove);
          System.out.println(n);
          nodeIter.remove();
        }
      }

      //if the current key no longer has any values, remove it from the hashmap
      if (nodes.isEmpty()) {
        it.remove();
      }
    }
}

  //Add boolean methods such as “isInteger(Dec dtype)” in SemanticAnalyzer.java to simplify the code for type checkin:
  //Given “int x[10]”, “x[2]” is an integer, and given “int input(void)”, “input()” is an integer

  final static int SPACES = 4;

  private void indent( int level ) {
    for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
  }

  private void checkReturnExpType(ReturnExp exp, String funcName, int desiredType) {
    //Check that the declared function return type and actual return type match
    if (exp.dType.type.type != desiredType) {
      //If they don't, output an error
      printError(exp.row, exp.col, "Return type must be type '" + desiredType + "' for function " + funcName);
    }
  }

  //Verifies that a return statement exists for a non-void function, verifies return type, and checks for unreachable code
  //Checks nested scopes (for e.g. if/else blocks that always return)
  private boolean checkForReturn(ExpList exps, String funcName, int returnType) {
    
    Exp head = null;
    boolean returnFound = false;

    //Iterate over all the expressions in the function
    while (exps != null) {
      head = exps.head;
      //Search for a return expression
      if (head instanceof ReturnExp) {
        returnFound = true;
        checkReturnExpType((ReturnExp) head, funcName, returnType);
      }
      //If there are any other expressions after a top-level return, print an error
      else if (returnFound) {
        printError(head.row, head.col, "Unreachable code detected after 'return'");
        break;
      }
      
      exps = exps.tail;
    }

    if (head != null && !returnFound) {
      //If the function returns a non-void value but has no return statement, print error
      if (returnType != NameTy.VOID) {
        //It's possible it may be an "if () return; else return;" situation or a loop, which must also be accounted for
        if (head instanceof IfExp) {
          IfExp ifHead = (IfExp) head;
          boolean ifPart = false;
          //Call function recursively on the 'if' part to determine if a return statement exists

          //Check if the expression is a returnExp, if it is check the type
          if (ifHead.thenpart instanceof ReturnExp) {
            returnFound = true;
            checkReturnExpType((ReturnExp) ifHead.thenpart, funcName, returnType);
            return returnFound;
          }
          //Otherwise, check if it's a CompoundExp and check type recursively
          else if (ifHead.thenpart instanceof CompoundExp)           {
            ExpList ifExps = ((CompoundExp)(ifHead.thenpart)).exps;
            ifPart = checkForReturn(ifExps, funcName, returnType);
          }
          if (ifHead.thenpart != null && (ifHead.thenpart instanceof ReturnExp || ifHead.thenpart instanceof CompoundExp)) {
            //Also call on the 'else' part if relevant -- checking for returnExp or compoundExp
            boolean elsePart = false;

            if (ifHead.elsepart instanceof ReturnExp) {
              returnFound = true;
              checkReturnExpType((ReturnExp) ifHead.elsepart, funcName, returnType);
              return returnFound;
            }
            else if (ifHead.elsepart instanceof CompoundExp) {
              ExpList elseExps = ((CompoundExp)(ifHead.elsepart)).exps;
              elsePart = checkForReturn(elseExps, funcName, returnType);
              return (ifPart && elsePart);
            }            
          }  
          else {
            printError(head.row, head.col, "Expected return statement for function '" + funcName + "'");
          }        

        }
        // else if (head instanceof WhileExp) {
        //   ExpList whileExps = ((CompoundExp) ((WhileExp) head).body).exps;


        //   returnFound = checkForReturn(whileExps, funcName, returnType);
        // }
        else {
          printError(head.row, head.col, "Expected return statement for function '" + funcName + "'");
        }        
      }
    }

    return returnFound;
  }


  public void visit (NameTy nameTy, int level ) {

  }

  public void visit (IndexVar var, int level ) {
    var.index.accept( this, level);

    //Check that the index being accesssed is an integer
    if (var.index.dType.type.type != NameTy.INT) {
      printError(var.index.row, var.index.col, "array index must be of type INT");      
    }  

    IntExp intExp;
    ArrayDec arrayDec;

    if (var.index instanceof IntExp) {
      intExp = (IntExp) var.index;
      arrayDec = (ArrayDec) lookup(var.name, false, var.row, var.col);
      if(!(intExp.value < arrayDec.size.value) && (intExp.value > 0)){
        printError(var.index.row, var.index.col, "array index is out of bounds");
      }
    }
  }

  public void visit (SimpleVar var, int level ) {

  }

  public void visit (ArrayDec array, int level ) {
    insert(array, level);

  }

  public void visit (ErrorDec badDec, int level ) {
    insert(badDec, level);
  }    

  public void visit (FunctionDec functionDec, int level ) {
    indent(level);
    System.out.println("Entering scope for function: " + functionDec.name);
    insert(functionDec, level);

    level++;
    functionDec.params.accept(this, level);
    functionDec.body.accept(this, level);

    ExpList exps = functionDec.body.exps;
    Exp head = null;
    boolean returnFound = false;

    // //Iterate over all the expressions in the function
    // while (exps != null) {
    //   head = exps.head;
    //   //Search for a return expression
    //   if (head instanceof ReturnExp) {
    //     returnFound = true;
    //     //Check that the declared function return type and actual return type match
    //     if (head.dType.type.type != functionDec.type.type) {
    //       //If they don't, output an error
    //       printError(head.row, head.col, "Return type must be type '" + functionDec.type + "' for function " + functionDec.name);
    //     }
    //   }
    //   //If there are any other expressions after a top-level return, print an error
    //   else if (returnFound) {
    //     printError(head.row, head.col, "Unreachable code detected after 'return'");
    //     break;
    //   }
      
    //   exps = exps.tail;
    // }

    // if (head != null) {
    //   //If the function returns a non-void value but has no return statement, print error
    //   if (!returnFound && functionDec.type.type != NameTy.VOID) {
    //     //It's possible it may be an "if () return; else return;" situation, which must also be accounted for
    //     if (head instanceof IfExp) {
    //       ExpList ifExps = ((IfExp) head).;
    //     }
    //     else {
    //       printError(head.row, head.col, "Expected return statement for function '" + functionDec.name + "'");
    //     }        
    //   }
    // }    

    checkForReturn(exps, functionDec.name, functionDec.type.type);

    deleteScope(level);
    indent(level - 1);
    System.out.println("Leaving the function scope");
  }

  public void visit (SimpleDec dec, int level ) {

    //Don't insert a declaration into the symbol table in the case of func (void)
    if (dec.name != null && dec.type.type != NameTy.VOID) {
      insert(dec, level);
    }
    
  }  

  public void visit (DecList decList, int level ) {
    indent( level );
    System.out.println("Entering the global scope:");
    
    level = GLOBAL_SCOPE;
    while( decList != null ) {
      decList.head.accept( this, level );
      decList = decList.tail;
    } 

    deleteScope(level);
    indent(level - 1);
    System.out.println("Leaving the global scope");
  }

  public void visit (VarDecList decList, int level ) {

    while( decList != null ) {
      decList.head.accept( this, level );
      decList = decList.tail;
    } 
  }

  public void visit( ExpList expList, int level ) {

    if (expList.head != null) {

      while( expList != null ) {
        expList.head.accept( this, level );
        expList = expList.tail;
      } 
    }    
  }  

  public void visit( AssignExp exp, int level ) {

    if (exp.lhs != null) {
      exp.lhs.accept( this, level );
      exp.rhs.accept( this, level );

      //TODO Check that LHS and RHS sides match
      Dec leftDec = lookup(exp.lhs.name, false, exp.lhs.row, exp.rhs.col);
      NameTy leftType = leftDec.type;
      NameTy rightType = exp.rhs.dType.type;
      
      if (leftType.type != rightType.type) {
        printError(exp.row, exp.col, "Attempt to assign type '" + rightType + "' to " + exp.lhs.name +", which has type '" + leftType + "'");
      }

      //Assign the overall expression the same type / declaration   
      exp.dType = leftDec;
    }
    else {
      exp.rhs.accept( this, level );
      printError(exp.row, exp.col, "Cannot assign to null");
      exp.dType = new ErrorDec(exp.row, exp.col);
    }        
  }

  public void visit (CallExp exp, int level ) {

    if (exp.args != null && exp.args.head != null) {
      exp.args.accept(this, level);
    }    

    Dec funcDec = lookup(exp.func, true, exp.row, exp.col);
    exp.dType = funcDec;
  }
  
  public void visit (CompoundExp compoundList, int level ) {
   
    if (compoundList.decs != null) {
      compoundList.decs.accept( this, level );
    }
    if (compoundList.exps != null) {
      compoundList.exps.accept( this, level );
    }    
  }    

  public void visit (ErrorExp exp, int level ) {
    exp.dType = new ErrorDec(exp.row, exp.col);
  }    

  public void visit( IfExp exp, int level ) {

    exp.test.accept( this, level );

    indent(level);
    System.out.println(BLOCK_ENTRY + " (if)");

    //TODO: Typecheck if condition -- must be an integer
    if (exp.test.dType == null || exp.test.dType.type.type != NameTy.INT) {
      printError(exp.test.row, exp.test.col, "'if' condition does not evaluate to INT");
    }

    exp.thenpart.accept( this, level +1);

    deleteScope(level + 1);
    indent(level);
    System.out.println(BLOCK_EXIT);

    if (exp.elsepart != null ) {
      indent( level );
      System.out.println(BLOCK_ENTRY + " (else)");

      exp.elsepart.accept( this, level +1);

      deleteScope(level + 1);
      indent(level);
      System.out.println(BLOCK_EXIT);
    }       
    
  }

  public void visit( IntExp exp, int level ) {
    //Assign the IntExp dType a dummy variable with integer type
    exp.dType = new SimpleDec(exp.row, exp.col, new NameTy(exp.row, exp.col, NameTy.INT), null);
  }

  public void visit (NilExp exp, int level ) {
    //Assign the IntExp dType a dummy variable with void type
    exp.dType = new SimpleDec(exp.row, exp.col, new NameTy(exp.row, exp.col, NameTy.VOID), null);
  }

  public void visit( OpExp exp, int level ) {

    exp.left.accept( this, level );
    exp.right.accept( this, level );

    //TODO assign dType based on children and perform error checking and recovery
    if (exp.left.dType.type.type != NameTy.INT) {
      printError(exp.left.row, exp.left.col, "Both operands must be integers");
      exp.dType = new ErrorDec(exp.row, exp.col);
    } 
    else if (exp.left.dType.type.type != exp.right.dType.type.type) {
      printError(exp.row, exp.col, "Type mismatch in operation. Left is '" + exp.left.dType.toString() + "'', right is '" + exp.right.dType.type + "'");
      exp.dType = new ErrorDec(exp.row, exp.col);
    }
    else {
      exp.dType = exp.left.dType;
    }
  }

  public void visit (ReturnExp exp, int level ) {     
    exp.exp.accept(this, level);

    //The declaration type of the return expression is the same as that of its child
    exp.dType = exp.exp.dType;
  }  

  public void visit( VarExp exp, int level ) {
     
    exp.variable.accept(this, level);

    //Assign dType based on the declaration of the given variable name
    exp.dType = lookup(exp.variable.name, false, exp.variable.row, exp.variable.col);
  }

  public void visit (WhileExp exp, int level ) {
    indent(level);
    System.out.println(BLOCK_ENTRY + " (loop)");
    
    exp.test.accept(this, level+1);
    exp.body.accept(this, level+1);

    //TODO: Ensure that the 'test' is an int. If not, throw an error:
    if (exp.test.dType == null || exp.test.dType.type.type != NameTy.INT) {
      printError(exp.test.row, exp.test.col, "loop condition does not evaluate to INT");
    }

    deleteScope(level + 1);
    indent(level);
    System.out.println(BLOCK_EXIT);
  }
}
