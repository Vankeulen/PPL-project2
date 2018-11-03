
import java.io.BufferedReader;
import java.io.StringReader;
// Practice Problem 3

public class PathTrip {
	public static void main(String[] args) {
		String programInput = "7 [ D 3 [ L M ] ] 5 R".toLowerCase();
		Lexer lex = new Lexer(new BufferedReader(new StringReader(programInput)));
		Token t = lex.getNextToken();
		
		while (true) {
			if (t == null || t.isKind("eof")) {
				break;
			}
			t = lex.getNextToken();
		}
		
		lex = new Lexer(new BufferedReader(new StringReader(programInput)));
		Parser parser = new Parser(lex);
		try {
			Node root = parser.parseTrip();
			TreeViewer it = new TreeViewer("tripPath", 0, 0, 1600, 1200, root);
			// root.execute();
		} catch (Exception e) {
			System.out.println("");
			System.out.println(e);
			for (StackTraceElement el : e.getStackTrace()) {
				System.out.println(el);

			}
			// e.printStackTrace();
		}
		
	}
	
	public static class Parser {
		
		private Lexer lex;
		public Parser(Lexer lex) {
			this.lex = lex;
		}
		
		public Node parseTrip() {
			// \/ part 1
			// rule is <path> <trip>?

            // <trip> -> <path>
			Node path = parsePath();

            Token t = lex.getNextToken();
            lex.putBackToken(t);
            // <trip> -> <path> <trip>
            if (t.isKind("num") || t.isKind("var")) {
				Node trip = parseTrip();
				return new Node("t", path, trip, null);
			}
			
			return new Node("t", path, null, null);
			// /\ part 1
		}
		
		public Node parsePath() {
			// \/ part 2
			// rule is <action> | <number> <action> | <number> [ <trip> ]
			Token t = lex.getNextToken();
			Node n = null, a = null, trip = null;
			//<path> -> A
			if (t.isKind("var")) {
				a = new Node("a", t.getDetails(), null, null, null);
				return new Node("p", n, a, trip);
			}
			
			if (!t.isKind("num")) {
				throw new RuntimeException("Invalid number, " + t);
			}

			//N A | N [ <trip> ]
			n = new Node("n", t.getDetails(), null, null, null);

			Token t2 = lex.getNextToken();
			// N A
			if (t2.isKind("var")) {
				a = new Node("a", t2.getDetails(), null, null, null);
				return new Node("p", n, a, trip);
			}
			// N [ <trip> ]
			if (!t2.getDetails().equals("[")) {
				throw new RuntimeException("expected open brace, had " + t2);
			}
			
			trip = parseTrip();
			
			Token t3 = lex.getNextToken();
			if (!t3.getDetails().equals("]")) {
				throw new RuntimeException("expected close brace, had " + t3);
			}
				
			return new Node("p", n, a, trip);
			// /\ part 2
		}
		
		
	}
}
