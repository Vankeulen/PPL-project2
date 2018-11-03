
/*  a Node holds one node of a parse tree
    with several pointers to children used
    depending on the kind of node
 */
import java.util.*;
import java.io.*;
import java.awt.*;

public class Node {

	// Added this type to hold runtime information.
	
	
	public static class RuntimeEnv {
		public MemTable table = new MemTable();
		public Map<String, Node> funcDefs = new HashMap<>();
		public boolean returning = false;
	}

	public static int count = 0;  // maintain unique id for each node

	private int id;

	public boolean isKind(String kind) { return kind.equals(this.kind); }
	public String getInfo() { return this.info; }

	private String kind;  // non-terminal or terminal category for the node
	private String info;  // extra information about the node such as
	// the actual identifier for an I

	public Node getFirst() { return first; }
	public Node getSecond() { return second; }
	public Node getThird() { return third; }

	// references to children in the parse tree
	private Node first, second, third;

	// Added this global field to replace the previously global 'MemTable'
	private static RuntimeEnv globalRuntime = new RuntimeEnv();

	private static Scanner keys = new Scanner(System.in);

	// construct a common node with no info specified
	public Node(String k, Node one, Node two, Node three) {
		kind = k;
		info = "";
		first = one;
		second = two;
		third = three;
		id = count;
		count++;
		System.out.println(this);
	}

	// construct a node with specified info
	public Node(String k, String inf, Node one, Node two, Node three) {
		kind = k;
		info = inf;
		first = one;
		second = two;
		third = three;
		id = count;
		count++;
		System.out.println(this);
	}

	// construct a node that is essentially a token
	public Node(Token token) {
		kind = token.getKind();
		info = token.getDetails();
		first = null;
		second = null;
		third = null;
		id = count;
		count++;
		System.out.println(this);
	}

	public String toString() {
		return "#" + id + "[" + kind + "," + info + "]<" + nice(first)
				+ " " + nice(second) + ">";
	}

	public String nice(Node node) {
		if (node == null) {
			return "";
		} else {
			return "" + node.id;
		}
	}

	// produce array with the non-null children
	// in order
	private Node[] getChildren() {
		int count = 0;
		if (first != null) {
			count++;
		}
		if (second != null) {
			count++;
		}
		if (third != null) {
			count++;
		}
		Node[] children = new Node[count];
		int k = 0;
		if (first != null) {
			children[k] = first;
			k++;
		}
		if (second != null) {
			children[k] = second;
			k++;
		}
		if (third != null) {
			children[k] = third;
			k++;
		}

		return children;
	}

	//******************************************************
	// graphical display of this node and its subtree
	// in given camera, with specified location (x,y) of this
	// node, and specified distances horizontally and vertically
	// to children
	public void draw(Camera cam, double x, double y, double h, double v) {

		System.out.println("draw node " + id);

		// set drawing color
		cam.setColor(Color.black);

		String text = kind;
		if (!info.equals("")) {
			text += "(" + info + ")";
		}
		cam.drawHorizCenteredText(text, x, y);

		// positioning of children depends on how many
		// in a nice, uniform manner
		Node[] children = getChildren();
		int number = children.length;
		System.out.println("has " + number + " children");

		double top = y - 0.75 * v;

		if (number == 0) {
			return;
		} else if (number == 1) {
			children[0].draw(cam, x, y - v, h / 2, v);
			cam.drawLine(x, y, x, top);
		} else if (number == 2) {
			children[0].draw(cam, x - h / 2, y - v, h / 2, v);
			cam.drawLine(x, y, x - h / 2, top);
			children[1].draw(cam, x + h / 2, y - v, h / 2, v);
			cam.drawLine(x, y, x + h / 2, top);
		} else if (number == 3) {
			children[0].draw(cam, x - h, y - v, h / 2, v);
			cam.drawLine(x, y, x - h, top);
			children[1].draw(cam, x, y - v, h / 2, v);
			cam.drawLine(x, y, x, top);
			children[2].draw(cam, x + h, y - v, h / 2, v);
			cam.drawLine(x, y, x + h, top);
		} else {
			System.out.println("no Node kind has more than 3 children???");
			System.exit(1);
		}

	}// draw

	public static void error(String message) {
		System.out.println(message);
		System.exit(1);
	}

	// sets up the next environment with parameters.
	// they are applied in order
	// between the args (first of this) and params (first of funcDef) sub nodes
	private void setupParams(RuntimeEnv next, Node funcDef, RuntimeEnv prev){
		Node params = funcDef.first;
		Node args = first;

		while (args != null && params != null) {
			Node arg = args.first;
			args = args.second;
			String param = params.info;
			params = params.first;
			double val = arg.evaluate(prev);

			System.out.println("Setting up param " + param + " = " + val);
			next.table.store(param, val);
		}
		// Warn about mismatcher arg counts
		if (params == null && args != null) {
			System.out.println("WARNING: Mismatched parameters for call of " + info + ", too many args");
		}
		if (args == null && params != null) {
			System.out.println("WARNING: Mismatched parameters for call of " + info + ", too few args");
		}
	}

	// Executes a function, returns the runtime env it used when done.
	// or null if the function did not exist.
	public RuntimeEnv funcCall(RuntimeEnv runtime) {
		System.out.println("exec funcCall " + info);

		if (runtime.funcDefs.containsKey(info)) {
			RuntimeEnv next = new RuntimeEnv();
			Node func = runtime.funcDefs.get(info);
			Node statements = func.second;

			next.funcDefs = runtime.funcDefs;
			next.table = new MemTable();
			next.table.store("_retval", 0);

			setupParams(next, func, runtime);

			if (statements != null) {
				statements.execute(next);
			}

			return next;

		} else {
			System.out.println("Cannot find function " + info);
			return null;
		}
	}

	//Executes the node with the global runtime environment.
	public void execute() { execute(globalRuntime); }

	// ask this node to execute itself
	// (for nodes that don't return a value)
	public void execute(RuntimeEnv runtime) {

		if (kind.equals("program")) {
			if (second != null) {
				// register funcdefs
				second.execute(runtime);
			}
			if (first != null) {
				// Execute original func call.
				System.out.println("Entryfunc is " + first.info);
				first.execute(runtime);

			}
		} else if (kind.equals("funcDefs")) {
			// actual definitions
			first.execute(runtime);

			if (second != null) {
				// next definitions
				second.execute(runtime);
			}

		} else if (kind.equals("funcDef")) {
			// Registers a function to the runtime environment.
			System.out.println("Registering function " + info);
			runtime.funcDefs.put(info, this);

		} else if (kind.equals("funcCall")) {
			// Just call the function,
			// using the current runtime to pass params...
			funcCall(runtime);

		} else if (kind.equals("stmts")) {
			if (first != null) {
				// actual statement
				first.execute(runtime);
				
				if (!runtime.returning && second != null) {
					// Next statements
					second.execute(runtime);
				}
			}
		} else if (kind.equals("if")) {
			double value = first.evaluate(runtime);
			
			if (value != 0) {
				second.execute(runtime);
			} else {
				third.execute(runtime);
			}
			
		} else if (kind.equals("return")) {
			// Evaluate expression, set value into runtime.
			double val = first.evaluate(runtime);
			runtime.table.store("_retval", val);
			runtime.returning = true;

		} else if (kind.equals("prtstr")) {
			System.out.print(info);
		} else if (kind.equals("prtexp")) {
			double value = first.evaluate(runtime);
			System.out.print(value);
		} else if (kind.equals("nl")) {
			System.out.print("\n");
		} else if (kind.equals("sto")) {
			double value = first.evaluate(runtime);
			runtime.table.store(info, value);
		} else {
			error("Unknown kind of node [" + kind + "]");
		}

	}// execute

	// public double evaluate() { return evaluate(globalRuntime); }
	// compute and return value produced by this node
	public double evaluate(RuntimeEnv runtime) {
		if (kind.equals("num")) {
			return Double.parseDouble(info);

		} else if (kind.equals("funcCall")) {
			// Call the function
			RuntimeEnv results = funcCall(runtime);
			if (results == null) { return 0; }
			// Return the return value.
			return results.table.retrieve("_retval");

		} else if (kind.equals("var")) {
			return runtime.table.retrieve(info);
		} else if (kind.equals("+") || kind.equals("-")) {
			double value1 = first.evaluate(runtime);
			double value2 = second.evaluate(runtime);
			if (kind.equals("+")) {
				return value1 + value2;
			} else {
				return value1 - value2;
			}
		} else if (kind.equals("*") || kind.equals("/")) {
			double value1 = first.evaluate(runtime);
			double value2 = second.evaluate(runtime);
			if (kind.equals("*")) {
				return value1 * value2;
			} else {
				return value1 / value2;
			}
		} else if (kind.equals("input")) {
			return keys.nextDouble();
		} else if (kind.equals("sqrt") || kind.equals("cos")
				|| kind.equals("sin") || kind.equals("atan")) {
			double value = first.evaluate(runtime);

			if (kind.equals("sqrt")) {
				return Math.sqrt(value);
			} else if (kind.equals("cos")) {
				return Math.cos(Math.toRadians(value));
			} else if (kind.equals("sin")) {
				return Math.sin(Math.toRadians(value));
			} else if (kind.equals("atan")) {
				return Math.toDegrees(Math.atan(value));
			} else {
				error("unknown function name [" + kind + "]");
				return 0;
			}

		} else if (kind.equals("pow")) {
			double value1 = first.evaluate(runtime);
			double value2 = second.evaluate(runtime);
			return Math.pow(value1, value2);
		} else if (kind.equals("opp")) {
			double value = first.evaluate(runtime);
			return -value;
		} else if (kind.equals("lt")) {
			double val1 = first.evaluate(runtime);
			double val2 = second.evaluate(runtime);
			return val1 < val2 ? 1 : 0;
		} else if (kind.equals("le")) {
			double val1 = first.evaluate(runtime);
			double val2 = second.evaluate(runtime);
			return val1 <= val2 ? 1 : 0;
		} else if (kind.equals("eq")) {
			double val1 = first.evaluate(runtime);
			double val2 = second.evaluate(runtime);
			return val1 == val2 ? 1 : 0;
		} else if (kind.equals("ne")) {
			double val1 = first.evaluate(runtime);
			double val2 = second.evaluate(runtime);
			return val1 != val2 ? 1 : 0;
		} else if (kind.equals("or")) {
			double val1 = first.evaluate(runtime);
			double val2 = second.evaluate(runtime);
			return (val1 != 0) ? 1 : ((val2 != 0) ? 1 : 0);
		} else if (kind.equals("and")) {
			double val1 = first.evaluate(runtime);
			double val2 = second.evaluate(runtime);
			return (val1 != 0 && val2 != 0) ? 1 : 0;
		} else if (kind.equals("not")) {
			double val1 = first.evaluate(runtime);
			return (val1 != 0) ? 0 : 1;
		}
		else {
			error("Unknown node kind [" + kind + "]");
			return 0;
		}

	}// evaluate

}// Node
