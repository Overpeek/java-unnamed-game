package logic;

import java.io.IOException;
import java.nio.ByteBuffer;

import utility.SaveManager;

public class RuntimeSettings {
	
	public static void loadSettings() {
		try {
			ByteBuffer byteBuffer = SaveManager.loadData("settings", "settings.data");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
