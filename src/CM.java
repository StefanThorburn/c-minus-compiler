/*
  Based on the tiny parser created by: Fei Song
*/
   
import java.io.*;
import absyn.*;
   
class CM {
  public final static boolean SHOW_TREE = true;
  static public void main(String argv[]) {    
    /* Start the parser */
    try {
      parser p = new parser(new Lexer(new FileReader(argv[0])));
      // implement "-a" and "-s" options
      Absyn result = (Absyn)(p.parse().value);      
      if (SHOW_TREE && result != null) {
         System.out.println("The abstract syntax tree is:");
         SemanticAnalyzer visitor = new SemanticAnalyzer();
         result.accept(visitor, 0); 
      }
    } catch (Exception e) {
      /* do cleanup here -- possibly rethrow e */
      e.printStackTrace();
    }
  }
}


