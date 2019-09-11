package logic;

import audio.Audio;
import graphics.Shader.ShaderException;
import guiwindows.GUIWindowManager;
import settings.CompiledSettings;
import utility.Logger;
import utility.SaveManager;

public class AsyncResLoader extends AsyncLoader {

	@Override
	public void run() {
		Database.initialize();
	}
	
	
	//Returns float between 0.0f-1.0f
	@Override
	public float queryLoadState() {
		float vanilla = Database.loadState();
		float mods = Database.modLoadState();
		
		if (mods < 0.0f) return vanilla;
		return (vanilla + mods) / 2.0f;
	}


	@Override
	public void finish() {
		TextureLoader.finish();
	}


	@Override
	public void init() {
		// Rest of the initializations
		Audio.init();
		SaveManager.init(CompiledSettings.GAME_NAME);
		ParticleManager.init();
		GUIWindowManager.initialize();
		try {
			ShaderManager.loadShaders();
		} catch (ShaderException e) {
			Logger.error("Couldn't load shaders");
		}
		
		// Textures
		TextureLoader.init(16);
	}

	
}
