package utility;

import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

	public static class DebugSlider implements ChangeListener {
		
		private static JFrame frame = init();
		private static JPanel panel;
		private static GridBagConstraints constraints;
		private static int y;
		
		private float sliderValue = 0.0f;
		private float min = 0.0f;
		private float max = 0.0f;
		
		
		
		private static JFrame init() {
			JFrame frame = new JFrame("Sliders");
			panel = new JPanel(new GridBagLayout());
	        constraints = new GridBagConstraints();
	        constraints.anchor = GridBagConstraints.WEST;
	        constraints.insets = new Insets(10, 10, 10, 10);
	        
			return frame;
		}
		
		
		public static void complete() {
			frame.add(panel);
			frame.pack();
			frame.setVisible(true);
		    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		
		public DebugSlider(float min, float max, float initial, String name) {
			this.min = min;
			this.max = max;
			this.sliderValue = initial;
			
			JSlider slider = new JSlider(JSlider.HORIZONTAL, -1000, 1000, (int) Maths.map(initial, min, max, -1000, 1000));
			slider.addChangeListener(this);
			slider.setMajorTickSpacing(100);
			
	        constraints.gridx = 0;
	        constraints.gridy = y;
			panel.add(new JLabel(name), constraints);

	        constraints.gridx = 1;
	        constraints.gridy = y;
			panel.add(new JLabel("" + min), constraints);

	        constraints.gridx = 2;
	        constraints.gridy = y;
			panel.add(slider, constraints);

	        constraints.gridx = 3;
	        constraints.gridy = y;
			panel.add(new JLabel("" + max), constraints);
			
			y++;
		}
		
		public float getSliderValue() {
			return sliderValue;
		}

		@Override
		public void stateChanged(ChangeEvent e) {
		    JSlider source = (JSlider)e.getSource();
		    sliderValue = Maths.map(source.getValue(), -1000.0f, 1000.0f, min, max);
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
