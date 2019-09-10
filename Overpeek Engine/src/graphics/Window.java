package graphics;

import static org.lwjgl.glfw.GLFW.GLFW_DECORATED;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_DEBUG_CONTEXT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_TRANSPARENT_FRAMEBUFFER;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwHideWindow;
import static org.lwjgl.glfw.GLFW.glfwIconifyWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowIcon;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_DONT_CARE;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_INVALID_ENUM;
import static org.lwjgl.opengl.GL11.GL_INVALID_OPERATION;
import static org.lwjgl.opengl.GL11.GL_INVALID_VALUE;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_OUT_OF_MEMORY;
import static org.lwjgl.opengl.GL11.GL_POINT;
import static org.lwjgl.opengl.GL11.GL_RENDERER;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glGetString;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL30.GL_INVALID_FRAMEBUFFER_OPERATION;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLUtil;

import utility.Application;
import utility.Loader;
import utility.Logger;
import utility.Maths;
import utility.vec2;
import utility.vec4;


public class Window {
	
	public static final int POLYGON_LINE			= 0x00000001;
	public static final int POLYGON_FILL			= 0x00000002;
	public static final int POLYGON_POINT			= 0x00000003;

	public static final int WINDOW_MULTISAMPLE_X2	= 0x00000020;
	public static final int WINDOW_MULTISAMPLE_X4	= 0x00000040;
	public static final int WINDOW_MULTISAMPLE_X8	= 0x00000080;
	public static final int WINDOW_BORDERLESS		= 0x00000100;
	public static final int WINDOW_RESIZEABLE		= 0x00001000;
	public static final int WINDOW_TRANSPARENT		= 0x00010000;
	public static final int WINDOW_FULLSCREEN		= 0x00100000;
	public static final int WINDOW_DEBUGMODE		= 0x01000000;
	public static final int WINDOW_HIDDEN			= 0x10000000;

	private boolean openHidden;
	private int polygonmode;
	private long window;
	private int width;
	private int height;
	private String title;
	private int resizeable;
	private int transparent;
	private int borders;
	private int fullscreen; 
	private int multisample;
	private Application active_Application;
	//private Renderer defaultRenderer;
	//private ArrayList<Button> button_objects;
	private vec4 clearColor;
	
	private boolean debug_mode = true;

	
	private boolean keys[] = new boolean[512];
	private boolean buttons[] = new boolean[128];
	private boolean singleKeys[] = new boolean[512];
	private boolean singleButtons[] = new boolean[128];
	
	private float cursorX, cursorY;
	private float scroll_total;
	private float scroll_delta;
	
	//public Shader shader;
	
	
	public void setCurrentApp(Application app) {
		active_Application = app;
	}
	
	
	
	
	GLFWCharCallback character = new GLFWCharCallback() {

		@Override
		public void invoke(long window, int character) {
			if (active_Application != null) active_Application.charCallback((char)character);
		}
		
	};
	
	GLFWKeyCallback key = new GLFWKeyCallback() {
		
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods)
	    {
			if (active_Application != null) active_Application.keyPress(key, action);
			
			if (key >= keys.length || key < 0) {
				Logger.warn("Unknown key: " + key);
				return;
			}
			if (action == GLFW_PRESS) { keys[key] = true; singleKeys[key] = true; }
			if (action == GLFW_RELEASE) { keys[key] = false; }
	    }
		
	};
	
	GLFWMouseButtonCallback button = new GLFWMouseButtonCallback() {
		
		@Override
		public void invoke(long window, int button, int action, int mods)
	    {
			if (active_Application != null) active_Application.buttonPress(button, action);
			
			if (button >= buttons.length || button < 0) {
				Logger.warn("Unknown button: " + button);
				return;
			}
			if (action == GLFW_PRESS) { buttons[button] = true; singleButtons[button] = true; }
			if (action == GLFW_RELEASE) { buttons[button] = false; }
			
//			for (Button b : button_objects) {
//				b.checkPressed(cursorX, cursorY, button);
//			}
	    }
		
	};
	
	GLFWCursorPosCallback cursor = new GLFWCursorPosCallback() {
		
		@Override
		public void invoke(long window, double xpos, double ypos)
	    {			
			cursorX = Maths.map((float) xpos, 0.0f, getWidth(), -getAspect(), getAspect());
			cursorY = Maths.map((float) ypos, 0.0f, getHeight(), -1.0f, 1.0f);
			
			if (active_Application != null) active_Application.mousePos((float) cursorX, (float) cursorY);
	    }
		
	};
	
	GLFWFramebufferSizeCallback resize = new GLFWFramebufferSizeCallback() {
		
		@Override
		public void invoke(long window, int _width, int _height)
	    {
			viewport(_width, _height);
			
			if (active_Application != null) active_Application.resize(_width, _height);
			
			width = _width;
			height = _height;
			//System.out.println("Width: " + width + ", Height: " + height);
	    }
		
	};
	
	GLFWScrollCallback scroll = new GLFWScrollCallback() {

		@Override
		public void invoke(long window, double xd, double yd) {
			if (active_Application != null) active_Application.scroll((float) xd, (float) yd);
			
			scroll_total += yd;
			scroll_delta += yd;
		}
		
	};
	
	public Window(int width, int height, String title, Application app, int mods) {
		this.title = title;
		this.width = width;
		this.height = height;
		setCurrentApp(app);
		
		
		//Flags
		if ((mods & WINDOW_MULTISAMPLE_X2) != 0) {
			multisample = 2;
		}
		else if ((mods & WINDOW_MULTISAMPLE_X4) != 0) {
			multisample = 4;
		}
		else if ((mods & WINDOW_MULTISAMPLE_X8) != 0) {
			multisample = 8;
		} else multisample = 0;
		
		if ((mods & WINDOW_TRANSPARENT) != 0) {
			transparent = 1;
		} else transparent = 0;
		
		if ((mods & WINDOW_BORDERLESS) != 0) {
			borders = 0;
		} else borders = 1;
		
		if ((mods & WINDOW_RESIZEABLE) != 0) {
			resizeable = 1;
		} else resizeable = 0;
		
		if ((mods & WINDOW_FULLSCREEN) != 0) {
			fullscreen = 1;
		} else fullscreen = 0;
		
		if ((mods & WINDOW_DEBUGMODE) != 0) {
			debug_mode = true;
		} else debug_mode = false;
		
		if ((mods & WINDOW_HIDDEN) != 0) {
			openHidden = true;
		} else openHidden = false;
		
		if ((mods & POLYGON_FILL) != 0) {
			polygonmode = POLYGON_FILL;
		} else if ((mods & POLYGON_LINE) != 0) {
			polygonmode = POLYGON_LINE;
		} else if ((mods & POLYGON_POINT) != 0) {
			polygonmode = POLYGON_POINT;
		} else polygonmode = POLYGON_FILL;
		
		init();
	}
	
	public void init() {
		//Init
		if (!glfwInit()) {
			throw new IllegalStateException("GLFW Init failed!");
		}
		
		//Window
		if (openHidden) glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
		if (debug_mode) glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GL_TRUE);
		if (multisample != 0) glfwWindowHint(GLFW_SAMPLES, multisample);
		glfwWindowHint(GLFW_RESIZABLE, resizeable);
		glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, transparent);
		glfwWindowHint(GLFW_DECORATED, borders);
		if (fullscreen != 0) window = glfwCreateWindow(width, height, title, glfwGetPrimaryMonitor(), 0);
		else window = glfwCreateWindow(width, height, title, 0, 0);
		if (window == NULL) {
			throw new IllegalStateException("Window creation failed!");
		}
		
		//GLFW configuration
		glfwMakeContextCurrent(window);
		glfwSwapInterval(0);
		glfwSetKeyCallback(window, key);
		glfwSetMouseButtonCallback(window, button);
		glfwSetFramebufferSizeCallback(window, resize);
		glfwSetCursorPosCallback(window, cursor);
		glfwSetScrollCallback(window, scroll);
		glfwSetCharCallback(window, character);
		
		//OpenGL
		GL.createCapabilities();
		if (debug_mode) {
			GLUtil.setupDebugMessageCallback();
			GL43.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL43.GL_DEBUG_SEVERITY_NOTIFICATION, (IntBuffer) null, false);
		}
		clearColor = new vec4(0.2f, 0.2f, 0.2f, 1.0f);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		
		if (polygonmode == POLYGON_FILL) glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		else if (polygonmode == POLYGON_LINE) glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		else glPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
		
		// GL version
		Logger.debug("OpenGL version: " + glGetString(GL11.GL_VERSION));
		
//		defaultRenderer = new Renderer();
//		button_objects = new ArrayList<Button>();
//		shader = Shader.multiTextureShader();
		Shader.setActiveWindow(this);
	}
	
	public void unhide() {
		glfwShowWindow(window);
	}
	
	public void hide() {
		glfwHideWindow(window);
	}
	
//	public void addButton(Button btn) {
//		button_objects.add(btn);
//	}
//	
//	private void drawButtons() {
//		for (Button b : button_objects) {
//			b.manualRender(defaultRenderer, this);
//		}
//	}
	
	public boolean shouldClose() {
		return glfwWindowShouldClose(window);
	}
	
	public void clear() {
		glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//		defaultRenderer.clear();
	}
	
	public void clear(vec4 c) {
		glClearColor(c.x, c.y, c.z, c.w);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		resetClearColor();
//		defaultRenderer.clear();
	}
	
	public void clear(float r, float g, float b, float a) {
		glClearColor(r, g, b, a);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		resetClearColor();
//		defaultRenderer.clear();
	}
	
	public void clearColor(vec4 c) {
		glClearColor(c.x, c.y, c.z, c.w);
		clearColor = c;
	}
	
	public void clearColor(float r, float g, float b, float a) {
		glClearColor(r, g, b, a);
		clearColor = new vec4(r, g, b, a);
	}
	
	private void resetClearColor() {
		glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
	}
	
	public void input() {
		for (int i = 0; i < singleKeys.length; i++) singleKeys[i] = false;
		for (int i = 0; i < singleButtons.length; i++) singleButtons[i] = false;
		scroll_delta = 0.0f;
		
		glfwPollEvents();
	}
	
	public void viewport() {
		GL30.glViewport(0, 0, width, height);
	}
	
	public void viewport(int width, int height) {
		GL30.glViewport(0, 0, width, height);
	}
	
	public void update() {
//		drawButtons();
//		defaultRenderer.draw(0, 0);
		
		if (debug_mode) checkGLErrors(true);
		glfwSwapBuffers(window);
	}
	
	public float getAspect() {
		return (float)width / (float)height;
	}
	
	public vec2 getWindowPos() {
		int[] xpos = new int[1], ypos = new int[1];
		glfwGetWindowPos(window, xpos, ypos);
		
		return new vec2(xpos[0], ypos[0]);
	}
	
	public void setWindowPos(vec2 pos) {
		glfwSetWindowPos(window, (int)pos.x, (int)pos.y);
	}
	
	public void checkGLErrors(boolean closeIfError) {
		int err = glGetError();
		if (err != 0) {
			String errorText;
			switch (err)
			{
			case GL_NO_ERROR:
				errorText = "How did you even get this error lol?!?";
				break;
			case GL_INVALID_ENUM:
				errorText = "Invalid enum!";
				break;
			case GL_INVALID_VALUE:
				errorText = "Invalid value!";
				break;
			case GL_INVALID_OPERATION:
				errorText = "Invalid operation!";
				break;
			case GL_INVALID_FRAMEBUFFER_OPERATION:
				errorText = "Invalid framebuffer operation!";
				break;
			case GL_OUT_OF_MEMORY:
				errorText = "Out of memory!";
				break;
			default:
				errorText = "Unknown error!";
				break;
			}
			if (closeIfError)
				Logger.error("OpenGL: " + err + " -- " + errorText);
			else
				Logger.crit("OpenGL: " + err + " -- " + errorText);
		}
	}
	
	public void setSwapInterval(int interval) {
		glfwSwapInterval(interval);
	}
	
	public void setBackFaceCulling(boolean on) {
		if (on) GL11.glEnable(GL11.GL_CULL_FACE);
		else GL11.glDisable(GL11.GL_CULL_FACE);
	}

	public void setIcon(String path) {
		BufferedImage img = null;
		try {
			InputStream is = Loader.loadRes(path);
			img = ImageIO.read(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	ByteBuffer data = ByteBuffer.allocateDirect(img.getWidth() * img.getHeight() * 4);
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				
				Color c = new Color(img.getRGB(x, y), true);
				
				data.put(4 * (x + y * img.getWidth()) + 0, (byte)c.getRed());
				data.put(4 * (x + y * img.getWidth()) + 1, (byte)c.getGreen());
				data.put(4 * (x + y * img.getWidth()) + 2, (byte)c.getBlue());
				data.put(4 * (x + y * img.getWidth()) + 3, (byte)c.getAlpha());
			}
		}
		data.flip();
		
		GLFWImage.Buffer gb = GLFWImage.create(1);
		GLFWImage iconGI = GLFWImage.create().set(img.getWidth(), img.getHeight(), data);
		gb.put(0, iconGI);
		
		glfwSetWindowIcon(window, gb);
	}
	
	public String getRenderer() {
		return glGetString(GL_RENDERER);
	}
	
	public String getGL() {
		return glGetString(GL11.GL_VERSION);
	}
	
	public String getLWJGL() {
		return Version.getVersion();
	}

	public boolean key(int key) {
		return keys[key];
	}

	public boolean button(int button) {
		return buttons[button];
	}

	public boolean keyPress(int key) {
		return singleKeys[key];
	}

	public boolean buttonPress(int button) {
		return singleButtons[button];
	}
	
	public void setCursor(float x, float y) {
		glfwSetCursorPos(window, x, y);
	}
	
	public vec2 getCursorFast() {
		return new vec2(cursorX, cursorY);
	}
	
	public vec2 getCursor() {
		double[] xpos = new double[1], ypos = new double[1];
		glfwGetCursorPos(window, xpos, ypos);

		float x = Maths.map((float) xpos[0], 0.0f, getWidth(), -getAspect(), getAspect());
		float y = Maths.map((float) ypos[0], 0.0f, getHeight(), -1.0f, 1.0f);
		
		return new vec2(x, y);
	}
	
	public float getScrollTotal() {
		return scroll_total;
	}
	
	public float getScrollDelta() {
		return scroll_delta;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getWidth() {
		return width;
	}

	public void hideToTray() {
		glfwIconifyWindow(window);
	}
	
}
