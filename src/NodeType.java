//Contains instance variables: “String name”,“Dec def”, “int level”, and a constructor

import absyn.*;

public class NodeType {
    public String name;
    public Dec def;
    public int level;

    public static final int NO_DEC = NameTy.VOID - 1;
    public static final int VOID = NameTy.VOID;
    public static final int INT_PRIM = NameTy.INT;
    public static final int INT_ARR = NameTy.INT + 1;

    
    public NodeType(String name, Dec def, int level) {
        this.name = name;
        this.def = def;
        this.level = level;
    }

    public String toString() {
        return def.toString();
    }
}
