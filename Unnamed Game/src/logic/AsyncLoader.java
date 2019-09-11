package logic;

public abstract class AsyncLoader implements Runnable {

	public abstract float queryLoadState();
	public abstract void init();
	public abstract void finish();

}
