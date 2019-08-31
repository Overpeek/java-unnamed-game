package utility.physics;

import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;

import utility.Keys;
import utility.vec2;

public class Physics {
	
	private World world;
	private vec2 mouse;
	private MouseJoint mouseJoint;
	private Body mouseBody;
	
	
	public void step(float ups, int iterations) {
		world.step(1.0f / ups, iterations, iterations);
	}
	
	public void mouseMove(float x, float y) {
		mouse = screenToWorld(new vec2(x, y));
		if (mouseJoint != null) mouseJoint.setTarget(new Vec2(mouse.x, mouse.y));
	}
	
	public void mouseButton(int button, int action) {
		if(button == Keys.MOUSE_BUTTON_LEFT && action == Keys.PRESS) {

			if (mouseJoint != null) return;
	        // Make a small box.

		    AABB aabb = new AABB();
	        aabb.lowerBound.set(mouse.x, mouse.y);
	        aabb.lowerBound.subLocal(new Vec2(0.001f, 0.001f));
	        aabb.upperBound.set(mouse.x, mouse.y);
	        aabb.upperBound.addLocal(new Vec2(0.001f, 0.001f));

	        // Query the world for overlapping shapes.
	        mouseBody = null;
	        world.queryAABB(new QueryCallback() {
				
				@Override
				public boolean reportFixture(Fixture fixture) {
					Body shapeBody = fixture.getBody();
					boolean inside = fixture.testPoint(new Vec2(mouse.x, mouse.y));
	                if (inside) {
	                	mouseBody = shapeBody;
	                    return false;
	                }
					return true;
				}
				
			}, aabb);

	        if (mouseBody != null) {
	            MouseJointDef md = new MouseJointDef();
	            md.bodyA = mouseBody;
	            md.bodyB = mouseBody;
	            md.target.set(new Vec2(mouse.x, mouse.y));
	            md.maxForce = 10000.0f * mouseBody.m_mass;
	            md.frequencyHz = 20.0f;
	            md.dampingRatio = 0.9f;
	            mouseJoint = (MouseJoint) world.createJoint(md);
	            mouseBody.setAwake(false);
	        }
	    }

	    /**
	     * Handle mouseUp events.
	     */
		if(button == Keys.MOUSE_BUTTON_LEFT && action == Keys.RELEASE) {
	        if (mouseJoint != null) {
	            world.destroyJoint(mouseJoint);
	            mouseJoint = null;
	        }
	    }
	}
	
	public World getWorld() {
		return world;
	}
	
	public Physics(vec2 gravity) {
		world = new World(new Vec2(gravity.x, gravity.y), true);
		world.setContinuousPhysics(true);
		world.setWarmStarting(true);
	}
	
	public vec2 worldToScreen(vec2 pos) { 
		return pos.mulLocal(1.0f / 10.0f);
	}
	
	public vec2 screenToWorld(vec2 pos) { 
		return pos.mulLocal(1.0f * 10.0f);
	}

}
