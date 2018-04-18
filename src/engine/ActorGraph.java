package engine;

import java.util.HashSet;

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
    private HashSet<ActorGraph> _actorTree = new HashSet<>();
    private ActorGraph _attachedTo;

    /**
     * Attaches an actor to this actor (the given actor
     * becomes part of this actor's tree)
     * @param actor actor to add to this actor's graph
     */
    public void attachActor(ActorGraph actor)
    {
        _actorTree.add(actor);
        actor.setAttachedTo(this);
    }

    /**
     * @return true if this actor is already attached to another actor
     */
    public boolean isAttached()
    {
        return _attachedTo != null;
    }

    /**
     * Checks to see if the given actor has been attached
     * to this actor
     */
    public boolean contains(ActorGraph actor)
    {
        return _actorTree.contains(actor);
    }

    /**
     * Removes the given actor from this actor's tree
     */
    public void removeActor(ActorGraph actor)
    {
        _actorTree.remove(actor);
        actor._attachedTo = null;
    }

    // Package private
    HashSet<ActorGraph> getActors()
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
        _attachedTo = actor;
    }

    private ActorGraph getAttachedTo()
    {
        return _attachedTo;
    }
}
