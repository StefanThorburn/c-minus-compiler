import absyn.*;

public class ShowTreeVisitor implements AbsynVisitor {

  final static int SPACES = 4;

  private void indent( int level ) {
    for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
  }

  public void visit (NameTy nameTy, int level ) {
    //TODO: Complete stub
  }

  public void visit (IndexVar var, int level ) {
    //TODO: Complete stub
  }

  public void visit (SimpleVar var, int level ) {
    //TODO: Complete stub
  }

  public void visit (ArrayDec array, int level ) {
    //TODO: Complete stub
  }

  public void visit (FunctionDec functionDec, int level ) {
    //TODO: Complete stub
  }

  public void visit (SimpleDec dec, int level ) {
    //TODO: Complete stub
  }  

  public void visit (DecList decList, int level ) {
    //TODO: Complete stub
  }

  public void visit (VarDecList decList, int level ) {
    //TODO: Complete stub
  }

  public void visit( ExpList expList, int level ) {
    //TODO: Update function
    while( expList != null ) {
      expList.head.accept( this, level );
      expList = expList.tail;
    } 
  }  

  public void visit( AssignExp exp, int level ) {
    //TODO: Update function
    indent( level );
    System.out.println( "AssignExp:" );
    level++;
    exp.lhs.accept( this, level );
    exp.rhs.accept( this, level );
  }

  public void visit (CallExp exp, int level ) {
    //TODO: Complete stub
  }
  
  public void visit (CompoundExp exp, int level ) {
    //TODO: Complete stub
  }    

  public void visit( IfExp exp, int level ) {
    //TODO: Update function
    indent( level );
    System.out.println( "IfExp:" );
    level++;
    exp.test.accept( this, level );
    exp.thenpart.accept( this, level );
    if (exp.elsepart != null )
       exp.elsepart.accept( this, level );
  }

  public void visit( IntExp exp, int level ) {
    //TODO: Update function
    indent( level );
    System.out.println( "IntExp: " + exp.value ); 
  }

  public void visit (NilExp exp, int level ) {
    //TODO: Complete stub
  }

  public void visit( OpExp exp, int level ) {
    //TODO: Update function
    indent( level );
    System.out.print( "OpExp:" ); 
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
      case OpExp.EQ:
        System.out.println( " = " );
        break;
      case OpExp.NE:
        System.out.println( " != " );
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
      default:
        System.out.println( "Unrecognized operator at line " + exp.row + " and column " + exp.col);
    }
    level++;
    exp.left.accept( this, level );
    exp.right.accept( this, level );
  }

  public void visit (ReturnExp exp, int level ) {
    //TODO: Complete stub
  }  

  public void visit( VarExp exp, int level ) {
    //TODO: Update function
    indent( level );
    //System.out.println( "VarExp: " + exp.name );
  }

  public void visit (WhileExp exp, int level ) {
    //TODO: Complete stub
  }
}
