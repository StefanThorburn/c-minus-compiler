package absyn;

public class FunctionDec extends Dec {

   public VarDecList params;
   public CompoundExp body;
   public int numArguments;

   public FunctionDec (int row, int col, NameTy type, String name, VarDecList params, CompoundExp body) {
      this.row = row;
      this.col = col;
      this.type = type;
      this.name = name;
      this.params = params;
      this.body = body;
      this.numArguments = 0;

      //Compute the necessary number of arguments to the function
      while (params != null) {
         //If the function has the form funcName (void), don't count that as an argument
         if (params.head != null && numArguments == 0 && params.head.type.type == NameTy.VOID) {
            break;
         }

         numArguments++;
         params = params.tail;
      }

   }

   public void accept( AbsynVisitor visitor, int level, boolean isAddr ) {
      visitor.visit( this, level, false );
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
