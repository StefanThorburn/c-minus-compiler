package absyn;

public class NameTy extends Exp {
  public final static int VOID = 0;
  public final static int INT = NameTy.VOID + 1;
  //Used only for type checking, not assigned to types of nodes in the tree
  public final static int INT_ARR = NameTy.INT + 1;
  public final static int NO_DEC = NameTy.VOID - 1;
  
  public int type;

  public NameTy( int row, int col, int type ) {
    this.row = row;
    this.col = col;
    this.type = type;
  }

  public void accept( AbsynVisitor visitor, int level, boolean isAddr ) {
    visitor.visit( this, level, isAddr );
  }

  @Override
  public String toString() {
    switch( type ) {
      case NameTy.INT:
        return "int";
      case NameTy.INT_ARR:
        return "int []";
      case NameTy.NO_DEC:
        return "(NO TYPE)";
      default:
        return "void";
    }

  }
}
