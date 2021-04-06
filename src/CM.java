/*
  Based on the tiny parser created by: Fei Song
*/
   
import absyn.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.FileReader;
   
class CM {
  // By default, don't show abstract syntax tree, do type checking, or output symbol tables
  public static boolean SHOW_TREE = false;
  public static boolean SHOW_SYM_TABLES = false;
  public static boolean GENERATE_CODE = false;
  public static boolean HAS_ERRORS = false;
  public static String INPUT_FILE = null;
  public static String FILE_NAME = null;

  static public void main(String argv[]) {    
    
    /* Parse command line arguments */
    // Iterate over all arguments, check for flags and input file
    for (String s : argv) {
      if (s.equals("-a")) {
        SHOW_TREE = true;
      }
      else if (s.equals("-s")) {
        SHOW_SYM_TABLES = true;
      }
      else if (s.equals("-c")) {
        GENERATE_CODE = true;
      }
      //Check if the string ends with '.cm'
      else if (s.length() > 3 && s.substring(s.length()-3).equals(".cm")) {        
        // If it does, make that the input file
        INPUT_FILE = s;
      }
    }

    if (INPUT_FILE == null) {
      System.out.println("No input file provided or incorrect file extension (must be .cm). Exiting...");
      System.exit(-1);
    }

    // Retrieve the actual file name from between the path and the extension
    // E.g. tests/sort.cm gives a result of 'sort'
    int nameStartIndex = INPUT_FILE.lastIndexOf('/');
    int nameEndIndex = INPUT_FILE.lastIndexOf('.');
    FILE_NAME = INPUT_FILE.substring(nameStartIndex + 1, nameEndIndex);
    
    /* Start the parser */
    try {
      // Save original stdout to switch back to it as needed
      PrintStream console = System.out;

      parser p = new parser(new Lexer(new FileReader(INPUT_FILE)));
      // implement "-a" and "-s" options
      Absyn result = (Absyn)(p.parse().value);  
      
      if (result != null) {
        if (!SHOW_SYM_TABLES && !SHOW_TREE && !GENERATE_CODE) {
          System.out.println("Showing errors only.");
          System.out.println("Use [-a] flag to print the abstract syntax tree" + "\n" 
                            + "Use [-s] flag to print the symbol table" + "\n"
                            + "Use [-c] to generate assembly code (.tm)");          
        }      
        // If the '-a' flag is set, print the abstract syntax tree to a .abs file
        if (SHOW_TREE) {
          System.out.println("Abstract syntax tree written to '" + FILE_NAME + ".abs'");

          //Redirect stdout
          File absFile = new File(FILE_NAME + ".abs");
          FileOutputStream absFos = new FileOutputStream(absFile);
          PrintStream absPS = new PrintStream(absFos);
          System.setOut(absPS);

          // Print abstract syntax tree to FILE_NAME.abs in current directory
          ShowTreeVisitor visitor = new ShowTreeVisitor();
          result.accept(visitor, 0, false); 

          //Reset stdout
          System.setOut(console);
        }
        if (SHOW_SYM_TABLES) {
          //Redirect stdout to a .sym file          
          File symFile = new File(FILE_NAME + ".sym");
          FileOutputStream symFos = new FileOutputStream(symFile);
          PrintStream symPS = new PrintStream(symFos);
          System.setOut(symPS);
        }        
        else {
          //Toss stdout output into the void while doing semantic analysis
          System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        }        

        // Perform semantic analysis
        SemanticAnalyzer analyzerVisitor = new SemanticAnalyzer();
        result.accept(analyzerVisitor, 0, false);

        //Restore stdout
        System.setOut(console);

        if (SHOW_SYM_TABLES) {
          //Print after having reported any errors
          System.out.println("Symbol table written to '" + FILE_NAME + ".sym'");
        }

        //Only generate code if the flag is set
        if (GENERATE_CODE) {

          //First, confirm that there are no syntax or semantic errors
          //HAS_ERRORS is true if there are any syntax errors or semantic errors, and false if both are error free
          HAS_ERRORS = (p.errorFound || analyzerVisitor.errorFound);

          if (HAS_ERRORS) {
            System.out.println("Cannot generate code while there are errors. Exiting...");
          }
          else {
            //No syntax or semantic errors, proceed with code generation
            System.out.println("Assembly code written to '" + FILE_NAME + ".tm'");

            //Redirect stdout to .tm file
            File tmFile = new File(FILE_NAME + ".tm");
            FileOutputStream tmFos = new FileOutputStream(tmFile);
            PrintStream tmPS = new PrintStream(tmFos);
            System.setOut(tmPS);

            //Perform code generation
            CodeGenerator generatorVisitor = new CodeGenerator();
            //result.accept(generatorVisitor, 0, false);
            generatorVisitor.visit(result, FILE_NAME + ".tm");
            
          }      
        }
      }

    } catch (FileNotFoundException e) {
      System.out.println("Could not find file '" + INPUT_FILE + "'. Check your spelling, and ensure it exists. Exiting...");
      System.exit(-1);      
    } catch (Exception e) {
      /* do cleanup here -- possibly rethrow e */
      e.printStackTrace();
    }
  }
}


