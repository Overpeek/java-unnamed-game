import java.awt.Color;
import java.awt.image.BufferedImage;

import utility.Debug;
import utility.SaveManager;
import world.Map;
import world.Map.MapTile;

public class Main {
	
	public static void main(String[] args) {
		SaveManager.init("Unnamed Game");
		Map map = new Map(false);
		map.load(args[0]);
		
		BufferedImage image = new BufferedImage(map.getTiles().length, map.getTiles().length, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < map.getTiles().length; x++) {
			for (int y = 0; y < map.getTiles().length; y++) {
				
				MapTile mapTile = map.getTile(x, y);
				
				Color color = new Color(0.0f, 0.0f, 0.0f);
				if (mapTile.object == 0) {
					switch (mapTile.tile) {
					case 0:
						color = new Color(0.42f, 0.27f, 0.14f);
						break;
					case 1:
						color = new Color(0.0f, 0.1f, 1.0f);
						break;
					case 2:
						color = new Color(0.76f, 0.7f, 0.5f);
						break;
					case 3:
						color = new Color(0.2f, 1.0f, 0.2f);
						break;
					case 4:
						color = new Color(0.7f, 0.7f, 0.7f);
						break;

					default:
						break;
					}
				} else {
					switch (mapTile.object) {
					case 1:
						color = new Color(0.1f, 1.0f, 0.1f);
						break;
					case 2:
						color = new Color(0.3f, 1.0f, 0.3f);
						break;
					case 3:
						color = new Color(0.6f, 1.0f, 0.6f);
						break;
					case 4:
						color = new Color(0.58f, 0.42f, 0.2f);
						break;
					case 5:
						color = new Color(0.5f, 0.5f, 0.5f);
						break;
					case 6:
						color = new Color(1.0f, 0.0f, 0.0f);
						break;
					case 7:
						color = new Color(0.1f, 0.1f, 0.1f);
						break;

					default:
						break;
					}
				}
				
				
				image.setRGB(x, y, color.getRGB());
			}
		}
		
		Debug.debugImagePopup(image);
	}

}
