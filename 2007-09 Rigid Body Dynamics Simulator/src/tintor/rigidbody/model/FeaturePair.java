package tintor.rigidbody.model;

import tintor.rigidbody.model.solid.Solid;
import tintor.util.Hash;

public class FeaturePair {
	public Body bodyA, bodyB;
	public Solid solidA, solidB;

//	public ContactPair(Body bodyA, Solid solidA, Body bodyB, Solid solidB) {
//		this.bodyA = bodyA;
//		this.solidA = solidA;
//		this.bodyB = bodyB;
//		this.solidB = solidB;
//	}

	@Override
	public int hashCode() {
		int ha = Hash.hash(bodyA.hashCode(), solidA.hashCode());
		int hb = Hash.hash(bodyB.hashCode(), solidB.hashCode());
		return Hash.hash(ha, hb);
	}

	@Override
	public boolean equals(Object o) {
		try {
			return equals((Contact) o);
		} catch (ClassCastException e) {
			return false;
		}
	}

	public boolean equals(Contact c) {
		return bodyA == c.bodyA && bodyB == c.bodyB && solidA == c.solidA && solidB == c.solidB;
				//|| (bodyA == c.bodyB && bodyB == c.bodyA && solidA == c.solidB && solidB == c.solidA);
	}
}