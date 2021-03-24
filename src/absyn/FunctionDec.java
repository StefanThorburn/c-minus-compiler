package absyn;

public class FunctionDec extends Dec {

   public VarDecList params;
   public CompoundExp body;

   public FunctionDec (int row, int col, NameTy type, String name, VarDecList params, CompoundExp body) {
      this.row = row;
      this.col = col;
      this.type = type;
      this.name = name;
      this.params = params;
      this.body = body;
   }

   public void accept( AbsynVisitor visitor, int level ) {
      visitor.visit( this, level );
   }

   public String toString() {
      VarDecList dummy = params;

      StringBuilder sb = new StringBuilder(this.name + ": (");
      while( dummy != null ) {
         //If the parameter is an int array, do this not-at-all-spaghetti-code workaround rather than printing the primitive type
         if (dummy.head instanceof ArrayDec && dummy.head.type.type == NameTy.INT) {
            sb.append(new NameTy(-1, -1, NameTy.INT_ARR));
         }
         else {
            sb.append(dummy.head.type);
         }         
         dummy = dummy.tail;

         if (dummy != null) {
            sb.append(", ");
         }
       } 
       sb.append(") => ");

       sb.append(type);
      
      return sb.toString();
   }
}
