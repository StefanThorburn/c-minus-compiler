import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import absyn.*;

public class SemanticAnalyzer implements AbsynVisitor {

  HashMap<String, ArrayList<NodeType>> table;

  private static final String BLOCK_ENTRY = "Entering a new block: ";
  private static final String BLOCK_EXIT = "Leaving the block";

  public SemanticAnalyzer() {
    table = new HashMap<String, ArrayList<NodeType>>();
  }

  private void printError (int row, int col, String msg) {
    StringBuilder sb = new StringBuilder("Error at line " + row + ", col " + col + ": ");
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

      //TODO: If there are two variables with the same name and the same scope, that is not permissible

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

  //Returns the most type of the most recent declaration of a particular variable name
  //Or the type of a function
  private int lookup (String name, Boolean isFunc, int row, int col) {
    
    for (Iterator<NodeType> iter = table.get(name).iterator(); iter.hasNext();) {
      NodeType n = iter.next();

      //If searching for a function, skip over any non-function matches
      if (isFunc && !(n.def.getClass().getName().equals("FunctionDec"))) {
        continue;
      }
      else {
        //Otherwise return the first one
        return n.def.type.type;
      }
    }

    printError(row, col, "undefined reference to '" + name + "'");
    return NodeType.NO_DEC;
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

  public void visit (NameTy nameTy, int level ) {

  }

  public void visit (IndexVar var, int level ) {
    var.index.accept( this, level);
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
    
    level++;
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
    }
    
    exp.rhs.accept( this, level );
  }

  public void visit (CallExp exp, int level ) {

    if (exp.args != null && exp.args.head != null) {
      exp.args.accept(this, level);
    }    
  }
  
  public void visit (CompoundExp compoundList, int level ) {
   
    if (compoundList.decs != null) {
      compoundList.decs.accept( this, level );
    }
    if (compoundList.exps != null) {
      compoundList.exps.accept( this, level );
    }    
  }    

  public void visit (ErrorExp compoundList, int level ) {

  }    

  public void visit( IfExp exp, int level ) {

    exp.test.accept( this, level );

    indent(level);
    System.out.println(BLOCK_ENTRY + " (if)");

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
    
  }

  public void visit (NilExp exp, int level ) {
    
  }

  public void visit( OpExp exp, int level ) {

    // switch( exp.op ) {
    //   case OpExp.PLUS:
    //     System.out.println( " + " );
    //     break;
    //   case OpExp.MINUS:
    //     System.out.println( " - " );
    //     break;
    //   case OpExp.MUL:
    //     System.out.println( " * " );
    //     break;
    //   case OpExp.DIV:
    //     System.out.println( " / " );
    //     break;
    //   case OpExp.LT:
    //     System.out.println( " < " );
    //     break;
    //   case OpExp.LE:
    //     System.out.println( " <= " );
    //     break;
    //   case OpExp.GT:
    //     System.out.println( " > " );
    //     break;
    //   case OpExp.GE:
    //     System.out.println( " >= " );
    //     break;
    //   case OpExp.EQ:
    //     System.out.println( " == " );
    //     break;
    //   case OpExp.NE:
    //     System.out.println( " != " );
    //     break;
    //   default:
    //     System.out.println( "Unrecognized operator at line " + exp.row + " and column " + exp.col);
    // }

    exp.left.accept( this, level );
    exp.right.accept( this, level );
  }

  public void visit (ReturnExp exp, int level ) {
     
    exp.exp.accept(this, level);
  }  

  public void visit( VarExp exp, int level ) {
     
    exp.variable.accept(this, level);
  }

  public void visit (WhileExp exp, int level ) {
    indent(level);
    System.out.println(BLOCK_ENTRY + " (loop)");
    
    exp.test.accept(this, level+1);
    exp.body.accept(this, level+1);

    deleteScope(level + 1);
    indent(level);
    System.out.println(BLOCK_EXIT);
  }
}
