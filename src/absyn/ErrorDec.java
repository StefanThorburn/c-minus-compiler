package absyn;

public class ErrorDec extends VarDec {

   public ErrorDec(int row, int col ) {
      this.row = row;
      this.col = col;
      //By default, assign a 'no declaration' type
      this.type = new NameTy(row, col, NameTy.NO_DEC);
   }

   public ErrorDec(int row, int col, NameTy type, String name) {
      this(row, col);
      this.type = type;
      this.name = name;
   }

   public void accept( AbsynVisitor visitor, int level ) {
      visitor.visit( this, level );
   }

   public String toString() {
      return "(ERROR_DEC): " + type;
   }
}
