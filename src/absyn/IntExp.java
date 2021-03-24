package absyn;

public class IntExp extends Exp {
  public int value;

  public IntExp( int row, int col, int value ) {
    this.row = row;
    this.col = col;
    this.value = value;
    dType = null;
  }

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }
}
