package logic;

import org.joml.Vector2f;
import org.joml.Vector3f;

import graphics.Renderer;
import graphics.Renderer.VertexData;
import utility.Colors;
import utility.DataIO;

public class Inventory {

	private int itemIds[][] = new int[Settings.INVENTORY_WIDTH][Settings.INVENTORY_HEIGHT + 1];
	private int itemCounts[][] = new int[Settings.INVENTORY_WIDTH][Settings.INVENTORY_HEIGHT + 1];
	private int selectedSlot = 0;
	private boolean visible = false;

	public Inventory() {
		init();
	}

	public void init() {
		for (int x = 0; x < Settings.INVENTORY_WIDTH; x++)
		{
			for (int y = 0; y < Settings.INVENTORY_HEIGHT + 1; y++)
			{
				itemIds[x][y] = 0;
				itemCounts[x][y] = 0;
			}
		}
	}
	
	public boolean load() {
		if (Settings.DEBUG_DISABLE_SAVING) {
			long inventory_data_size;
			Game.getMap();
			char data[] = DataIO.readChar(Game.getMap().saveLocation(Settings.SAVE_PATH) + "\\inventory.data");
			if (data == null) return false;
			
			for (int x = 0; x < Settings.INVENTORY_WIDTH; x++)
			{
				for (int y = 0; y < Settings.INVENTORY_HEIGHT + 1; y++)
				{
					itemIds[x][y] = data[(x + Settings.INVENTORY_WIDTH * y) * 2 + 0];
					itemCounts[x][y] = data[(x + Settings.INVENTORY_WIDTH * y) * 2 + 1];
				}
			}
			return true;
		}
		return false;
	}
	
	public void save() {
		char data[] = new char[(Settings.INVENTORY_WIDTH * (Settings.INVENTORY_HEIGHT + 1)) * 2];
		for (int x = 0; x < Settings.INVENTORY_WIDTH; x++)
		{
			for (int y = 0; y < Settings.INVENTORY_HEIGHT + 1; y++)
			{
				data[(x + Settings.INVENTORY_WIDTH * y) * 2 + 0] = (char) itemIds[x][y];
				data[(x + Settings.INVENTORY_WIDTH * y) * 2 + 1] = (char) itemCounts[x][y];
			}
		}
		DataIO.writeChar(Game.getMap().saveLocation(Settings.SAVE_PATH) + "inventory.data", data);
	}
	
	public void render(Renderer renderer) {
		if (selectedSlot <= -1) selectedSlot = 4;
		if (selectedSlot >= 5) selectedSlot = 0;
	
		//int selectedId = itemIds[(int)selectedSlot][0];
	
		for (int x = 0; x < Settings.INVENTORY_WIDTH; x++)
		{
			if (x == (int)selectedSlot) {
				renderOneSlot(renderer,
					(x - Settings.INVENTORY_WIDTH / 2.0f) * Settings.INVENTORY_SCALE * Game.renderScale(),
					-1.0f,
					Settings.INVENTORY_SCALE * Game.renderScale() * 1.2f,
					itemIds[x][0],
					itemCounts[x][0]
				);
			} else if (x > (int)selectedSlot) {
				renderOneSlot(renderer,
					(x - Settings.INVENTORY_WIDTH / 2.0f + 0.2f) * Settings.INVENTORY_SCALE * Game.renderScale(),
					-1.0f,
					Settings.INVENTORY_SCALE * Game.renderScale(),
					itemIds[x][0],
					itemCounts[x][0]
				);
			}
			else {
				renderOneSlot(renderer, 
					(x - Settings.INVENTORY_WIDTH / 2.0f) * Settings.INVENTORY_SCALE * Game.renderScale(),
					-1.0f, 
					Settings.INVENTORY_SCALE * Game.renderScale(), 
					itemIds[x][0], 
					itemCounts[x][0]
				);
			}
		}
		
		if (visible) {
			for (int y = 0; y < Settings.INVENTORY_HEIGHT; y++)
			{
				for (int x = 0; x < Settings.INVENTORY_WIDTH; x++)
				{
					
					renderOneSlot(renderer, 
						(x - Settings.INVENTORY_WIDTH / 2.0f) * Settings.INVENTORY_SCALE * Game.renderScale(), 
						(y - Settings.INVENTORY_HEIGHT / 2.0f) * Settings.INVENTORY_SCALE * Game.renderScale(),
						Settings.INVENTORY_SCALE * Game.renderScale(),
						itemIds[x][y + 1], 
						itemCounts[x][y + 1]
					);
				}
			}
		}
	}

	private void renderOneSlot(Renderer renderer, float x, float y, float scale, int itemid, int itemcount) {
		//All inventory slots
		renderer.submitVertex(new VertexData(new Vector3f(x, y, 0.0f), new Vector2f(0.0f, 0.0f), 80, Colors.WHITE));
		renderer.submitVertex(new VertexData(new Vector3f(x, y + scale, 0.0f), new Vector2f(0.0f, 1.0f), 80, Colors.WHITE));
		renderer.submitVertex(new VertexData(new Vector3f(x + scale, y + scale, 0.0f), new Vector2f(1.0f, 1.0f), 80, Colors.WHITE));
		
		renderer.submitVertex(new VertexData(new Vector3f(x, y, 0.0f), new Vector2f(0.0f, 0.0f), 80, Colors.WHITE));
		renderer.submitVertex(new VertexData(new Vector3f(x + scale, y + scale, 0.0f), new Vector2f(1.0f, 1.0f), 80, Colors.WHITE));
		renderer.submitVertex(new VertexData(new Vector3f(x + scale, y, 0.0f), new Vector2f(1.0f, 0.0f), 80, Colors.WHITE));
	
		//All inventory items
		if (itemid != 0) {
			int item_texture = Database.items.get(itemid).texture;
			renderer.submitVertex(new VertexData(new Vector3f(x, y, 0.0f), new Vector2f(0.0f, 0.0f), item_texture, Colors.WHITE));
			renderer.submitVertex(new VertexData(new Vector3f(x, y + scale, 0.0f), new Vector2f(0.0f, 1.0f), item_texture, Colors.WHITE));
			renderer.submitVertex(new VertexData(new Vector3f(x + scale, y + scale, 0.0f), new Vector2f(1.0f, 1.0f), item_texture, Colors.WHITE));

			renderer.submitVertex(new VertexData(new Vector3f(x, y, 0.0f), new Vector2f(0.0f, 0.0f), item_texture, Colors.WHITE));
			renderer.submitVertex(new VertexData(new Vector3f(x + scale, y + scale, 0.0f), new Vector2f(1.0f, 1.0f), item_texture, Colors.WHITE));
			renderer.submitVertex(new VertexData(new Vector3f(x + scale, y, 0.0f), new Vector2f(1.0f, 0.0f), item_texture, Colors.WHITE));
		}
		//m_renderer->fontRenderer->renderText(
		//	glm::vec3(x, y, 0.0f),
		//	glm::vec2(1.0 / 3.0) * scale,
		//	std::to_string(itemcount).c_str(),
		//	oe::topLeft
		//);
	}

	private void update() {
	
	}
	
	private void clear() {
		for (int y = 0; y < Settings.INVENTORY_HEIGHT + 1; y++)
		{
			for (int x = 0; x < Settings.INVENTORY_WIDTH; x++)
			{
				itemIds[x][y] = 0;
				itemCounts[x][y] = 0;
			}
		}
	}
	
	void removeSelected(int n) {
		itemCounts[(int)selectedSlot][0] -= n;

		//oe::Logger::out(m_itemCounts[int(selectedSlot)][0]);

		if (itemCounts[(int)selectedSlot][0] <= 0) itemIds[(int)selectedSlot][0] = 0;
	}

	boolean addItem(int id, int n) {
		for (int y = 0; y < Settings.INVENTORY_HEIGHT + 1; y++)
		{
			for (int x = 0; x < Settings.INVENTORY_WIDTH; x++)
			{
				//oe::Logger::out("inv ", n);
				if (itemIds[x][y] == 0 || itemIds[x][y] == id) {
					itemIds[x][y] = id;
					itemCounts[x][y] += n;
					if (itemCounts[x][y] + n >= Database.items.get(id).stack_size) {
						itemCounts[x][y] = Database.items.get(id).stack_size;
						n -= Database.items.get(id).stack_size + 1 - itemCounts[x][y];
						if (n <= 0) return true;
					}
					else return true;
				}
			}
		}
		return false;
	}

	void dropSelected(int n) {
		
	}

	void dropAll() {
		for (int x = 0; x < Settings.INVENTORY_WIDTH; x++)
		{
			for (int y = 0; y < Settings.INVENTORY_HEIGHT + 1; y++)
			{
				if (itemIds[x][y] != 0) {
					dropItem(x, y, -1);
				}
			}
		}
	}

	void dropItem(int x, int y, int n) {
		if (n == -1) {
			for (int count = 0; count < itemCounts[x][y]; count++)
			{
				Game.getMap().itemDrop(Game.getPlayer().getX(), Game.getPlayer().getY(), itemIds[x][y]);
			}
			itemCounts[x][y] = 0;
			itemIds[x][y] = 0;
		}
		for (int count = 0; count < n; count++)
		{
			if (itemCounts[x][y] > 0) {
				Game.getMap().itemDrop(Game.getPlayer().getX(), Game.getPlayer().getY(), itemIds[x][y]);
				
				itemCounts[x][y]--;

				if (itemCounts[x][y] <= 0) {
					itemIds[x][y] = 0;
					break;
				}
			}
			itemIds[x][y] = 0;
			break;
		}
	}
	
}
