package logic;

public class AsyncResLoader extends Thread {

	@Override
	public void run() {
		Database.initialize();
	}
	
	
	//Returns float between 0.0f-1.0f
	public float queryLoadState() {
		float vanilla = Database.loadState();
		float mods = Database.modLoadState();
		
		if (mods < 0.0f) return vanilla;
		return (vanilla + mods) / 2.0f;
	}

	
}
