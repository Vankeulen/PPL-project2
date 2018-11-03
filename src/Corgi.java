
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Scanner;

public class Corgi {

	public static void main(String[] args) throws Exception {

		//System.out.print("Enter name of Corgi program file: ");
		 String programInput = "main()\n" +
				 "\n" +
				 "def fact( n )\n" +
				 "\n" +
				 "   if lt(n 1) \n" +
				 "      return 1\n" +
				 "   else\n" +
				 "      temp = fact( n-1 )\n" +
				 "      return n * temp\n" +
				 "   end\n" +
				 "\n" +
				 "end\n" +
				 "\n" +
				 "def main()\n" +
				 "\n" +
				 "  \"enter n: \"\n" +
				 "  n = input()\n" +
				 "  print( fact(n) )\n" +
				 "\n" +
				 "end\n";

		/* String programInput = "main()\n" +
				"\n" +
				"def main()\n" +
				"    whatever()\n" +
				"\n" +
				"end\n" +
				"\n" +
				"def whatever()\n" +
				"    print stuff(1, 2, 3, 4)\n" +
				"    newline\n" +
				"    print \"yeet\"" +
				"    newline\n" +
				"\n" +
				"end\n" +
				"\n" +
				"def stuff(a, b, c, d) \n" +
				"    return a + b * (c + d)\n" +
				"end";
				//*/
		/*
		Scanner keys = new Scanner(System.in);
		String name = keys.nextLine();
		//*/

		Lexer lex = new Lexer(new BufferedReader( new StringReader(programInput) ) );
		
		Parser parser = new Parser(lex);

		try {
			// start with <statements>
			Node root = parser.parseProgram();
			root.execute();
		} catch (Exception e) {
			System.out.println("");
			System.out.println(e);
			for (StackTraceElement el : e.getStackTrace()) {
				System.out.println(el);

			}
			// e.printStackTrace();
		}
		// display parse tree for debugging/testing:
//    TreeViewer viewer = new TreeViewer("Parse Tree", 0, 0, 800, 500, root );
		// execute the parse tree

	}// main

}
