package utility.physics;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.dynamics.Fixture;

import graphics.Renderer;
import utility.vec2;

public class PhysicsCircle extends PhysicsObject {
	
	Fixture fixture;

	public PhysicsCircle(vec2 position, float radius, boolean is_static, Physics physics) {
		super(physics, position, new vec2(radius), 0.0f, is_static);
		
		CircleShape shape = new CircleShape();
		shape.m_radius = radius;
		//shape.setRadius(radius);
		
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
		
		renderer.submit(new graphics.primitives.Circle(render_pos, render_size, 0, color));
	}

}
