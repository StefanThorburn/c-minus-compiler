package absyn;

public class ErrorExp extends Exp {

  public ErrorExp( int row, int col ) {
    this.row = row;
    this.col = col;
    dType = null;
  }

  public void accept( AbsynVisitor visitor, int level, boolean isAddr ) {
    visitor.visit( this, level, false );
  }
}

