package tintor.rigidbody.model;

public interface Constraint {
	void prepare(float dt);

	void processCollision();

	void processContact(float e);

	void correct(float dt);

	void render();
}