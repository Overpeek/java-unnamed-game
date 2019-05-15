package world;

import java.util.ArrayList;

import creatures.Creature;
import creatures.Item;
import creatures.Zombie;
import utility.Logger;

public class Map {

	ArrayList<Creature> creatures;

	public String saveLocation(String path) {
		String m_name = "test";
		return path + m_name + "\\";
	}

	public Creature addCreature(float x, float y, int id, boolean item) {
		switch (id) {
		case 1: // Zombie
			creatures.add(new Zombie(x, y));
			break;
		case 2: // Item
			creatures.add(new Item(x, y, id));
			break;
		default:
			Logger.out("Invalid creature id: " + id, Logger.type.WARNING);
			break;
		}

		return creatures.get(creatures.size() - 1);
	}

	public void removeCreature(int i) {
		if (i >= creatures.size())
			return;
		if (creatures.get(i) == null)
			return;
		creatures.remove(i);
	}

	public void removeCreature(Creature creature) {
		for (int i = 0; i < creatures.size(); i++) {
			if (creatures.get(i) == creature) {
				removeCreature(i);
				return;
			}
		}
	}

	public Creature itemDrop(float x, float y, int id) {
		Creature newItem = addCreature(x, y, id, true);
		newItem.setVel_x(utility.Math.random(-0.2f, 0.2f));
		newItem.setVel_y(utility.Math.random(-0.2f, 0.2f));
		return newItem;
	}
}
