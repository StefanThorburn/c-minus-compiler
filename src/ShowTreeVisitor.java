import absyn.*;

public class ShowTreeVisitor implements AbsynVisitor {

  final static int SPACES = 4;

  private void indent( int level ) {
    for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
  }

  public void visit (NameTy nameTy, int level ) {
    //TODO: Complete stub
    indent(level);
    System.out.println(nameTy.toString());
  }

  public void visit (IndexVar var, int level ) {
    //TODO: Complete stub
    indent(level);
    System.out.println( "IndexVar: " + var.name);
    level++;
    indent(level);
    System.out.println("Index: ");
    var.index.accept( this, level + 1);
  }

  public void visit (SimpleVar var, int level ) {
    //TODO: Complete stub
    indent(level);
    System.out.println( "SimpleVar: " + var.name);
  }

  public void visit (ArrayDec array, int level ) {
    //TODO: Complete stub
    indent(level);
    System.out.println( "ArrayDec: " + array.type.toString() + " " + array.name + " [" + array.size.value + "]");
  }

  public void visit (ErrorDec compoundList, int level ) {
    indent(level);
    System.out.println("ErrorDec");
  }    

  public void visit (FunctionDec functionDec, int level ) {
    //TODO: Complete stub
    indent(level);
    System.out.println( "FunctionDec: " + functionDec.result.toString() + " " + functionDec.func);
    level++;
    functionDec.params.accept(this, level);
    functionDec.body.accept(this, level);
  }

  public void visit (SimpleDec dec, int level ) {
    //TODO: Complete stub
    indent(level);
    if (dec.type.type != NameTy.VOID) {
      System.out.println( "SimpleDec: " + dec.type.toString() + " " + dec.name);
    }
    else {
      System.out.println( "SimpleDec: " + dec.type.toString());
    }
  }  

  public void visit (DecList decList, int level ) {
    indent( level );
    System.out.println("Declaration list:");
    level++;
    while( decList != null ) {
      decList.head.accept( this, level );
      decList = decList.tail;
    } 
  }

  public void visit (VarDecList decList, int level ) {
    indent( level );
    System.out.println("Variable Declaration list: ");
    level++;
    while( decList != null ) {
      decList.head.accept( this, level );
      decList = decList.tail;
    } 
  }

  public void visit( ExpList expList, int level ) {
    indent( level );
    if (expList.head == null) {
      System.out.println("Empty ExpList");
    }
    else {
      System.out.println("Expression list: ");
      level++;
      while( expList != null ) {
        expList.head.accept( this, level );
        expList = expList.tail;
      } 
    }    
  }  

  public void visit( AssignExp exp, int level ) {
    indent( level );
    System.out.println( "AssignExp: ");
    level++;
    if (exp.lhs != null) {
      exp.lhs.accept( this, level );
    }
    else {
      indent( level );
      System.out.println( "ErrorVarExp");
    }
    
    exp.rhs.accept( this, level );
  }

  public void visit (CallExp exp, int level ) {
    //TODO: Complete stub
    indent( level );
    System.out.println("CallExp: " + exp.func);    
    if (exp.args == null) {
      level++;
      indent(level);
      System.out.println("ErrorArgs");
    }
    else if (exp.args.head != null) {
      level++;
      exp.args.accept(this, level);
    }    
  }
  
  public void visit (CompoundExp compoundList, int level ) {
    indent(level);
    System.out.println("Compound statement list: ");    
    if (compoundList.decs != null) {
      compoundList.decs.accept( this, level+1 );
    }
    if (compoundList.exps != null) {
      compoundList.exps.accept( this, level+1 );
    }    
  }    

  public void visit (ErrorExp compoundList, int level ) {
    indent(level);
    System.out.println("ErrorExp");
  }    

  public void visit( IfExp exp, int level ) {
    indent( level );
    System.out.println( "IfExp: " );
    level++;    
    exp.test.accept( this, level );
    
    indent( level );
    System.out.println("then: ");
    exp.thenpart.accept( this, level +1);

    if (exp.elsepart != null ) {
      indent( level );
      System.out.println("else: ");
      exp.elsepart.accept( this, level +1);
    }       
    
  }

  public void visit( IntExp exp, int level ) {
    indent( level );
    System.out.println( "IntExp: " + exp.value ); 
  }

  public void visit (NilExp exp, int level ) {
    indent(level);
    System.out.println("NilExp");
  }

  public void visit( OpExp exp, int level ) {
    indent( level );
    System.out.print( "OpExp: " ); 
    switch( exp.op ) {
      case OpExp.PLUS:
        System.out.println( " + " );
        break;
      case OpExp.MINUS:
        System.out.println( " - " );
        break;
      case OpExp.MUL:
        System.out.println( " * " );
        break;
      case OpExp.DIV:
        System.out.println( " / " );
        break;
      case OpExp.LT:
        System.out.println( " < " );
        break;
      case OpExp.LE:
        System.out.println( " <= " );
        break;
      case OpExp.GT:
        System.out.println( " > " );
        break;
      case OpExp.GE:
        System.out.println( " >= " );
        break;
      case OpExp.EQ:
        System.out.println( " == " );
        break;
      case OpExp.NE:
        System.out.println( " != " );
        break;
      default:
        System.out.println( "Unrecognized operator at line " + exp.row + " and column " + exp.col);
    }
    level++;
    exp.left.accept( this, level );
    exp.right.accept( this, level );
  }

  public void visit (ReturnExp exp, int level ) {
    indent(level);
    System.out.println("ReturnExp: ");
    exp.exp.accept(this, level+1);
  }  

  public void visit( VarExp exp, int level ) {
    indent( level );
    System.out.println( "VarExp: ");
    exp.variable.accept(this, level+1);
  }

  public void visit (WhileExp exp, int level ) {
    indent(level);
    System.out.println("Loop");
    
    level++;
    indent(level);
    System.out.println("Condition: ");
    exp.test.accept(this, level+1);

    indent(level);
    System.out.println("Body: ");
    exp.body.accept(this, level+1);
  }
}
