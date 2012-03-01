package tintor.rigidbody.main.worlds;

import java.awt.event.KeyEvent;

import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;
import tintor.opengl.GLA;
import tintor.rigidbody.model.BallJoint;
import tintor.rigidbody.model.Body;
import tintor.rigidbody.model.Shape;
import tintor.rigidbody.model.World;
import tintor.rigidbody.model.effector.Drag;
import tintor.rigidbody.model.effector.MuscleServo;

public class Arm extends World {
	MuscleServo mservo, mservo2, mservo3; 
	
	public Arm() {
		Body torso = new Body(new Vector3(0, -6.5, 0), Quaternion.Identity, Shape.box(5, 3, 5), 5);
		add(torso);
		
		Body lowArm = new Body(new Vector3(5.5, -4, 0), Quaternion.Identity, Shape.box(10, 1, 1), 1);
		add(lowArm);
		joints.add(new BallJoint(torso, lowArm, new Vector3(0, -4, -1)));
		joints.add(new BallJoint(torso, lowArm, new Vector3(0, -4, 1)));

		Body highArm = new Body(new Vector3(16.5, -4, 0), Quaternion.Identity, Shape.box(10, 1, 1), 1);
		add(highArm);
		joints.add(new BallJoint(lowArm, highArm, new Vector3(11, -4, -1)));
		joints.add(new BallJoint(lowArm, highArm, new Vector3(11, -4, 1)));

		Body hand = new Body(new Vector3(24, -4, 0), Quaternion.Identity, Shape.box(3, 0.25f, 3), 1);
		hand.color = GLA.red;
		add(hand);
		joints.add(new BallJoint(highArm, hand, new Vector3(22, -4, -1)));
		joints.add(new BallJoint(highArm, hand, new Vector3(22, -4, 1)));

		mservo = new MuscleServo(torso, lowArm, new Vector3(2.5, -6.5, 0), new Vector3(5.5, -5, 0));
		mservo.active = true;
		mservo.maxForce = 200;
		effectors.add(mservo);

		mservo2 = new MuscleServo(lowArm, highArm, new Vector3(5.5, -5, 0), new Vector3(16.5, -5, 0));
		mservo2.active = true;
		mservo2.maxForce = 200;
		effectors.add(mservo2);

		mservo3 = new MuscleServo(highArm, hand, new Vector3(16.5, -5, 0), new Vector3(24, -5, 0));
		mservo3.active = true;
		mservo3.maxForce = 200;
		effectors.add(mservo3);

		Body box = new Body(new Vector3(16, -7, 0), Quaternion.Identity, Shape.box(2, 2, 2), 0.5f);
		box.color = GLA.white;
		add(box);
		
		effectors.add(new Drag());
		surface(-8, 1);
	}

	@Override
	public void keyDown(final int key) {
		switch (key) {
		case KeyEvent.VK_Z:
			mservo3.goalPos += 0.25;
			break;
		case KeyEvent.VK_X:
			mservo3.goalPos -= 0.25;
			break;
		case KeyEvent.VK_LEFT:
			mservo2.goalPos -= 0.25;
			break;
		case KeyEvent.VK_RIGHT:
			mservo2.goalPos += 0.25;
			break;
		case KeyEvent.VK_UP:
			mservo.goalPos += 0.25;
			break;
		case KeyEvent.VK_DOWN:
			mservo.goalPos -= 0.25;
			break;
		case KeyEvent.VK_C:
			mservo.active = !mservo.active;
			mservo2.active = !mservo2.active;
			mservo3.active = !mservo3.active;
			break;
		}
	}
}