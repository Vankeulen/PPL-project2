
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Scanner;

public class Corgi {

	public static void main(String[] args) throws Exception {

		System.out.print("Enter name of Corgi program file: ");






		Scanner keys = new Scanner(System.in);
//		String name = keys.nextLine();
		String name = "src/factorialtest.txt";

		Lexer lex = new Lexer( name );


//		Lexer lex = new Lexer(new BufferedReader( new StringReader(programInput) ) );
		
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
