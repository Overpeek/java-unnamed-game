import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import utility.Logger;

class Main {
	
	public static void main(String args[]) throws ScriptException {

		Logger.debug("RUN");
		
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("javascript");

		engine.eval("print(48 + 349)");
		
	}
	
}
