package creatures;

import java.util.ArrayList;

import graphics.Renderer;
import logic.Main;
import logic.Pathfinder;
import logic.CompiledSettings;
import utility.Logger;
import utility.Maths;
import utility.vec2;

public class Zombie extends Creature {
	
	private Pathfinder path;
	private float untilnexttarget;
	private float wait;
	private float check_player_cooldown;
	private float hit_cooldown;
	private float curtarget_x;
	private float curtarget_y;
	private boolean chasing;
	private int result;
	private int retrace_checkpoint;
	private ArrayList<vec2> retrace;

	@Override
	public Creature construct(float x, float y, String id) {
		path = null;
		untilnexttarget = 500.0f;
		wait = 0.0f;
		hit_cooldown = 0.0f;
		chasing = false;
		curtarget_x = 0.0f;
		curtarget_y = 0.0f;
		check_player_cooldown = 0.0f;
		result = 0;
		retrace_checkpoint = 0;
		return commonConstruct(x, y, "zombie");
	}

	@Override
	public void die() {
		commonDie();
	}

	@Override
	public void draw(Renderer renderer, float preupdate_scale) {
		commonDraw(renderer, preupdate_scale);
	}

	@Override
	public void update(float ups) {
		commonUpdate(ups);
	}

	@Override
	public void collide(float ups) {
		commonCollide(ups);
	}

	@Override
	public void ai(float ups) {

		untilnexttarget -= 1.0 / ups;
		wait -= 1.0 / ups;
		check_player_cooldown -= 1.0 / ups;
		
		Creature player = Main.game.getMap().getPlayer();
		vec2 playerPos = player.getPos();
		if (check_player_cooldown <= 0) {
			check_player_cooldown = Maths.random(0.0f, 1.0f);
		
			if (canSee(playerPos.x, playerPos.y)) {
				chasing = true;
				path = null;
				return;
			}
			else chasing = false;
		}
		if (chasing) {
			vec2 dstToPlayer = new vec2(playerPos.x - getPos().x, playerPos.y - getPos().y);
			vec2 dirToPlayer = dstToPlayer.normalizeLocal();
			if (dstToPlayer.length() > 1.0f) {
				getAcc().x += dirToPlayer.x * getData().walkSpeed;
				getAcc().y += dirToPlayer.y * getData().walkSpeed;
				setHeading(getAcc().x, getAcc().y);
			}
			else {
				hit_cooldown += 1.0 / ups;
				if (hit_cooldown > 0.5) {
					hit_cooldown = 0;
					hit();
				}
			}
			setHeading(dirToPlayer.x, dirToPlayer.y);
		}
		if (untilnexttarget < 0) {
			wait = 5.0f;
			untilnexttarget = 20.0f;
		
		
			curtarget_x = Maths.random(-16.0f, 16.0f);
			curtarget_y = Maths.random(-16.0f, 16.0f);
		
		
			int startnode_x = (int) Math.floor(Maths.clamp(getPos().x, 0.0f, (float)CompiledSettings.MAP_SIZE_TILES));
			int startnode_y = (int) Math.floor(Maths.clamp(getPos().y, 0.0f, (float)CompiledSettings.MAP_SIZE_TILES));
			int endnode_x = (int) Math.floor(Maths.clamp(getPos().x + curtarget_x, 0.0f, (float)CompiledSettings.MAP_SIZE_TILES));
			int endnode_y = (int) Math.floor(Maths.clamp(getPos().y + curtarget_y, 0.0f, (float)CompiledSettings.MAP_SIZE_TILES));
		
			path = new Pathfinder(startnode_x, startnode_y, endnode_x, endnode_y, 10);
			if (path.failed) {
				path = null;
			}
			result = 0;
		}
		if (wait < 0) {
			untilnexttarget = Maths.random(0.5f, 2.0f);
			wait = 40;
		
		
			result = 0;
			chasing = false;
			path = null;
		}
		
		if (path != null && !chasing) followTarget(ups);

		
		setHeading(getAcc().x, getAcc().y);
		
	}

	private void followTarget(float ups) {
		if (result == 0) {
			result = path.runNSteps((int) (200.0f / ups));
			wait = 20;
			untilnexttarget = 100;
			if (result != 0) {
				retrace = path.retrace();
				retrace_checkpoint = 0;
			}
		}
		
		if (result != 0) {
		
			if (retrace.size() <= 0) {
				path = null;
				return;
			}
		
			int arr = retrace.size() - 1 - retrace_checkpoint;
			float mov_x = (retrace.get(arr).x - getPos().x + 0.5f);
			float mov_y = (retrace.get(arr).y - getPos().y + 0.5f);
		
			if (mov_x < 0.2f && mov_x > -0.2f && mov_y < 0.2f && mov_y > -0.2f) {
				retrace_checkpoint++;
				if (retrace_checkpoint >= retrace.size()) {
					untilnexttarget = Maths.random(0.5f, 2.0f);
					wait = 4000;
					getVel().x = 0.0f;
					getVel().y = 0.0f;
					path = null;
					return;
				}
			}
		
			if (Math.abs(mov_x) > 0.2f) getAcc().x += Math.signum(mov_x) * getData().walkSpeed;
			if (Math.abs(mov_y) > 0.2f) getAcc().y += Math.signum(mov_y) * getData().walkSpeed;
			setHeading(getAcc().x, getAcc().y);
		}
	}

}
