package engine;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An ActorGraph is an extension of the Actor concept, where
 * one actor may have various actors which fall under them. In
 * the world this translates to this group of actors moving as a group,
 * with most of the individual qualities preserved but some key
 * qualities inherited by the root actor.
 *
 * For example, if you are one actor that is part of a larger
 * actor tree, the following do not get changed:
 *         1) Your translation into the world will be preserved
 *         2) Your scale will be preserved
 *         3) Your rotation will be preserved
 *         4) Your depth will be preserved
 *
 * However, you will lose the following:
 *         1) Your speed (you inherit it from the parent)
 *         2) Your acceleration (also inherited from the parent)
 *
 * This ensures that you move as a group.
 *
 * @author Justin Hall
 */
public abstract class ActorGraph extends Actor {
    private static final Object _obj = new Object();
    private ConcurrentHashMap<ActorGraph, Object> _actorTree = new ConcurrentHashMap<>();
    private AtomicReference<ActorGraph> _attachedTo = new AtomicReference<>(null);

    /**
     * Attaches an actor to this actor (the given actor
     * becomes part of this actor's tree)
     * @param actor actor to add to this actor's graph
     */
    public void attachActor(ActorGraph actor)
    {
        _actorTree.putIfAbsent(actor, _obj);
        actor.setAttachedTo(this);
    }

    /**
     * @return true if this actor is already attached to another actor
     */
    public boolean isAttached()
    {
        return _attachedTo.get() != null;
    }

    /**
     * Checks to see if the given actor has been attached
     * to this actor
     */
    public boolean contains(ActorGraph actor)
    {
        return _actorTree.containsKey(actor);
    }

    /**
     * Removes the given actor from this actor's tree
     */
    public void removeActor(ActorGraph actor)
    {
        _actorTree.remove(actor);
        actor._attachedTo.set(null);
    }

    // Package private
    protected ConcurrentHashMap<ActorGraph, Object> getActors()
    {
        return _actorTree;
    }

    private void setAttachedTo(ActorGraph actor)
    {
        if (contains(actor))
        {
            throw new RuntimeException("ERROR: Attempting to attach actor A to actor B, then trying to" +
                    "attach actor B to actor A - cyclic graph");
        }
        if (isAttached()) actor.removeActor(this); // Remove ourself from old actor's tree
        _attachedTo.set(actor);
    }

    private ActorGraph getAttachedTo()
    {
        return _attachedTo.get();
    }
}
