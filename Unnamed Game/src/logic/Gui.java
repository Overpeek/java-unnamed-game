package logic;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import graphics.Renderer;
import graphics.VertexData;
import graphics.TextLabelTexture;
import utility.Colors;
import utility.Logger;

public class Gui {
	
	private static final int MAX_TEXT_LINES = 10;
	private static final int GUI_FRAME_LOGGER_SIZE = 200;
	private static final float GUI_FRAME_LOGGER_BAR_WIDTH = 0.005f;
	private static final Vector4f COLOR_HEALTH_BAR = new Vector4f(1.0f, 0.2f, 0.2f, 1.0f);
	private static final Vector4f COLOR_STAMINA_BAR = new Vector4f(0.2f, 1.0f, 0.2f, 1.0f);
	

	private int selectedButton;
	private boolean selected_logger;

	private long currentFrame;
	private long currentUpdate;
	private float frame_logger[];
	private float update_logger[];
	private float avg_frame;
	private float avg_update;

	private float chat_opened_timer;
	private int current_input_history;
	private boolean chat_opened;
	private boolean chat_just_opened;
	private ArrayList<String> last_inputs;
	private ArrayList<String> text_lines;
	private String current_line;
	private String current_line_reserved;
	
	private TextLabelTexture fps_label;
	private TextLabelTexture ups_label;
	private TextLabelTexture pos_label;
	private TextLabelTexture cpu_label;
	private TextLabelTexture gpu_label;
	private TextLabelTexture ram_label;
	private TextLabelTexture renderer_label;

	private TextLabelTexture avg_frame_label;
	private TextLabelTexture avg_update_label;
	

	public Gui(float maxHealth, float maxStamina, float healthGainRate, float staminaGainRate) {
		frame_logger = new float[GUI_FRAME_LOGGER_SIZE];
		update_logger = new float[GUI_FRAME_LOGGER_SIZE];

		for (int i = 0; i < GUI_FRAME_LOGGER_SIZE; i++) {
			frame_logger[i] = 0;
			update_logger[i] = 0;
		}

		current_input_history = 0;
		chat_just_opened = false;
		chat_opened = false;
		chat_opened_timer = 0.0f;
		currentUpdate = 0;
		currentFrame = 0;
		selectedButton = 0;
		selected_logger = false;
		avg_frame = 0.0f;
		avg_update = 0.0f;
		
		fps_label = TextLabelTexture.bakeTextToTexture("FPS: 0");
		ups_label = TextLabelTexture.bakeTextToTexture("UPS: 0");
		pos_label = TextLabelTexture.bakeTextToTexture("POS: 0");
		cpu_label = TextLabelTexture.bakeTextToTexture("CPU: 0");
		gpu_label = TextLabelTexture.bakeTextToTexture("GPU: 0");
		ram_label = TextLabelTexture.bakeTextToTexture("RAM: 0");
		renderer_label = TextLabelTexture.bakeTextToTexture("RENDERER: 0");
		
		avg_frame_label = TextLabelTexture.bakeTextToTexture("0");
		avg_update_label = TextLabelTexture.bakeTextToTexture("0");
		
		//getProcessor();
	}
	
	private String getProcessor() {
		InetAddress ip;
	    try {
	  
	        ip = InetAddress.getLocalHost();
	        System.out.println("Current host name : " + ip.getHostName());
	        System.out.println("Current IP address : " + ip.getHostAddress());
	        String nameOS= System.getProperty("os.name");
	        System.out.println("Operating system Name=>"+ nameOS);
	        String osType= System.getProperty("os.arch");
	        System.out.println("Operating system type =>"+ osType);
	        String osVersion= System.getProperty("os.version");
	        System.out.println("Operating system version =>"+ osVersion);
	         
	        System.out.println(System.getenv("PROCESSOR_IDENTIFIER"));
	        System.out.println(System.getenv("PROCESSOR_ARCHITECTURE"));
	        System.out.println(System.getenv("PROCESSOR_ARCHITEW6432"));
	        System.out.println(System.getenv("NUMBER_OF_PROCESSORS"));
	        /* Total number of processors or cores available to the JVM */
	    System.out.println("Available processors (cores): " + 
	        Runtime.getRuntime().availableProcessors());
	 
	    /* Total amount of free memory available to the JVM */
	    System.out.println("Free memory (bytes): " + 
	        Runtime.getRuntime().freeMemory());
	 
	    /* This will return Long.MAX_VALUE if there is no preset limit */
	    long maxMemory = Runtime.getRuntime().maxMemory();
	    /* Maximum amount of memory the JVM will attempt to use */
	    System.out.println("Maximum memory (bytes): " + 
	        (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));
	 
	    /* Total memory currently in use by the JVM */
	    System.out.println("Total memory (bytes): " + 
	        Runtime.getRuntime().totalMemory());
	         
	         
	        NetworkInterface network = NetworkInterface.getByInetAddress(ip);
	  
	        byte[] mac = network.getHardwareAddress();
	  
	        System.out.print("Current MAC address : ");
	  
	        StringBuilder sb = new StringBuilder();
	        for (int i = 0; i < mac.length; i++) {
	            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));     
	        }
	        System.out.println(sb.toString());
	  
	    } catch (UnknownHostException e) {
	  
	        e.printStackTrace();
	  
	    } catch (SocketException e){
	  
	        e.printStackTrace();
	  
	    }
	    catch (Exception e){
	  
	        e.printStackTrace();
	  
	    }
		return current_line;
	}
	
	public void render(Renderer blur_renderer, Renderer normal_renderer) {
		//Health bar
		Vector3f pos = new Vector3f(Main.game.getWindow().getAspect() - 0.6f, -1.0f + 0.1f, 0.0f).mul(Main.game.renderScale());
		Vector2f size = new Vector2f(0.5f, 0.05f).mul(Main.game.renderScale());
		blur_renderer.points.submitVertex(new VertexData(pos, size, 0, Colors.BLACK));

		pos = new Vector3f(Main.game.getWindow().getAspect() - 0.595f, -1.0f + 0.105f, 0.0f).mul(Main.game.renderScale());
		size = new Vector2f(0.49f * (Main.game.getPlayer().getHealth() / Main.game.getPlayer().getMaxHealth()), 0.04f).mul(Main.game.renderScale());
		blur_renderer.points.submitVertex(new VertexData(pos, size, 0, COLOR_HEALTH_BAR));

		//Stealth bar
		pos = new Vector3f(Main.game.getWindow().getAspect() - 0.6f, -1.0f + 0.16f, 0.0f).mul(Main.game.renderScale());
		size = new Vector2f(0.5f, 0.05f).mul(Main.game.renderScale());
		blur_renderer.points.submitVertex(new VertexData(pos, size, 0, Colors.BLACK));

		pos = new Vector3f(Main.game.getWindow().getAspect() - 0.595f, -1.0f + 0.165f, 0.0f).mul(Main.game.renderScale());
		size = new Vector2f(0.49f * (Main.game.getPlayer().getStamina() / Main.game.getPlayer().getMaxStamina()), 0.04f).mul(Main.game.renderScale());
		blur_renderer.points.submitVertex(new VertexData(pos, size, 0, COLOR_STAMINA_BAR));
		
		float textScale = 0.08f * Main.game.renderScale();
		float x = -Main.game.getWindow().getAspect();
		if (Main.game.debugMode) {
			//Debug mode text
			String text = "UPS: " + Main.game.getLoop().getUps();
			fps_label.rebake(text, Main.game.getGlyphs());
			fps_label.draw(new Vector3f(0.0f, 0.0f, 0.0f), new Vector2f(textScale));
			Logger.debug("Drawn " + new Vector3f(x, -1.0f + (textScale * 1), 0.0f));
		}
		if (Main.game.advancedDebugMode) {
			//Advanced debug mode text
			String text = "Position X: " + Main.game.getPlayer().getPos().x + ", Y: " + Main.game.getPlayer().getPos().y;
			pos_label.rebake(text, Main.game.getGlyphs());
			pos_label.draw(new Vector3f(x, -1.0f + (textScale * 2), 0.0f), new Vector2f(textScale, textScale));
			text = "Renderer: " + Main.game.getWindow().getRenderer();
			renderer_label.rebake(text, Main.game.getGlyphs());
			renderer_label.draw(new Vector3f(x, -1.0f + (textScale * 4), 0.0f), new Vector2f(textScale, textScale));
			text = "CPU: ";
			cpu_label.rebake(text, Main.game.getGlyphs());
			cpu_label.draw(new Vector3f(x, -1.0f + (textScale * 5), 0.0f), new Vector2f(textScale, textScale));

			currentFrame++;
			if (currentFrame >= GUI_FRAME_LOGGER_SIZE) currentFrame = 0;
			frame_logger[(int) currentFrame] = Main.game.getLoop().getFns();

			avg_frame = 0;
			for (int i = 0; i < GUI_FRAME_LOGGER_SIZE; i++)
			{
				avg_frame += frame_logger[i];
			}
			avg_frame /= (float)GUI_FRAME_LOGGER_SIZE;

			pos = new Vector3f(-Main.game.getWindow().getAspect(), 2.0f / 3.0f, 0.0f);
			size = new Vector2f(GUI_FRAME_LOGGER_BAR_WIDTH * (GUI_FRAME_LOGGER_SIZE - 1), 0.005f).mul(Main.game.renderScale());
			Vector3f textPos = new Vector3f(-Main.game.getWindow().getAspect(), 2.0f / 3.0f - 0.02f * Main.game.renderScale(), 0.0f);
			Vector2f textSize = new Vector2f(textScale * 0.8f);
			normal_renderer.points.submitVertex(new VertexData(pos, size, 0, Colors.GREEN));
			if (selected_logger) {
				//Top bar
				text = "frame: " + (int)avg_frame;
				avg_frame_label.rebake(text);
				avg_frame_label.draw(textPos, textSize);

				//Actual logger
				for (int i = 0; i < GUI_FRAME_LOGGER_SIZE; i++) {
					float bar_height = (frame_logger[i] / avg_frame) / 3.0f;
					pos = new Vector3f(-Main.game.getWindow().getAspect() + (GUI_FRAME_LOGGER_BAR_WIDTH * Main.game.renderScale() * i), 1.0f - bar_height, 0.0f);
					size = new Vector2f(GUI_FRAME_LOGGER_BAR_WIDTH * Main.game.renderScale(), bar_height);

					Vector4f barColor = new Vector4f(Math.max(0.0f, bar_height - 0.5f), Math.max(0.0f, 0.5f - bar_height), 0.0f, 1.0f);
					normal_renderer.points.submitVertex(new VertexData(pos, size, 0, barColor));
				}
			}
			else {
				text = "frame: " + (int)avg_update;
				avg_update_label.rebake(text);
				avg_update_label.draw(textPos, textSize);

				for (int i = 0; i < GUI_FRAME_LOGGER_SIZE; i++) {
					float bar_height = (update_logger[i] / avg_update) / 3.0f;
					pos = new Vector3f(-Main.game.getWindow().getAspect() + (GUI_FRAME_LOGGER_BAR_WIDTH * Main.game.renderScale() * i), 1.0f - bar_height, 0.0f);
					size = new Vector2f(GUI_FRAME_LOGGER_BAR_WIDTH * Main.game.renderScale(), bar_height);

					float br = Math.max(0.0f, bar_height);
					Vector4f barColor = new Vector4f(br, 1.0f - br, 0.0f, 1.0f);
					normal_renderer.points.submitVertex(new VertexData(pos, size, 0, barColor));
				}
			}
		}
	}
	
}
