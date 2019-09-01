package utility;

public class GameLoop implements Runnable {

	private boolean running;
	private int ups_cap;
	
	private int ups, fps;
	private int uns, fns;
	private Application app;
	private boolean auto_manage = false;
	
	
	public GameLoop(int ups, Application application) {
		this.ups_cap = ups;
		this.app = application;
		ups = 0;
		fps = 0;
		uns = 0;
		fns = 0;
	}
	
	// Get target ups
	public int getTargetUPS() {
		return ups_cap;
	}
	
	//Get nanoseconds spent on last update
	public int getUns() {
		return uns;
	}
	
	//Get nanoseconds spent on last frame
	public int getFns() {
		return fns;
	}
	
	//Get update count from last second
	public int getUps() {
		return ups;
	}
	
	//Get frame count from last second
	public int getFps() {
		return fps;
	}
	
	//Disable autonomous window clear, update and input
	public GameLoop disableAutoManage() {
		auto_manage = false;
		return this;
	}
	
	//Enable autonomous window clear, update and input
	public GameLoop enableAutoManage() {
		auto_manage = true;
		return this;
	}
	
	@Override
	public void run() {
		if (app != null) app.gameloop = this;
		app.init();
	    long lastTime = System.nanoTime();
	    double delta = 0.0;
	    double ns = 1000000000.0 / ups_cap;
	    long timer = System.currentTimeMillis();
	    int updates = 0;
	    int frames = 0;
		running = true;
	    while(running){
	    	if (auto_manage) { if (app.window.shouldClose()) stop(); }
	    	
	        long now = System.nanoTime();
	        delta += (now - lastTime) / ns;
	        lastTime = now;
	        if (delta >= 1.0) {
		        long calcums = System.nanoTime();
	            app.update();
	            uns = (int) (System.nanoTime() - calcums);
	            updates++;
	            delta--;
	        }
	        long calcfms = System.nanoTime();
	        if (auto_manage) { app.window.clear(); app.window.input(); }
	        app.render((float)delta);
	        if (auto_manage) { app.window.update(); }
	        fns = (int) (System.nanoTime() - calcfms);
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
		//Logger.debug("Stopfunc " + running + ", " + this.toString());
	}
	
}
