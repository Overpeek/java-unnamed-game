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
	
	public static class Timer {
		
		private long startNS = 0;

		
		
		public Timer() {
			startNS = System.nanoTime();
		}
		
		public int nanoseconds() {
			return (int) (System.nanoTime() - startNS);
		}
		
		public float microseconds() {
			return nanoseconds() / 1000.0f;
		}
		
		public float milliseconds() {
			return microseconds() / 1000.0f;
		}
		
	}
	
	public static void debugFreeze(Window window, GameLoop loop) {
		while(true) {
			if (window != null && loop != null) {
				if (window.shouldClose()) System.exit(0);
				window.input();
				if (window.key(Keys.KEY_ESCAPE)) return;
				Logger.debug("press ESC to continue");
			}

			Logger.debug("DEBUG FROZEN");
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
	
}
