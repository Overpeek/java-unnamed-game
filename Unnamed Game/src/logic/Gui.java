package logic;

import java.util.ArrayList;

import graphics.Renderer;
import graphics.Shader;
import graphics.TextLabelTexture;
import graphics.primitives.Quad;
import utility.Colors;
import utility.Console;
import utility.Keys;
import utility.Logger;
import utility.Maths;
import utility.mat4;
import utility.vec2;
import utility.vec4;
import world.Map.MapTile;

public class Gui {

	private static final int MAX_TEXT_LINES = 10;
	private static final int GUI_FRAME_LOGGER_SIZE = 300;
	private static final int GUI_UPDATE_LOGGER_SIZE = 60;
	private static final float GUI_FRAME_LOGGER_BAR_WIDTH = 1.0f / GUI_FRAME_LOGGER_SIZE;
	private static final float GUI_UPDATE_LOGGER_BAR_WIDTH = 1.0f / GUI_UPDATE_LOGGER_SIZE;
	private static final vec4 COLOR_HEALTH_BAR = new vec4(1.0f, 0.2f, 0.2f, 1.0f);
	private static final vec4 COLOR_STAMINA_BAR = new vec4(0.2f, 1.0f, 0.2f, 1.0f);
	private static final vec4 COLOR_CHAT_INPUT_BG = new vec4(0.0f, 0.0f, 0.0f, 0.2f);
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
	private boolean chat_opened;
	private boolean chat_just_opened;
	
	private Shader gui_shader;
	private Renderer blur_renderer;
	private Renderer normal_renderer;

	private TextLabelTexture fps_label;
	private TextLabelTexture ups_label;
	private TextLabelTexture pos_label;
	private TextLabelTexture cpu_label;
	private TextLabelTexture gpu_label;
	private TextLabelTexture ram_label;
	private TextLabelTexture renderer_label;

	private TextLabelTexture avg_frame_label;
	private TextLabelTexture avg_update_label;

	private int current_input_history;
	private String current_line;
	private String current_line_reserved;
	private ArrayList<String> last_inputs = new ArrayList<String>();
	private String text_lines[] = new String[MAX_TEXT_LINES];
	private TextLabelTexture text_chat_label[] = new TextLabelTexture[MAX_TEXT_LINES];
	private TextLabelTexture text_input_label;

	private TextLabelTexture pause_indicator_label;
	private TextLabelTexture button_label_0;
	private TextLabelTexture button_label_1;
	private TextLabelTexture button_label_2;
	
	private Console consoleWindow;
	
	

	public Gui(float maxHealth, float maxStamina, float healthGainRate, float staminaGainRate) {
		frame_logger = new float[GUI_FRAME_LOGGER_SIZE];
		update_logger = new float[GUI_UPDATE_LOGGER_SIZE];

		for (int i = 0; i < GUI_FRAME_LOGGER_SIZE; i++) frame_logger[i] = 0;
		for (int i = 0; i < GUI_UPDATE_LOGGER_SIZE; i++) update_logger[i] = 0;

		current_input_history = 0;
		chat_just_opened = false;
		chat_opened = false;
		chat_opened_timer = 0.0f;
		currentUpdate = 0;
		currentFrame = 0;
		current_line = "";
		selectedButton = 0;
		selected_logger = true;
		avg_frame = 0.0f;
		avg_update = 0.0f;

		fps_label = TextLabelTexture.bakeToTexture("FPS: 0");
		ups_label = TextLabelTexture.bakeToTexture("UPS: 0");
		pos_label = TextLabelTexture.bakeToTexture("POS: 0");
		cpu_label = TextLabelTexture.bakeToTexture("Threads: " + Runtime.getRuntime().availableProcessors());
		gpu_label = TextLabelTexture.bakeToTexture("GPU: " + Main.game.getWindow().getRenderer());
		renderer_label = TextLabelTexture.bakeToTexture("Renderer: " + Main.game.getWindow().getGL());
		ram_label = TextLabelTexture.bakeToTexture("Memory: null");

		avg_frame_label = TextLabelTexture.bakeToTexture("0");
		avg_update_label = TextLabelTexture.bakeToTexture("0");

		pause_indicator_label = TextLabelTexture.bakeToTexture("PAUSED");
		button_label_0 = TextLabelTexture.bakeToTexture("RESUME");
		button_label_1 = TextLabelTexture.bakeToTexture("SETTINGS");
		button_label_2 = TextLabelTexture.bakeToTexture("SAVE AND EXIT");

		text_input_label = TextLabelTexture.bakeToTexture("");
		TextLabelTexture.bakeToTexture("0");
		for (int i = 0; i < MAX_TEXT_LINES; i++) {
			text_lines[i] = "";
			text_chat_label[i] = TextLabelTexture.bakeToTexture(text_lines[i]);
		}
		
		consoleWindow = new Console(new vec2(0.0f, 0.0f), new vec2(0.5f, 0.5f));

		gui_shader = Shader.multiTextureShader();
		blur_renderer = new Renderer();
		normal_renderer = new Renderer();
	}
	
	private void drawFrameLogger() {
		float textScale = 0.08f * Main.game.renderScale();
		vec2 pos = new vec2(-Main.game.getWindow().getAspect(), 2.0f / 3.0f);
		vec2 size = new vec2(GUI_FRAME_LOGGER_BAR_WIDTH * (GUI_FRAME_LOGGER_SIZE - 1), 0.005f).mul(Main.game.renderScale());
		vec2 textPos = new vec2(-Main.game.getWindow().getAspect(), 2.0f / 3.0f - textScale * Main.game.renderScale());
		vec2 textSize = new vec2(textScale * 0.8f);
		normal_renderer.submit(new Quad(pos, size, 0, Colors.GREEN));
		if (selected_logger) { // Frame logger
			// Top bar
			String text = "F avg ms: " + (int) avg_frame;
			avg_frame_label.rebake(text);
			avg_frame_label.queueDraw(textPos, textSize);

			// Actual logger
			for (int i = 0; i < GUI_FRAME_LOGGER_SIZE; i++) {
				float bar_height = (frame_logger[i] / avg_frame) / 3.0f;
				pos = new vec2(
						-Main.game.getWindow().getAspect() + (GUI_FRAME_LOGGER_BAR_WIDTH * Main.game.renderScale() * i),
						1.0f - bar_height);
				size = new vec2(
						GUI_FRAME_LOGGER_BAR_WIDTH * Main.game.renderScale(), 
						bar_height);

				float color_rg = Math.max(0.0f, 0.8f - bar_height);
				vec4 barColor = new vec4(Math.max(0.0f, 1.0f - color_rg), color_rg, 0.0f, 1.0f);
				normal_renderer.submit(new Quad(pos, size, 0, barColor));
			}
		} else { // Update logger
					// Top bar
			String text = "U avg ms: " + (int) avg_update;
			avg_update_label.rebake(text);
			avg_update_label.queueDraw(textPos, textSize);

			// Actual logger
			for (int i = 0; i < GUI_UPDATE_LOGGER_SIZE; i++) {
				float bar_height = (update_logger[i] / avg_update) / 3.0f;
				pos = new vec2(
						-Main.game.getWindow().getAspect() + (GUI_UPDATE_LOGGER_BAR_WIDTH * Main.game.renderScale() * i),
						1.0f - bar_height);
				size = new vec2(
						GUI_UPDATE_LOGGER_BAR_WIDTH * Main.game.renderScale(), 
						bar_height);

				vec4 barColor = new vec4(Math.max(0.0f, bar_height - 0.5f), Math.max(0.0f, 0.5f - bar_height), 0.0f, 1.0f);
				normal_renderer.submit(new Quad(pos, size, 0, barColor));
			}
		}
	}
	
	private void updateUpdateLogger() {
		currentUpdate++;
		if (currentUpdate >= GUI_UPDATE_LOGGER_SIZE)
			currentUpdate = 0;
		update_logger[(int) currentUpdate] = Main.game.getLoop().getUns() / 1000000.0f;

		avg_update = 0;
		for (int i = 0; i < GUI_UPDATE_LOGGER_SIZE; i++) {
			avg_update += update_logger[i];
		}
		avg_update /= (float) GUI_UPDATE_LOGGER_SIZE;
	}
	
	private void updateFrameLogger() {
		currentFrame++;
		if (currentFrame >= GUI_FRAME_LOGGER_SIZE)
			currentFrame = 0;
		frame_logger[(int) currentFrame] = Main.game.getLoop().getFns() / 1000000.0f;

		avg_frame = 0;
		int nonZeroCount = 0;
		for (int i = 0; i < GUI_FRAME_LOGGER_SIZE; i++) {
			if (frame_logger[i] != 0.0f)
				nonZeroCount++;
			avg_frame += frame_logger[i];
		}
		avg_frame /= (float) nonZeroCount;
	}

	private void drawDebugScreen() {
		float textScale = 0.08f * Main.game.renderScale();
		float x = -Main.game.getWindow().getAspect();
		vec2 size = new vec2(textScale);
		vec2 pos = new vec2(x, -1.0f);
		
		// Debug mode
		if (Main.game.debugMode) {
			// Debug mode text
			String text = "FPS: " + Main.game.getLoop().getFps();
			fps_label.rebake(text);
			fps_label.queueDraw(pos, size);

			pos.add(0.0f, textScale);
			text = "UPS: " + Main.game.getLoop().getUps();
			ups_label.rebake(text);
			ups_label.queueDraw(pos, size);
		}
		
		// Advanced debug mode
		if (Main.game.advancedDebugMode) {
			// Advanced debug mode text
			pos.add(0.0f, textScale);
			String text = "Position X: " + Main.game.getPlayer().getPos().x + ", Y: " + Main.game.getPlayer().getPos().y;
			pos_label.rebake(text);
			pos_label.queueDraw(pos, size);

			pos.add(0.0f, textScale);
			renderer_label.queueDraw(pos, size);

			pos.add(0.0f, textScale);
			gpu_label.queueDraw(pos, size);

			pos.add(0.0f, textScale);
			float usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576.0f;
			float maxMemory = Runtime.getRuntime().totalMemory() / 1048576.0f;
			ram_label.rebake("Memory: " + usedMemory + "MB / " + maxMemory + "MB");
			pos_label.rebake(text);
			ram_label.queueDraw(pos, size);

			pos.add(0.0f, textScale);
			cpu_label.queueDraw(pos, size);
			
			drawFrameLogger();
		}
	}
	
	private void drawChat() {
		float textScale = 0.08f * Main.game.renderScale();
		if (chat_opened || chat_opened_timer > 0) {

			if (chat_opened) {
				vec2 pos = new vec2(-Main.game.getWindow().getAspect(), 1.0f - textScale);
				vec2 size = new vec2(textScale);

				// Text input line
				normal_renderer.submit(new Quad(pos, new vec2(Main.game.getWindow().getAspect() * 2.0f, textScale), 0, COLOR_CHAT_INPUT_BG));
				text_input_label.rebake(current_line);
				text_input_label.queueDraw(pos, size);
			}

			for (int i = 0; i < MAX_TEXT_LINES; i++) {
				vec2 pos = new vec2(-Main.game.getWindow().getAspect(), 1.0f - ((i + 3) * textScale));
				vec2 size = new vec2(textScale);

				text_chat_label[i].rebake(text_lines[i]);
				text_chat_label[i].queueDraw(pos, size);
			}
		}
	}
	
	private void drawPauseMenu() {
		if (Main.game.paused) {

			// Loop trough all pause menu buttons
			if (selectedButton > PAUSEMENU_BUTTON_COUNT - 1)
				selectedButton = 0;
			if (selectedButton < 0)
				selectedButton = PAUSEMENU_BUTTON_COUNT - 1;

			vec2 buttonTextScale = new vec2(0.1f * Main.game.renderScale());

			pause_indicator_label.queueDrawCentered(new vec2(0.003f, -0.003f - 0.75f), buttonTextScale);

			for (int i = 0; i < 3; i++) {
				float shade = 0.1f;
				if (i == selectedButton)
					shade = 0.2f;
				vec2 pos = new vec2(0.0f - 0.4f, -0.50f - 0.05f + (i * 0.15f));
				vec2 size = new vec2(0.8f, 0.1f);

				normal_renderer.submit(new Quad(pos, size, 0, new vec4(0.0f, 0.0f, 0.0f, shade)));
			}

			buttonTextScale = new vec2(0.1f * Main.game.renderScale());
			button_label_0.queueDrawCentered(new vec2(0.0f, -0.5f), buttonTextScale);
			button_label_1.queueDrawCentered(new vec2(0.0f, -0.35f), buttonTextScale);
			button_label_2.queueDrawCentered(new vec2(0.0f, -0.20f), buttonTextScale);
		}
	}
	
	private void drawHUD() {
		float renderScale = Main.game.renderScale();
		float heightDifference = 0.06f * renderScale;
		// Bar bg
		vec2 pos = new vec2(Main.game.getWindow().getAspect(), -1.0f).add(-0.6f * renderScale, 0.1f * renderScale);
		vec2 size = new vec2(0.5f, 0.05f).mul(Main.game.renderScale());
		blur_renderer.submit(new Quad(pos, size, 0, Colors.BLACK)); // HP
		pos.add(0.0f, heightDifference);
		blur_renderer.submit(new Quad(pos, size, 0, Colors.BLACK)); // S

		pos = new vec2(Main.game.getWindow().getAspect(), -1.0f).add(-0.595f * renderScale, 0.105f * renderScale);
		size = new vec2(0.49f * (Main.game.getPlayer().getHealth() / Main.game.getPlayer().getMaxHealth()), 0.04f).mul(Main.game.renderScale() * Main.game.guiScale());
		blur_renderer.submit(new Quad(pos, size, 0, COLOR_HEALTH_BAR)); // HP
		pos.add(0.0f, heightDifference);
		blur_renderer.submit(new Quad(pos, size, 0, COLOR_STAMINA_BAR)); // S
	}
	
	public void draw() {
		
		
		blur_renderer.clear();
		normal_renderer.clear();
		
		// Projection
		mat4 pr_matrix = new mat4().ortho(-Main.game.getWindow().getAspect(), Main.game.getWindow().getAspect(), 1.0f, -1.0f);
		gui_shader.setUniformMat4("pr_matrix", pr_matrix);
		
		// Heads up display
		drawHUD();

		// Debug screen
		updateFrameLogger();
		drawDebugScreen();

		// Pause menu
		drawPauseMenu();
		
		// Chat
		drawChat();
		
		consoleWindow.draw();
		
		gui_shader.bind();
		TextureLoader.getTexture().bind();
		blur_renderer.draw();
		normal_renderer.draw();
	}

	public void shiftChatUp() {
		for (int i = MAX_TEXT_LINES - 1; i >= 1; i--) {
			text_lines[i] = text_lines[i - 1];
		}
	}

	public void addChatLine(String text) {
		if (text.length() == 0)
			return;

		Logger.info("chat: " + text);
		chat_opened_timer = 5.0f;
		shiftChatUp();
		text_lines[0] = text;
	}

	private void addInputToLatest(String text) {
		last_inputs.add(text);
	}

	public void update(float ups) {
		chat_opened_timer -= 1.0f / ups;
		
		updateUpdateLogger();
	}

	public void keyPress(int key, int action) {
		if (Main.game.paused) {
			// Pause menu navigation
			if (action == Keys.PRESS && key == Keys.KEY_DOWN) {
				selectedButton++;
				return;
			}
			if (action == Keys.PRESS && key == Keys.KEY_UP) {
				selectedButton--;
				return;
			}
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
		} else {
			if (action == Keys.PRESS && key == Keys.KEY_T) {
				// Only opens chat
				if (chat_opened)
					return;

				chat_opened = true;
				chat_just_opened = true;
				return;
			}
			if (action == Keys.PRESS && key == Keys.KEY_ESCAPE && chat_opened) {
				// Only closes chat if its opened
				chat_opened = false;
				chat_opened_timer = 5.0f;
				return;
			}
			if (action == Keys.PRESS && key == Keys.KEY_ENTER && chat_opened) {
				// Only closes chat if its opened
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
				// Removes last character of current text input
				// if chat is currently opened
				if (current_line.length() > 0)
					current_line = current_line.substring(0, current_line.length() - 1);
				return;
			}
			if (action == Keys.PRESS && key == Keys.KEY_UP && chat_opened) {
				if (current_input_history == -1)
					current_line_reserved = current_line;
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
		if (action == Keys.PRESS && key == Keys.KEY_F9) {
			selected_logger = !selected_logger;
			return;
		}
	}

	private void selectInputHistory() {
		if (last_inputs.size() == 0)
			return;
		current_input_history = (int) Maths.clamp(current_input_history, -1.0f, (int) last_inputs.size() - 1);

		if (current_input_history == -1) {
			current_line = current_line_reserved;
			return;
		}
		current_line = last_inputs.get((int) (last_inputs.size() - 1 - current_input_history));
	}

	public void typing(int character, int mods) {
		if (chat_opened) {
			if (chat_just_opened) {
				chat_just_opened = false;
				return;
			} else {
				current_line += (char) character;
			}
		}
	}

	private void userInput() {
		if (current_line.length() == 0)
			return;

		// Check if command is typed
		if (current_line.charAt(0) == '/') { // /command arg0 arg1
			//Parse input to all arguments
			String[] argumentList = current_line.substring(1).split(" "); // Split input to all arguments and remove "/"
			ArrayList<String> list = new ArrayList<String>();
			for (int i = 0; i < argumentList.length; i++) {
				
				if (!argumentList[i].equals("")) { 
					list.add(argumentList[i]); 
					continue; 
				}
				
			}
			argumentList = new String[list.size()];
			for (int i = 0; i < argumentList.length; i++) {
				
				argumentList[i] = list.get(i);
				//Logger.debug("arg[" + i + "] = " + argumentList[i]);
				
			}

			// COMMANDS
			if (argumentList[0].equals("clear")) {
				// Easter egg #01
				tooFewArguments();
				addChatLine("Haha, just kidding ya lol");
				addChatLine("Pls stop reading my source code, my english is bad r/engrish");

				// Execute the command
				for (int i = 0; i < text_lines.length; i++) {
					text_lines[i] = " ";
				}
			}
			// TP COMMAND
			else if (argumentList[0].equals("tp")) {
				if (argumentList.length < 3) {
					tooFewArguments();
					return;
				}

				// Use arguments
				float posX = Main.game.getPlayer().getPos().x;
				float posY = Main.game.getPlayer().getPos().y;

				// Position arguments
				if (argumentList.length >= 3) {

					vec2 returnVec = argToPos(argumentList[1], argumentList[2]);
					if (returnVec == null) {
						addChatLine("Invalid $3[x] [y]$0 arguments!");
						return;
					}
					posX = returnVec.x;
					posY = returnVec.y;

				}

				// Execute the command
				Main.game.getPlayer().setPos(new vec2(posX, posY));

				addChatLine("Teleported player to " + posX + ", " + posY);
			}
			// RESPAWN COMMAND
			else if (argumentList[0].equals("respawn")) {
				Main.game.getPlayer().die();
				addChatLine("The player went crazy and decided to commit not alive");
			}
			// SPAWN COMMAND
			else if (argumentList[0].equals("spawn")) {
				if (argumentList.length < 2) {
					tooFewArguments();
					return;
				}
				Integer id = 0;
				Integer n = 1;
				boolean item = false;

				float posX = Main.game.getPlayer().getPos().x;
				float posY = Main.game.getPlayer().getPos().y;

				// Id argument
				if (argumentList.length >= 2) {

					id = argToInt(argumentList[1]);
					if (id == null) {
						addChatLine("Invalid $3[id]$0 argument!");
						return;
					}

				}

				// Position arguments
				if (argumentList.length >= 4) {

					vec2 returnVec = argToPos(argumentList[2], argumentList[3]);
					if (returnVec == null) {
						addChatLine("Invalid $6<x> <y>$0 arguments!");
						return;
					}

				}

				// Item? arguments
				if (argumentList.length >= 5) {

					if (argumentList[4].equals("true")) {
						item = true;
					} else if (argumentList[4].equals("false")) {
						item = false;
					} else {
						addChatLine("Invalid $6<item>$0 argument!");
						return;
					}
				}

				// Count arguments
				if (argumentList.length >= 6) {

					n = argToInt(argumentList[5]);
					if (n == null) {
						addChatLine("Invalid $6<n>$0 argument!");
						return;
					}

				}

				// Check errors
				if (!item) {
					if (id >= Database.getCreatureCount()) {
						addChatLine(id + " isn't a valid creature");
					}
				} else {
					if (id >= Database.getCreatureCount()) {
						addChatLine(id + " isnt a valid item");
					}
				}

				// Execute the command
				if (item) {
					addChatLine(n + " " + Database.getItem(id).name + "(s) spawned at " + posX + ", " + posY);
				} else {
					addChatLine(n + " " + Database.getCreature(id).name + "(s) spawned at " + posX + ", " + posY);
				}
				for (int i = 0; i < (int) n; i++) {
					//Main.game.getMap().addCreature(posX, posY, id, item);
				}
			}
			// SETSPAWN COMMAND
			else if (argumentList[0].equals("setspawn")) {
				if (argumentList.length < 3) {
					tooFewArguments();
					return;
				}
				
				float posX = Main.game.getPlayer().getPos().x;
				float posY = Main.game.getPlayer().getPos().y;

				// Position arguments
				if (argumentList.length >= 3) {

					vec2 returnVec = argToPos(argumentList[1], argumentList[2]);
					if (returnVec == null) {
						addChatLine("Invalid $6<x> <y>$0 arguments!");
						return;
					}

				}

				// Execute the command
				Main.game.getPlayer().setSpawnPoint((int) posX, (int) posY);
			}
			// OBJECT COMMAND
			else if (argumentList[0].equals("object")) {
				if (argumentList.length < 2) {
					tooFewArguments();
					return;
				}

				String id = null;

				float posX = Main.game.getPlayer().getPos().x;
				float posY = Main.game.getPlayer().getPos().y;

				// Id argument
				if (argumentList.length >= 2) id = argumentList[1];

				// Position arguments
				if (argumentList.length >= 3) {

					vec2 returnVec = argToPos(argumentList[2], argumentList[3]);
					if (returnVec == null) {
						addChatLine("Invalid $6<x> <y>$0 arguments!");
						return;
					}

				}

				// Check errors
				if (Database.getObject(id) == null) {
					addChatLine(id + " isn't a valid object");
					return;
				}

				// Execute the command
				addChatLine(Database.getObject(id).name + " placed at " + (int) posX + ", " + (int) posY);
				MapTile selected_Tile = Main.game.getMap().getTile((int) posX, (int) posY);
				selected_Tile.object = Database.getObject(id).index;
				selected_Tile.objectHealth = Database.getObject(id).health;
				
				Main.game.getMap().updateCloseTiles((int) posX, (int) posY);
			}
			// SAVE COMMAND
			else if (argumentList[0].equals("save")) {
				Main.game.saveWorld();
			}
			// LOAD COMMAND
			else if (argumentList[0].equals("load")) {

				String name = Main.game.getMap().getWorldName();
				// Id argument
				if (argumentList.length >= 2) {
					name = argumentList[1];
				}

				// Logger.out(name);
				Main.game.loadWorld(name, false);

			}
			// UPDATEWARP COMMAND
			else if (argumentList[0].equals("warp")) {

				Integer updates = 0;
				// Id argument
				if (argumentList.length >= 2) {

					updates = argToInt(argumentList[1]);
					if (updates == null) {
						addChatLine("Invalid $3[updates]$0 argument!");
						return;
					}

				}

				if (updates < 0) {
					addChatLine("Cannot warp less than 0 updates");
					return;
				}

				addChatLine("Warping " + updates + " updates");

				for (int i = 0; i < updates; i++) {
					Main.game.update();
				}
			}
			// GOD COMMAND
			else if (argumentList[0].equals("god")) {

				boolean inGodmode = Main.game.getPlayer().getGodmode();
				if (!inGodmode)
					addChatLine("Godmode: on");
				else
					addChatLine("Godmode: off");

				Main.game.getPlayer().setGodmode(!inGodmode);

			}
			// NOCLIP COMMAND
			else if (argumentList[0].equals("noclip")) {

				boolean clipmode = Main.game.getPlayer().getClipmode();
				Main.game.getPlayer().setClipmode(!clipmode);
				
				if (clipmode)
					addChatLine("Clipmode: clipping");
				else
					addChatLine("Clipmode: not clipping");

			}
			// LIST COMMAND
			else if (argumentList[0].equals("list")) {
				listCommand(argumentList);
				return;
			}
			// HELP COMMAND
			else if (argumentList[0].equals("help")) {
				Integer page = 0;

				if (argumentList.length >= 2) {

					page = argToInt(argumentList[1]);
					//Test for if it was command help request
					if (page == null) {
						if (argumentList[1].equals("help")) {
							addChatLine("$0/$4help $6<page or command>");
							addChatLine("argument 0 can be empty, page number or command");
							addChatLine("* can be added before any position argument to add player's position");
							addChatLine("default position is always player's position");
							addChatLine("$7example: /help tp");
						} else if (argumentList[1].equals("clear")) {
							addChatLine("$0/$4clear");
							addChatLine("clear chat");
							addChatLine("$7example: /clear");
						} else if (argumentList[1].equals("tp")) {
							addChatLine("$0/$4tp $3[x] [y]");
							addChatLine("teleport player to position $3[x, y]$0");
							addChatLine("$7example: /tp 10 10");
							addChatLine("$7example: /tp *0 *5");
						} else if (argumentList[1].equals("respawn")) {
							addChatLine("$0/$4respawn");
							addChatLine("kill player to respawn");
							addChatLine("$7example: /respawn");
						} else if (argumentList[1].equals("spawn")) {
							addChatLine("$0/$4spawn $3[id] $6<x> <y> <item> <n>");
							addChatLine("spawn creature(s) with $3[id]");
							addChatLine("$6<x y>$0 is position where the creature will be spawned");
							addChatLine("$6<item>$0 is boolean value (true or false) and tells if the spawned creature is an item");
							addChatLine("$6<n>$0 is the count of spawned creatures");
							addChatLine("$7example: /spawn unnamed *0 *0 false 1");
							addChatLine("$7example: /spawn fiber *0 *0 true 1");
						} else if (argumentList[1].equals("setspawn")) {
							addChatLine("$0/$4setspawn $6<x> <y>");
							addChatLine("set player's spawnpoint where player will respawn");
							addChatLine("$7example: /setspawn 100 100");
						} else if (argumentList[1].equals("object")) {
							addChatLine("$0/$4object $3[id] $6<x> <y>");
							addChatLine("set object $3[id]$0 at position $6<x> <y>");
							addChatLine("$7example: /object stonewall *2 *0");
						} else if (argumentList[1].equals("save")) {
							addChatLine("$0/$4save");
							addChatLine("save world");
							addChatLine("$7example: /save");
						} else if (argumentList[1].equals("load")) {
							addChatLine("$0/$4load $6<name>");
							addChatLine("load world with $6<name>$0 or reload world if no name argument");
							addChatLine("$7example: /setspawn 100 100");
						} else if (argumentList[1].equals("warp")) {
							addChatLine("$0/$4warp $3[updates]");
							addChatLine("warp through $3[updates]$0 updates as fast as possible");
							addChatLine("default UPS (updates per second) is 60");
							addChatLine("$7example: /warp 6000");
						} else if (argumentList[1].equals("god")) {
							addChatLine("$0/$4god");
							addChatLine("toggle godmode");
							addChatLine("$7example: /god");
						} else if (argumentList[1].equals("noclip")) {
							addChatLine("$0/$4noclip");
							addChatLine("toggle noclip");
							addChatLine("$7example: /noclip");
						} else if (argumentList[1].equals("list")) {
							addChatLine("$0/$4list $3[tiles, objects, creatures, items, particles, sounds, mods, biomes]$0");
							addChatLine("list of all objects $3[tiles, objects, creatures, items, particles, sounds, mods, biomes]$0");
							addChatLine("$7example: /list objects");
						} else {
							addChatLine("Command \"" + argumentList[1] + "\", doesn't exist");
						}
						return;
					} else {
						helpPage(page);
						return;
					}
				}
				helpPage(0);
			}
			// COMMAND NOT FOUND
			else {
				addChatLine("Command \"" + argumentList[0] + "\", doesn't exist");
			}
		} else {
			// Not a command but regular text
			addChatLine("Player: " + current_line);
		}
	}
	
	/**
	 * argv[0] is the command itself
	 * */
	private void listCommand(String argv[]) {		
		String listtype = null;
		// Id argument
		if (argv.length >= 2) {
			listtype = argv[1];
		}
		
		int usedComponents = 0;
		String currentLine = "";
		switch (listtype) {
			case "tiles":
				for (int i = 0; i < Database.getTileCount(); i++) {
					currentLine += Database.getTile(i).data_name;
					usedComponents++;
					if (usedComponents > 8) {
						usedComponents = 0;
						addChatLine(currentLine);
						currentLine = "";
					}
					currentLine += ", ";
				}
				break;
			case "objects":
				for (int i = 0; i < Database.getObjectCount(); i++) {
					currentLine += Database.getObject(i).data_name;
					usedComponents++;
					if (usedComponents > 8) {
						usedComponents = 0;
						addChatLine(currentLine);
						currentLine = "";
					}
					currentLine += ", ";
				}
				break;
			case "creatures":
				for (int i = 0; i < Database.getCreatureCount(); i++) {
					currentLine += Database.getCreature(i).data_name;
					usedComponents++;
					if (usedComponents > 8) {
						usedComponents = 0;
						addChatLine(currentLine);
						currentLine = "";
					}
					currentLine += ", ";
				}
				break;
			case "items":
				for (int i = 0; i < Database.getItemCount(); i++) {
					currentLine += Database.getItem(i).data_name;
					usedComponents++;
					if (usedComponents > 8) {
						usedComponents = 0;
						addChatLine(currentLine);
						currentLine = "";
					}
					currentLine += ", ";
				}
				break;
			case "particles":
				for (int i = 0; i < Database.getParticleCount(); i++) {
					currentLine += Database.getParticle(i).data_name;
					usedComponents++;
					if (usedComponents > 8) {
						usedComponents = 0;
						addChatLine(currentLine);
						currentLine = "";
					}
					currentLine += ", ";
				}
				break;
			case "sounds":
				for (int i = 0; i < Database.getSoundCount(); i++) {
					currentLine += Database.getSound(i).data_name;
					usedComponents++;
					if (usedComponents > 8) {
						usedComponents = 0;
						addChatLine(currentLine);
						currentLine = "";
					}
					currentLine += ", ";
				}
				break;
			case "biomes":
				for (int i = 0; i < Database.getBiomeCount(); i++) {
					currentLine += Database.getBiome(i).data_name;
					usedComponents++;
					if (usedComponents > 8) {
						usedComponents = 0;
						addChatLine(currentLine);
						currentLine = "";
					}
					currentLine += ", ";
				}
				break;
			case "mods":
				for (int i = 0; i < Database.getModCount(); i++) {
					currentLine += Database.getMod(i).data_name;
					usedComponents++;
					if (usedComponents > 8) {
						usedComponents = 0;
						addChatLine(currentLine);
						currentLine = "";
					}
					currentLine += ", ";
				}
				break;
	
			default:
				addChatLine("Invalid $3[tiles, objects, creatures, items, particles, sounds, mods, biomes]$0 argument!");
				break;
		}
		addChatLine(currentLine.substring(0, currentLine.length() - 2));
	}
	
	private void helpPage(int page) {
		addChatLine("-- List of commands [" + page + "] --");
		
		switch (page) {
		default:
			addChatLine("$0/$4help $6<page or command>");
			addChatLine("$0/$4clear");
			addChatLine("$0/$4tp $3[x] [y]");
			addChatLine("$0/$4respawn");
			addChatLine("$0/$4spawn $3[id] $6<x> <y> <item> <n>");
			break;
			
		case 1:
			addChatLine("$0/$4setspawn $6<x> <y>");
			addChatLine("$0/$4object $3[id] $6<x> <y>");
			addChatLine("$0/$4save");
			addChatLine("$0/$4load $6<name>");
			addChatLine("$0/$4warp $3[updates]");
			break;
			
		case 2:
			addChatLine("$0/$4god");
			addChatLine("$0/$4noclip");
			addChatLine("$0/$4list $3[tiles, objects, creatures, items, particles, sounds, mods, biomes]$0");
			// addChatLine("empty -- template for new commands");
			// addChatLine("empty -- template for new commands");
			break;
			
		}
	}

//	private Float argToFloat(String string) {
//		float n = 0;
//
//		try {
//			n += Float.parseFloat(string);
//		} catch (NumberFormatException e) {
//			return null;
//		}
//		return n;
//	}

	private Integer argToInt(String string) {
		float n = 0;

		try {
			n += Float.parseFloat(string);
		} catch (NumberFormatException e) {
			return null;
		}
		return Math.round(n);
	}

	private vec2 argToPos(String x_string, String y_string) {
		float x = 0;
		float y = 0;

		if (x_string.charAt(0) == '*') {
			x = Main.game.getPlayer().getPos().x;
			x_string = x_string.substring(1);
		}
		if (y_string.charAt(0) == '*') {
			y = Main.game.getPlayer().getPos().y;
			y_string = y_string.substring(1);
		}

		try {
			if (x_string.length() > 0)
				x += Float.parseFloat(x_string);
			if (y_string.length() > 0)
				y += Float.parseFloat(y_string);
		} catch (NumberFormatException e) {
			Logger.debug("Couldn't parse \"" + x_string + "\", \"" + y_string + "\"");
			return null;
		}
		return new vec2(x, y);
	}

	private void tooFewArguments() {
		addChatLine("Not enough arguments");
		addChatLine("Type in \"/help\" to see list of commands and their uses");
	}
	
	public boolean chatOpened() {
		return chat_opened;
	}

}
