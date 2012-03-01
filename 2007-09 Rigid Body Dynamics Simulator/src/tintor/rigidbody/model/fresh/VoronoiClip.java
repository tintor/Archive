package tintor.rigidbody.model.fresh;

import tintor.geometry.Matrix3;
import tintor.geometry.Vector3;
import tintor.geometry.extended.Polygon3;

class WeakPairMap<A, B, V> {
	public void put(final A keyA, final B keyB, final V value) {
		throw new RuntimeException();
	}

	public V get(final A keyA, final B keyB) {
		throw new RuntimeException();
	}
}

class Body {
	Matrix3 orientation;
	Vector3 position;
	Polygon3[] surface;
}

// for each edge E from vertex V 

public class VoronoiClip {
	public static class Result {
		Object featureA, featureB;
		boolean penetration;

		State getState() {
			if (featureA instanceof Vector3 && featureB instanceof Vector3) return State.VertexVertex;
			if (featureA instanceof Polygon3 && featureB instanceof Vector3) return State.VertexFace;
			throw new RuntimeException();
		}
	}

	// ALG 2 (post clipping derivative checks)
	// 1 if N_low != empty and D'ex(lambda_low) > 0 then
	// 2    X <- N_low
	// 3 else if N_high != empty and D'ex(lambda_high) < 0 then
	// 4    X <- N_high

	// ALG 3 handleLocalMin (detect penetration or dislodge from a local minimum)
	//  1 d_max <- -Inf
	//  2 for all faces F' on F's polyhedron do
	//  3   P' <- plane(F')
	//  4   d = Dp'(V)
	//  5   if d > d_max then
	//  6           d_max = d
	//  7           F0 <- F'
	//  8 if d_max <= 0 then
	//              result.penetration = true
	//  9   return true
	// 10 F <- F0
	// 11 return false

	private static enum State {
		VertexVertex {
			@Override
			boolean run(final Result result) {
				// 1 for each Voronoi plane P in Vp(V1, E)
				// 2    if Dp(V2) < 0 then
				// 3            V1 <- E
				// 4            return false
				// 1 for each Voronoi plane P in Vp(V2, E)
				// 2    if Dp(V1) < 0 then
				// 3            V2 <- E
				// 4            return false
				// 6 return true
				throw new RuntimeException();
			}
		},
		VertexEdge {
			@Override
			boolean run(final Result result) {
				//  1 for each Voronoi plane P in Vp(E, N)
				//  2   if Dp(V) > 0 then
				//  3           E <- N
				//  4           return false
				//  5 clip E against Vr(V) [alg 1]
				//  6 if N_low = N_high != 0
				//  7   V <- N_low
				//  8 else
				//  9   check derivatives, possibly update V [alg 2]
				// 10 return !V.updated?
				throw new RuntimeException();
			}
		},
		VertexFace {
			@Override
			boolean run(final Result result) {
				//  1 search for Voronoi plane P = Vp(F, E) that minimizes Dp(V)
				//  2 if Dp(V) < 0 then
				//  3   F <- E
				//  4   return false
				//  5 P <- plane(F)
				//  6 search for edge E, incident to V and V'
				//                      such that sign(Dp(V)) * sign(Dp(V) - Dp(V')) > 0
				//  7 if E exists then
				//  8   V <- E
				//  9   return false
				// 10 if Dp(V) > 0 then return true
				// 11 return handleLocalMin [alg 3]
				throw new RuntimeException();
			}
		},
		EdgeEdge {
			@Override
			boolean run(final Result result) {
				//  1 clip E2 against vertex-edge planes of Vr(E1) [alg 1]
				//  2 if E2 simple excluded by Vp(E1, V) then
				//  3   E1 <- V
				//  4 else
				//  5   check derivatives, possibly update E1 [alg 2]
				//  6 if E1 was updated then
				//  7 return false
				//  8 clip E2 against face-edge planes of Vr(E1) [alg 1]
				//  9 if E2 simply excluded by Vp(E1, F) then
				// 10   E1 <- F
				// 11 else
				// 12   check derivatives, possibly update E1 [alg 2]
				// 13 if E1 was updated then
				// 14   return false
				// 15 repeat steps 1-14, swapping roles of E1 & E2
				// 16 return true
				throw new RuntimeException();
			}
		},
		EdgeFace {
			@Override
			boolean run(final Result result) {
				//  1 clip E against Vr(F) [alg 1]
				//  2 if E excluded from Vr(F) then
				//  3   F <- closest edge or vertex on F to E
				//  4   return false
				//  5 d_low <- Def(lambda_low)
				//  6 d_high <- Def(lambda_high)
				//  7 if d_low * d_high <= 0 then
				//              result.penetration = true
				//  8   return true
				//  9 if D'ef(lambda_low) >= 0 then
				// 10   if N_low != empty then
				// 11           F <- N_low
				// 12   else
				// 13           E <- tail(E)
				// 14 else
				// 15   if N_high != empty then
				// 16           F <- N_high
				// 17   else
				// 18           E <- head(E)
				// 19 return false
				throw new RuntimeException();
			}
		};

		// returns if current state is final
		abstract boolean run(Result result);
	}

	public Result query(final Body bodyA, final Body bodyB) {
		Result result = map.get(bodyA, bodyB);
		if (result == null) {
			result = new Result();
			// TODO select random initial features
			map.put(bodyA, bodyB, result);
		}

		while (!result.getState().run(result)) {}

		return result;
	}

	private final WeakPairMap<Body, Body, Result> map = new WeakPairMap<Body, Body, Result>();
}