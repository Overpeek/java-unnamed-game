package logic;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.activation.MailcapCommandMap;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.IconifyAction;

import creatures.*;
import graphics.Renderer;
import graphics.TextLabelTexture;
import graphics.VertexData;
import utility.Colors;
import utility.Keys;
import utility.Logger;
import utility.Maths;
import utility.vec2;
import utility.vec3;
import utility.vec4;
import world.Map.MapTile;

public class Gui {
	
	private static final int MAX_TEXT_LINES = 10;
	private static final int GUI_FRAME_LOGGER_SIZE = 200;
	private static final float GUI_FRAME_LOGGER_BAR_WIDTH = 0.005f;
	private static final vec4 COLOR_HEALTH_BAR = new vec4(1.0f, 0.2f, 0.2f, 1.0f);
	private static final vec4 COLOR_STAMINA_BAR = new vec4(0.2f, 1.0f, 0.2f, 1.0f);
	private static final int PAUSEMENU_BUTTON_COUNT = 3;
	

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

	private TextLabelTexture text_input_label;
	private ArrayList<TextLabelTexture> text_chat_label;

	private TextLabelTexture pause_indicator_label;
	private TextLabelTexture button_label_0;
	private TextLabelTexture button_label_1;
	private TextLabelTexture button_label_2;
	

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
		selected_logger = true;
		avg_frame = 0.0f;
		avg_update = 0.0f;
		
		fps_label = TextLabelTexture.bakeToTexture("FPS: 0");
		ups_label = TextLabelTexture.bakeToTexture("UPS: 0");
		pos_label = TextLabelTexture.bakeToTexture("POS: 0");
		cpu_label = TextLabelTexture.bakeToTexture("CPU: 0");
		gpu_label = TextLabelTexture.bakeToTexture("GPU: 0");
		ram_label = TextLabelTexture.bakeToTexture("RAM: 0");
		renderer_label = TextLabelTexture.bakeToTexture("RENDERER: 0");
		
		avg_frame_label = TextLabelTexture.bakeToTexture("0");
		avg_update_label = TextLabelTexture.bakeToTexture("0");
		
		pause_indicator_label = TextLabelTexture.bakeToTexture("PAUSED");
		button_label_0 = TextLabelTexture.bakeToTexture("RESUME");
		button_label_1 = TextLabelTexture.bakeToTexture("SETTINGS");
		button_label_2 = TextLabelTexture.bakeToTexture("SAVE AND EXIT");
		
		text_input_label = TextLabelTexture.bakeToTexture("0");
		text_chat_label = new ArrayList<TextLabelTexture>();
		TextLabelTexture.bakeToTexture("0");
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
	
	public void renderWithBlur(Renderer blur_renderer, Renderer normal_renderer) {
		//Health bar
		vec3 pos = new vec3(Main.game.getWindow().getAspect() - 0.6f, -1.0f + 0.1f, 1.0f).mult(Main.game.renderScale() * Main.game.guiScale());
		vec2 size = new vec2(0.5f, 0.05f).mult(Main.game.renderScale() * Main.game.guiScale());
		blur_renderer.points.submitVertex(new VertexData(pos, size, 0, Colors.BLACK));

		pos = new vec3(Main.game.getWindow().getAspect() - 0.595f, -1.0f + 0.105f, 1.0f).mult(Main.game.renderScale() * Main.game.guiScale());
		size = new vec2(0.49f * (Main.game.getPlayer().getHealth() / Main.game.getPlayer().getMaxHealth()), 0.04f).mult(Main.game.renderScale() * Main.game.guiScale());
		blur_renderer.points.submitVertex(new VertexData(pos, size, 0, COLOR_HEALTH_BAR));

		//Stealth bar
		pos = new vec3(Main.game.getWindow().getAspect() - 0.6f, -1.0f + 0.16f, 1.0f).mult(Main.game.renderScale() * Main.game.guiScale());
		size = new vec2(0.5f, 0.05f).mult(Main.game.renderScale() * Main.game.guiScale());
		blur_renderer.points.submitVertex(new VertexData(pos, size, 0, Colors.BLACK));

		pos = new vec3(Main.game.getWindow().getAspect() - 0.595f, -1.0f + 0.165f, 1.0f).mult(Main.game.renderScale() * Main.game.guiScale());
		size = new vec2(0.49f * (Main.game.getPlayer().getStamina() / Main.game.getPlayer().getMaxStamina()), 0.04f).mult(Main.game.renderScale() * Main.game.guiScale());
		blur_renderer.points.submitVertex(new VertexData(pos, size, 0, COLOR_STAMINA_BAR));
		
		float textScale = 0.08f * Main.game.renderScale();
		size = new vec2(textScale);
		float x = -Main.game.getWindow().getAspect();
		if (Main.game.debugMode) {
			//Debug mode text
			pos = new vec3(x, -1.0f, 0.0f);
			String text = "FPS: " + Main.game.getLoop().getFps();
			fps_label.rebake(text);
			fps_label.queueDraw(pos, size);
			
			pos.add(0.0f, textScale, 0.0f);
			text = "UPS: " + Main.game.getLoop().getUps();
			ups_label.rebake(text);
			ups_label.queueDraw(pos, size);
		} else 
			return;
		
		if (Main.game.advancedDebugMode) {
			//Advanced debug mode text
			pos.add(0.0f, textScale, 0.0f);
			String text = "Position X: " + Main.game.getPlayer().getPos().x + ", Y: " + Main.game.getPlayer().getPos().y;
			pos_label.rebake(text);
			pos_label.queueDraw(pos, size);

			pos.add(0.0f, textScale, 0.0f);			
			text = "Renderer: " + Main.game.getWindow().getRenderer();
			renderer_label.rebake(text);
			renderer_label.queueDraw(pos, size);

			pos.add(0.0f, textScale, 0.0f);
			text = "Threads: " + Runtime.getRuntime().availableProcessors();
			cpu_label.rebake(text);
			cpu_label.queueDraw(pos, size);

			currentFrame++;
			if (currentFrame >= GUI_FRAME_LOGGER_SIZE) currentFrame = 0;
			frame_logger[(int) currentFrame] = Main.game.getLoop().getFns() / 1000000.0f;

			avg_frame = 0;
			int nonZeroCount = 0;
			for (int i = 0; i < GUI_FRAME_LOGGER_SIZE; i++)
			{
				if (frame_logger[i] != 0.0f) nonZeroCount++;
				avg_frame += frame_logger[i];
			}
			avg_frame /= (float)nonZeroCount;

			pos = new vec3(-Main.game.getWindow().getAspect(), 2.0f / 3.0f, 0.0f);
			size = new vec2(GUI_FRAME_LOGGER_BAR_WIDTH * (GUI_FRAME_LOGGER_SIZE - 1), 0.005f).mult(Main.game.renderScale());
			vec3 textPos = new vec3(-Main.game.getWindow().getAspect(), 2.0f / 3.0f - 0.02f * Main.game.renderScale(), 0.0f);
			vec2 textSize = new vec2(textScale * 0.8f);
			normal_renderer.points.submitVertex(new VertexData(pos, size, 0, Colors.GREEN));
			if (selected_logger) { //Frame logger
				//Top bar
				text = "avg ms per frame: " + (int)avg_frame;
				avg_frame_label.rebake(text);
				avg_frame_label.queueDraw(textPos, textSize);

				//Actual logger
				for (int i = 0; i < GUI_FRAME_LOGGER_SIZE; i++) {
					float bar_height = (frame_logger[i] / avg_frame) / 3.0f;
					//Logger.debug(bar_height);
					pos = new vec3(-Main.game.getWindow().getAspect() + (GUI_FRAME_LOGGER_BAR_WIDTH * Main.game.renderScale() * i), 1.0f - bar_height, 0.0f);
					size = new vec2(GUI_FRAME_LOGGER_BAR_WIDTH * Main.game.renderScale(), bar_height);

					float color_rg = Math.max(0.0f, 0.8f - bar_height);
					vec4 barColor = new vec4(Math.max(0.0f, 1.0f - color_rg), color_rg, 0.0f, 1.0f);
					normal_renderer.points.submitVertex(new VertexData(pos, size, 0, barColor));
				}
			}
			else { //Update logger
				//Top bar
				text = "avg ms per update: " + (int)avg_update;
				avg_update_label.rebake(text);
				avg_update_label.queueDraw(textPos, textSize);

				//Actual logger
				for (int i = 0; i < GUI_FRAME_LOGGER_SIZE; i++) {
					float bar_height = (update_logger[i] / avg_update) / 3.0f;
					//Logger.debug(bar_height);
					pos = new vec3(-Main.game.getWindow().getAspect() + (GUI_FRAME_LOGGER_BAR_WIDTH * Main.game.renderScale() * i), 1.0f - bar_height, 0.0f);
					size = new vec2(GUI_FRAME_LOGGER_BAR_WIDTH * Main.game.renderScale(), bar_height);

					vec4 barColor = new vec4(Math.max(0.0f, bar_height - 0.5f), Math.max(0.0f, 0.5f - bar_height), 0.0f, 1.0f);
					normal_renderer.points.submitVertex(new VertexData(pos, size, 0, barColor));
				}
			}
		}
	}
	
	public void renderWithoutBlur(Renderer renderer) {
		float textScale = 0.08f * Main.game.renderScale();
		float x = -Main.game.getWindow().getAspect();

		if (Main.game.paused) {

			//Loop trough all pause menu buttons
			if (selectedButton > PAUSEMENU_BUTTON_COUNT - 1) selectedButton = 0;
			if (selectedButton < 0) selectedButton = PAUSEMENU_BUTTON_COUNT - 1;

			vec2 buttonTextScale = new vec2(0.1f * Main.game.renderScale());

			//String text = "PAUSED";
			pause_indicator_label.queueDraw(new vec3(0.003f, -0.003f - 0.75f, 0.0f), buttonTextScale, Colors.WHITE);

			for (int i = 0; i < 3; i++)
			{
				float shade = 0.1f;
				if (i == selectedButton) shade = 0.2f;
				vec3 pos = new vec3(0.0f - 0.4f, -0.50f - 0.05f + (i * 0.15f), 0.0f);
				vec2 size = new vec2(0.8f, 0.1f);

				renderer.quads.submit(pos, size, 0, new vec4(0.0f, 0.0f, 0.0f, shade));
			}

			buttonTextScale = new vec2(0.1f * Main.game.renderScale());
			//text = "RESUME";
			button_label_0.queueDrawCentered(new vec3(0.0f, -0.5f, 0.0f), buttonTextScale, Colors.WHITE);
			//renderer->fontRenderer->renderText(glm::vec3(0.0, -0.5, 0.0f), glm::vec2(textScale, textScale), text.c_str(), oe::center);

			//text = "SETTINGS";
			button_label_1.queueDrawCentered(new vec3(0.0f, -0.35f, 0.0f), buttonTextScale, Colors.WHITE);
			//renderer->fontRenderer->renderText(glm::vec3(0.0, -0.35, 0.0f), glm::vec2(textScale, textScale), text.c_str(), oe::center);

			//text = "SAVE AND EXIT";
			button_label_2.queueDrawCentered(new vec3(0.0f, -0.20f, 0.0f), buttonTextScale, Colors.WHITE);
			//renderer->fontRenderer->renderText(glm::vec3(0.0, -0.20, 0.0f), glm::vec2(textScale, textScale), text.c_str(), oe::center);
		}

		if (chat_opened || chat_opened_timer > 0) {
			if (chat_opened) {
				vec3 pos = new vec3(-Main.game.getWindow().getAspect(), 1.0f - textScale, 0.0f);
				vec2 size = new vec2(textScale);

				//renderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x, pos.y, pos.z), glm::vec2(0.0f, 0.0f), 20, glm::vec4(0.0, 0.0, 0.0, 0.2)));
				//renderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x, pos.y - size.y, pos.z), glm::vec2(0.0f, 1.0f), 20, glm::vec4(0.0, 0.0, 0.0, 0.2)));
				//renderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x + Main.game.getWindow()->getAspect() * 2, pos.y - size.y, pos.z), glm::vec2(1.0f, 1.0f), 20, glm::vec4(0.0, 0.0, 0.0, 0.2)));
				//renderer->quadRenderer->submitVertex(oe::VertexData(glm::vec3(pos.x + Main.game.getWindow()->getAspect() * 2, pos.y, pos.z), glm::vec2(1.0f, 0.0f), 20, glm::vec4(0.0, 0.0, 0.0, 0.2)));
				
				//Text input line
				String drawTextInputLine = "";
				if (current_line.length() != 0) {
					drawTextInputLine = current_line;
				}
				text_input_label.rebake(drawTextInputLine);
				text_input_label.queueDraw(pos, size, Colors.WHITE);
			}

			for (int i = 0; i < text_lines.size(); i++)
			{
				vec3 pos = new vec3(-Main.game.getWindow().getAspect(), 1.0f - ((i + 3) * textScale), 0.0f);
				vec2 size = new vec2(textScale);

				text_chat_label.get(i).rebake(text_lines.get(text_lines.size() - 1 - i));
				text_chat_label.get(i).queueDraw(pos, size, Colors.WHITE);
			}
		}
	}

	public void addChatLine(String text) {
		Logger.info("chat:" + text);
		chat_opened_timer = 5.0f;
		text_lines.add(text);
		if (text_lines.size() > MAX_TEXT_LINES) text_lines.remove(0);
	}

	private void addInputToLatest(String text) {
		last_inputs.add(text);
		if (last_inputs.size() > MAX_TEXT_LINES) text_lines.remove(0);
	}

	public void update(float divider) {
		chat_opened_timer -= 1.0f / divider;
		if (Main.game.advancedDebugMode) {
			currentUpdate++;
			if (currentUpdate >= GUI_FRAME_LOGGER_SIZE) currentUpdate = 0;
			update_logger[(int) currentUpdate] = Main.game.getLoop().getUns() / 1000000.0f;

			avg_update = 0;
			for (int i = 0; i < GUI_FRAME_LOGGER_SIZE; i++)
			{
				avg_update += update_logger[i];
			}
			avg_update /= (float)GUI_FRAME_LOGGER_SIZE;
		}
	}

	public void keyPress(int key, int action) {
		if (Main.game.paused) {
			//Pause menu navigation
			if (action == Keys.PRESS && key == Keys.KEY_DOWN) { selectedButton++; return; }
			if (action == Keys.PRESS && key == Keys.KEY_UP) { selectedButton--; return; }
			if (action == Keys.PRESS && (key == Keys.KEY_ENTER || key == Keys.KEY_SPACE)) {
				if (selectedButton == 0) {
					Main.game.paused = false;
					return;
				}
				if (selectedButton == 1) {
					addChatLine("Not implemented");
					return;
				}
				if (selectedButton == 2) {
					Main.game.getLoop().stop();
					return;
				}
				return;
			}
		}
		else {
			if (action == Keys.PRESS && key == Keys.KEY_T) {
				//Only opens chat
				if (chat_opened) return;

				chat_opened = true;
				chat_just_opened = true;
				return;
			}
			if (action == Keys.PRESS && key == Keys.KEY_ESCAPE && chat_opened) {
				//Only closes chat if its opened
				chat_opened = false;
				chat_opened_timer = 5.0f;
				return;
			}
			if (action == Keys.PRESS && key == Keys.KEY_ENTER && chat_opened) {
				//Only closes chat if its opened
				chat_opened = false;
				chat_opened_timer = 5.0f;
				userInput();
				addInputToLatest(current_line);
				current_line = "";
				current_line_reserved = "";
				current_input_history = -1;
				return;
			}
			if (action == Keys.PRESS && key == Keys.KEY_BACKSPACE && chat_opened) {
				//Removes last character of current text input
				//if chat is currently opened
				if (current_line.length() > 0) current_line = current_line.substring(current_line.length() - 2);
				return;
			}
			if (action == Keys.PRESS && key == Keys.KEY_UP && chat_opened) {
				if (current_input_history == -1) current_line_reserved = current_line;
				current_input_history++;
				selectInputHistory();
				return;
			}
			if (action == Keys.PRESS && key == Keys.KEY_DOWN && chat_opened) {
				current_input_history--;
				selectInputHistory();
				return;
			}
		}
		if (action == Keys.PRESS && key == Keys.KEY_F9) { selected_logger = !selected_logger; return; }
	}

	private void selectInputHistory() {
		if (last_inputs.size() == 0) return;
		current_input_history = (int) Maths.clamp(current_input_history, -1.0f, (int)last_inputs.size() - 1);

		if (current_input_history == -1) {
			current_line = current_line_reserved;
			return;
		}
		current_line = last_inputs.get((int)(last_inputs.size() - 1 - current_input_history));
	}

	public void typing(int character, int mods) {
		if (chat_opened) {
			if (chat_just_opened) {
				chat_just_opened = false;
				return;
			}
			else {
				current_line += (char)character;
			}
		}
	}

	private void userInput() {
		//Check if command is typed
		if (current_line.charAt(0) == '/') { // /command arg0 arg1
			int space = current_line.indexOf(' ');  // /command|| arg0 arg1
			String command = current_line.substring(1, space - 1); // command
			String arguments = current_line.substring(space + 1); // arg0 arg1
			ArrayList<String> argumentList = new ArrayList<String>();

			//Check if has arguments
			if (space != -1) {

				String leftArguments = arguments;
				int nextArg = 0;
				while (nextArg != -1) {
					nextArg = leftArguments.indexOf(' ');
					String thisargument = leftArguments.substring(0, nextArg);
					leftArguments = leftArguments.substring(nextArg + 1);
					argumentList.add(thisargument);
				}

			}
			else {
				//No arguments
			}

			//COMMANDS
			if (command == "clear") {
				//Easter egg :DD
				tooFewArguments();
				addChatLine("Haha, just kidding ya :D");

				//Execute the command
				text_lines.clear();
			}
			//TP COMMAND
			else if (command == "tp") {
				if (argumentList.size() < 2) {
					tooFewArguments();
				}

				//Use arguments
				float posX = Main.game.getPlayer().getPos().x;
				float posY = Main.game.getPlayer().getPos().y;

				//Position arguments
				if (argumentList.size() >= 2) {

					vec2 returnVec = argToPos(argumentList.get(0), argumentList.get(1));
					if (returnVec == null) {
						addChatLine("Invalid <x y> arguments!");
						return;
					}
					posX = returnVec.x;
					posX = returnVec.y;
					
				}

				//Execute the command
				Main.game.getPlayer().setPos(new vec2(posX, posY));

				addChatLine("Teleported player to " + posX + ", " + posY);
			}
			//RESPAWN COMMAND
			else if (command == "respawn") {
				Main.game.getPlayer().die();
				addChatLine("The player went crazy and decided to commit not alive");
			}
			//SPAWN COMMAND
			else if (command == "spawn") {
				if (argumentList.size() < 1) {
					tooFewArguments();
					return;
				}
				int id = 0;
				int n = 1;
				boolean item = false;

				float posX = Main.game.getPlayer().getPos().x;
				float posY = Main.game.getPlayer().getPos().y;


				//Id argument
				if (argumentList.size() >= 1) {

					id = (int) argToFloat(argumentList.get(0));
					if (id == (int) Float.NaN) {
						addChatLine("Invalid [id] argument!");
						return;
					}

				}

				//Position arguments
				if (argumentList.size() >= 3) {

					vec2 returnVec = argToPos(argumentList.get(0), argumentList.get(1));
					if (returnVec == null) {
						addChatLine("Invalid <x y> arguments!");
						return;
					}

				}

				//Position arguments
				if (argumentList.size() >= 4) {

					if (argumentList.get(3).equals("true")) {
						item = true;
					}
					else if (argumentList.get(3).equals("false")) {
						item = false;
					}
					else {
						addChatLine("Invalid <item> argument!");
						return;
					}
				}

				//Position arguments
				if (argumentList.size() >= 5) {

					n = (int) argToFloat(argumentList.get(4));
					if (n == (int) Float.NaN) {
						addChatLine("Invalid <n> argument!");
						return;
					}

				}

				//Check errors
				if (!item) {
					if (id >= Database.getCreatureCount()) {
						addChatLine(id + " isn't a valid creature");
					}
				}
				else {
					if (id >= Database.getCreatureCount()) {
						addChatLine(id + " isnt a valid item");
					}
				}

				//Execute the command
				if (item) {
					addChatLine(n + " " + Database.getItem(id).name + "(s) spawned at " + posX + ", " + posY);
				}
				else {
					addChatLine(n + " " + Database.getCreature(id).name + "(s) spawned at " + posX + ", " + posY);
				}
				for (int i = 0; i < (int)n; i++) {
					Main.game.getMap().addCreature(posX, posY, id, item);
				}
			}
			//SETSPAWN COMMAND
			else if (command == "setspawn") {
				float posX = Main.game.getPlayer().getPos().x;
				float posY = Main.game.getPlayer().getPos().y;

				//Position arguments
				if (argumentList.size() >= 2) {

					vec2 returnVec = argToPos(argumentList.get(0), argumentList.get(1));
					if (returnVec == null) {
						addChatLine("Invalid <x y> arguments!");
						return;
					}

				}

				//Execute the command
				Main.game.getPlayer().setSpawnPoint((int)posX, (int)posY);
			}
			//OBJECT COMMAND
			else if (command == "object") {
				if (argumentList.size() < 1) {
					tooFewArguments();
					return;
				}

				int id = 0;

				float posX = Main.game.getPlayer().getPos().x;
				float posY = Main.game.getPlayer().getPos().y;


				//Id argument
				if (argumentList.size() >= 1) {

					id = (int) argToFloat(argumentList.get(0));
					if (id == (int) Float.NaN) {
						addChatLine("Invalid [id] argument!");
						return;
					}

				}

				//Position arguments
				if (argumentList.size() >= 3) {

					vec2 returnVec = argToPos(argumentList.get(0), argumentList.get(1));
					if (returnVec == null) {
						addChatLine("Invalid <x y> arguments!");
						return;
					}

				}

				//Check errors
				if (id >= Database.getObjectCount()) {
					addChatLine(id + " isnt a valid object");
				}

				//Execute the command
				addChatLine(Database.getObject(id).name + " placed at " + posX + ", " + posY);
				MapTile selected_Tile = Main.game.getMap().getTile((int)posX, (int)posY);
				selected_Tile.object = id;
				selected_Tile.objectHealth = Database.getObject(id).health;
			}
			//SAVE COMMAND
			else if (command == "save") {
				Main.game.saveWorld();
			}
			//LOAD COMMAND
			else if (command == "load") {
				
				String name = Main.game.getMap().getWorldName();
				//Id argument
				if (argumentList.size() >= 1) {
					name = argumentList.get(0);
				}

				//Logger.out(name);
				Main.game.loadWorld(name, false);
				
			}
			//UPDATEWARP COMMAND
			else if (command == "warp") {
				
				int updates = 0;
				//Id argument
				if (argumentList.size() >= 1) {
					
					updates = (int) argToFloat(argumentList.get(0));
					if (updates == (int) Float.NaN) {
						addChatLine("Invalid [updates] argument!");
						return;
					}
					
				}

				if (updates < 0) {
					addChatLine("Cannot warp less than 0 updates");
					return;
				}

				addChatLine("Warping " + updates + " updates");

				for (int i = 0; i < updates; i++)
				{
					Main.game.update();
				}
			}
			//GOD COMMAND
			else if (command == "god") {
				
				boolean inGodmode = Main.game.getPlayer().getGodmode();
				if (!inGodmode) addChatLine("Godmode: ON");
				else addChatLine("Godmode: OFF");

				Main.game.getPlayer().setGodmode(!inGodmode);
			
			}
			//NOCLIP COMMAND
			else if (command == "noclip") {

				boolean clipmode = Main.game.getPlayer().getGodmode();
				if (clipmode) addChatLine("Clip: NO");
				else addChatLine("Clip: YES");

				Main.game.getPlayer().setClipmode(!clipmode);
				
			}
			//HELP COMMAND
			else if (command == "help") {
				int page = 0;

				if (argumentList.size() >= 1) {
					
					page = (int) argToFloat(argumentList.get(0));
					if (page == (int) Float.NaN) {
						addChatLine("Invalid [page] argument!");
						return;
					}
					
				}

				addChatLine("-- List of commands [" + page + "] --");

				if (page == 0) {
					addChatLine("$0/$4help $6<0-2>                    $7-- list of commands");
					addChatLine("$0/$4clear                           $7-- clears chat");
					addChatLine("$0/$4tp $3[x] [y]                    $7-- teleports to [x] [y]");
					addChatLine("$0/$4respawn                         $7-- kills the player");
					addChatLine("$0/$4spawn $3[id] $6<x y> <item> <n> $7-- spawns creature");
				}
				else if (page == 1) {
					addChatLine("$0/$4setspawn $6<x y>                $7-- sets spawnpoint");
					addChatLine("$0/$4object $3[id] $6<x y>           $7-- places object");
					addChatLine("$0/$4save                            $7-- saves the world");
					addChatLine("$0/$4load $6<name>                   $7-- load world");
					addChatLine("$0/$4warp $3[updates]                $7-- update warping");
				}
				else if (page == 2) {
					addChatLine("$0/$4god                               $7-- toggle godmode");
					addChatLine("$0/$4noclip                            $7-- toggle noclip");
					//addChatLine("empty                        -- template for new commands");
					//addChatLine("empty                        -- template for new commands");
					//addChatLine("empty                        -- template for new commands");
				}
			}
			//COMMAND NOT FOUND
			else {
				addChatLine("Command \"" + current_line + "\", was not found");
			}
		}
		else {
			//Not a command but regular text
			addChatLine("Player: " + current_line);
		}
	}

	private float argToFloat(String string) {
		float n = 0;

		try
		{
			n += Float.parseFloat(string);
		}
		catch (NumberFormatException e)
		{
			return Float.NaN;
		}
		return n;
	}

	private vec2 argToPos(String x_string, String y_string) {
		float x = 0;
		float y = 0;

		if (x_string.charAt(0) == '*') {
			x = Main.game.getPlayer().getPos().x;
			x_string = x_string.substring(1, x_string.length() - 1);
		}
		if (y_string.charAt(0) == '*') {
			y = Main.game.getPlayer().getPos().y;
			y_string = y_string.substring(1, y_string.length() - 1);
		}

		try
		{
			if(x_string.length() != 0) x += Float.parseFloat(x_string);
			if (y_string.length() != 0) y += Float.parseFloat(y_string);
		}
		catch (NumberFormatException e)
		{
			//e.printStackTrace();
			return null;
		}
		return new vec2(x, y);
	}

	private void tooFewArguments() {
		addChatLine("Not enough arguments");
		addChatLine("Type in \"/help\" to see list of commands and their uses");
	}
	
}
