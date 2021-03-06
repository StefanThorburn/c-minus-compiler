package absyn;

public class NameTy extends Exp {
  public final static int INT = 0;
  public final static int VOID = NameTy.INT + 1;
  
  public int type;

  public NameTy( int row, int col, int type ) {
    this.row = row;
    this.col = col;
    this.type = type;
  }

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }
}
