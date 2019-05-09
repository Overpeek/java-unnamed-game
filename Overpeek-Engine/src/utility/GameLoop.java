package utility;

public class GameLoop implements Runnable {

	private boolean running;
	private int ups_cap;
	
	private int ups, fps;
	private Runnable init, update, render, cleanup;
	
	
	public GameLoop(int ups, Runnable init, Runnable update, Runnable render, Runnable cleanup) {
		this.ups_cap = ups;
		this.init = init;
		this.update = update;
		this.render = render;
		this.cleanup = cleanup;
	}
	
	public int getUps() {
		return ups;
	}
	
	public int getFps() {
		return fps;
	}
	
	public void run() {
		init.run();
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
	            update.run();
	            updates++;
	            delta--;
	        }
	        render.run();
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

	    cleanup.run();
	}
	
	public void stop() {
		running = false;
	}
	
}
