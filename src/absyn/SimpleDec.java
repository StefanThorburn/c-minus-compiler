package absyn;

public class SimpleDec extends VarDec {

   public SimpleDec(int row, int col, NameTy type, String name) {
      this.row = row;
      this.col = col;
      this.type = type;
      this.name = name;
   }

   public void accept( AbsynVisitor visitor, int level, boolean isAddr ) {
      visitor.visit( this, level, false );
   }

   public String toString() {
      return this.name + ": " + this.type;
   }
}
