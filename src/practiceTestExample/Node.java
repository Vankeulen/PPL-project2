package practiceTestExample;

import java.util.HashMap;
import java.util.Map;

public class Node {
	public static Map<String, Double> mem = new HashMap<>();
	
	public static void main(String[] args) {
		// Node tree for 5 + 6
		mem.put("cars", 20.0);
		
		
		Node five = new Node("N", "5", null, null);
		Node six = new Node("N", "6", null, null);
		Node fivePlusSix = new Node("expression", "+", five, six);
		Node fiveTimesSix = new Node("expression", "*", five, six);
		
		Node cars = new Node("V", "cars", null, null);
		Node carsPlus = new Node("expression", "+", cars, fiveTimesSix);
		Node justCars = new Node("expression", "", cars, null);
		
		Node parenCars = new Node("expression", "(", cars, null);
		
		System.out.println(five.evaluate());
		System.out.println(six.evaluate());
		
		System.out.println(fivePlusSix.evaluate());
		System.out.println(fiveTimesSix.evaluate());
		
		System.out.println(cars.evaluate());
		System.out.println(parenCars.evaluate());
		System.out.println(justCars.evaluate());
		System.out.println(carsPlus.evaluate());
	}
	
	public final String kind;
	public final String info;
	
	public final Node first;
	public final Node second;
	public Node(String kind, String info, Node first, Node second) {
		this.kind = kind;
		this.info = info;
		this.first = first;
		this.second = second;
	}
	
	public double getMemValue(String v) { 
		if (!mem.containsKey(v)) { return 0; }
		return mem.get(v);
	}
	
	public double evaluate() {
		if (kind.equals("expression")) {
			if (info.equals("")) {
				// \/ part 1
				return first.evaluate();
				// /\ part 1
			} else if (info.equals("+")) {
				// \/ part 2 
				double a = first.evaluate();
				double b = second.evaluate();
				return a + b;
				// /\ part 2
			} else if (info.equals("*")) {
				// \/ part 3
				double a = first.evaluate();
				double b = second.evaluate();
				return a * b;
				// /\ part 3
			} else if (info.equals("(")) {
				// \/ part 4 
				return first.evaluate();
				// /\ part 4
			}
		} else if (kind.equals("N")) {
			return Double.parseDouble(info);
		} else if (kind.equals("V")) {
			// \/ part 5
			return getMemValue(info);
			// /\ part 5
		}
		throw new RuntimeException("Unknown kind");
	}

}