package utility;

public class GameLoop implements Runnable {

	private boolean running;
	private int ups_cap;
	
	private int ups, fps;
	private Application app;
	
	
	public GameLoop(int ups, Application application) {
		this.ups_cap = ups;
		this.app = application;
	}
	
	public int getUps() {
		return ups;
	}
	
	public int getFps() {
		return fps;
	}
	
	@Override
	public void run() {
		app.init();
	    long lastTime = System.nanoTime();
	    double delta = 0.0;
	    double ns = 1000000000.0 / ups_cap;
	    long timer = System.currentTimeMillis();
	    int updates = 0;
	    int frames = 0;
		running = true;
	    while(running){
	        long now = System.nanoTime();
	        delta += (now - lastTime) / ns;
	        lastTime = now;
	        if (delta >= 1.0) {
	            app.update();
	            updates++;
	            delta--;
	        }
	        app.render((float)delta);
	        frames++;
	        if (System.currentTimeMillis() - timer > 1000) {
	            timer += 1000;
	            //System.out.println(updates + " ups, " + frames + " fps");
	            fps = frames;
	            ups = updates;
	            updates = 0;
	            frames = 0;
	        }
	    }

	    app.cleanup();
	}
	
	public void stop() {
		running = false;
	}
	
}
