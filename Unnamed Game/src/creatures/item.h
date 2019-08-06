#pragma once

#include "creature.h"

class Item : public Creature {
private:
	bool nothing;
	
public:
	Item(float x, float y, int id);

	void ai(float ups);
	void update(int index, float ups);
	void collide(float ups);
};