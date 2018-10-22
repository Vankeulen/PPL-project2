/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.HashMap;
import java.util.Map;

/**
 * All logic for a simple calculator language
 */
public class Calculator {
	
	private static boolean letter(int code) {
		return 'a' <= code && code <= 'z'
				|| 'A' <= code && code <= 'Z';
	}

	private static boolean digit(int code) {
		return '0' <= code && code <= '9';
	}
	
	private static char[] operators = {
		'+', '-', '*', '/', 
		'(', ')', ',', '=',
	};
	private static boolean operator(int code) {
		for (int i = 0; i < operators.length; i++) {
			if (code == operators[i]) { return true; }
		}
		return false;
	}
	
	/** Holds logic for parsing a Calculator program tree 
	 * from a stream of {@see Token}s. (A {@see Lexer}) */
	public static class Parser {
		
		private Lexer lex;
		public Parser(Lexer lex) {
			this.lex = lex;
		}
		
		/** Parses an expression */ 
		public Node parseExpression() {
			Node term = parseTerm();
			
			Token t = lex.peekToken();
			if (t.isKind("Operator")) {
				String op = "";
				Node expr2 = null;
				if (t.getDetails().equals("-")) {
					op = "-";
					lex.next(); // Consume the "-" token
					expr2 = parseExpression();
				}
				if (t.getDetails().equals("+")) {
					op = "+";
					lex.next(); // Consume the "+" token
					expr2 = parseExpression();
				}
				if (!op.equals("")) {
					return new Node(op, term, expr2, null);
				}
			}
			
			return term;
		}
		
		public Node parseTerm() {
			Node factor = parseFactor();
			
			Token t = lex.peekToken();
			if (t.isKind("Operator")) {
				String op = "";
				Node term2 = null;
				if (t.getDetails().equals("*")) {
					op = "*";
					lex.next(); // Consume the "*" token
					term2 = parseTerm();
				}
				if (t.getDetails().equals("/")) {
					op = "/";
					lex.next(); // Consume the "/" token
					term2 = parseTerm();
				}
				if (!op.equals("")) {
					return new Node(op, factor, term2, null);
				}
			}
			
			return factor;
		}
		
		public Node parseFactor() {
			Token t = lex.peekToken();
			
			if (t.isKind("Operator")) {
				if (t.getDetails().equals("(")) { // Nested (expression)
					lex.next(); // Consume "(" token
					
					Node expr = parseExpression();
					
					t = lex.peekToken;
					if (!t.isKind("Operator") && !t.getDetails().equals(")")) {
						throw new RuntimeException("Mismatched parens, unexpected " + t);
					}
					lex.next(); // Consume ")" token
					
					return expr;
				} else if (t.getDetails().equals("-")) { // -factor
					lex.next(); // Consume "-" token
					
					Node factor = parseFactor();
					
					return new Node("-", factor, null, null);
				}
				
				// If not "(" or "-", operators are invalid!
				throw new RuntimeException("Unexpected Character " + t);
			
			} else if (t.isKind("Number")) {
				lex.next(); // Consume that number
				return new Node("num", t.getDetails(), null, null, null);
			} else if (t.isKind("Name")) {
				lex.next(); // Consume that name
				return new Node("var", t.getDetails(), null, null, null);
			}
			
			throw new RuntimeException("Unexpected " + t);
		}
		
		
	}
	
	/** Turns a String of characters into a stream of {@see Token}s */
	public static class Lexer {
		
		private final String input;
		private int i;
		
		private Token token;
		private Token peekToken;
		
		public Lexer(String input) {
			this.input = input;
			i = 0;
			peekToken = getNextToken();
		}
		
		/** Next token that has not yet been consumed. */
		public Token peekToken() {
			return this.peekToken;
		}
		
		/** Last non whitespace token passed. */
		public Token lastToken() {
			return this.token;
		}
		
		/** Move forward one token. */
		public void move() {
			if (!peekToken.isKind("Whitespace")) {
				token = peekToken;
			}
			peekToken = getNextToken();
		}
		
		/** Move to next non-whitespace token. */
		public void next() {
			move();
			while (peekToken.isKind("Whitespace")) {
				move();
			}
		}
		
		/** */
		private Token getNextToken() {
			if (i >= input.length()) {
				return new Token("OutOfCharacters", "");
			}
			
			char sym = input.charAt(i);
			
			if (sym == '\r' || sym == '\n' || sym == '\t' || sym == ' ') {
				i++;
				return new Token("Whitespace", ""+sym);
			}
			
			if ('a' <= sym && sym <= 'z') {
				return new Token("Name", readName());
			}
			
			if (digit(sym) || sym == '.') {
				return new Token("Number", readNumber());
			}
			
			if (sym == '\"') {
				return new Token("String", readString());
			}
			
			if (operator(sym)) {
				i++;
				return new Token("Operator", ""+sym);
			}
			
			return new Token("UnknownCharacter", ""+sym);
		}
		private String readName() {
			StringBuilder name = new StringBuilder();
			//String name = "";
			char sym = input.charAt(i);
			
			while (true) {
				
				if (letter(sym) || digit(sym)) {
					name.append((char)sym);
					//name += sym;
					i++;
					if (i >= input.length()) { return name.toString(); }
					
					sym = input.charAt(i);
				} else {
					return name.toString();
					//return name;
				}				
			}
		}
		
		private String readString() {
			StringBuilder string = new StringBuilder();
			char sym = input.charAt(i);
			
			if (sym == '\"') {
				string.append(sym);
				i++;
				sym = input.charAt(i);
				while (true) {
					if (sym == '\n') { 
						throw new RuntimeException("newline in string literal!");
					}
					
					if (sym != '\"') {
						string.append(sym);
						i++;
						sym = input.charAt(i);
					} else {
						string.append(sym);
						i++;
						return string.toString();
					}
				}
			}
			
			throw new RuntimeException("Trying to read a string, but no quotes are around!");
		}
		
		private String readNumber() {
			StringBuilder number = new StringBuilder();
			char sym = input.charAt(i);
			boolean firstIsDot = sym == '.';
			
			boolean hasDot = false;
			while (true) {
				sym = input.charAt(i);
				
				if (digit(sym) || sym == '.') {
					if (hasDot && sym == '.') {
						throw new RuntimeException("Multiple dots in number.");
					}
					
					number.append((char)sym);
					i++;	
				}
					
				if (i >= input.length() || !(digit(sym) || sym == '.')) { 
					if (firstIsDot && number.length() == 1) {
						throw new RuntimeException("Only dots in number");
					}
					return number.toString(); 
				}
			}
		}
		
	}
	// Logic to run a calculator program tree...
	public static class Runner {
		public Map<String, Double> values;
		public Node program;
		
		public Runner(Node program) {
			this.program = program;
			values = new HashMap<>();
		}
		
		public Runner(Node program, Map<String, Double> values) {
			this.program = program;
			this.values = values;
		}
		
		public double evaluate() {
			return eval(program);
		}
		
		public double eval(Node n) {
			if (n.isKind("var")) {
				String varName = n.getInfo();
				return values.containsKey(varName) ? values.get(varName) : 0;
			}
			if (n.isKind("num")) {
				try {
					return Double.parseDouble(n.getInfo());
				} catch (Exception e) { return 0; }				
			}
			
			if (n.isKind("+")) {
				return eval(n.getFirst()) + eval(n.getSecond());
			}
			if (n.isKind("*")) {
				return eval(n.getFirst()) * eval(n.getSecond());
			}
			if (n.isKind("/")) {
				return eval(n.getFirst()) / eval(n.getSecond());
			}
			if (n.isKind("-")) {
				if (n.getSecond() != null) {
					return eval(n.getFirst()) - eval(n.getSecond());
				} else {
					return - eval(n.getFirst());
				}
			}
			// if (n.isKind(kind))
			return 0;
		}
			
		
		
	}
	
	public static void main(String[] args) {
		String input = "5 * a + 3 * (b + 4) * c";
		
		Lexer lex = new Lexer(input);
		
		Token t;
		while (true) {
			t = lex.getNextToken();
			System.out.println(t);
			if (t.isKind("OutOfCharacters") || t.isKind("InvalidCharacter")) {
				break;
			}
		}
		
		lex = new Lexer(input);
		Parser parser = new Parser(lex);
		Node expr = parser.parseExpression();
		
		Map<String,Double> data = new HashMap<>();
		data.put("a", 5.0);
		data.put("b", 15.0);
		data.put("c", 25.0);
		Runner runner1 = new Runner(expr);
		Runner runner2 = new Runner(expr, data);
		
		System.out.println("With a=0,b=0,c=0: " + runner1.evaluate());
		System.out.println("With a=5,b=15,c=25: " + runner2.evaluate());
		
		// double value = expr.eval();
		// System.out.println("Evaluated to " + value);
		
		new TreeViewer("Yeet", 0, 0, 1600, 1200, expr);
		
	}
	
	
		
	
	
}
