package absyn;

public abstract class VarDec extends Dec {
   //Keeps track of whether a variable is global or not
   //0 is global, 1 is local
   public int nestLevel;
   //Represents the offset of this variable within the related stack frame for memory access
   public int offset;
}
