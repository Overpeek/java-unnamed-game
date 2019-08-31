package utility.physics;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;

import utility.vec2;
import utility.vec4;

public abstract class PhysicsObject {

	protected Physics physics;
	protected vec2 size;
	protected Body body;
	protected vec4 color;
	
	
	protected PhysicsObject(Physics physics, vec2 position, vec2 size, float angle, boolean is_static) {
	    this.physics = physics;
		this.size = size;
		this.color = new vec4().random(0.0f, 1.0f);
		this.color.w = 1.0f;
		
		
	    BodyDef bodyDef = new BodyDef();
	    bodyDef.position.set(position.x, position.y);
	    bodyDef.angle = angle;
	    bodyDef.allowSleep = true;
	    bodyDef.type = is_static ? BodyType.STATIC : BodyType.DYNAMIC;
	    this.body = physics.getWorld().createBody(bodyDef);
	}
	
	public Body getBody() {
		return body;
	}
	
	public void stop() {
		body.setLinearVelocity(new Vec2(0.0f, 0.0f));
	}
	
	public vec2 getLinearVelocity() {
		return new vec2(body.getLinearVelocity().x, body.getLinearVelocity().y);
	}
	
	public void setLinearVelocity(vec2 newVel) {
		body.setLinearVelocity(new Vec2(newVel.x, newVel.y));
	}
	
	/*
	 * point is nullable
	 * **/
	public void addForce(vec2 force, vec2 point) {
		if (point == null) 
			body.applyForce(new Vec2(force.x, force.y), body.getPosition());
		else
			body.applyForce(new Vec2(force.x, force.y), new Vec2(point.x, point.y));
	}
	
	public void setPosition(vec2 newPosition) {
		body.setTransform(new Vec2(newPosition.x, newPosition.y), getAngle());
	}
	
	public vec2 getPosition() {
		return new vec2(body.getPosition().x, body.getPosition().y);
	}
	
	public float getAngle() {
		return body.getAngle();
	}
	
	public void setAngle(float angle) {
		body.setTransform(new Vec2(getPosition().x, getPosition().y), getAngle());
	}
	
	public vec2 getSize() {
		return size;
	}

}
