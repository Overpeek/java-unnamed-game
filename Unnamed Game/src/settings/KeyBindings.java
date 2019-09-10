package settings;

import java.util.ArrayList;

import utility.Callback;
import utility.Keys;

public class KeyBindings {
	
	public static class KeyBind {
		
		private Callback press;
		private Callback repeat;
		private Callback release;
		
		protected int index = 0;
		
		private int keyboard_primary = -3;
		private int keyboard_secondary = -3;
		
		private int mouse_primary = -3;
		private int mouse_secondary = -3;
		
		private int joystick_primary = -3;
		private int joystick_secondary = -3;
		
		private int gamepad_primary = -3;
		private int gamepad_secondary = -3;
		
		public KeyBind(Callback press, Callback repeat, Callback release) {
			this.press = press;
			this.repeat = repeat;
			this.release = release;
		}
		
		public void setPrimary(int keyboard, int mouse, int joystick, int gamepad) {
			keyboard_primary = keyboard;
			mouse_primary = mouse;
			joystick_primary = joystick;
			gamepad_primary = gamepad;
		}
		
		public void setSecondary(int keyboard, int mouse, int joystick, int gamepad) {
			keyboard_secondary = keyboard;
			mouse_secondary = mouse;
			joystick_secondary = joystick;
			gamepad_secondary = gamepad;
		}
		
		protected void activated(int keyboard, int mouse, int joystick, int gamepad, int action) {
			
			if (keyboard 	== keyboard_primary || keyboard == keyboard_secondary 	|| 
				mouse 		== mouse_primary 	|| mouse 	== mouse_secondary 		||
				joystick 	== joystick_primary || joystick == joystick_secondary 	||
				gamepad 	== gamepad_primary 	|| gamepad 	== gamepad_secondary	) 
			{
				switch (action) {
				case Keys.PRESS:
					if (press != null) press.callback();
					break;
				case Keys.REPEAT:
					if (repeat != null) repeat.callback();
					break;
				case Keys.RELEASE:
					if (release != null) release.callback();
					break;

				default:
					break;
				}
			}
			
		}
	}
	
	private static ArrayList<KeyBind> bindings = new ArrayList<KeyBind>();
	private static int binding_key = -1;
	private static boolean binding_mode = false;

	public static void startBind(boolean primary, int binding_index) {
		binding_mode = primary;
		binding_key = binding_index;
	}
	
	public static void startBind(boolean primary, KeyBind binding) {
		binding_mode = primary;
		binding_key = binding.index;
	}
	
	public static void cancelBind() {
		binding_mode = false;
		binding_key = -1;
	}
	
	private static void input(int keyboard, int mouse, int joystick, int gamepad, int action) {
		//Logger.debug(keyboard, mouse, joystick, gamepad, action);
		
		if (binding_key != -1) {
			if (binding_mode)
				bindings.get(binding_key).setPrimary(keyboard, mouse, joystick, gamepad);
			else
				bindings.get(binding_key).setSecondary(keyboard, mouse, joystick, gamepad);
			binding_key = 0;
		}
		
		for (int i = 0; i < bindings.size(); i++) {
			bindings.get(i).activated(keyboard, mouse, joystick, gamepad, action);
		}
	}
	
	public static void key(int key, int action) {	
		input(key, -2, -2, -2, action);
	}
	
	public static void button(int button, int action) {
		input(-2, button, -2, -2, action);
	}
	
	public static void joystick(int button, int action) {
		input(-2, -2, button, -2, action);
	}
	
	public static void gamepad(int button, int action) {
		input(-2, -2, -2, button, action);
	}

	public static int addNewBindable(KeyBind kb) {
		int i = bindings.size();
		kb.index = i;
		bindings.add(kb);
		return i;
	}

}
