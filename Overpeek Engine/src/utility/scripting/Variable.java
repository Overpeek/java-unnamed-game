package utility.scripting;

public class Variable {

	public static class IllegalFunctionException extends Throwable {
		private static final long serialVersionUID = 6632021371278529927L;
		
	}
	
	private static enum type {
		STRING, NUMBER
	}
	
	
	private String string = "";
	private float number = 0.0f;
	
	private type variable_type;
	
	public Variable(String value) throws NumberFormatException {
		if (value.startsWith("\"") && value.endsWith("\"")) { // it is String value
			string = value.substring(1, value.length() - 1);
			variable_type = type.STRING;
		} else { // it is float value
			number = Float.parseFloat(value);
			variable_type = type.NUMBER;
		}
	}
	
	public Variable(float value) {
		number = value;
		variable_type = type.NUMBER;
	}
	
	private Variable(type variable_type) {
		this.variable_type = variable_type;
	}
	
	@Override
	public String toString() {
		if (variable_type == type.STRING) {
			return string;
		} else {
			return "" + number;
		}
	}
	
	/*
	 * Static methods
	 **/
	
	/*
	 * Returns a + b
	 * 
	 * Examples: "r" + 5 = "r5"
	 * 			5.4 + 2 = 7.4
	 * 			2 + "4" = 6
	 **/
	public static Variable sum(Variable a, Variable b) throws NumberFormatException {
		Variable var = new Variable(a.variable_type);
		
		if (a.variable_type == type.STRING) { // a is String
			
			if (b.variable_type == type.STRING) { 					// String and String
				var.string = a.string + b.string;
			} 
			
			else { 													// String and Number
				var.string = a.string + b.number;
			}
			
		} 
		
		else { // a is Number
			
			if (b.variable_type == type.STRING) { 					// Number and String
				var.number = a.number + Float.parseFloat(b.string);		// parsing is required
			} 
			
			else { 													// Number and Number
				var.number = a.number + b.number;
			}
			
		}
		
		return var;
	}

	/*
	 * Returns a - b
	 * 
	 * Examples: "r" - 5 = error
	 * 			5.4 - 2 = 3.4
	 * 			2 - "4" = -2
	 **/
	public static Variable sub(Variable a, Variable b) throws NumberFormatException, IllegalArgumentException{
		Variable var = new Variable(a.variable_type);
		
		if (a.variable_type == type.STRING) { // a is String
			
			// No subtracting from string
			throw new IllegalArgumentException();
			
		} 
		
		else { // a is Number
			
			if (b.variable_type == type.STRING) { 					// Number and String
				var.number = a.number - Float.parseFloat(b.string);		// parsing is required
			} 
			
			else { 													// Number and Number
				var.number = a.number - b.number;
			}
			
		}
		
		return var;
	}

	/*
	 * Returns a * b
	 * 
	 * Examples: "r" * 5 = rrrrr
	 * 			5.4 * 2 = 10.8
	 * 			2 * "4" = 8
	 **/
	public static Variable mul(Variable a, Variable b) throws NumberFormatException, IllegalArgumentException {
		Variable var = new Variable(a.variable_type);
		
		if (a.variable_type == type.STRING) { // a is String
			
			if (b.variable_type == type.STRING) { 					// String and String
				
				// Can't multiply String by String
				throw new IllegalArgumentException();
				
			} 
			
			else { 													// String and Number
				var.string = "";
				for (int i = 0; i < b.number; i++) {
					var.string += a.string;
				}
			}
			
		} 
		
		else { // a is Number
			
			if (b.variable_type == type.STRING) { 					// Number and String
				var.number = a.number * Float.parseFloat(b.string);		// parsing is required
			} 
			
			else { 													// Number and Number
				var.number = a.number * b.number;
			}
			
		}
		
		return var;
	}

	/*
	 * Returns a / b
	 * 
	 * Examples: "r" / 5 = error
	 * 			5.4 / 2 = 2.7
	 * 			2 / "4" = 0.5
	 **/
	public static Variable div(Variable a, Variable b) throws IllegalArgumentException, NumberFormatException {
		Variable var = new Variable(a.variable_type);
		
		if (a.variable_type == type.STRING) { // a is String
			
			// Can't divide String
			throw new IllegalArgumentException();
			
		} 
		
		else { // a is Number
			
			if (b.variable_type == type.STRING) { 					// Number and String
				var.number = a.number / Float.parseFloat(b.string);		// parsing is required
			} 
			
			else { 													// Number and Number
				var.number = a.number / b.number;
			}
			
		}
		
		return var;
	}

}
