package utility.scripting;

import java.util.HashMap;

import utility.Logger;

public class Script {
	
	public static class CompileError extends Throwable {
		private static final long serialVersionUID = -8828955519974694721L;
		
		
		
	}
	
	public static class RunError extends Throwable {
		private static final long serialVersionUID = -30243236876468796L;
		
		
		
	}
	
	private HashMap<String, Variable> variables;
//	private HashMap<String, Integer> intVariables;
	private String lines[];
	
	
	public Script() {}
	
	/*
	 * Returns false if compilations was successful
	 * otherwise throws CompileError
	 * **/
	public boolean compile(String script) throws CompileError {
		lines = script.split("\n");
		variables = new HashMap<String, Variable>();
		for(String line : lines) {
			Logger.debug(line);
		}
		
		
		return false;
	}
	
	public boolean run(int delay_per_line, String... args) throws RunError, InterruptedException {
		for(int i = 0; i < lines.length; i++) { // i is also current line
			//Logger.debug("line: " + i + ", contents: (" + lines[i] + ")");
			Thread.sleep(delay_per_line);
			String subline[] = lines[i].split(" ");
			
			if (subline.length == 0) { // Empty line
				
			}
			
			else if (subline[0].equals("var")) { // Assing new variable
				// subline[0] : var
				// subline[1] : name
				// subline[2] : =
				// subline[3] : 8, "s", 9.5f
				
				if (subline.length == 2) { // var name
					variables.put(subline[1], null);
				}
				
				else if (subline.length == 4) { // var name = 8
					variables.put(subline[1], new Variable(subline[3]));
				}
				
				else {
					throw new RunError();
				}
			}
			
			else if (subline[0].equals("print")) {
				// subline[0] : print
				// subline[1] : variable, "text"

				if (subline.length == 2) {
					if (variables.containsKey(subline[1])) { // If it is variable
						Logger.info(variables.get(subline[1]));
					}
					
					else {
						Logger.info(subline[1]);
					}
				}
				
				else {
					throw new RunError();
				}
				
			}
			
			else if (subline[0].equals("goto")) {
				// subline[0] : goto
				// subline[1] : (line) 2

				if (subline.length == 2) {
					int lineNumber = Integer.parseInt(subline[1]);
					i = lineNumber - 2;
				}
				
				else {
					throw new RunError();
				}
				
			}
			
			else if (subline[0].equals("if")) {
				// subline[0] : if
				// subline[1] : variable
				// subline[2] : <, >, =,...
				// subline[3] : 2.0, variable2
				
				Variable leftSide;
				Variable rightSide;
				if (variables.containsKey(subline[0])) leftSide = variables.get(subline[0]);
				else leftSide = new Variable(subline[0]);
				if (variables.containsKey(subline[3])) rightSide = variables.get(subline[3]);
				else rightSide = new Variable(subline[3]);

				if (subline.length == 4) {
					
					
					
				}
				
				else {
					throw new RunError();
				}
				
			}
			
			else if (variables.containsKey(subline[0])) { // assigning or basic mathematical function
				// subline[0] : variable
				// subline[1] : =, +=,....
				// subline[2] : "new"
				
				Variable leftSide;
				Variable rightSide;
				if (variables.containsKey(subline[0])) leftSide = variables.get(subline[0]);
				else leftSide = new Variable(subline[0]);
				if (variables.containsKey(subline[2])) rightSide = variables.get(subline[2]);
				else rightSide = new Variable(subline[2]);
				
				if (subline[1].equals("=")) { // assign
					variables.put(subline[0], rightSide);
				}
				
				else if (subline[1].equals("+=")) { // sum
					variables.put(subline[0], Variable.sum(leftSide, rightSide));
				}
				
				else if (subline[1].equals("-=")) { // sub
					variables.put(subline[0], Variable.sub(leftSide, rightSide));
				}
				
				else if (subline[1].equals("*=")) { // mul
					variables.put(subline[0], Variable.mul(leftSide, rightSide));
				}
				
				else if (subline[1].equals("/=")) { // div
					variables.put(subline[0], Variable.div(leftSide, rightSide));
				}
				
				else {
					throw new RunError();
				}
				
			}
			
			else { // unknown operation
				throw new RunError();
			}

		}
		return false;		
	}

}
