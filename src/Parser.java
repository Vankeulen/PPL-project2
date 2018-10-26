
/*
    This class provides a recursive descent parser 
    for Corgi (a simple calculator language),
    creating a parse tree which can be interpreted
    to simulate execution of a Corgi program
 */
import java.util.*;
import java.io.*;

public class Parser {

	private Lexer lex;

	public Parser(Lexer lexer) {
		lex = lexer;
	}

	public Node parseProgram() {
		// rule is <funcCall> <funcDefs>?
		
		Node funcCall = parseFuncCall();
		
		Token next = lex.getNextToken();
		if (next.isKind("def")) {
			lex.putBackToken(next);
			return new Node("program", "", funcCall, parseFuncDefs(), null);
		}
		
		return new Node("program", "", funcCall, null, null);
	}
	
	
	// Added this whole method to parse all func defs
	private Node parseFuncDefs() {
		// rule is <funcDef> <funcDefs>?
		
		Node funcDef = parseFuncDef();
		
		Token defCheck = lex.getNextToken();
		if (defCheck.isKind("def")) {
			lex.putBackToken(defCheck);
			Node moreDefs = parseFuncDefs();
			return new Node("funcDefs", funcDef, moreDefs, null);
		}
		
		return new Node("funcDefs", funcDef, null, null);
	}
	
	// Added this whole method to parse individual func defs
	private Node parseFuncDef() {
		// rule is def <var> ( <params>? ) <statements>? end
		
		Token defCheck = lex.getNextToken();
		if (!defCheck.isKind("def")) {
			throw new RuntimeException("Trying to parse a function def, but no def keyword!");
		}
		
		Token varCheck = lex.getNextToken();
		if (!varCheck.isKind("var")) {
			throw new RuntimeException("Function definitions must have names");
		}
		
		Token openParenCheck = lex.getNextToken();
		if (!openParenCheck.isKind("single") || !openParenCheck.getDetails().equals("(")) {
			throw new RuntimeException("Function definitions must have a params list");
		}
		
		Node params = null;
		Node statements = null;
		
		Token closeParenCheck = lex.getNextToken();
		if (!closeParenCheck.isKind("single") || !closeParenCheck.getDetails().equals(")")) {
			
			lex.putBackToken(closeParenCheck);
			params = parseParams();
			closeParenCheck = lex.getNextToken();
			if (!closeParenCheck.isKind("single") || !closeParenCheck.getDetails().equals(")")) {
				throw new RuntimeException("Unexpected token in parameters " + closeParenCheck);
			}
		}
			
		Token endCheck = lex.getNextToken();
		if (!endCheck.isKind("end")) {
			
			lex.putBackToken(endCheck);
			statements = parseStatements();
			endCheck = lex.getNextToken();
			if (!endCheck.isKind("end")) {
				throw new RuntimeException("Unexpected token, function defs must end with 'end'!");
			}
		}
			
		return new Node("funcDef", varCheck.getDetails(), params, statements, null);
		
		
	} // </funcDef>
	
	private Node parseStatements() {
		System.out.println("-----> parsing <statements>:");

		Node first = parseStatement();

		// look ahead to see if there are more statement's
		Token token = lex.getNextToken();

		if (token.isKind("eof")) {
			return new Node("stmts", first, null, null);
		} else if (token.isKind("end") || token.isKind("else")) {
			// added this to handle last statement in method block
			lex.putBackToken(token);
			return new Node("stmts", first, null, null);
			
		} else {
			lex.putBackToken(token);
			Node second = parseStatements();
			return new Node("stmts", first, second, null);
		}
	}// <statements>

	private Node parseStatement() {
		System.out.println("-----> parsing <statement>:");

		Token token = lex.getNextToken();

		// ---------------->>>  print <string>  or   print <expr>
		if (token.isKind("print")) {
			token = lex.getNextToken();

			if (token.isKind("string")) {// print <string>
				return new Node("prtstr", token.getDetails(),
						null, null, null);
			} else {// must be first token in <expr>
				// put back the token we looked ahead at
				lex.putBackToken(token);
				Node first = parseExpr();
				return new Node("prtexp", first, null, null);
			}
			// ---------------->>>  newline
		} else if (token.isKind("newline")) {
			return new Node("nl", null, null, null);
		} else if (token.isKind("return")) {
			// Added return statement 
			return new Node("return", parseExpr(), null, null);
		} else if (token.isKind("var")) {
			// --------------->>>   <var> = <expr>
			String varName = token.getDetails();
			Token nameToken = token;
			token = lex.getNextToken();
			
			if (token.isKind("single")) {
				if (token.getDetails().equals("=")) {
					Node first = parseExpr();
					return new Node("sto", varName, first, null, null);
				}
				
				if (token.getDetails().equals("(")) {
					lex.putBackToken(token);
					lex.putBackToken(nameToken);
					return parseFuncCall();
				}
			}
			else if(varName.equals("if")){
				lex.putBackToken(token);
				Node expr = parseExpr();
				Node ifStatements = parseStatements();

				Token elseToken = lex.getNextToken();
				errorCheck(elseToken, "else");

				Node elseStatements = parseStatements();

				Token endToken = lex.getNextToken();
				errorCheck(endToken, "end");

                return new Node("stmt", "if", ifStatements, expr, elseStatements);
			}

			
			throw new RuntimeException("Unexpected Symbol after varname: " + token);
			// errorCheck(token, "single", "=");
		} else {
			System.out.println("Token " + token
					+ " can't begin a statement");
			System.exit(1);
			return null;
		}

	}// <statement>

	private Node parseExpr() {
		System.out.println("-----> parsing <expr>");

		Node first = parseTerm();

		// look ahead to see if there's an addop
		Token token = lex.getNextToken();

		if (token.matches("single", "+")
				|| token.matches("single", "-")) {
			Node second = parseExpr();
			return new Node(token.getDetails(), first, second, null);
		}
		if(token.matches("single","(")){
			return parseArgs();
		}
		// is just one term
		lex.putBackToken(token);
		return first;

	}// <expr>

	private Node parseTerm() {
		System.out.println("-----> parsing <term>");

		Node first = parseFactor();

		// look ahead to see if there's a multop
		Token token = lex.getNextToken();

		if (token.matches("single", "*")
				|| token.matches("single", "/")) {
			Node second = parseTerm();
			return new Node(token.getDetails(), first, second, null);
		} else {// is just one factor
			lex.putBackToken(token);
			return first;
		}

	}// <term>
	
	
	private Node parseParams() {
		// rule is <var> <params>?
		Token token = lex.getNextToken();
		if (!token.isKind("var")) { throw new RuntimeException("Unexpected token " + token + " in parameters"); }
		
		Token commaCheck = lex.getNextToken();
		if (commaCheck.isKind("single") && commaCheck.getDetails().equals(",")) {
			Node nextParams = parseParams();
			return new Node("params", token.getDetails(), nextParams, null, null);
		}
		lex.putBackToken(commaCheck);
		return new Node("params", token.getDetails(), null, null, null);
	}
	
	private Node parseArgs() {
		// rule is <expr> <args>?
		Node expr = parseExpr();
		Token commaCheck = lex.getNextToken();
		if (commaCheck.isKind("single") && commaCheck.getDetails().equals(",")) {
			Node nextArgs = parseArgs();
			return new Node("args", expr, nextArgs, null);
		}
		lex.putBackToken(commaCheck);
		return new Node("args", expr, null, null);
	}
	
	private Node parseFuncCall() {
		// rule is <var> ( <args>? ) 
		Token token = lex.getNextToken();
		if (!token.isKind("var")) { throw new RuntimeException("funcCall must start with a varname"); }
		
		Token openParen = lex.getNextToken();
		if (!openParen.isKind("single") || !openParen.getDetails().equals("(")) {
			throw new RuntimeException("Expeted open paren at func call, found " + openParen);
		}
		
		Token checkEndParen = lex.getNextToken();
		if (checkEndParen.isKind("single") && checkEndParen.getDetails().equals(")")) {
			return new Node("funcCall", token.getDetails(), null, null, null);
		} else {
			lex.putBackToken(checkEndParen);
		}

		Node args = parseArgs();
		checkEndParen = lex.getNextToken();

		if (checkEndParen.isKind("single") && checkEndParen.getDetails().equals(")")) {
			return new Node("funcCall", token.getDetails(), args, null, null);
		} else {
			throw new RuntimeException("Mismatched parens");
		}
				
	}
	
	private Node parseFactor() {
		System.out.println("-----> parsing <factor>");

		Token token = lex.getNextToken();

		if (token.isKind("num")) {
			return new Node("num", token.getDetails(), null, null, null);
		} else if (token.isKind("var")) {
			
			Token checkParen = lex.getNextToken();
			if (checkParen.isKind("single") && checkParen.getDetails().equals("(")){
				lex.putBackToken(checkParen);
				lex.putBackToken(token);
				return parseFuncCall();
				
				
			} else {
				lex.putBackToken(checkParen);
				return new Node("var", token.getDetails(), null, null, null);
			}
			
		} else if (token.matches("single", "(")) {
			Node first = parseExpr();
			token = lex.getNextToken();
			errorCheck(token, "single", ")");
			return first;
		} else if (token.isKind("bif0")) {
			String bifName = token.getDetails();
			token = lex.getNextToken();
			errorCheck(token, "single", "(");
			token = lex.getNextToken();
			errorCheck(token, "single", ")");

			return new Node(bifName, null, null, null);
		} else if (token.isKind("bif1")) {
			String bifName = token.getDetails();
			token = lex.getNextToken();
			errorCheck(token, "single", "(");
			Node first = parseExpr();
			token = lex.getNextToken();
			errorCheck(token, "single", ")");

			return new Node(bifName, first, null, null);
		} else if (token.isKind("bif2")) {
			String bifName = token.getDetails();
			token = lex.getNextToken();
			errorCheck(token, "single", "(");
			Node first = parseExpr();
			token = lex.getNextToken();
			errorCheck(token, "single", ",");
			Node second = parseExpr();
			token = lex.getNextToken();
			errorCheck(token, "single", ")");

			return new Node(bifName, first, second, null);
		} else if (token.matches("single", "-")) {
			Node first = parseFactor();
			return new Node("opp", first, null, null);
		} else {
			System.out.println("Can't have factor starting with " + token);
			System.exit(1);
			return null;
		}

	}// <factor>

	// check whether token is correct kind
	private void errorCheck(Token token, String kind) {
		if (!token.isKind(kind)) {
			System.out.println("Error:  expected " + token
					+ " to be of kind " + kind);
			System.exit(1);
		}
	}

	// check whether token is correct kind and details
	private void errorCheck(Token token, String kind, String details) {
		if (!token.isKind(kind)
				|| !token.getDetails().equals(details)) {
			System.out.println("Error:  expected " + token
					+ " to be kind=" + kind
					+ " and details=" + details);
			System.exit(1);
		}
	}

}
