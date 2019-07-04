import utility.GameLoop;

public class Main {

	// Main func
	public static void main(String args[]) {
		new Thread(new GameLoop(60, new RayMarching()).enableAutoManage()).run();
	}

}
