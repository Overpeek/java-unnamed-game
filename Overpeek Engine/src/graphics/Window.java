package graphics;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.joml.Vector4f;
import org.lwjgl.Version;
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
import utility.Logger.type;


public class Window {

	public static final int WINDOW_MULTISAMPLE_X2	= 0x0000020;
	public static final int WINDOW_MULTISAMPLE_X4	= 0x0000040;
	public static final int WINDOW_MULTISAMPLE_X8	= 0x0000080;
	public static final int WINDOW_BORDERLESS		= 0x0000001;
	public static final int WINDOW_RESIZEABLE		= 0x0000100;
	public static final int WINDOW_TRANSPARENT		= 0x0001000;
	public static final int WINDOW_FULLSCREEN		= 0x0010000;
	public static final int WINDOW_DEBUGMODE		= 0x0100000;

	public static final int POLYGON_LINE			= 0x1000000;
	public static final int POLYGON_FILL			= 0x2000000;
	public static final int POLYGON_POINT			= 0x3000000;
	

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
	
	private boolean debug_mode = true;

	
	private boolean keys[] = new boolean[512];
	private boolean buttons[] = new boolean[128];
	private boolean singleKeys[] = new boolean[512];
	private boolean singleButtons[] = new boolean[128];
	
	private float cursorX, cursorY;
	private float scroll_total;
	private float scroll_delta;
	
	
	public void setCurrentApp(Application app) {
		active_Application = app;
	}
	
	GLFWKeyCallback key = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods)
	    {
			if (active_Application != null) active_Application.keyPress(key, action);
			
			if (key >= keys.length || key < 0) {
				Logger.out("Unknown key: " + key, type.WARNING);
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
				Logger.out("Unknown button: " + button, type.WARNING);
				return;
			}
			if (action == GLFW_PRESS) { buttons[button] = true; singleButtons[button] = true; }
			if (action == GLFW_RELEASE) { buttons[button] = false; }
	    }
	};
	
	GLFWCursorPosCallback cursor = new GLFWCursorPosCallback() {
		@Override
		public void invoke(long window, double xpos, double ypos)
	    {
			if (active_Application != null) active_Application.mousePos((float) xpos, (float) ypos);
			
			cursorX = Maths.map((float) xpos, 0.0f, getWidth(), -getAspect(), getAspect());
			cursorY = Maths.map((float) ypos, 0.0f, getHeight(), 1.0f, -1.0f);
	    }
	};
	
	GLFWFramebufferSizeCallback resize = new GLFWFramebufferSizeCallback() {
		
		@Override
		public void invoke(long window, int _width, int _height)
	    {
			if (active_Application != null) active_Application.resize(_width, _height);
			
			width = _width;
			height = _height;
			System.out.println("Width: " + width + ", Height: " + height);
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
	
	public Window(int width, int height, String title, int mods) {
		this.title = title;
		this.width = width;
		this.height = height;
		
		
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
		
		if ((mods & POLYGON_FILL) != 0) {
			polygonmode = POLYGON_FILL;
			Logger.debug("Polygonmode is now " + polygonmode);
		} else if ((mods & POLYGON_LINE) != 0) {
			polygonmode = POLYGON_LINE;
			Logger.debug("Polygonmode is now " + polygonmode);
		} else if ((mods & POLYGON_POINT) != 0) {
			polygonmode = POLYGON_POINT;
			Logger.debug("Polygonmode is now " + polygonmode);
		} else polygonmode = POLYGON_FILL;
		
		init();
	}
	
	public void init() {
		//Init
		if (!glfwInit()) {
			throw new IllegalStateException("GLFW Init failed!");
		}
		
		//Window
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
		
		//OpenGL
		GL.createCapabilities();
		if (debug_mode) {
			GLUtil.setupDebugMessageCallback();
			GL43.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL43.GL_DEBUG_SEVERITY_NOTIFICATION, (IntBuffer) null, false);
		}
		glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		
		Logger.debug("Polygonmode: " + polygonmode);
		if (polygonmode == POLYGON_FILL) glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		else if (polygonmode == POLYGON_LINE) glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		else glPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
	}
	
	public boolean shouldClose() {
		return glfwWindowShouldClose(window);
	}
	
	public void clear() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
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
	
	public void update() {
		if (debug_mode) checkGLErrors();
		glfwSwapBuffers(window);
	}
	
	public float getAspect() {
		return (float)width / (float)height;
	}
	
	public void checkGLErrors() {
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
			Logger.out("OpenGL: " + err + " -- " + errorText, Logger.type.ERROR);
		}
	}
	
	public void clearColor(Vector4f c) {
		glClearColor(c.x, c.y, c.z, c.w);
	}
	
	public void setClearColor(float r, float g, float b, float a) {
		glClearColor(r, g, b, a);
	}
	
	public void setSwapInterval(int interval) {
		glfwSwapInterval(interval);
	}
	
	public void setBackFaceCulling(boolean on) {
		if (on) GL11.glEnable(GL11.GL_CULL_FACE);
		else GL11.glDisable(GL11.GL_CULL_FACE);
	}
	
	public void setLineWidth(float w) {
		GL11.glLineWidth(w);
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
	
	public float getCursorX() {
		return cursorX;
	}
	
	public float getCursorY() {
		return cursorY;
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
	
}
