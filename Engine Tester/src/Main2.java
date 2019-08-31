import java.io.IOException;

import utility.DataIO;
import utility.scripting.Script;
import utility.scripting.Script.CompileError;
import utility.scripting.Script.RunError;

public class Main2 {
	
	public static void main(String args[]) {
		Script script = new Script();
		try {
			
			// Load script source
			String scriptString = DataIO.readTextFile("/test.script");
			
			// Compile script
			script.compile(scriptString);
			
			// Run script main function
			script.run(100);
		
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CompileError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RunError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
