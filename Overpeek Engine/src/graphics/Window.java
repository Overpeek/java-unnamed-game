package graphics;

import static org.lwjgl.glfw.GLFW.GLFW_DECORATED;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;

import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.stb.STBImage;

import utility.Logger;
import utility.Logger.type;


public class Window {

	public static final int WINDOW_MULTISAMPLE_X2	= 0x000020;
	public static final int WINDOW_MULTISAMPLE_X4	= 0x000040;
	public static final int WINDOW_MULTISAMPLE_X8	= 0x000080;
	public static final int WINDOW_BORDERLESS		= 0x000001;
	public static final int WINDOW_RESIZEABLE		= 0x000100;
	public static final int WINDOW_TRANSPARENT		= 0x001000;
	public static final int WINDOW_FULLSCREEN		= 0x010000;

	public static final int POLYGON_LINE			= 0;
	public static final int POLYGON_FILL			= 1;
	public static final int POLYGON_POINT			= 2;
	

	private long window;
	private int width;
	private int height;
	private String title;
	private int resizeable;
	private int transparent;
	private int borders;
	private int fullscreen; 
	private int multisample;
	
	private boolean debug_mode = true;

	
	private boolean keys[] = new boolean[512];
	private boolean buttons[] = new boolean[128];
	private boolean singleKeys[] = new boolean[512];
	private boolean singleButtons[] = new boolean[128];
	
	private float mouseX, mouseY;
	private float scroll_total;
	private float scroll_delta;
	
	
	GLFWKeyCallback key = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods)
	    {
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
			mouseX = (float) xpos;
			mouseY = (float) ypos;
	    }
	};
	
	GLFWFramebufferSizeCallback resize = new GLFWFramebufferSizeCallback() {
		
		@Override
		public void invoke(long window, int _width, int _height)
	    {
			width = _width;
			height = _height;
			System.out.println("Width: " + width + ", Height: " + height);
	    }
	};
	
	GLFWScrollCallback scroll = new GLFWScrollCallback() {

		@Override
		public void invoke(long window, double xd, double yd) {
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
		if (debug_mode) GLUtil.setupDebugMessageCallback();
		glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
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
	
	public void update() {
		if (debug_mode) checkGLErrors();
		glfwSwapBuffers(window);
	}
	
	public float aspect() {
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
			throw new IllegalStateException("OpenGL: " + err + " -- " + errorText);
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
		int x[] = new int[1], y[] = new int[1], nrChannels[] = new int[1];
		ByteBuffer data = STBImage.stbi_load(path, x, y, nrChannels, 0);
		
		GLFWImage.Buffer gb = GLFWImage.create(1);
		GLFWImage iconGI = GLFWImage.create().set(x[0], y[0], data);
		gb.put(0, iconGI);
		
		glfwSetWindowIcon(window, gb);
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
	
	public float getMouseX() {
		return mouseX;
	}
	
	public float getMouseY() {
		return mouseY;
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
