/* sample code given by the professor

import absyn.*;

public class CodeGenerator implements AbsynVisitor {
    int mainEntry;
    int globalOffset;
    // constructor for initialization and all emitting routines
    
    public void visit(Absyntrees) {   // wrapper for post-order traversal
        // generate the prelude
        
        // generate the i/o routines
        
        // call the visit method for DecList
        visit(trees, 0, false);
        // generate finale
        
    }

    // implement all visit methods in AbsynVisitor
    public void visit(DecListdecs, int offset, Boolean isAddress) {
             
    }
        //...
}

*/