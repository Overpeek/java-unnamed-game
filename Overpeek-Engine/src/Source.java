import graphics.Application;

public class Source {
	
	public static void main(String args[]) {
        (new Thread(new Application())).start();
    }
}
