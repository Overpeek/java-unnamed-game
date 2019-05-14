import utility.Application;
import utility.GameLoop;

public class Main {

	public static void main(String args[]) {
		Application game = new Game();
		GameLoop loop = new GameLoop(60, game);
		game.gameloop = loop;
        (new Thread(loop)).start();
	}
}
