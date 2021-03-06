package absyn;

public class WhileExp extends Exp {
    public Exp test;
    public Exp body;
    
    public WhileExp(int row, int col, Exp test, Exp body){
        this.row=row;
        this.col=col;
        this.body = body;
    }
}
