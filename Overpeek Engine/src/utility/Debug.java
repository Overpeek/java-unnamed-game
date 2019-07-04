package utility;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import graphics.Window;

public class Debug {
	
	public static void debugFreeze(Window window, GameLoop loop) {
		while(true) {
			if (window.shouldClose()) System.exit(0);
			if (window.key(Keys.KEY_ESCAPE)) return;

			Logger.debug("FROZEN FOR DEBUG: press esc to continue");
			window.input();
		}
	}
	
	public static BufferedImage resize(BufferedImage img, int newW, int newH) { 
	    Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_FAST);
	    BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2d = dimg.createGraphics();
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();

	    return dimg;
	}  
	
	public static void debugImagePopup(BufferedImage img) {
		JPanel panel = new JPanel();
	    JLabel label = new JLabel(new ImageIcon(img));
	    panel.add(label);
	    JFrame.setDefaultLookAndFeelDecorated(true);
	    JFrame frame = new JFrame("DEBUG");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.add(panel); 
	    frame.pack();
	    frame.setVisible(true);
	}
	
	
	private static long startMS = 0;
	public static void startTimer() {
		startMS = System.currentTimeMillis();
	}
	
	public static int getElapsedMS() {
		return (int) (System.currentTimeMillis() - startMS);
	}
	
	public static void printElapsedMS(String description) {
		Logger.debug(description + (getElapsedMS()) + "ms");
	}
	
	public static void printElapsedMS() {
		printElapsedMS("Operation took ");
	}
	
}
