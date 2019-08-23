package utility.physics;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.Fixture;

import graphics.Renderer;
import graphics.primitives.Quad;
import utility.vec2;

public class PhysicsBox extends PhysicsObject {
	
	Fixture fixture;

	public PhysicsBox(vec2 position, vec2 size, boolean is_static, float angle, Physics physics) {
		super(physics, position, size, angle, is_static);
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(size.x / 2.0f, size.y / 2.0f);
		
		fixture = body.createFixture(shape, 1.0f);
		fixture.setRestitution(0.1f);
		fixture.setFriction(0.0f);
	}
	
	public void setRestitution(float value) {
		fixture.setRestitution(value);
	}
	
	public void setFriction(float value) {
		fixture.setFriction(value);
	}
	
	public void setDensity(float value) {
		fixture.setDensity(value);
	}
	
	public void submit(Renderer renderer) {
		vec2 render_pos = physics.worldToScreen(getPosition());
		vec2 render_size = physics.worldToScreen(getSize());
		
		renderer.submit(new Quad(render_pos, render_size, getAngle(), 0, color));
	}

}
