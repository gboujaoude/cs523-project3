package engine;

import java.util.HashSet;

/**
 * This is used by the PhysicsSimulation component to notify an object
 * that it has collided/overlapped with another object.
 */
public interface CollisionEventCallback {
    /**
     * @param actor the actor in question
     * @param actors list of actors that collided with actor
     */
    void onActorOverlapped(Actor actor, HashSet<Actor> actors);
}
