package tintor.rigidbody.model.collisiondetection;

import tintor.rigidbody.model.Contact;

public interface ContactEvent {
	void contactEvent(Contact c);
}