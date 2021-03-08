package absyn;

public class ErrorDec extends VarDec {

   public ErrorDec(int row, int col ) {
      this.row = row;
      this.col = col;
   }

   public void accept( AbsynVisitor visitor, int level ) {
      visitor.visit( this, level );
   }
}
