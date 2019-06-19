package logic;
import utility.GameLoop;

public class Main {
	
	public static Game game;

	public static void main(String args[]) {
		game = new Game();
		GameLoop loop = new GameLoop(60, game);
		game.gameloop = loop;
        (new Thread(loop)).start();
	}
}
