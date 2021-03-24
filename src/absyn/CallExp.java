package absyn;

public class CallExp extends Exp{
    public String func;
    public ExpList args;
    
   public CallExp(int row, int col, String func, ExpList args){
       this.row = row;
       this.col = col;
       this.func = func;
       this.args = args;
       dType = null;
   }

   public void accept( AbsynVisitor visitor, int level ) {
       visitor.visit( this, level );
    }
}
